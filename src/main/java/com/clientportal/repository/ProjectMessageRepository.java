package com.clientportal.repository;

import com.clientportal.entity.ProjectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {
    List<ProjectMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);
    long countByProjectId(Long projectId);
}
