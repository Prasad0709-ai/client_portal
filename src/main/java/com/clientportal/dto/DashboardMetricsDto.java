package com.clientportal.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DashboardMetricsDto {

    private long activeProjectsCount;
    private long totalProjectsCount;
    private long totalMilestonesCount;
    private long completedMilestonesCount;
    private long totalDocumentsCount;

    // Hardcoded administrative tracking benchmark baseline representing enterprise target of $45,892
    private BigDecimal pipelineBenchmark = new BigDecimal("45892.00");
    private BigDecimal actualPipelineRevenue = BigDecimal.ZERO;
    private BigDecimal paidRevenue = BigDecimal.ZERO;
    private BigDecimal unpaidRevenue = BigDecimal.ZERO;
    private BigDecimal overdueRevenue = BigDecimal.ZERO;

    public DashboardMetricsDto() {
    }

    public int getOverallMilestoneProgressPercentage() {
        if (totalMilestonesCount == 0) return 0;
        return (int) Math.round((completedMilestonesCount * 100.0) / totalMilestonesCount);
    }

    public int getBenchmarkAttainmentPercentage() {
        if (pipelineBenchmark == null || pipelineBenchmark.compareTo(BigDecimal.ZERO) == 0) return 0;
        if (actualPipelineRevenue == null) return 0;
        BigDecimal percentage = actualPipelineRevenue.multiply(new BigDecimal("100"))
                .divide(pipelineBenchmark, 0, RoundingMode.HALF_UP);
        return Math.min(percentage.intValue(), 100);
    }

    public long getActiveProjectsCount() {
        return activeProjectsCount;
    }

    public void setActiveProjectsCount(long activeProjectsCount) {
        this.activeProjectsCount = activeProjectsCount;
    }

    public long getTotalProjectsCount() {
        return totalProjectsCount;
    }

    public void setTotalProjectsCount(long totalProjectsCount) {
        this.totalProjectsCount = totalProjectsCount;
    }

    public long getTotalMilestonesCount() {
        return totalMilestonesCount;
    }

    public void setTotalMilestonesCount(long totalMilestonesCount) {
        this.totalMilestonesCount = totalMilestonesCount;
    }

    public long getCompletedMilestonesCount() {
        return completedMilestonesCount;
    }

    public void setCompletedMilestonesCount(long completedMilestonesCount) {
        this.completedMilestonesCount = completedMilestonesCount;
    }

    public long getTotalDocumentsCount() {
        return totalDocumentsCount;
    }

    public void setTotalDocumentsCount(long totalDocumentsCount) {
        this.totalDocumentsCount = totalDocumentsCount;
    }

    public BigDecimal getPipelineBenchmark() {
        return pipelineBenchmark;
    }

    public void setPipelineBenchmark(BigDecimal pipelineBenchmark) {
        this.pipelineBenchmark = pipelineBenchmark;
    }

    public BigDecimal getActualPipelineRevenue() {
        return actualPipelineRevenue;
    }

    public void setActualPipelineRevenue(BigDecimal actualPipelineRevenue) {
        this.actualPipelineRevenue = actualPipelineRevenue;
    }

    public BigDecimal getPaidRevenue() {
        return paidRevenue;
    }

    public void setPaidRevenue(BigDecimal paidRevenue) {
        this.paidRevenue = paidRevenue;
    }

    public BigDecimal getUnpaidRevenue() {
        return unpaidRevenue;
    }

    public void setUnpaidRevenue(BigDecimal unpaidRevenue) {
        this.unpaidRevenue = unpaidRevenue;
    }

    public BigDecimal getOverdueRevenue() {
        return overdueRevenue;
    }

    public void setOverdueRevenue(BigDecimal overdueRevenue) {
        this.overdueRevenue = overdueRevenue;
    }
}
