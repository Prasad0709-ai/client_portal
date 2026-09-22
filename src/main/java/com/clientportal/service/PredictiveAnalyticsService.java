package com.clientportal.service;

import com.clientportal.dto.ProjectForecastDto;
import com.clientportal.entity.*;
import com.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PredictiveAnalyticsService {

    private final ProjectRepository projectRepository;

    public PredictiveAnalyticsService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Generates statistical delivery forecasting and multi-factor risk scoring
     * for a single project based on milestone velocity, deadlines, and financial friction.
     */
    public ProjectForecastDto forecastProject(Project project) {
        ProjectForecastDto dto = new ProjectForecastDto();
        dto.setProjectId(project.getId());
        dto.setProjectTitle(project.getTitle());
        dto.setClientCompany(project.getClient() != null ? project.getClient().getCompany() : "N/A");
        dto.setTargetEndDate(project.getEndDate());

        List<Milestone> milestones = project.getMilestones() != null ? project.getMilestones() : Collections.emptyList();
        long totalMilestones = milestones.size();
        long completedMilestones = milestones.stream().filter(Milestone::isCompleted).count();
        long uncompletedMilestones = totalMilestones - completedMilestones;

        dto.setTotalMilestones(totalMilestones);
        dto.setCompletedMilestones(completedMilestones);

        // 1. Calculate Milestone Velocity (average days to achieve one milestone)
        LocalDate startDate = project.getStartDate() != null ? project.getStartDate() : LocalDate.now().minusDays(30);
        long daysElapsed = Math.max(1, ChronoUnit.DAYS.between(startDate, LocalDate.now()));

        double daysPerMilestone = 14.0; // Default baseline cadence: 14 days/milestone
        if (completedMilestones > 0) {
            daysPerMilestone = (double) daysElapsed / completedMilestones;
        }
        dto.setMilestoneVelocityDays(Math.round(daysPerMilestone * 10.0) / 10.0);

        // 2. Projected Completion Date & Slippage Modeling
        LocalDate projectedEnd;
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            projectedEnd = LocalDate.now();
        } else if (uncompletedMilestones <= 0) {
            projectedEnd = LocalDate.now().plusDays(3);
        } else {
            long remainingDays = Math.round(uncompletedMilestones * daysPerMilestone);
            projectedEnd = LocalDate.now().plusDays(remainingDays);
        }
        dto.setProjectedEndDate(projectedEnd);

        long slippageDays = 0;
        if (project.getEndDate() != null && project.getStatus() != ProjectStatus.COMPLETED) {
            slippageDays = ChronoUnit.DAYS.between(project.getEndDate(), projectedEnd);
        }
        dto.setSlippageDays(slippageDays);

        // 3. Multi-Factor Risk Scoring (0 - 100)
        int riskScore = calculateRiskScore(project, slippageDays, milestones);
        dto.setRiskScore(riskScore);
        dto.setCompletionProbability(Math.max(5, 100 - riskScore));

        // 4. Risk Level Classification & Badge
        if (riskScore < 30) {
            dto.setRiskLevel("LOW");
            dto.setRiskBadgeClass("bg-emerald-500/10 text-emerald-400 border-emerald-500/20");
        } else if (riskScore < 65) {
            dto.setRiskLevel("MODERATE");
            dto.setRiskBadgeClass("bg-amber-500/10 text-amber-400 border-amber-500/20");
        } else {
            dto.setRiskLevel("CRITICAL");
            dto.setRiskBadgeClass("bg-rose-500/10 text-rose-400 border-rose-500/20");
        }

        // 5. Automated AI Remediation Recommendation
        dto.setRecommendation(generateRecommendation(project, slippageDays, milestones, riskScore));

        return dto;
    }

    private int calculateRiskScore(Project project, long slippageDays, List<Milestone> milestones) {
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return 0;
        }

        int score = 0;

        // Factor A: Schedule Slippage Weight (up to 40 pts)
        if (slippageDays <= 0) {
            score += 5; // Healthy / on-track
        } else if (slippageDays <= 7) {
            score += 15;
        } else if (slippageDays <= 21) {
            score += 28;
        } else {
            score += 40;
        }

        // Factor B: Overdue Milestones Weight (up to 30 pts)
        long overdueMilestones = milestones.stream()
                .filter(m -> !m.isCompleted() && m.getDueDate() != null && m.getDueDate().isBefore(LocalDate.now()))
                .count();
        score += Math.min(30, (int) overdueMilestones * 12);

        // Factor C: Financial Invoicing Exposure (up to 30 pts)
        if (project.getInvoices() != null) {
            boolean hasOverdueInvoice = project.getInvoices().stream()
                    .anyMatch(i -> i.getStatus() == InvoiceStatus.OVERDUE);
            if (hasOverdueInvoice) {
                score += 20;
            } else {
                boolean hasUnpaid = project.getInvoices().stream()
                        .anyMatch(i -> i.getStatus() == InvoiceStatus.UNPAID);
                if (hasUnpaid) {
                    score += 10;
                }
            }
        }

        return Math.min(100, Math.max(0, score));
    }

    private String generateRecommendation(Project project, long slippageDays, List<Milestone> milestones, int riskScore) {
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return "Project deliverables finalized. Transitioning into warranty support phase.";
        }

        List<String> overdueTitles = milestones.stream()
                .filter(m -> !m.isCompleted() && m.getDueDate() != null && m.getDueDate().isBefore(LocalDate.now()))
                .map(Milestone::getTitle)
                .collect(Collectors.toList());

        if (!overdueTitles.isEmpty()) {
            return "Overdue deliverables detected ('" + overdueTitles.get(0) + "'). Allocate senior technical bandwidth immediately.";
        }

        if (slippageDays > 14) {
            return "Projected " + slippageDays + "-day milestone slippage. Initiate scope triage and buffer sprint with " + project.getClient().getCompany() + ".";
        }

        if (riskScore > 40) {
            return "Milestone cadence is trending behind target. Consider parallelizing sprint items.";
        }

        return "Delivery velocity is steady. On schedule to meet target completion dates.";
    }

    /**
     * Forecasts all projects currently in progress or planning.
     */
    public List<ProjectForecastDto> forecastAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::forecastProject)
                .sorted(Comparator.comparingInt(ProjectForecastDto::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Forecasts projects for a specific client.
     */
    public List<ProjectForecastDto> forecastProjectsForClient(Long clientId) {
        return projectRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::forecastProject)
                .collect(Collectors.toList());
    }
}
