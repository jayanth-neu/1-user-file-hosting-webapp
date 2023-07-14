package com.example.demo.service;

import com.amazonaws.SdkBaseException;
import com.example.demo.model.Document;
import com.example.demo.model.User;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.second.model.UserToken;
import com.example.demo.util.exception.CustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.sql.PseudoColumnUsage;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;


@Service
public class UserService implements UserDetailsService {
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    @Lazy
    private AWSS3ClientService awss3ClientService;

    @Autowired
    @Lazy
    private AWSSnsClientService awsSnsClientService;

    @Autowired
    private UserTokenService userTokenService;

    public User saveUser(User user) throws Exception {
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new CustomErrorException("UserName must be set", HttpStatus.BAD_REQUEST);
        if (!isValidMail(user.getUsername()))
            throw new CustomErrorException("Invalid mail for user : " + user.getUsername(), HttpStatus.BAD_REQUEST);
        User prevUser = userRepository.findByUsername(user.getUsername());
        if (prevUser != null) {
            throw new CustomErrorException("Mail already exists", HttpStatus.BAD_REQUEST, prevUser);
        }
        user.setAccount_created(new Date());
        user.setAccount_updated(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        UserToken userToken = userTokenService.saveUserToken(user.getUsername());
        logger.info(userToken.toString());
        awsSnsClientService.publishSNSMessage(user.getUsername()+":"+userToken.getToken());
        return userRepository.save(user);
    }

    public User updateUser(User user) throws Exception {
        User prevUser = userRepository.findByUsername(user.getUsername());
        checkIfVerified(prevUser);
        if (user.getAccount_updated() != null ||
                user.getAccount_created() != null ||
                user.getId() != null
        ) throw new CustomErrorException("Cannot update given fields", HttpStatus.BAD_REQUEST, prevUser);
        if (user.getLast_name() == null && user.getPassword() == null && user.getFirst_name() == null)
            throw new CustomErrorException("Nothing to update", HttpStatus.BAD_REQUEST, prevUser);
        Set<ConstraintViolation<User>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(user);
        if (!violations.isEmpty())
            throw new CustomErrorException("Correct the violations", HttpStatus.BAD_REQUEST, violations.stream().map(ConstraintViolation::getMessage));

        boolean changed = false;
        if (!prevUser.getFirst_name().equals(user.getFirst_name())) {
            prevUser.setFirst_name(user.getFirst_name());
            changed = true;
        }
        if (!prevUser.getLast_name().equals(user.getLast_name())) {
            prevUser.setLast_name(user.getLast_name());
            changed = true;
        }

        if (!passwordEncoder.matches(user.getPassword(), prevUser.getPassword())) {
            prevUser.setPassword(passwordEncoder.encode(user.getPassword()));
            changed = true;
        }

        if (changed) prevUser.setAccount_updated(new Date());
        else {
            throw new CustomErrorException("Nothing to update", HttpStatus.BAD_REQUEST, prevUser);
        }
        return userRepository.save(prevUser);
    }

    public User getUser(String userName) {
        return userRepository.findByUsername(userName);
    }
    public boolean verifyUser(String userName, String token) throws Exception{
        logger.info("VerifyUserCalled by "+userName + " with token : "+ token);
        User user = userRepository.findByUsername(userName);
        if(user == null) throw new CustomErrorException("No such user exists", HttpStatus.BAD_REQUEST);
        if(user.isVerified()) throw new CustomErrorException("Already verified. Link no longer valid", HttpStatus.BAD_REQUEST);
        boolean isVerified = userTokenService.verifyUser(userName,token);
        user.setVerified(isVerified);
        user.setAccount_updated(new Date());
        userRepository.save(user);
        if(!isVerified) throw new CustomErrorException("Token expired. Contact admin for reset", HttpStatus.BAD_REQUEST);
        return isVerified;
    }


    public Optional<User> getByUserId(String uuid) {
        return userRepository.findById(UUID.fromString(uuid));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        User user = getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + " not found in users DB");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    private boolean isValidMail(String mail) {
        String regex = "^\\S+@\\S+\\.\\S+$";
        return Pattern.compile(regex)
                .matcher(mail)
                .matches();
    }

    public Document saveUserDoc(String userName, MultipartFile file) throws Exception{
        if (file.isEmpty()) {
            throw new CustomErrorException("Cannot upload empty file",HttpStatus.BAD_REQUEST);
        }


        User user = getUser(userName);
        checkIfVerified(user);

        List <Document> documents = documentRepository.findAllByUserId(user.getId());
        for(Document document : documents){
            if(document.getName().equals(file.getOriginalFilename()))
            {
                try{
                    awss3ClientService.deleteFileFromS3Bucket(document.getS3_bucket_path());
                }catch (SdkBaseException se) {
                    logger.warn("Failed to delete old file : "+ document.getS3_bucket_path(), se.toString());
                    //throw new CustomErrorException("Unable to delete profile image from s3", HttpStatus.INTERNAL_SERVER_ERROR);
                    se.printStackTrace();
                }
                documentRepository.deleteById(document.getDoc_id());
            }
        }

        String fileUrl="";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        try{
            fileUrl = awss3ClientService.uploadFile(file.getInputStream(), user.getId().toString(), Optional.of(metadata), file.getOriginalFilename());
        }catch (SdkBaseException e){
            throw new CustomErrorException("Failed to upload to s3",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Document i = new Document(file.getOriginalFilename(), fileUrl, new Date(),user.getId());

        return documentRepository.save(i);
    }
    public User passUserToEndpoint(String userName){
        User user = getUser(userName);
        checkIfVerified(user);
        return user;
    }
    public boolean checkIfVerified(User user){
        if(!user.isVerified()) throw new CustomErrorException("User not verified yet", HttpStatus.BAD_REQUEST);
        return true;
    }
    public List<Document> getUserDocuments(String userName) {
        User user = getUser(userName);
        checkIfVerified(user);
        List<Document> documents = documentRepository.findAllByUserId(getUser(userName).getId());
        if(documents == null || documents.isEmpty()) throw new CustomErrorException("User does not have any document", HttpStatus.NOT_FOUND);
        return  documents;
    }


    public Document getDocuments(UUID id) {
        Document document = documentRepository.findUserById(id);
        if(document == null ) throw new CustomErrorException("Does not have any document", HttpStatus.NOT_FOUND);
        return  document;
    }

    public void deleteUserDocument(UUID id) {
        Document document = documentRepository.findUserById(id);
        if(document == null) throw new CustomErrorException("User does not have any documents", HttpStatus.NOT_FOUND);
        try {
            awss3ClientService.deleteFileFromS3Bucket(document.getS3_bucket_path());
        } catch (SdkBaseException se) {
            throw new CustomErrorException("Unable to delete User document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        documentRepository.delete(document);
    }

}
