package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByQuantityLessThan(Integer quantity);
    
    List<Product> findByQuantity(Integer quantity);
    
    List<Product> findByQuantityGreaterThan(Integer quantity);
    
    List<Product> findBySupplierId(String supplierId);
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findInStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.quantity <= ?1")
    List<Product> findLowStockProducts(Integer reorderLevel);
    
    long countByQuantityGreaterThan(Integer quantity);
    
    long countByQuantity(Integer quantity);
    
    Product findBySku(String sku);
    
    boolean existsBySku(String sku);
}