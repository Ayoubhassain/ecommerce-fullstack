package com.luv2code.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OrderStatusRequest {

    @NotBlank
    private String status;
}
