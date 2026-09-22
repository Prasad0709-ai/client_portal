package com.clientportal.service;

import com.clientportal.dto.MilestoneDto;
import com.clientportal.entity.Milestone;
import com.clientportal.entity.Project;
import com.clientportal.repository.MilestoneRepository;
import com.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<Milestone> findByProjectId(Long projectId) {
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId);
    }

    public Milestone addMilestone(Long projectId, MilestoneDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        Milestone milestone = new Milestone(
                project,
                dto.getTitle(),
                dto.getDueDate(),
                dto.isCompleted()
        );
        return milestoneRepository.save(milestone);
    }

    public Milestone toggleMilestoneStatus(Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with ID: " + milestoneId));

        milestone.setCompleted(!milestone.isCompleted());
        return milestoneRepository.save(milestone);
    }

    public void deleteMilestone(Long milestoneId) {
        if (!milestoneRepository.existsById(milestoneId)) {
            throw new IllegalArgumentException("Milestone not found with ID: " + milestoneId);
        }
        milestoneRepository.deleteById(milestoneId);
    }
}
