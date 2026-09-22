package com.clientportal.repository;

import com.clientportal.entity.Invoice;
import com.clientportal.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByProjectId(Long projectId);

    List<Invoice> findByProjectClientId(Long clientId);

    List<Invoice> findByProjectClientIdOrderByDueDateAsc(Long clientId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i")
    BigDecimal calculateTotalInvoiced();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal calculateTotalByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.project.client.id = :clientId")
    BigDecimal calculateTotalForClient(@Param("clientId") Long clientId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.project.client.id = :clientId AND i.status = :status")
    BigDecimal calculateTotalForClientByStatus(@Param("clientId") Long clientId, @Param("status") InvoiceStatus status);
}
