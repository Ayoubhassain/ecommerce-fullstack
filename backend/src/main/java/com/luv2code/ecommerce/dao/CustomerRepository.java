package com.luv2code.ecommerce.dao;

import com.luv2code.ecommerce.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// exported = false: customers and their orders are only reachable through secured controllers
@RepositoryRestResource(exported = false)
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findByEmail(String email);
}
