package com.pharmago.backend.repository;

import com.pharmago.backend.entity.Invoice;
import com.pharmago.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrder(Order order);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
