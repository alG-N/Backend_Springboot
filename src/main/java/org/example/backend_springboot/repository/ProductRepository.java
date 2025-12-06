package org.example.backend_springboot.repository;

import org.example.backend_springboot.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByAvailableTrue();
    List<Product> findByCategoryId(Integer categoryId);
}
