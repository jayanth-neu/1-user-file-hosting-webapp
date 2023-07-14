package com.example.demo.second.repository;

import com.example.demo.second.model.UserToken;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;


// creating find my email to send the fields
@EnableScan
public interface UserTokenRepository extends CrudRepository<UserToken, String> {
    UserToken findByEmail(String username);
}
