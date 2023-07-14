package com.example.demo.service;

import com.amazonaws.SdkBaseException;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishResult;
import com.example.demo.config.AWSConfig;
import com.example.demo.util.exception.CustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Lazy
@Service
public class AWSSnsClientService {
    Logger logger = LoggerFactory.getLogger(AWSSnsClientService.class);

    @Autowired
    AWSConfig awsConfig;

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    private AmazonSNS snsClient;

    @PostConstruct
    private void generateSnsClient(){
        try{
            this.snsClient = AmazonSNSClient.builder()
                    .withCredentials(awsConfig.getAwsCredentials())
                    .withRegion(awsConfig.getRegion())
                    .build();
        } catch (SdkBaseException e){
            throw new CustomErrorException("Unable to find aws creds", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CustomErrorException e){
            throw e;
        }
    }

    public void publishSNSMessage(String message) {
        try{
            logger.info("Publishing SNS message: " + message);
            PublishResult result = snsClient.publish(topicArn, message);
            logger.info("SNS Message ID: " + result.getMessageId());
        }catch (Exception e) {
            logger.warn("SNS publishing failed with "+e.getMessage());
            throw e;
        }
    }
}
