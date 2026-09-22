package com.clientportal.controller;

import com.clientportal.entity.Invoice;
import com.clientportal.entity.Project;
import com.clientportal.entity.User;
import com.clientportal.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientPortalController {

    private final UserService userService;
    private final ProjectService projectService;
    private final InvoiceService invoiceService;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    private final ProjectMessageService projectMessageService;
    private final DocumentService documentService;

    public ClientPortalController(UserService userService,
                                  ProjectService projectService,
                                  InvoiceService invoiceService,
                                  PredictiveAnalyticsService predictiveAnalyticsService,
                                  ProjectMessageService projectMessageService,
                                  DocumentService documentService) {
        this.userService = userService;
        this.projectService = projectService;
        this.invoiceService = invoiceService;
        this.predictiveAnalyticsService = predictiveAnalyticsService;
        this.projectMessageService = projectMessageService;
        this.documentService = documentService;
    }

    @GetMapping("/dashboard")
    public String clientDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User client = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated client not found: " + email));

        List<Project> clientProjects = projectService.findProjectsByClientId(client.getId());
        List<Invoice> clientInvoices = invoiceService.findByClientId(client.getId());
        List<com.clientportal.dto.ProjectForecastDto> clientForecasts = predictiveAnalyticsService.forecastProjectsForClient(client.getId());

        BigDecimal totalInvoiced = invoiceService.getTotalInvoicedForClient(client.getId());
        BigDecimal totalPaid = invoiceService.getPaidForClient(client.getId());
        BigDecimal totalOutstanding = invoiceService.getOutstandingForClient(client.getId());

        long totalMilestones = clientProjects.stream().mapToLong(Project::getTotalMilestonesCount).sum();
        long completedMilestones = clientProjects.stream().mapToLong(Project::getCompletedMilestonesCount).sum();
        int overallProgress = totalMilestones == 0 ? 0 : (int) Math.round((completedMilestones * 100.0) / totalMilestones);

        long totalDocuments = clientProjects.stream().mapToLong(p -> p.getDocuments().size()).sum();

        model.addAttribute("client", client);
        model.addAttribute("projects", clientProjects);
        model.addAttribute("invoices", clientInvoices);
        model.addAttribute("forecasts", clientForecasts);
        model.addAttribute("totalInvoiced", totalInvoiced != null ? totalInvoiced : BigDecimal.ZERO);
        model.addAttribute("totalPaid", totalPaid != null ? totalPaid : BigDecimal.ZERO);
        model.addAttribute("totalOutstanding", totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO);
        model.addAttribute("overallProgress", overallProgress);
        model.addAttribute("totalDocuments", totalDocuments);

        return "clients/dashboard";
    }

    @PostMapping("/projects/{projectId}/messages")
    public String postMessage(@PathVariable Long projectId,
                              @RequestParam("message") String content,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User client = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated client not found: " + email));

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        // Tenant isolation: client can only post to their own projects
        if (!project.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("Access denied: You can only communicate on your assigned projects.");
        }

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message content cannot be blank.");
            return "redirect:/clients/dashboard";
        }

        projectMessageService.postMessage(projectId, client.getId(), content);
        redirectAttributes.addFlashAttribute("successMessage", "Message posted to project discussion channel.");
        return "redirect:/clients/dashboard";
    }

    @PostMapping("/projects/{projectId}/documents/upload")
    public String uploadDocument(@PathVariable Long projectId,
                                 @RequestParam("file") MultipartFile file,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User client = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated client not found: " + email));

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        // Tenant isolation: client can only upload to their own projects
        if (!project.getClient().getId().equals(client.getId())) {
            throw new AccessDeniedException("Access denied: You can only upload files to your assigned projects.");
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/clients/dashboard";
        }

        try {
            documentService.storeFile(projectId, file);
            redirectAttributes.addFlashAttribute("successMessage", "Document '" + file.getOriginalFilename() + "' shared successfully.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }

        return "redirect:/clients/dashboard";
    }
}
