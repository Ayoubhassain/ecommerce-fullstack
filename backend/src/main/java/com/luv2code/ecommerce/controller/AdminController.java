package com.luv2code.ecommerce.controller;

import com.luv2code.ecommerce.dto.*;
import com.luv2code.ecommerce.service.AdminProductService;
import com.luv2code.ecommerce.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/** Admin area. Every route here requires the ADMIN role (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminProductService productService;
    private final OrderService orderService;

    public AdminController(AdminProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    // ---------- products ----------

    @GetMapping("/products")
    public PageResponse<ProductDto> products(@RequestParam(required = false) String search,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(productService.list(search,
                PageRequest.of(page, Math.min(size, 100), Sort.by("id"))));
    }

    @GetMapping("/products/{id}")
    public ProductDto product(@PathVariable Long id) {
        return productService.get(id);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    // ---------- orders ----------

    @GetMapping("/orders")
    public PageResponse<OrderSummaryDto> orders(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(orderService.findAll(PageRequest.of(page, Math.min(size, 100))));
    }

    @PutMapping("/orders/{id}/status")
    public OrderSummaryDto updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return orderService.updateStatus(id, request.getStatus());
    }
}
