#!/bin/bash

# Exit on any error
set -e

# Update the package lists
echo "Package updates"
sudo apt-get update

# Upgrade all installed packages
echo "Package upgrades"
sudo apt-get upgrade -y

# Install unzip package: 
sudo apt-get install unzip

# Install MySQL
echo "Setting up MySQL Server"
sudo apt-get install mysql-server -y

# Setup MySQL database
echo "Setting up MySQL DB"
sudo mysql -e "CREATE DATABASE IF NOT EXISTS csye6225;"

# Define the user and group names
user="csye6225user"
group="csye6225group"

# Check if the user exists
if ! id "$user" &>/dev/null; then
    echo "User $user does not exist. Creating user.."
    sudo useradd -m $user
    echo "User $user created."
else
    echo "User $user already exists, proceeding.."
fi

# Check if the group exists
if ! getent group $group &>/dev/null; then
    echo "Group $group does not exist. Creating group.."
    sudo groupadd $group
    echo "Group $group created."
else
    echo "Group $group already exists, proceeding.."
fi

# Add user to the group
echo "Adding $user to $group..."
sudo usermod -aG $group $user
echo "$user has been added to $group."

# Add user to the group
echo "Adding $user to $group..."
sudo usermod -aG $group $user
echo "$user has been added to $group."

# Create directory and unzip application
echo "Creating directory and unzipping application..."
sudo mkdir -p /opt/csye6225
sudo unzip /tmp/webapp.zip -d /opt/csye6225

# Move the .env file from /tmp to /opt/csye6225/webapp
echo "Moving .env file..."
sudo mv /tmp/.env /opt/csye6225/webapp/.env

# Update permissions
echo "Updating permissions..."
sudo chown -R $user:$group /opt/csye6225
sudo chmod -R 750 /opt/csye6225

echo "Setup complete."
