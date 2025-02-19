### Assignment-1 Running guide

## Prerequisites
- Latest version of Java and Maven should installed
- MySQL server is installed and running.
- Database "health_check_db" created on your local MySQL server.
- Access to the Spring Boot project's configuration files.

## Setting up the repository
Clone this repository using `git clone <repository-url>` and then navigate to the directory

## Enviromental Variables
To run this project, you will need to add the following environment variables to a .env file in the project directory.

`DB_USERNAME = <your-database-username>`
`DB_PASSWORD = <your-database-password>`
`DB_URL = <your-database-url>`

## Running the application
Run the spring boot application.java file 


## Test the application
Use postman or curl commands to test various requests

## Assignment - 2 : Added script file and Test cases

- POST, PUT and DELETE Methods will not be allowed
- GET Request with request payload or query parameter will not be allowed
- Database connectivity error will be notified by Service Unavailable ( HTTP 503 ) status code

## Script File

Contains commands to :
1. Install package updates and upgrades
2. Install unzip package
3. Install MySQL Server
4. Setup and create csye6225 database
5. Create user
6. Create group
7. Added user to the group
8. Unzippling the application folder to the csye6225 directory
9. Copying the .env file to the applciation folder
10. Updating the folder permissions, making the user and group the owners of that folder

## Setting up the application on VM
1. To check ssh aliases: 
    `nano ~/.ssh/config`
	-> Update the IP Address of the VM inside the ssh config

2. To connect to VM : 
    `ssh digitalocean`

3. To copy the script file to VM: 
	1. Navigate to the script file directory in the local mac
	2. `scp setup_script.sh digitalocean:/tmp`

4. To copy the zip file to VM:
	1. Navigate to the zip file directory in the local mac
	2. `scp webapp.zip digitalocean:/tmp`

5. Create an env file in the tmp folder of vm
    1. `nano .env`
    2. Paste the DB Credentials of the user created in the script

6. Making the script executable : 
    `chmod +x setup_script.sh`

7. Executing the script : 
    `./setup_script.sh`

## Running the application on VM


1. Granting Privileges to user : 

    `sudo mysql -e "CREATE USER IF NOT EXISTS 'csye6225user'@'localhost' IDENTIFIED BY 'password';"` 

    `sudo mysql -e "GRANT ALL PRIVILEGES ON csye6225.* TO 'csye6225user'@'localhost';"`

    `sudo mysql -e "FLUSH PRIVILEGES;"`

2. Install Java JDK (Choose the version based on your application compatibility) 
    `sudo apt install -y openjdk-17-jdk`

3. Navigate to application directory (Assuming the source code is already there) 
    `cd /opt/csye6225/webapp` 

4. Use Maven to build the application 
    `sudo apt install maven`

5.  `mvn clean install -DskipTests`

6. Create .jar file
    `mvn package -DskipTests` 

7. Find the jar file (usually in the target directory) 
    `JAR_PATH=$(find ./target/ -name "*.jar" | head -n 1)`

8. Allow firewall access to port 8080 if your app uses this port 
    `sudo ufw allow 8080` 

9. Run the Spring Boot application as csye6225user 
    `sudo -u csye6225user java -jar $JAR_PATH` 

10. Hit the request from Postman ( update the VM’s IP Address ) 

11. Check if record is inserted in DB:
    `mysql -u csye6225user -p`

    then : `USE csye6225;`

    `select * from health_check;`


### Assignment - 3

1. AWS - CLI Setup
    i. Configure dev and demo profiles with access and secret keys

2. To check if the AWS- CLI Setup is successful

    * Created GetUserPolicy and attached to GetUserGroup and added cli-demo-user to that group
    * `aws iam get-user --profile demo`