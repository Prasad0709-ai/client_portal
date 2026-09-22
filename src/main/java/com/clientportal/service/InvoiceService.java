package com.clientportal.service;

import com.clientportal.dto.InvoiceDto;
import com.clientportal.entity.Invoice;
import com.clientportal.entity.InvoiceStatus;
import com.clientportal.entity.Project;
import com.clientportal.repository.InvoiceRepository;
import com.clientportal.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProjectRepository projectRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, ProjectRepository projectRepository) {
        this.invoiceRepository = invoiceRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByProjectId(Long projectId) {
        return invoiceRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByClientId(Long clientId) {
        return invoiceRepository.findByProjectClientIdOrderByDueDateAsc(clientId);
    }

    public Invoice createInvoice(Long projectId, InvoiceDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        Invoice invoice = new Invoice(
                project,
                dto.getAmount(),
                dto.getStatus() != null ? dto.getStatus() : InvoiceStatus.UNPAID,
                dto.getIssueDate(),
                dto.getDueDate()
        );
        return invoiceRepository.save(invoice);
    }

    public Invoice updateStatus(Long invoiceId, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new IllegalArgumentException("Invoice not found with ID: " + invoiceId);
        }
        invoiceRepository.deleteById(invoiceId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInvoicedForClient(Long clientId) {
        return invoiceRepository.calculateTotalForClient(clientId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getPaidForClient(Long clientId) {
        return invoiceRepository.calculateTotalForClientByStatus(clientId, InvoiceStatus.PAID);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOutstandingForClient(Long clientId) {
        BigDecimal unpaid = invoiceRepository.calculateTotalForClientByStatus(clientId, InvoiceStatus.UNPAID);
        BigDecimal overdue = invoiceRepository.calculateTotalForClientByStatus(clientId, InvoiceStatus.OVERDUE);
        return (unpaid != null ? unpaid : BigDecimal.ZERO).add(overdue != null ? overdue : BigDecimal.ZERO);
    }
}
