package com.example.demo.second.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName = "UserToken")
public class UserToken {
    @DynamoDBHashKey
    private String email;

    //OneTimeToken
    @DynamoDBAttribute
    @DynamoDBGeneratedUuid(DynamoDBAutoGenerateStrategy.CREATE)
    private String token;

    @DynamoDBAttribute
    private long tokenTtl;
    //TODO: Add TTL, Another table for mail sent

    public long getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(long tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserToken() {
    }

    public UserToken(String email) {
        this.email = email;
    }

    public UserToken(String email, String token) {
        this.email = email;
        this.token = token;
    }
// sending email, token and ttl to sns
    @Override
    public String toString() {
        return "UserToken{" +
                "email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", tokenTtl=" + tokenTtl +
                '}';
    }
}

