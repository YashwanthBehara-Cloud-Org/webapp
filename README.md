### Assignment-1 Running guide

## Prerequisites
- Latest version of Java and Maven should installed
- MySQL server is installed and running.
- Database "health_check_db" created on your local MySQL server.
- Access to the Spring Boot project's configuration files.

## Setting up the repository
Clone this repository using `git clone <repository-url` and then navigate to the directory

## Enviromental Variables
To run this project, you will need to add the following environment variables to a .env file in the project directory.

`DB_USERNAME = <your-database-username>`
`DB_PASSWORD = <your-database-password>`
`DB_URL = <your-database-url>`

## Installing Dependencies
Run the following command to install the project dependencies:

`mvn clean install`

This command will clean the existing builds, resolve Maven dependencies, and compile the project.

## Running the Application
To start the application, use:

`mvn spring-boot:run`

## Test the application
Use postman or curl commands to test various requests