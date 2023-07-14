package com.example.demo.service;

import com.amazonaws.SdkBaseException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.example.demo.config.AWSConfig;
import com.example.demo.second.repository.UserTokenRepository;
import com.example.demo.util.exception.CustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;

@Configuration
//basePackages = "edu.neu.userFiles.webservice.second.repository.UserTokenRepository"
//basePackageClasses = UserTokenRepository.class
//type = FilterType.ANNOTATION,
//includeFilters = {@ComponentScan.Filter( classes = UserTokenRepository.class)}
@EnableDynamoDBRepositories
        (basePackages = "edu.neu.userFiles.webservice.second.repository",
                basePackageClasses = UserTokenRepository.class,
                includeFilters = {
                        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {UserTokenRepository.class})
                }
        )
//@EnableDynamoDBRepositories(basePackageClasses = UserTokenRepository.class)
public class AWSDynamoDbClientService {
    Logger logger = LoggerFactory.getLogger(AWSSnsClientService.class);

    @Autowired
    AWSConfig awsConfig;

    @Value("${aws.dynamodb.url}")
    private String awsDynamoDBEndPoint;

    private AmazonDynamoDB dynamoDBClient;

    @PostConstruct
    private void generateDynamoDbClient(){
        try{
            this.dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsDynamoDBEndPoint,"us-east-1"))
                    //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-east-1"))
                    .withCredentials(awsConfig.getAwsCredentials())
                    //.withRegion(awsConfig.getRegion())
                    .build();
        } catch (SdkBaseException e){
            throw new CustomErrorException("Unable to find aws creds", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CustomErrorException e){
            throw e;
        }
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return dynamoDBClient;
    }
    @Primary
    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.DEFAULT;
    }

//    public DynamoDBMapper getMapper() {
//        return new DynamoDBMapper(dynamoDBClient,dynamoDBMapperConfig());
//    }
}
