package com.clientportal.service;

import com.clientportal.entity.Project;
import com.clientportal.entity.ProjectMessage;
import com.clientportal.entity.User;
import com.clientportal.repository.ProjectMessageRepository;
import com.clientportal.repository.ProjectRepository;
import com.clientportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectMessageService {

    private final ProjectMessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectMessageService(ProjectMessageRepository messageRepository,
                                 ProjectRepository projectRepository,
                                 UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectMessage> getMessagesForProject(Long projectId) {
        return messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    public ProjectMessage postMessage(Long projectId, Long authorId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be blank.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with id: " + authorId));

        ProjectMessage message = new ProjectMessage(project, author, content.trim());
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public long countMessages(Long projectId) {
        return messageRepository.countByProjectId(projectId);
    }
}
