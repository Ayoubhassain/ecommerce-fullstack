package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.OrderRepository;
import com.luv2code.ecommerce.dto.OrderSummaryDto;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class OrderService {

    public static final Set<String> STATUSES = Set.of("NEW", "SHIPPED", "DELIVERED", "CANCELLED");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> findForCustomer(String email, Pageable pageable) {
        return orderRepository.findByCustomerEmailIgnoreCaseOrderByDateCreatedDesc(email, pageable)
                .map(OrderSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> findAll(Pageable pageable) {
        return orderRepository.findAllByOrderByDateCreatedDesc(pageable).map(OrderSummaryDto::from);
    }

    @Transactional
    public OrderSummaryDto updateStatus(Long id, String status) {
        String normalized = status.trim().toUpperCase();
        if (!STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Status must be one of " + STATUSES);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
        order.setStatus(normalized);
        return OrderSummaryDto.from(orderRepository.save(order));
    }
}
