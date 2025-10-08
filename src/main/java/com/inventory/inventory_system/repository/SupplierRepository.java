package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    List<Supplier> findByContactPersonContainingIgnoreCase(String contactPerson);
    
    List<Supplier> findByEmail(String email);
    
    List<Supplier> findByIsActive(Boolean isActive);
    
    long countByIsActive(Boolean isActive);
    
    List<Supplier> findByCountry(String country);
    
    Optional<Supplier> findBySupplierCode(String supplierCode);
}