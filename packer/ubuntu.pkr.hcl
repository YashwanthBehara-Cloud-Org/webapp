# Define variables
variable "aws_region" {}
variable "gcp_project_id" {}
variable "gcp_region" {}
variable "gcp_zone" {}
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
  ami_name      = "webapp-ubuntu-24-04"
  region        = var.aws_region
  source_ami    = "ami-04b4f1a9cf54c11d0"
  instance_type = "t2.micro"
  ssh_username  = "ubuntu"
}

# GCP Image Source
source "googlecompute" "gcp-ubuntu" {
  project_id          = var.gcp_project_id
  region              = var.gcp_region
  zone                = var.gcp_zone
  machine_type        = "e2-medium"
  source_image_family = "ubuntu-2204-lts"
  image_name          = "webapp-ubuntu-24-04"
  ssh_username        = "ubuntu"
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
      "sudo apt update -y",
      "sudo apt install -y openjdk-17-jdk mysql-server",

      # Start MySQL service
      "sudo systemctl start mysql",
      "sudo systemctl enable mysql",

      # Create database and user
      "sudo mysql -e \"CREATE DATABASE IF NOT EXISTS health_check_db;\"",
      "sudo mysql -e \"CREATE USER IF NOT EXISTS '${var.db_username}'@'localhost' IDENTIFIED BY '${var.db_password}';\"",
      "sudo mysql -e \"GRANT ALL PRIVILEGES ON health_check_db.* TO '${var.db_username}'@'localhost';\"",
      "sudo mysql -e \"FLUSH PRIVILEGES;\"",

      # Create user and group
      "sudo groupadd csye6225",
      "sudo useradd -r -s /usr/sbin/nologin -g csye6225 csye6225",

      # Set ownership and permissions
      "sudo chown -R csye6225:csye6225 /opt/myapp",
      "sudo chmod -R 755 /opt/myapp",

      # Create .env file
      "echo 'DB_URL=${var.db_url}' | sudo tee /opt/myapp/.env",
      "echo 'DB_USERNAME=${var.db_username}' | sudo tee -a /opt/myapp/.env",
      "echo 'DB_PASSWORD=${var.db_password}' | sudo tee -a /opt/myapp/.env",


      # Change owner to csye6225 so Java can access it
      "sudo chown csye6225:csye6225 /opt/myapp/.env",

      # Change permissions to allow read access by the user but no write access
      "sudo chmod 644 /opt/myapp/.env",

      # Configure systemd service
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

      # Start service
      "sudo systemctl daemon-reload",
      "sudo systemctl enable myapp.service"
    ]
  }
}
