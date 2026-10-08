package com.luv2code.ecommerce.dto;

import com.luv2code.ecommerce.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private String imageUrl;
    private boolean active;
    private int unitsInStock;
    private Long categoryId;
    private String categoryName;

    public static ProductDto from(Product p) {
        return new ProductDto(p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getUnitPrice(), p.getImageUrl(), p.isActive(), p.getUnitsInStock(),
                p.getCategory().getId(), p.getCategory().getCategoryName());
    }
}
