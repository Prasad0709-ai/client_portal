package com.clientportal.controller;

import com.clientportal.entity.Document;
import com.clientportal.entity.Role;
import com.clientportal.entity.User;
import com.clientportal.service.DocumentService;
import com.clientportal.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/documents")
public class DocumentDownloadController {

    private final DocumentService documentService;
    private final UserService userService;

    public DocumentDownloadController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        Document document = documentService.findById(id);

        // Security check: If client, verify document belongs to client's project
        String email = authentication.getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (currentUser.getRole() == Role.CLIENT) {
            Long projectClientId = document.getProject().getClient().getId();
            if (!projectClientId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Unauthorized to access document from other organizations");
            }
        }

        Resource resource = documentService.loadFileAsResource(id);

        String contentType = "application/octet-stream";
        try {
            Path path = Paths.get(document.getFilePath());
            String probeType = Files.probeContentType(path);
            if (probeType != null) {
                contentType = probeType;
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
}
