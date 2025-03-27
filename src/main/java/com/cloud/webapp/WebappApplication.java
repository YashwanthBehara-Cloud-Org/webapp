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
		System.setProperty("AWS_S3_BUCKET_NAME", dotenv.get("AWS_S3_BUCKET_NAME",""));
		System.setProperty("aws.accessKeyId", dotenv.get("aws_access_key_id", ""));
		System.setProperty("aws.secretAccessKey", dotenv.get("aws_secret_access_key", ""));
		System.setProperty("aws.region", dotenv.get("aws_region", "us-east-1"));
		System.setProperty("STATSD_HOST", dotenv.get("STATSD_HOST", "localhost"));

		SpringApplication.run(WebappApplication.class, args);
	}
}
