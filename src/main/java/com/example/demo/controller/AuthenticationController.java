package com.example.demo.controller;

import com.example.demo.model.Document;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.util.exception.CustomErrorException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/v1")
@SecurityRequirement(name = "webservice-user-api")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    @GetMapping("/users")
    public List<User> getAllusers() {
        return userService.getAllUsers();
    }

    @PostMapping(value = "/account")
    public ResponseEntity<User> saveUser(@Valid @RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/v1/user").toUriString());
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.created(uri)
                    .body(savedUser);
        } catch (CustomErrorException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomErrorException(e.getMessage(), HttpStatus.UNAUTHORIZED, user);
        }
    }

    @PutMapping(value = "/account/{id}")

    public ResponseEntity updateUserInfo(@RequestBody Map<String, Object> body, @PathVariable String id) throws Exception {

        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        User userById = userService.getByUserId(id)
                .orElseThrow(() ->
                        new CustomErrorException("Cannot find given user id", HttpStatus.BAD_REQUEST));

        if (!userById.getUsername().equals(userName))
            throw new CustomErrorException("Trying to access other user", HttpStatus.FORBIDDEN);
        if (body.containsKey("id") || body.containsKey("account_created") || body.containsKey("account_updated"))
            throw new CustomErrorException("Cannot update given fields", HttpStatus.BAD_REQUEST);


        User user = new User();
        user.setUsername(userName);
        if (body.containsKey("first_name")) user.setFirst_name(body.get("first_name").toString());
        if (body.containsKey("last_name")) user.setLast_name(body.get("last_name").toString());
        if (body.containsKey("password")) user.setPassword(body.get("password").toString());
        userService.updateUser(user);
        return ResponseEntity.noContent().build();

    }

    @GetMapping(value = "/account/{id}")
    public ResponseEntity<User> getUserInfo(@PathVariable String id) {
        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        User user = userService.getByUserId(id)
                .orElseThrow(() ->
                        new CustomErrorException("Cannot find given user id", HttpStatus.BAD_REQUEST));

        if (!user.getUsername().equals(userName))
            throw new CustomErrorException("Trying to access other user", HttpStatus.FORBIDDEN);
        return ResponseEntity.ok(userService.getUser(userName));
    }

    @PostMapping(value = "/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Document> addOrUpdateUserProfileImage(@RequestParam("file") MultipartFile file) throws Exception{
        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        Document document = userService.saveUserDoc(userName, file);
        return ResponseEntity.ok(document);
    }


    @GetMapping(value = "/documents")
    public ResponseEntity <List<Document>> getUserDoc() {
        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();

        return ResponseEntity.ok(userService.getUserDocuments(userName));
    }

    @GetMapping(value= "/documents/{id}")
    public Document getDoc(@PathVariable UUID id) {
        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();


        User user = userService.getUser(userName);
        userService.checkIfVerified(user);

        Document document =  userService.getDocuments(id);
//                .orElseThrow(() ->
//                        new CustomErrorException("Cannot find given user", HttpStatus.BAD_REQUEST));

        if (!user.getId().equals(document.getUser_id()))
            throw new CustomErrorException("Trying to access other user documents", HttpStatus.FORBIDDEN);

        return ResponseEntity.ok(userService.getDocuments(id)).getBody();
    }

    @DeleteMapping(value = "/documents/{id}")
    public ResponseEntity deletedocument(@PathVariable UUID id) {
        String userName = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();

        User user = userService.getUser(userName);
        userService.checkIfVerified(user);

        Document document =  userService.getDocuments(id);
//                .orElseThrow(() ->
//                        new CustomErrorException("Cannot find given user", HttpStatus.BAD_REQUEST));

        if (!user.getId().equals(document.getUser_id()))
            throw new CustomErrorException("Trying to access other user documents", HttpStatus.FORBIDDEN);


        userService.deleteUserDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/verifyUserEmail")
    public ResponseEntity verifyUserEmail(@RequestParam(name = "email") String mail,
                                          @RequestParam String token) throws Exception{
        if(mail.contains(" ")){
            logger.warn("mail : " + mail + " got encoded by a whitespace character. Changing it back");
            mail = mail.replace(" ","+");
        }
        userService.verifyUser(mail, token);
        return ResponseEntity.noContent().build();
    }

}
