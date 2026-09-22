package com.clientportal.repository;

import com.clientportal.entity.Project;
import com.clientportal.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByCreatedAtDesc();

    List<Project> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Project> findByClientId(Long clientId);

    long countByStatus(ProjectStatus status);

    long countByStatusNot(ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status IN ('PLANNING', 'IN_PROGRESS', 'ON_HOLD')")
    long countActiveProjects();
}
