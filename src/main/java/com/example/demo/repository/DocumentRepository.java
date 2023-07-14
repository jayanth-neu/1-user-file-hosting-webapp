package com.example.demo.repository;

import com.example.demo.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    @Query("select i from Document i where i.user_id = ?1")
    List<Document> findAllByUserId(UUID id);
    @Query("select i from Document i where i.doc_id = ?1")
    Document findUserById(UUID id);
}
