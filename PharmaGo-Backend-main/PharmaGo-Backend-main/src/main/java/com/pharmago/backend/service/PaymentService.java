package com.pharmago.backend.service;

import com.pharmago.backend.entity.Order;
import com.pharmago.backend.entity.Payment;
import com.pharmago.backend.repository.OrderRepository;
import com.pharmago.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Transactional
    public Payment processPayment(Order order, String paymentMethod) {
        // In a real app, this would call a payment gateway (Stripe/Razorpay)
        // Here we simulate a successful payment for development
        
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(Payment.PaymentStatus.SUCCESS)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .paymentMethod(paymentMethod)
                .build();

        paymentRepository.save(payment);

        // Update Order Status
        if (order.getStatus() == Order.OrderStatus.PLACED || order.getStatus() == Order.OrderStatus.PENDING_VERIFICATION) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }
        orderRepository.save(order);

        // Generate Invoice automatically upon successful payment
        invoiceService.generateInvoice(order);

        return payment;
    }

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + order.getId()));
    }
}
