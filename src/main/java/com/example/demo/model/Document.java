package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.UUID;


@Entity
@Table(name="document")
@JsonIgnoreProperties(value={ "content_type", "content_length" })

public class Document {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    @Type(type="org.hibernate.type.UUIDCharType")
    private UUID doc_id;
    @Column(nullable = false)
    private UUID user_id;

    @NotEmpty
    private String name;

    private Date date_created;
    @NotEmpty
    private String s3_bucket_path;

    public Document(String name, String s3_bucket_path, Date date_created, UUID user_id) {
        this.name = name;
        this.s3_bucket_path = s3_bucket_path;
        this.date_created = date_created;
        this.user_id = user_id;
    }
    public Document() {

    }
    public UUID getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(UUID doc_id) {
        this.doc_id = doc_id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }

    public UUID getUser_id() {
        return user_id;
    }
    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }
}
