package com.clientportal.controller;

import com.clientportal.dto.DashboardMetricsDto;
import com.clientportal.dto.ProjectForecastDto;
import com.clientportal.entity.Project;
import com.clientportal.entity.ProjectStatus;
import com.clientportal.entity.Role;
import com.clientportal.entity.User;
import com.clientportal.service.PredictiveAnalyticsService;
import com.clientportal.service.ProjectService;
import com.clientportal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    private final ProjectService projectService;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    private final UserService userService;

    public AnalyticsApiController(ProjectService projectService,
                                  PredictiveAnalyticsService predictiveAnalyticsService,
                                  UserService userService) {
        this.projectService = projectService;
        this.predictiveAnalyticsService = predictiveAnalyticsService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        DashboardMetricsDto metrics = projectService.getDashboardMetrics();
        List<ProjectForecastDto> forecasts = predictiveAnalyticsService.forecastAllProjects();

        Map<String, Object> response = new HashMap<>();
        response.put("metrics", metrics);
        response.put("forecasts", forecasts);
        response.put("benchmarkTarget", 45892.00);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{id}/forecast")
    public ResponseEntity<ProjectForecastDto> getProjectForecast(@PathVariable Long id, Authentication authentication) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));

        // Tenancy check: If user is client, verify project ownership
        if (authentication != null) {
            String email = authentication.getName();
            User user = userService.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == Role.CLIENT) {
                if (!project.getClient().getId().equals(user.getId())) {
                    throw new AccessDeniedException("Unauthorized to view forecast for other projects");
                }
            }
        }

        ProjectForecastDto forecast = predictiveAnalyticsService.forecastProject(project);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData() {
        List<Project> projects = projectService.findAllProjects();
        DashboardMetricsDto metrics = projectService.getDashboardMetrics();

        // 1. Status distribution
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            long count = projects.stream().filter(p -> p.getStatus() == status).count();
            statusCounts.put(status.getDisplayName(), count);
        }

        // 2. Financial Breakdown vs $45,892 benchmark
        Map<String, Object> financialData = new LinkedHashMap<>();
        financialData.put("Paid", metrics.getPaidRevenue());
        financialData.put("Unpaid", metrics.getUnpaidRevenue());
        financialData.put("Overdue", metrics.getOverdueRevenue());
        financialData.put("BenchmarkTarget", metrics.getPipelineBenchmark());

        // 3. Risk distribution
        List<ProjectForecastDto> forecasts = predictiveAnalyticsService.forecastAllProjects();
        Map<String, Long> riskCounts = new LinkedHashMap<>();
        riskCounts.put("Low Risk", forecasts.stream().filter(f -> "LOW".equals(f.getRiskLevel())).count());
        riskCounts.put("Moderate Risk", forecasts.stream().filter(f -> "MODERATE".equals(f.getRiskLevel())).count());
        riskCounts.put("Critical Risk", forecasts.stream().filter(f -> "CRITICAL".equals(f.getRiskLevel())).count());

        Map<String, Object> chartPayload = new HashMap<>();
        chartPayload.put("statusDistribution", statusCounts);
        chartPayload.put("financialBreakdown", financialData);
        chartPayload.put("riskDistribution", riskCounts);

        return ResponseEntity.ok(chartPayload);
    }
}
