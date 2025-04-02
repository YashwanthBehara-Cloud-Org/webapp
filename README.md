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


- **Configure AWS Profiles:**
  - Configure the dev profile:
    ```bash
    aws configure --profile dev
    # Enter Access key and Secret key
    ```
  - Configure the demo profile:
    ```bash
    aws configure --profile demo
    # Enter Access and Secret keys
    ```

- **Verify AWS CLI is properly set up:**
  - Created GetUserPolicy and attached it to GetUserGroup, and added cli-demo-user to that group:
    ```bash
    aws iam get-user --profile demo
    ```

- **Checking if AWS Profiles are Set Up Correctly:**
  - Navigate to AWS configuration directory and check credentials and configuration:
    ```bash
    cd ~/.aws
    cat credentials  # For secret keys
    cat config       # For configuration
    ```

- **Installing and Setting Up Terraform:**
  - Add the HashiCorp tap and install Terraform:
    ```bash
    brew tap hashicorp/tap
    brew install hashicorp/tap/terraform
    ```
  - Initialize and plan Terraform execution:
    ```bash
    terraform init
    terraform plan
    terraform apply
    ```

- **Managing Terraform Workspaces:**
  - Manage workspaces:
    ```bash
    terraform workspace new dev         # Creates new workspace
    terraform workspace select dev     # Selects dev as current workspace
    terraform workspace delete dev     # Deletes dev workspace
    terraform workspace show           # Shows current workspace
    terraform workspace list           # Lists all workspaces
    ```

- **Planning and Applying Infrastructure with Specific Variable Files:**
  - Plan and apply using specific variable files:
    ```bash
    terraform plan -var-file="var_dev.tfvars"
    terraform apply -var-file="var_dev.tfvars"
    ```


### Assignment - 4

#### Setup Packer to Build Machine Images for AWS and GCP

1. **GitHub Workflows Setup:**
   - **Packer - Check:** Validates packer format and configuration.
   - **Packer - Build:** Builds the machine images.

2. **Packer Build Workflow Steps:**
   - Builds the application JAR file in the GitHub runner.
   - Installs required plugins and dependencies.
   - Configures AWS using the dev user to build the AMI.
   - Configures GCP using the dev service account to build the machine image.
   - Executes the packer build command.
   - Retrieves the AWS AMI ID and GCP image name after successful creation.
   - Shares the AWS AMI with the demo account.
   - Uses the GCP Compute Image to create a VM instance and then a machine image.
   - Shares the GCP machine image with the demo project service account.

3. **Packer File Execution (`packer/ubuntu.pkr.hcl`):**
   - Uses a default image to create a temporary instance for building machine images.
   - Transfers the application JAR file from GitHub artifacts to the temporary instance.
   - Installs MySQL, JDK, and other dependencies.
   - Sets up the database, creates a user (`csye7225user`), and grants database access.
   - Creates an environment file for the application.
   - Configures a service file to initialize dependencies, MySQL server, and run the application upon instance creation using the custom machine image.

### Assignment - 5

#### Objective
1. To remove the local installation of MySQL Database in the AMI and connect to RDS DB Instance on web application start-up 
2. And to also store the uploaded files metadata in S3 Buckets.


---

#### S3 Bucket
- Create a private S3 bucket with a unique UUID-based name.
- Allow Terraform to delete the bucket even if it contains objects:
  ```bash
  aws s3 rm s3://<bucket-name> --recursive
  ```
- Enable default encryption (AES256).
- Configure a lifecycle policy to transition objects to STANDARD_IA after 30 days.

---

#### DB Security Group
- Create a dedicated EC2 security group for the RDS instance.
- Add an ingress rule to allow **TCP traffic on port 3306** from the **application security group**.
- Block all public access.

---

#### RDS Parameter Group
- Create a **custom DB parameter group** matching your DB engine/version.
- Apply this parameter group to the RDS instance (avoid using default).

---

#### RDS Instance Configuration

| Property              | Value                 |
|-----------------------|-----------------------|
| Database Engine       | MySQL                 |
| DB Instance Class     | Cheapest available    |
| Multi-AZ Deployment   | No                    |
| DB Identifier         | csye6225              |
| Master Username       | csye6225              |
| Master Password       | Strong password       |
| Subnet Group          | Private Subnet Only   |
| Public Accessibility  | No                    |
| DB Name               | csye6225              |

Attach the database security group to this RDS instance.

---

#### EC2 User Data Configuration
- Create a user data script which will run when EC2 Instance is provisioned
  - So, the flow goes like this..first the packer's script runs..which will create the `myapp.service` file
  - Then, the EC2 Script ( i.e : `user-script` ) runs which will configure the RDS DB , creates a user `csye6225` for that DB, inject the environment variables into `.env` file and execute the command to run the service file
  - Then, the service file runs..which will run the application ( web app )

---

#### Web Application
- Must use the RDS DB when deployed on EC2.
- Local DB usage is only permitted for integration testing.

---

### Assignment - 6

#### Objective
To configure **CloudWatch Logs** and **Custom Metrics** monitoring using **Micrometer + StatsD + CloudWatch Agent**.

#### Logging Setup using CloudWatch Agent

1. CloudWatch Agent is installed in the AMI (via Packer).
2. It’s configured and started using Terraform EC2 `user_data`.
3. IAM role with logs permissions (`logs:PutLogEvents`, etc.) is attached.

#### Custom Metrics with Micrometer + StatsD + CloudWatch

1. Micrometer is used in Spring Boot to define custom counters and timers.
2. Metrics are sent as UDP packets to `localhost:8125` using `StatsdMeterRegistry`.
3. CloudWatch Agent listens to port 8125 and forwards these metrics to AWS CloudWatch.
4. EC2 IAM Role allows `cloudwatch:PutMetricData`.
5. Metrics are visible under CloudWatch → Metrics → StatsD namespace.

#### Summary

- AMI contains CloudWatch Agent.
- EC2 startup configures and runs the agent.
- Metrics are emitted via Micrometer → StatsD → CloudWatch Agent → AWS CloudWatch.
- Secure setup (no hardcoded credentials).

