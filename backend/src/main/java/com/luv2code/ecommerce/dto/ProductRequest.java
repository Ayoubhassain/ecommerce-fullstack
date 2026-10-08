package com.luv2code.ecommerce.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 255)
    private String sku;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    @Size(max = 255)
    private String imageUrl;

    private boolean active = true;

    @Min(0)
    private int unitsInStock;

    @NotNull
    private Long categoryId;
}
