package com.example.demo.service;

import com.amazonaws.services.dynamodbv2.model.TimeToLiveSpecification;
import com.amazonaws.services.dynamodbv2.model.UpdateTimeToLiveRequest;
import com.example.demo.second.model.UserToken;
import com.example.demo.second.repository.UserTokenRepository;
import com.example.demo.util.exception.CustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class UserTokenService {
    Logger logger = LoggerFactory.getLogger(AWSS3ClientService.class);

    @Autowired
    UserTokenRepository userTokenRepository;

    @Autowired
    AWSDynamoDbClientService dynamoDbClientService;

    @Value("${aws.dynamodb.ttlSec}")
    private long tokenExpiryTime;

    private boolean isTtlSet;

    public UserToken saveUserToken(String email){
        UserToken ut = new UserToken(email);
        ut.setTokenTtl((System.currentTimeMillis()/1000L)+tokenExpiryTime);
        //Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.MINUTE,1);
        //double ttl =  (cal.getTimeInMillis() / 1000L);

        //dynamoDbClientService.getMapper().save(ut);
        userTokenRepository.save(ut);
        if(!isTtlSet) setTtl();
        return userTokenRepository.findByEmail(email);
    }

    //@PostConstruct
    private void setTtl(){
        try{
            UpdateTimeToLiveRequest req = new UpdateTimeToLiveRequest();
            //TODO: get tableName as a param, for being generic to tables
            req.setTableName("UserToken");
            TimeToLiveSpecification ttlSpec = new TimeToLiveSpecification();
            ttlSpec.setAttributeName("tokenTtl");
            ttlSpec.setEnabled(true);
            req.withTimeToLiveSpecification(ttlSpec);
            dynamoDbClientService.amazonDynamoDB().updateTimeToLive(req);
        }catch (Exception e){
            //TODO: currently silencing this non-critical exceptions
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        }
        isTtlSet = true;
    }

    public boolean verifyUser(String userName, String token){
        UserToken ut = userTokenRepository.findByEmail(userName);
        long currentEpoch = System.currentTimeMillis()/1000L;
        //Handling case when TTL is not expired / TTL feature is not set
        if(ut==null || currentEpoch-ut.getTokenTtl() > tokenExpiryTime) {
            throw new CustomErrorException("Token expired. Contact admin", HttpStatus.BAD_REQUEST);
        }
        if(!ut.getToken().equals(token)) {
            logger.warn("verification token changed for user");
            throw new CustomErrorException("Token changed/comprimised. Contact admin", HttpStatus.BAD_REQUEST);
        }
        return true;
    }
}
