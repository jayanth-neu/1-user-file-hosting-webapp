package com.example.demo.config;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.example.demo.util.exception.CustomErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;


@Lazy
@Configuration
public class AWSConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Bean
    public AWSCredentialsProvider getAwsCredentials() throws SdkClientException{
        List<AWSCredentialsProvider> providers = new ArrayList<>();
        //providers.add(new ProfileCredentialsProvider("dev"));
        //providers.add(new AWSStaticCredentialsProvider(new BasicAWSCredentials("","")));
        providers.add(InstanceProfileCredentialsProvider.getInstance());
        providers.add(new DefaultAWSCredentialsProviderChain());
        AWSCredentialsProvider cp = new AWSCredentialsProviderChain(
                providers.toArray(new AWSCredentialsProvider[providers.size()]));
        try{
            cp.getCredentials();
        } catch (SdkClientException e){
            throw new CustomErrorException("Unable to load AWS credentials from any provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return cp;
    }

    public Regions getRegion() {
        return Regions.fromName(region);
    }
}
