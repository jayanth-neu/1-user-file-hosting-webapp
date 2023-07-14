#!/bin/bash

sleep 30
sudo apt-get update -y

#install java
sudo apt-get install openjdk-17-jdk -y
java --version

#postgresql14-client
sudo apt-get install -y postgresql-client

cd ~ || exit

#Ensure packer file placed this at tmp, before using it.
sudo mv /tmp/webservice.service /etc/systemd/system/
sudo chmod u+x /etc/systemd/system/webservice.service
sudo systemctl daemon-reload
systemctl status webservice.service -l
sudo systemctl enable webservice.service

#cloudwatch agent setup
wget download-link https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
sudo cp /tmp/cloudwatch_config.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
#recommended config location as per docs
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json \
    -s
sudo systemctl enable amazon-cloudwatch-agent.service
sudo systemctl start amazon-cloudwatch-agent.service
systemctl status amazon-cloudwatch-agent.service -l




