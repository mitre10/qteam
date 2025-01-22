# S3 Service Application
## Overview 
This project is a Spring Boot application designed to interact with Amazon S3. 
It provides functionality to retrieve and list objects from an S3 bucket, supports pagination, and can download the objects. 
## Features 
- Retrieve a resource based on its ID 
- List the contents of a folder with pagination support
- Download object
- Exception handling for various scenarios 
- Unit tests using JUnit and Mockito 
## Getting Started 
### Prerequisites 
- Java 17 or higher 
- Maven 3.6.3 or higher 
- AWS Account with S3 bucket access 
- AWS SDK for Java 
### Run the application
1. Update the application.properties file with your S3 bucket details.
You can either export the variables `S3_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `S3_BUCKET_NAME` with your credentials or 
change the default values listed in properties file
```java
aws.s3.region=${S3_REGION:eu-west-1}
aws.s3.accessKey=${AWS_ACCESS_KEY_ID:test-access-key}
aws.s3.secretKey=${AWS_SECRET_ACCESS_KEY:test-secret-key}
aws.s3.bucketName=${S3_BUCKET_NAME:test-bucket}
```
2. Build the project using command
``mvn clean install``
3. Start the application using command
``mvn spring-boot:run``
