package com.clientportal.service;

import com.clientportal.dto.DashboardMetricsDto;
import com.clientportal.dto.ProjectDto;
import com.clientportal.entity.*;
import com.clientportal.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final DocumentRepository documentRepository;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.metrics.pipeline-benchmark:45892.00}")
    private BigDecimal pipelineBenchmark;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          MilestoneRepository milestoneRepository,
                          DocumentRepository documentRepository,
                          InvoiceRepository invoiceRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.milestoneRepository = milestoneRepository;
        this.documentRepository = documentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<Project> findAllProjects() {
        return projectRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsByClientId(Long clientId) {
        return projectRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(ProjectDto dto) {
        User client = userRepository.findById(dto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + dto.getClientId()));

        Project project = new Project(
                dto.getTitle(),
                dto.getDescription(),
                client,
                dto.getStatus() != null ? dto.getStatus() : ProjectStatus.PLANNING,
                dto.getStartDate(),
                dto.getEndDate()
        );
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, ProjectDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());

        if (dto.getClientId() != null && !dto.getClientId().equals(project.getClient().getId())) {
            User client = userRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + dto.getClientId()));
            project.setClient(client);
        }

        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * Aggregates dashboard metrics including active project count,
     * milestone counts, document counts, and pipeline revenue benchmark.
     */
    @Transactional(readOnly = true)
    public DashboardMetricsDto getDashboardMetrics() {
        DashboardMetricsDto metrics = new DashboardMetricsDto();

        long totalProjects = projectRepository.count();
        long activeProjects = projectRepository.countActiveProjects();
        long totalMilestones = milestoneRepository.count();
        long completedMilestones = milestoneRepository.countByIsCompletedTrue();
        long totalDocs = documentRepository.count();

        BigDecimal actualRevenue = invoiceRepository.calculateTotalInvoiced();
        BigDecimal paidRevenue = invoiceRepository.calculateTotalByStatus(InvoiceStatus.PAID);
        BigDecimal unpaidRevenue = invoiceRepository.calculateTotalByStatus(InvoiceStatus.UNPAID);
        BigDecimal overdueRevenue = invoiceRepository.calculateTotalByStatus(InvoiceStatus.OVERDUE);

        metrics.setTotalProjectsCount(totalProjects);
        metrics.setActiveProjectsCount(activeProjects);
        metrics.setTotalMilestonesCount(totalMilestones);
        metrics.setCompletedMilestonesCount(completedMilestones);
        metrics.setTotalDocumentsCount(totalDocs);

        metrics.setPipelineBenchmark(pipelineBenchmark != null ? pipelineBenchmark : new BigDecimal("45892.00"));
        metrics.setActualPipelineRevenue(actualRevenue != null ? actualRevenue : BigDecimal.ZERO);
        metrics.setPaidRevenue(paidRevenue != null ? paidRevenue : BigDecimal.ZERO);
        metrics.setUnpaidRevenue(unpaidRevenue != null ? unpaidRevenue : BigDecimal.ZERO);
        metrics.setOverdueRevenue(overdueRevenue != null ? overdueRevenue : BigDecimal.ZERO);

        return metrics;
    }
}
