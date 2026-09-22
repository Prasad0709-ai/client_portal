package com.clientportal.controller;

import com.clientportal.dto.DashboardMetricsDto;
import com.clientportal.dto.InvoiceDto;
import com.clientportal.dto.MilestoneDto;
import com.clientportal.dto.ProjectDto;
import com.clientportal.dto.ProjectForecastDto;
import com.clientportal.entity.*;
import com.clientportal.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final MilestoneService milestoneService;
    private final DocumentService documentService;
    private final InvoiceService invoiceService;
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    private final ProjectMessageService projectMessageService;

    public AdminProjectController(ProjectService projectService,
                                  UserService userService,
                                  MilestoneService milestoneService,
                                  DocumentService documentService,
                                  InvoiceService invoiceService,
                                  PredictiveAnalyticsService predictiveAnalyticsService,
                                  ProjectMessageService projectMessageService) {
        this.projectService = projectService;
        this.userService = userService;
        this.milestoneService = milestoneService;
        this.documentService = documentService;
        this.invoiceService = invoiceService;
        this.predictiveAnalyticsService = predictiveAnalyticsService;
        this.projectMessageService = projectMessageService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardMetricsDto metrics = projectService.getDashboardMetrics();
        List<Project> projects = projectService.findAllProjects();
        List<User> clients = userService.findAllClients();
        List<ProjectForecastDto> forecasts = predictiveAnalyticsService.forecastAllProjects();

        model.addAttribute("metrics", metrics);
        model.addAttribute("projects", projects);
        model.addAttribute("clients", clients);
        model.addAttribute("forecasts", forecasts);
        model.addAttribute("newProject", new ProjectDto());
        model.addAttribute("statuses", ProjectStatus.values());

        return "admin/dashboard";
    }

    @GetMapping("/projects/new")
    public String newProjectForm(Model model) {
        model.addAttribute("projectDto", new ProjectDto());
        model.addAttribute("clients", userService.findAllClients());
        model.addAttribute("statuses", ProjectStatus.values());
        return "admin/project-form";
    }

    @PostMapping("/projects")
    public String createProject(@Valid @ModelAttribute("projectDto") ProjectDto projectDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", userService.findAllClients());
            model.addAttribute("statuses", ProjectStatus.values());
            return "admin/project-form";
        }

        projectService.createProject(projectDto);
        redirectAttributes.addFlashAttribute("successMessage", "Project '" + projectDto.getTitle() + "' created successfully.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/projects/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));

        model.addAttribute("project", project);
        model.addAttribute("milestones", milestoneService.findByProjectId(id));
        model.addAttribute("documents", documentService.findByProjectId(id));
        model.addAttribute("invoices", invoiceService.findByProjectId(id));
        model.addAttribute("messages", projectMessageService.getMessagesForProject(id));
        model.addAttribute("forecast", predictiveAnalyticsService.forecastProject(project));
        model.addAttribute("newMilestone", new MilestoneDto());
        model.addAttribute("newInvoice", new InvoiceDto());
        model.addAttribute("invoiceStatuses", InvoiceStatus.values());

        return "admin/project-detail";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));

        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setClientId(project.getClient().getId());
        dto.setStatus(project.getStatus());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());

        model.addAttribute("projectDto", dto);
        model.addAttribute("clients", userService.findAllClients());
        model.addAttribute("statuses", ProjectStatus.values());
        return "admin/project-form";
    }

    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("projectDto") ProjectDto projectDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", userService.findAllClients());
            model.addAttribute("statuses", ProjectStatus.values());
            return "admin/project-form";
        }

        projectService.updateProject(id, projectDto);
        redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully.");
        return "redirect:/admin/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Project and associated records deleted.");
        return "redirect:/admin/dashboard";
    }

    // -------------------------------------------------------------
    // Milestone Endpoints
    // -------------------------------------------------------------
    @PostMapping("/projects/{projectId}/milestones")
    public String addMilestone(@PathVariable Long projectId,
                               @ModelAttribute("newMilestone") MilestoneDto milestoneDto,
                               RedirectAttributes redirectAttributes) {
        milestoneService.addMilestone(projectId, milestoneDto);
        redirectAttributes.addFlashAttribute("successMessage", "Milestone added.");
        return "redirect:/admin/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/milestones/{milestoneId}/toggle")
    public String toggleMilestone(@PathVariable Long projectId,
                                  @PathVariable Long milestoneId,
                                  @RequestParam(value = "redirect", defaultValue = "detail") String redirect,
                                  RedirectAttributes redirectAttributes) {
        milestoneService.toggleMilestoneStatus(milestoneId);
        redirectAttributes.addFlashAttribute("successMessage", "Milestone status updated.");
        if ("dashboard".equals(redirect)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/milestones/{milestoneId}/delete")
    public String deleteMilestone(@PathVariable Long projectId,
                                  @PathVariable Long milestoneId,
                                  RedirectAttributes redirectAttributes) {
        milestoneService.deleteMilestone(milestoneId);
        redirectAttributes.addFlashAttribute("successMessage", "Milestone removed.");
        return "redirect:/admin/projects/" + projectId;
    }

    // -------------------------------------------------------------
    // Document Upload & Delete
    // -------------------------------------------------------------
    @PostMapping("/projects/{projectId}/documents/upload")
    public String uploadDocument(@PathVariable Long projectId,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please choose a file to upload.");
            return "redirect:/admin/projects/" + projectId;
        }

        try {
            documentService.storeFile(projectId, file);
            redirectAttributes.addFlashAttribute("successMessage", "File '" + file.getOriginalFilename() + "' uploaded successfully.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload file: " + e.getMessage());
        }

        return "redirect:/admin/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/documents/{documentId}/delete")
    public String deleteDocument(@PathVariable Long projectId,
                                 @PathVariable Long documentId,
                                 RedirectAttributes redirectAttributes) {
        documentService.deleteDocument(documentId);
        redirectAttributes.addFlashAttribute("successMessage", "Document deleted.");
        return "redirect:/admin/projects/" + projectId;
    }

    // -------------------------------------------------------------
    // Invoice Endpoints
    // -------------------------------------------------------------
    @PostMapping("/projects/{projectId}/invoices")
    public String addInvoice(@PathVariable Long projectId,
                             @Valid @ModelAttribute("newInvoice") InvoiceDto invoiceDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid invoice details provided.");
            return "redirect:/admin/projects/" + projectId;
        }

        invoiceService.createInvoice(projectId, invoiceDto);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice created successfully.");
        return "redirect:/admin/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/invoices/{invoiceId}/status")
    public String updateInvoiceStatus(@PathVariable Long projectId,
                                      @PathVariable Long invoiceId,
                                      @RequestParam("status") InvoiceStatus status,
                                      RedirectAttributes redirectAttributes) {
        invoiceService.updateStatus(invoiceId, status);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice status updated to " + status.getDisplayName());
        return "redirect:/admin/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/invoices/{invoiceId}/delete")
    public String deleteInvoice(@PathVariable Long projectId,
                                @PathVariable Long invoiceId,
                                RedirectAttributes redirectAttributes) {
        invoiceService.deleteInvoice(invoiceId);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice deleted.");
        return "redirect:/admin/projects/" + projectId;
    }

    // -------------------------------------------------------------
    // Communication & Project Discussion Endpoints
    // -------------------------------------------------------------
    @PostMapping("/projects/{projectId}/messages")
    public String postMessage(@PathVariable Long projectId,
                              @RequestParam("message") String content,
                              org.springframework.security.core.Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message cannot be empty.");
            return "redirect:/admin/projects/" + projectId;
        }

        User admin = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found: " + authentication.getName()));

        projectMessageService.postMessage(projectId, admin.getId(), content);
        redirectAttributes.addFlashAttribute("successMessage", "Update posted to project communication channel.");
        return "redirect:/admin/projects/" + projectId;
    }
}
