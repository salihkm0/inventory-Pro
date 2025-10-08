package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    // Find sales between dates
    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find sales by product ID
    List<Sale> findByProductId(Long productId);
    
    // Find sales by category
    List<Sale> findByProductCategory(String category);
    
    // Custom query for low quantity sales
    @Query("SELECT s FROM Sale s WHERE s.quantity < :threshold")
    List<Sale> findLowQuantitySales(@Param("threshold") Integer threshold);
    
    // Remove the problematic queries that use DATE_FORMAT
    // @Query("SELECT s.productCategory, SUM(s.totalAmount) FROM Sale s GROUP BY s.productCategory")
    // List<Object[]> getSalesAmountByCategory();
    
    // @Query("SELECT FUNCTION('DATE_FORMAT', s.saleDate, '%b %Y'), SUM(s.totalAmount) FROM Sale s GROUP BY FUNCTION('DATE_FORMAT', s.saleDate, '%b %Y') ORDER BY MIN(s.saleDate)")
    // List<Object[]> getMonthlySalesSummary();
}