package com.example.demo.service;

import com.amazonaws.SdkBaseException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.example.demo.config.AWSConfig;
import com.example.demo.util.exception.CustomErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@Lazy
@Service
public class AWSS3ClientService {
    Logger logger = LoggerFactory.getLogger(AWSS3ClientService.class);

    @Autowired
    AWSConfig awsConfig;

    private AmazonS3 s3client;

    @Value("${aws.s3.bucketName:test}")
    private String bucketName;

    @PostConstruct
    private void generateS3Client(){
        try{
            this.s3client = AmazonS3ClientBuilder.standard()
                    .withCredentials(awsConfig.getAwsCredentials())
                    .withRegion(awsConfig.getRegion())
                    .build();
            //.withRegion(Regions.US_EAST_1)
            //AmazonS3ClientBuilder.defaultClient();
            validateBucket(bucketName);
        } catch (SdkBaseException e){
            throw new CustomErrorException("Unable to find aws creds", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CustomErrorException e){
            throw e;
        }
    }

    private void validateBucket(String bucketName) throws CustomErrorException{
        try{
            if(!s3client.doesBucketExistV2(bucketName)){
                throw new CustomErrorException("Unable to find s3 bucket"+ bucketName, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (CustomErrorException e){
            throw e;
        }catch (SdkBaseException e){
            throw new CustomErrorException("Unable to find aws creds", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public String uploadFile(InputStream inputStream, String folderHelper,Optional<Map<String, String>> metadata, String filename ) throws Exception{

        String fileUrl = bucketName + "/" + folderHelper + "/" + filename;
        uploadFileTos3bucket(folderHelper + "/" + filename, inputStream, metadata);
        //file.delete();
        return fileUrl;
    }

    public static File convertMultiPartToFile(MultipartFile file){
        File convFile = null;
        try {
            convFile = new File(file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
        }catch (IOException e) {
            e.printStackTrace();
            throw new CustomErrorException("Unable to parse multiPart file", HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e) {
            e.printStackTrace();
            throw new CustomErrorException("Unable to parse multiPart file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return convFile;
    }

    /**
     * UniqueName for everyFile to avoid overriding
     * @param multiPart
     * @return
     */
    //TODO: can take userid too, since 1 user can have 1 profile pic
    //userid/image would be better choice than userID-image. As in future we may store other blob content in same folder too
    private String generateUniqueFileName(MultipartFile multiPart) {
        String pattern = "yyyy-MM-dd-hh-mm-ss";
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date()) +"_"+ multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file, Optional<Map<String, String>> metadata) throws Exception{
        uploadFileTos3bucket(fileName, new FileInputStream(file), metadata);
    }
    private void uploadFileTos3bucket(String fileName, InputStream inputStream, Optional<Map<String, String>> metadata) throws Exception{
        //validateBucket(bucketName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        //objectMetadata.setContentLength();
        metadata.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, fileName, inputStream,objectMetadata );
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag("created-by", "Webservice-JAVA SDK"));
        tags.add(new Tag("project", "userFiles-spring2022"));
        putRequest.setTagging(new ObjectTagging(tags));
        s3client.putObject(putRequest);
    }

    public void deleteFileFromS3Bucket(String fileUrl) {

        String fileName = fileUrl.substring(fileUrl.indexOf("/")+1);
        //s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
        request.setKeys(List.of(new DeleteObjectsRequest.KeyVersion(fileName)));
        request.setQuiet(true);
        DeleteObjectsResult result = s3client.deleteObjects(request);
        if(!result.getDeletedObjects().isEmpty()){
            logger.warn("Failed while deleting : "+ fileUrl);
        }
    }
}
