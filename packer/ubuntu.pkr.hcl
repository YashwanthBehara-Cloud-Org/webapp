# Define variables
variable "aws_region" {}
variable "aws_source_ami" {}
variable "aws_ami_name" {}
variable "aws_instance_type" {}
variable "aws_ssh_username" {}
variable "aws_demo_account_id" {}

variable "gcp_project_id" {}
variable "gcp_region" {}
variable "gcp_zone" {}
variable "gcp_machine_type" {}
variable "gcp_source_image" {}
variable "gcp_image_name" {}
variable "gcp_ssh_username" {}
variable "gcp_demo_project_id" {}

variable "db_username" {}
variable "db_password" {}
variable "db_url" {}

packer {
  required_plugins {
    amazon = {
      source  = "github.com/hashicorp/amazon"
      version = ">= 1.0.0"
    }
    googlecompute = {
      source  = "github.com/hashicorp/googlecompute"
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

# GCP Machine Image Source
source "googlecompute" "gcp-ubuntu" {
  project_id   = var.gcp_project_id
  region       = var.gcp_region
  zone         = var.gcp_zone
  machine_type = var.gcp_machine_type
  source_image = var.gcp_source_image
  image_name   = var.gcp_image_name
  ssh_username = var.gcp_ssh_username
}

# Single Build Block that Builds Both AWS & GCP Simultaneously
build {
  sources = [
    "source.amazon-ebs.aws-ubuntu",
    "source.googlecompute.gcp-ubuntu"
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

      # DO NOT ENABLE OR START THE SERVICE HERE
      "sudo systemctl daemon-reload"
  ]
}

}
