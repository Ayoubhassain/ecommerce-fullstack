package com.luv2code.ecommerce.dto;

import com.luv2code.ecommerce.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
public class OrderSummaryDto {

    private Long id;
    private String orderTrackingNumber;
    private String customerName;
    private String customerEmail;
    private int totalQuantity;
    private BigDecimal totalPrice;
    private String status;
    private Date dateCreated;

    public static OrderSummaryDto from(Order o) {
        String name = o.getCustomer() == null ? null
                : o.getCustomer().getFirstName() + " " + o.getCustomer().getLastName();
        String email = o.getCustomer() == null ? null : o.getCustomer().getEmail();
        return new OrderSummaryDto(o.getId(), o.getOrderTrackingNumber(), name, email,
                o.getTotalQuantity(), o.getTotalPrice(), o.getStatus(), o.getDateCreated());
    }
}
