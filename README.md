# Description
A user file hosting application built using Java Springboot and AWS services. Includes an automated CI/CD pipeline built using GitHub Actions. Cloudformation template is used for Infrasture as a code (IaC)

## Technologies

- Spring boot
- Postgres
- Maven
- Java17
- Bash

## Operating Instructions

- Preferred IDE : Intellij
- To build the project : 
- Use java17, maven
- Execute : `mvn clean install`
- Main class : `./src/main/java/edu/neu/csye6225/webapp/DemoApplication.java`
- Property Configuration/App tuning: `./src/main/resources/application.properties`
- Run Commands:
    1. `mvn spring-boot:run` or
    2. `java -jar target/webservice-0.0.1-SNAPSHOT.jar` or
       - Note 
           1. Underlying persistent data service(postgres) must be serving on 5432 port,
           2. No other service must be blocking 8081 port
- For github workflow refer to `./.github/workflows/maven.yml`

##Tips
- To speed up the build use `mvn clean install -DskipTests`
- To change tuning parameters for service at last point, use cmdline args
    Example: `java -jar -Dserver.port=8083 -Dspring.datasource.url=jdbc:postgresql://localhost/database spring.jar`
- Provide necessary permissions to scripts via: `chmod 764 <file>`

# Architecture

<br/>

![image](https://github.com/SaiChandGhanta/1-user-file-hosting-webapp/blob/main/user%20files%20hosting.png)
<br/>

# CICD Workflow

<br/>

![image](https://github.com/SaiChandGhanta/1-user-file-hosting-webapp/blob/main/CICD.png)
<br/>

# Infrastucture and Serverless repos

[Click here for Infrastructure repo](https://github.com/SaiChandGhanta/2-user-file-hosting-infrastructure)

[Click here for Serverless repo](https://github.com/SaiChandGhanta/3-user-file-hosting-serverless)




