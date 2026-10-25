package com.pharmago.backend.service;

import com.pharmago.backend.entity.Invoice;
import com.pharmago.backend.entity.Order;
import com.pharmago.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice generateInvoice(Order order) {
        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .amount(order.getTotalAmount())
                .build();
        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceByOrder(Order order) {
        return invoiceRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Invoice not found for order: " + order.getId()));
    }
}
