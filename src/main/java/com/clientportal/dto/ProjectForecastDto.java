package com.clientportal.dto;

import java.time.LocalDate;

public class ProjectForecastDto {

    private Long projectId;
    private String projectTitle;
    private String clientCompany;
    private LocalDate targetEndDate;
    private LocalDate projectedEndDate;
    private long slippageDays;
    private int riskScore; // 0 - 100
    private String riskLevel; // LOW, MODERATE, CRITICAL
    private String riskBadgeClass;
    private double milestoneVelocityDays; // Average days per milestone
    private long completedMilestones;
    private long totalMilestones;
    private int completionProbability; // 0 - 100%
    private String recommendation;

    public ProjectForecastDto() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getClientCompany() {
        return clientCompany;
    }

    public void setClientCompany(String clientCompany) {
        this.clientCompany = clientCompany;
    }

    public LocalDate getTargetEndDate() {
        return targetEndDate;
    }

    public void setTargetEndDate(LocalDate targetEndDate) {
        this.targetEndDate = targetEndDate;
    }

    public LocalDate getProjectedEndDate() {
        return projectedEndDate;
    }

    public void setProjectedEndDate(LocalDate projectedEndDate) {
        this.projectedEndDate = projectedEndDate;
    }

    public long getSlippageDays() {
        return slippageDays;
    }

    public void setSlippageDays(long slippageDays) {
        this.slippageDays = slippageDays;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskBadgeClass() {
        return riskBadgeClass;
    }

    public void setRiskBadgeClass(String riskBadgeClass) {
        this.riskBadgeClass = riskBadgeClass;
    }

    public double getMilestoneVelocityDays() {
        return milestoneVelocityDays;
    }

    public void setMilestoneVelocityDays(double milestoneVelocityDays) {
        this.milestoneVelocityDays = milestoneVelocityDays;
    }

    public long getCompletedMilestones() {
        return completedMilestones;
    }

    public void setCompletedMilestones(long completedMilestones) {
        this.completedMilestones = completedMilestones;
    }

    public long getTotalMilestones() {
        return totalMilestones;
    }

    public void setTotalMilestones(long totalMilestones) {
        this.totalMilestones = totalMilestones;
    }

    public int getCompletionProbability() {
        return completionProbability;
    }

    public void setCompletionProbability(int completionProbability) {
        this.completionProbability = completionProbability;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
