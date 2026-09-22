package com.clientportal.service;

import com.clientportal.entity.Document;
import com.clientportal.entity.Project;
import com.clientportal.repository.DocumentRepository;
import com.clientportal.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final Path fileStorageLocation;

    public DocumentService(DocumentRepository documentRepository,
                           ProjectRepository projectRepository,
                           @Value("${app.upload.dir:./data/uploads/}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where uploaded files will be stored.", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Document> findByProjectId(Long projectId) {
        return documentRepository.findByProjectIdOrderByUploadedAtDesc(projectId);
    }

    @Transactional(readOnly = true)
    public Document findById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));
    }

    public Document storeFile(Long projectId, MultipartFile file) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence " + originalFileName);
        }

        // Generate unique physical filename to avoid name collisions
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        String storedFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        Path projectDir = this.fileStorageLocation.resolve("project_" + projectId);
        Files.createDirectories(projectDir);

        Path targetLocation = projectDir.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document(
                project,
                originalFileName,
                targetLocation.toString(),
                file.getSize()
        );

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long documentId) {
        Document document = findById(documentId);
        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or unreadable: " + document.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path malformed: " + document.getFileName(), ex);
        }
    }

    public void deleteDocument(Long documentId) {
        Document document = findById(documentId);
        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        documentRepository.delete(document);
    }
}
