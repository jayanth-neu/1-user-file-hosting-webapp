variable "aws_access_key" {
  type    = string
  default = ""
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "aws_secret_key" {
  type    = string
  default = ""
}

variable "source_ami" {
  type    = string
  default = "ami-08c40ec9ead489470"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = ""
}

variable "GITHUB_REF" {
  type    = string
  default = ""
}

variable "aws_accounts" {
  type    = list(string)
  default = ["675945825324","562996519735"]
}

locals { timestamp = regex_replace(timestamp(), "[- TZ:]", "") }
source "amazon-ebs" "webapp_ami" {
  ami_description = "AmazonUbuntu AMI for CSYE 6225 Fall 2022-webApp"
  ami_name        = "userFiles_${var.GITHUB_REF}"
  instance_type   = "t2.micro"
  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 20
    volume_type           = "gp2"
    delete_on_termination = true
  }
  # Attach additional volumes here: https://www.packer.io/plugins/builders/amazon/ebs#ami-block-device-mappings-example
  region       = "${var.aws_region}"
  access_key   = "${var.aws_access_key}"
  secret_key   = "${var.aws_secret_key}"
  source_ami   = "${var.source_ami}"
  ssh_username = "${var.ssh_username}"
  ssh_timeout  = "10m"
  subnet_id    = "${var.subnet_id}"
  associate_public_ip_address= true
  ssh_interface = "public_ip"
  ami_users    = "${var.aws_accounts}"

  tags = {
    Name ="chandu_webapp_${local.timestamp}"
    OS_Version = "Amazon Ubuntu"
    Base_AMI_ID = "{{ .SourceAMI }}"
    Base_AMI_Name = "{{ .SourceAMIName }}"
    Base_AMI_Name = "{{ .SourceAMIName }}"
    Extra = "{{ .SourceAMITags.TagName }}"
    Purpose = "CSYE-6225 Fall 2022 app"
  }
}

build {
  sources = ["source.amazon-ebs.webapp_ami"]
  provisioner "shell" {
    inline = ["mkdir workspace"]
  }
  provisioner "file" {
    source = "webservice.jar"
    destination = "~/workspace/webservice.jar"
  }
  provisioner "file" {
    source = "cloudwatch_config.json"
    destination = "/tmp/cloudwatch_config.json"
  }
  provisioner "file" {
    source = "webservice.service"
    destination = "/tmp/webservice.service"
  }
  provisioner "shell" {
    script = "webapp-provisioner.sh"
  }
}
