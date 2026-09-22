package com.clientportal.repository;

import com.clientportal.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProjectIdOrderByUploadedAtDesc(Long projectId);

    List<Document> findByProjectId(Long projectId);
}
