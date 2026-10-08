package com.luv2code.ecommerce.dao;

import com.luv2code.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerEmailIgnoreCaseOrderByDateCreatedDesc(String email, Pageable pageable);

    Page<Order> findAllByOrderByDateCreatedDesc(Pageable pageable);
}
