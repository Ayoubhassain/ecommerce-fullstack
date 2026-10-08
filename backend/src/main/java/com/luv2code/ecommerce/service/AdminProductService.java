package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.ProductCategoryRepository;
import com.luv2code.ecommerce.dao.ProductRepository;
import com.luv2code.ecommerce.dto.ProductDto;
import com.luv2code.ecommerce.dto.ProductRequest;
import com.luv2code.ecommerce.entity.Product;
import com.luv2code.ecommerce.entity.ProductCategory;
import com.luv2code.ecommerce.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public AdminProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> list(String search, Pageable pageable) {
        Page<Product> page = (search == null || search.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContaining(search.trim(), pageable);
        return page.map(ProductDto::from);
    }

    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        return ProductDto.from(find(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest request) {
        Product product = find(id);
        apply(product, request);
        return ProductDto.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(find(id));
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    private void apply(Product product, ProductRequest request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category " + request.getCategoryId() + " not found"));
        product.setSku(request.getSku().trim());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());
        product.setUnitsInStock(request.getUnitsInStock());
        product.setCategory(category);
    }
}
