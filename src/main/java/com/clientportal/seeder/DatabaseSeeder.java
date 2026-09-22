package com.clientportal.seeder;

import com.clientportal.entity.*;
import com.clientportal.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final DocumentRepository documentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProjectMessageRepository projectMessageRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadDirectory;

    public DatabaseSeeder(UserRepository userRepository,
                          ProjectRepository projectRepository,
                          MilestoneRepository milestoneRepository,
                          DocumentRepository documentRepository,
                          InvoiceRepository invoiceRepository,
                          ProjectMessageRepository projectMessageRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.upload.dir:./data/uploads/}") String uploadDir) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.documentRepository = documentRepository;
        this.invoiceRepository = invoiceRepository;
        this.projectMessageRepository = projectMessageRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Admin Profile
        User admin = new User(
                "Alex Vance (System Admin)",
                "admin@portal.com",
                passwordEncoder.encode("admin123"),
                "Data Alcott Systems",
                Role.ADMIN
        );
        userRepository.save(admin);

        // 2. Create Client Profile
        User client = new User(
                "Marcus Sterling",
                "client@techretail.com",
                passwordEncoder.encode("client123"),
                "TechRetail Inc.",
                Role.CLIENT
        );
        userRepository.save(client);

        // Additional enterprise client for rich multi-tenant view
        User client2 = new User(
                "Elena Rostova",
                "client2@novabanking.com",
                passwordEncoder.encode("client123"),
                "Nova Banking Solutions",
                Role.CLIENT
        );
        userRepository.save(client2);

        // 3. Seed Primary Project 1 for TechRetail Inc.
        Project project1 = new Project(
                "Omnichannel POS Cloud Modernization",
                "Migration and refactoring of legacy retail point-of-sale infrastructure into resilient microservices with real-time inventory synchronization and PCI-compliant tokenized payment pipelines.",
                client,
                ProjectStatus.IN_PROGRESS,
                LocalDate.now().minusDays(45),
                LocalDate.now().plusDays(45)
        );
        projectRepository.save(project1);

        // Milestones for Project 1
        Milestone m1 = new Milestone(project1, "Architecture Blueprint & Requirements Gathering", LocalDate.now().minusDays(30), true);
        Milestone m2 = new Milestone(project1, "Inventory API & Microservices Scaffold", LocalDate.now().minusDays(10), true);
        Milestone m3 = new Milestone(project1, "PCI-DSS Payment Gateway Integration", LocalDate.now().plusDays(15), false);
        Milestone m4 = new Milestone(project1, "Load Testing & Storewide Deployment", LocalDate.now().plusDays(45), false);
        milestoneRepository.save(m1);
        milestoneRepository.save(m2);
        milestoneRepository.save(m3);
        milestoneRepository.save(m4);

        // Create physical sample documents on disk and persist records
        createSampleDocument(project1, "Architecture_Specification_v2.4.pdf", "Enterprise Architecture Blueprint (JV-CRM-005)\nSystem topology, API contracts, and security boundaries.");
        createSampleDocument(project1, "Payment_Integration_Guide.pdf", "PCI-DSS tokenization workflows, webhook endpoints, and transaction lifecycle documentation.");
        createSampleDocument(project1, "Security_Compliance_Audit.pdf", "SOC2 Type II validation and penetration test compliance sign-off report.");

        // Invoices for Project 1 totaling the $45,892.00 target baseline:
        // $14,500.00 (PAID) + $16,800.00 (UNPAID) + $14,592.00 (UNPAID) = $45,892.00
        Invoice inv1 = new Invoice(project1, new BigDecimal("14500.00"), InvoiceStatus.PAID, LocalDate.now().minusDays(35), LocalDate.now().minusDays(5));
        Invoice inv2 = new Invoice(project1, new BigDecimal("16800.00"), InvoiceStatus.UNPAID, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20));
        Invoice inv3 = new Invoice(project1, new BigDecimal("14592.00"), InvoiceStatus.UNPAID, LocalDate.now(), LocalDate.now().plusDays(30));
        invoiceRepository.save(inv1);
        invoiceRepository.save(inv2);
        invoiceRepository.save(inv3);

        // 4. Seed Secondary Project for Nova Banking Solutions
        Project project2 = new Project(
                "Core Banking Mobile API Integration",
                "High-throughput transactional API gateway for mobile banking app with biometric authentication and instantaneous settlement.",
                client2,
                ProjectStatus.PLANNING,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(80)
        );
        projectRepository.save(project2);

        Milestone m2_1 = new Milestone(project2, "Security & Compliance Scoping", LocalDate.now().plusDays(10), true);
        Milestone m2_2 = new Milestone(project2, "OpenAPI Specifications & Mock Servers", LocalDate.now().plusDays(30), false);
        Milestone m2_3 = new Milestone(project2, "Production Sandbox Certification", LocalDate.now().plusDays(60), false);
        milestoneRepository.save(m2_1);
        milestoneRepository.save(m2_2);
        milestoneRepository.save(m2_3);

        createSampleDocument(project2, "API_Design_Contract.json", "{\n  \"openapi\": \"3.0.1\",\n  \"info\": {\"title\": \"Nova Banking Mobile Gateway\", \"version\": \"1.0.0\"}\n}");

        Invoice inv4 = new Invoice(project2, new BigDecimal("22500.00"), InvoiceStatus.PAID, LocalDate.now().minusDays(8), LocalDate.now().plusDays(15));
        Invoice inv5 = new Invoice(project2, new BigDecimal("18200.00"), InvoiceStatus.OVERDUE, LocalDate.now().minusDays(40), LocalDate.now().minusDays(10));
        invoiceRepository.save(inv4);
        invoiceRepository.save(inv5);

        // 5. Seed Project Communication Messages
        ProjectMessage msg1 = new ProjectMessage(
                project1,
                client,
                "Hi Alex, our engineering team has reviewed the Architecture Blueprint v2.4. Could you confirm the target sandbox date for the PCI-DSS tokenized payment gateway?"
        );
        ProjectMessage msg2 = new ProjectMessage(
                project1,
                admin,
                "Hello Marcus! The microservices scaffold is complete and load-tested. We are initiating the payment gateway sandbox configuration ahead of schedule this Thursday."
        );
        ProjectMessage msg3 = new ProjectMessage(
                project1,
                client,
                "Outstanding update. We uploaded the updated store terminal specifications under the project documents tab for reference."
        );
        projectMessageRepository.save(msg1);
        projectMessageRepository.save(msg2);
        projectMessageRepository.save(msg3);

        ProjectMessage msg4 = new ProjectMessage(
                project2,
                client2,
                "Welcome onboard team. We have provided our OpenAPI draft for the mobile biometric auth endpoints."
        );
        ProjectMessage msg5 = new ProjectMessage(
                project2,
                admin,
                "Received Elena. Compliance team is auditing the contract before spinning up the mock servers."
        );
        projectMessageRepository.save(msg4);
        projectMessageRepository.save(msg5);
    }

    private void createSampleDocument(Project project, String fileName, String content) {
        try {
            Path projectDir = uploadDirectory.resolve("project_" + project.getId());
            Files.createDirectories(projectDir);
            Path filePath = projectDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, content);
            }
            long size = Files.size(filePath);
            Document doc = new Document(project, fileName, filePath.toString(), size);
            documentRepository.save(doc);
        } catch (IOException ignored) {
        }
    }
}
