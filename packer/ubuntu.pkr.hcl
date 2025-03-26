# Define variables
variable "aws_region" {}
variable "aws_source_ami" {}
variable "aws_ami_name" {}
variable "aws_instance_type" {}
variable "aws_ssh_username" {}
variable "aws_demo_account_id" {}


packer {
  required_plugins {
    amazon = {
      source  = "github.com/hashicorp/amazon"
      version = ">= 1.0.0"
    }
  }
}

# AWS Image Source
source "amazon-ebs" "aws-ubuntu" {
  ami_name      = var.aws_ami_name
  region        = var.aws_region
  source_ami    = var.aws_source_ami
  instance_type = var.aws_instance_type
  ssh_username  = var.aws_ssh_username
}


# Single Build Block that Builds Both AWS & GCP Simultaneously
build {
  sources = [
    "source.amazon-ebs.aws-ubuntu"
  ]

  provisioner "shell" {
    inline = [
      "sudo mkdir -p /opt/myapp",
      "sudo chown ubuntu:ubuntu /opt/myapp",
      "sudo chmod 755 /opt/myapp"
    ]
  }

  # Transfer the JAR file
  provisioner "file" {
    source      = "artifact/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/opt/myapp/webapp.jar"
  }

  provisioner "shell" {
    inline = [
      "sudo apt-get update -y",
      "sudo apt-get upgrade -y",
      "sudo apt-get install -y software-properties-common",
      "sudo add-apt-repository universe",
      "sudo apt-get update -y",
      "sudo apt-get install -y openjdk-17-jdk",

      # Create user and group
      "sudo groupadd csye6225",
      "sudo useradd -r -s /usr/sbin/nologin -g csye6225 csye6225",

      # Set ownership and permissions
      "sudo chown -R csye6225:csye6225 /opt/myapp",
      "sudo chmod -R 755 /opt/myapp",

      # Create log directory for application
      "sudo mkdir -p /opt/myapp/logs",
      "sudo chown csye6225:csye6225 /opt/myapp/logs",
      "sudo chmod 755 /opt/myapp/logs"

      # Configure systemd service (but DO NOT enable/start it)
      "echo '[Unit]' | sudo tee /etc/systemd/system/myapp.service",
      "echo 'Description=My Java Application' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'After=network.target' | sudo tee -a /etc/systemd/system/myapp.service",

      "echo '[Service]' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'User=csye6225' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'EnvironmentFile=/opt/myapp/.env' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'WorkingDirectory = /opt/myapp' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'ExecStart=/usr/bin/java -jar /opt/myapp/webapp.jar' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'Restart=always' | sudo tee -a /etc/systemd/system/myapp.service",

      "echo '[Install]' | sudo tee -a /etc/systemd/system/myapp.service",
      "echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/myapp.service",

      # Install CloudWatch Agent
      wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
      sudo dpkg -i -E ./amazon-cloudwatch-agent.deb

      # Create CloudWatch Agent config directory
      sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc

      # Copy CloudWatch config (this will be overridden by user-data)
      sudo touch /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
      

      # DO NOT ENABLE OR START THE SERVICE HERE
      "sudo systemctl daemon-reload"
    ]
  }

}
