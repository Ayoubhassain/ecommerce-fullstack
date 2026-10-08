package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.CustomerRepository;
import com.luv2code.ecommerce.dao.ProductRepository;
import com.luv2code.ecommerce.dto.Purchase;
import com.luv2code.ecommerce.dto.PurchaseResponse;
import com.luv2code.ecommerce.entity.Customer;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.entity.OrderItem;
import com.luv2code.ecommerce.entity.Product;
import com.luv2code.ecommerce.exception.NotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private CustomerRepository customerRepository;
    private ProductRepository productRepository;

    public CheckoutServiceImpl(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {

        // retrieve the order info from dto
        Order order = purchase.getOrder();

        // generate tracking number
        String orderTrackingNumber = generateOrderTrackingNumber();
        order.setOrderTrackingNumber(orderTrackingNumber);
        order.setStatus("NEW");

        // populate order with orderItems
        // prices are taken from the database, never trusted from the browser
        Set<OrderItem> orderItems = purchase.getOrderItems();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("The order has no items");
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (OrderItem item : orderItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product " + item.getProductId() + " not found"));
            item.setUnitPrice(product.getUnitPrice());
            item.setImageUrl(product.getImageUrl());
            totalPrice = totalPrice.add(product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQuantity += item.getQuantity();
            order.add(item);
        }
        order.setTotalPrice(totalPrice);
        order.setTotalQuantity(totalQuantity);

        // populate order with billingAddress and shippingAddress
        order.setBillingAddress(purchase.getBillingAddress());
        order.setShippingAddress(purchase.getShippingAddress());

        // reuse the existing customer with the same email instead of creating a duplicate
        Customer customer = purchase.getCustomer();
        Customer existing = customerRepository.findByEmail(customer.getEmail());
        if (existing != null) {
            customer = existing;
        }
        customer.add(order);

        // save to the database
        customerRepository.save(customer);

        // return a response
        return new PurchaseResponse(orderTrackingNumber);
    }

    private String generateOrderTrackingNumber() {

        // generate a random UUID number (UUID version-4)
        // For details see: https://en.wikipedia.org/wiki/Universally_unique_identifier
        //
        return UUID.randomUUID().toString();
    }
}
