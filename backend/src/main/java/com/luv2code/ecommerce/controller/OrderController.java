package com.luv2code.ecommerce.controller;

import com.luv2code.ecommerce.dto.OrderSummaryDto;
import com.luv2code.ecommerce.dto.PageResponse;
import com.luv2code.ecommerce.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Orders of the logged-in customer. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/me")
    public PageResponse<OrderSummaryDto> myOrders(Principal principal,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(orderService.findForCustomer(principal.getName(),
                PageRequest.of(page, Math.min(size, 50))));
    }
}
