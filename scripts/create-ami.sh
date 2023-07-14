#!/bin/bash

export AWS_PROFILE=dev

packer build \
ami.pkr.hcl

unset AWS_PROFILE