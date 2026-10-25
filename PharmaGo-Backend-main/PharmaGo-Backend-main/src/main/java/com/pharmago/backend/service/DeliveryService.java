package com.pharmago.backend.service;

import com.pharmago.backend.entity.Delivery;
import com.pharmago.backend.entity.Order;
import com.pharmago.backend.repository.DeliveryRepository;
import com.pharmago.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Delivery createDelivery(Order order, String address) {
        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryAddress(address)
                .status(Delivery.DeliveryStatus.PENDING)
                .build();
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery assignDelivery(Long deliveryId, String personName, String personPhone) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        delivery.setDeliveryPersonName(personName);
        delivery.setDeliveryPersonPhone(personPhone);
        delivery.setStatus(Delivery.DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery updateStatus(Long deliveryId, Delivery.DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        
        delivery.setStatus(status);
        
        if (status == Delivery.DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
            delivery.getOrder().setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        } else if (status == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.getOrder().setStatus(Order.OrderStatus.DELIVERED);
        }
        
        orderRepository.save(delivery.getOrder());
        return deliveryRepository.save(delivery);
    }

    public Optional<Delivery> getDeliveryByOrder(Order order) {
        return deliveryRepository.findByOrder(order);
    }
}
