package com.cloud.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class WebappApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("aws.s3.bucketName", dotenv.get("aws.s3.bucketName"));
		System.setProperty("aws_access_key_id", dotenv.get("aws_access_key_id", ""));
		System.setProperty("aws_secret_access_key", dotenv.get("aws_secret_access_key", ""));
		System.setProperty("aws_region", dotenv.get("aws_region", "us-east-1"));
		SpringApplication.run(WebappApplication.class, args);
	}
}
