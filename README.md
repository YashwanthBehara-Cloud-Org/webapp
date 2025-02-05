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
1. Install package updated and upgrades
2. Install unzip package
3. Install MySQL Server
4. Setup and create csye6225 database
5. Create user
6. Create group
7. Added user to the group
8. Unzippling the application folder to the csye6225 directory
9. Copying the .env file to the applciation folder
10. Updating the folder permissions, making the user and group the owners of that folder