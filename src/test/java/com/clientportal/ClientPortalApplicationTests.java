package com.clientportal;

import com.clientportal.dto.DashboardMetricsDto;
import com.clientportal.entity.*;
import com.clientportal.repository.*;
import com.clientportal.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ClientPortalApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ProjectMessageRepository projectMessageRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Verify DatabaseSeeder provisions Admin and Client profiles with BCrypt")
    void testDatabaseSeederProfiles() {
        Optional<User> adminOpt = userRepository.findByEmail("admin@portal.com");
        assertThat(adminOpt).isPresent();
        User admin = adminOpt.get();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getCompany()).isEqualTo("Data Alcott Systems");
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();

        Optional<User> clientOpt = userRepository.findByEmail("client@techretail.com");
        assertThat(clientOpt).isPresent();
        User client = clientOpt.get();
        assertThat(client.getRole()).isEqualTo(Role.CLIENT);
        assertThat(client.getCompany()).isEqualTo("TechRetail Inc.");
        assertThat(passwordEncoder.matches("client123", client.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Verify Dynamic Milestone Progress Percentage Calculation")
    void testDynamicProgressCalculation() {
        Project testProject = new Project("Test Progress Calculation", "Testing dynamic ratio", null, ProjectStatus.IN_PROGRESS, LocalDate.now(), LocalDate.now().plusDays(30));

        // 0 milestones -> 0%
        assertThat(testProject.getProgressPercentage()).isEqualTo(0);

        Milestone m1 = new Milestone(testProject, "M1", LocalDate.now(), true);
        Milestone m2 = new Milestone(testProject, "M2", LocalDate.now(), false);
        testProject.getMilestones().add(m1);
        testProject.getMilestones().add(m2);

        // 1 of 2 completed -> 50%
        assertThat(testProject.getProgressPercentage()).isEqualTo(50);

        Milestone m3 = new Milestone(testProject, "M3", LocalDate.now(), true);
        Milestone m4 = new Milestone(testProject, "M4", LocalDate.now(), true);
        testProject.getMilestones().add(m3);
        testProject.getMilestones().add(m4);

        // 3 of 4 completed -> 75%
        assertThat(testProject.getProgressPercentage()).isEqualTo(75);
    }

    @Test
    @DisplayName("Verify Enterprise Target Benchmark of $45,892 in Aggregated Pipeline Metrics")
    void testEnterprisePipelineBenchmark() {
        DashboardMetricsDto metrics = projectService.getDashboardMetrics();
        assertThat(metrics.getPipelineBenchmark()).isEqualByComparingTo(new BigDecimal("45892.00"));
        assertThat(metrics.getBenchmarkAttainmentPercentage()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Public Access: /login is accessible to unauthenticated guests")
    void testPublicLoginRoute() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("RBAC Security: Unauthenticated request to /admin/dashboard redirects to /login")
    void testAdminUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("RBAC Security: Client role accessing /admin/dashboard receives HTTP 403 Forbidden")
    @WithMockUser(username = "client@techretail.com", roles = {"CLIENT"})
    void testClientForbiddenFromAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC Security: Admin role successfully accesses /admin/dashboard")
    @WithMockUser(username = "admin@portal.com", roles = {"ADMIN"})
    void testAdminAuthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("metrics", "projects", "clients"));
    }

    @Test
    @DisplayName("RBAC Security: Client role successfully accesses /clients/dashboard")
    @WithMockUser(username = "client@techretail.com", roles = {"CLIENT"})
    void testClientAuthorizedAccess() throws Exception {
        mockMvc.perform(get("/clients/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/dashboard"))
                .andExpect(model().attributeExists("client", "projects", "invoices", "totalPaid", "totalOutstanding"));
    }

    @Autowired
    private com.clientportal.service.PredictiveAnalyticsService predictiveAnalyticsService;

    @Test
    @org.springframework.transaction.annotation.Transactional
    @DisplayName("Predictive Analytics: Verify velocity calculation and risk scoring model")
    void testPredictiveAnalyticsForecasting() {
        Project project = projectRepository.findAll().stream().findFirst().orElseThrow();
        com.clientportal.dto.ProjectForecastDto forecast = predictiveAnalyticsService.forecastProject(project);

        assertThat(forecast).isNotNull();
        assertThat(forecast.getProjectId()).isEqualTo(project.getId());
        assertThat(forecast.getProjectedEndDate()).isNotNull();
        assertThat(forecast.getRiskScore()).isBetween(0, 100);
        assertThat(forecast.getRiskLevel()).isIn("LOW", "MODERATE", "CRITICAL");
        assertThat(forecast.getRecommendation()).isNotBlank();
        assertThat(forecast.getMilestoneVelocityDays()).isGreaterThan(0);
    }

    @Test
    @DisplayName("REST API: Verify /api/analytics/dashboard returns metrics and forecasts JSON")
    @WithMockUser(username = "admin@portal.com", roles = {"ADMIN"})
    void testAnalyticsDashboardApi() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics").exists())
                .andExpect(jsonPath("$.forecasts").isArray())
                .andExpect(jsonPath("$.benchmarkTarget").value(45892.00));
    }

    @Test
    @DisplayName("REST API: Verify /api/analytics/chart-data returns status and financial breakdowns")
    @WithMockUser(username = "admin@portal.com", roles = {"ADMIN"})
    void testAnalyticsChartDataApi() throws Exception {
        mockMvc.perform(get("/api/analytics/chart-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDistribution").isMap())
                .andExpect(jsonPath("$.financialBreakdown").isMap())
                .andExpect(jsonPath("$.financialBreakdown.BenchmarkTarget").value(45892.00))
                .andExpect(jsonPath("$.riskDistribution").isMap());
    }

    @Test
    @DisplayName("REST API: Verify /api/analytics/projects/{id}/forecast returns single project forecast")
    @WithMockUser(username = "admin@portal.com", roles = {"ADMIN"})
    void testProjectForecastApi() throws Exception {
        Project project = projectRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(get("/api/analytics/projects/" + project.getId() + "/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId()))
                .andExpect(jsonPath("$.riskScore").isNumber())
                .andExpect(jsonPath("$.recommendation").isString());
    }

    @Test
    @DisplayName("Communication: Verify seeded project discussion messages exist")
    void testProjectCommunicationSeeding() {
        assertThat(projectMessageRepository.count()).isGreaterThanOrEqualTo(5);
        Project project1 = projectRepository.findAll().stream().filter(p -> p.getTitle().contains("Omnichannel")).findFirst().orElseThrow();
        assertThat(projectMessageRepository.findByProjectIdOrderByCreatedAtAsc(project1.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Communication: Verify Admin can post an update to project discussion")
    @WithMockUser(username = "admin@portal.com", roles = {"ADMIN"})
    void testAdminCanPostProjectMessage() throws Exception {
        Project project = projectRepository.findAll().stream().findFirst().orElseThrow();
        long initialCount = projectMessageRepository.countByProjectId(project.getId());

        mockMvc.perform(post("/admin/projects/" + project.getId() + "/messages")
                        .with(csrf())
                        .param("message", "Sprint review completed. Moving to staging."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/projects/" + project.getId()))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(projectMessageRepository.countByProjectId(project.getId())).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("Communication: Verify Client can post inquiry to their assigned project")
    @WithMockUser(username = "client@techretail.com", roles = {"CLIENT"})
    void testClientCanPostProjectMessage() throws Exception {
        User client = userRepository.findByEmail("client@techretail.com").orElseThrow();
        Project clientProject = projectRepository.findByClientId(client.getId()).stream().findFirst().orElseThrow();
        long initialCount = projectMessageRepository.countByProjectId(clientProject.getId());

        mockMvc.perform(post("/clients/projects/" + clientProject.getId() + "/messages")
                        .with(csrf())
                        .param("message", "We uploaded the POS hardware staging spec for review."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(projectMessageRepository.countByProjectId(clientProject.getId())).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("Communication RBAC: Client forbidden from posting to another client's project")
    @WithMockUser(username = "client@techretail.com", roles = {"CLIENT"})
    void testClientForbiddenFromPostingToUnassignedProject() throws Exception {
        User otherClient = userRepository.findByEmail("client2@novabanking.com").orElseThrow();
        Project otherProject = projectRepository.findByClientId(otherClient.getId()).stream().findFirst().orElseThrow();

        mockMvc.perform(post("/clients/projects/" + otherProject.getId() + "/messages")
                        .with(csrf())
                        .param("message", "Unauthorized message attempt."))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Document Sharing: Verify Client can upload a file to their assigned project")
    @WithMockUser(username = "client@techretail.com", roles = {"CLIENT"})
    void testClientDocumentUpload() throws Exception {
        User client = userRepository.findByEmail("client@techretail.com").orElseThrow();
        Project clientProject = projectRepository.findByClientId(client.getId()).stream().findFirst().orElseThrow();
        long initialDocCount = documentRepository.findByProjectId(clientProject.getId()).size();

        MockMultipartFile testFile = new MockMultipartFile(
                "file",
                "client_requirements_v1.txt",
                "text/plain",
                "Client custom hardware requirements notes".getBytes()
        );

        mockMvc.perform(multipart("/clients/projects/" + clientProject.getId() + "/documents/upload")
                        .file(testFile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(documentRepository.findByProjectId(clientProject.getId()).size()).isEqualTo(initialDocCount + 1);
    }
}
