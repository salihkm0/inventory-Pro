package com.inventory.inventory_system.service;

import com.inventory.inventory_system.dto.SupplierDTO;
import com.inventory.inventory_system.entity.Supplier;
import com.inventory.inventory_system.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductService productService;
    
    // Basic CRUD operations
    public List<Supplier> getAllSuppliers() {
        try {
            return supplierRepository.findAll();
        } catch (Exception e) {
            System.err.println("⚠️ Could not retrieve suppliers from database: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Supplier saveSupplier(Supplier supplier) {
        if (supplier.getCreatedAt() == null) {
            supplier.setCreatedAt(java.time.LocalDateTime.now());
        }
        supplier.setUpdatedAt(java.time.LocalDateTime.now());
        
        // Ensure supplier code is unique if provided
        if (supplier.getSupplierCode() != null && !supplier.getSupplierCode().trim().isEmpty()) {
            Optional<Supplier> existingSupplier = supplierRepository.findBySupplierCode(supplier.getSupplierCode());
            if (existingSupplier.isPresent() && !existingSupplier.get().getId().equals(supplier.getId())) {
                throw new RuntimeException("Supplier code already exists: " + supplier.getSupplierCode());
            }
        }
        
        return supplierRepository.save(supplier);
    }
    
    public void deleteSupplier(Long id) {
        try {
            supplierRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting supplier: " + e.getMessage());
        }
    }
    
    // Search functionality
    public List<Supplier> searchSuppliers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSuppliers();
        }
        return supplierRepository.findByNameContainingIgnoreCase(keyword);
    }
    
    // DTO methods
    public List<SupplierDTO> getAllSupplierDTOs() {
        List<Supplier> suppliers = getAllSuppliers();
        return suppliers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SupplierDTO> searchSupplierDTOs(String keyword) {
        List<Supplier> suppliers = searchSuppliers(keyword);
        return suppliers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Statistics
    public long getTotalSuppliersCount() {
        return supplierRepository.count();
    }
    
    public long getActiveSuppliersCount() {
        return supplierRepository.countByIsActive(true);
    }
    
    // DTO conversion
    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setCity(supplier.getCity());
        dto.setCountry(supplier.getCountry());
        dto.setSupplierCode(supplier.getSupplierCode());
        dto.setIsActive(supplier.getIsActive());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        
        // Calculate product count
        List<com.inventory.inventory_system.entity.Product> supplierProducts = 
            productService.getProductsBySupplier(supplier.getId().toString());
        dto.setProductCount(supplierProducts != null ? supplierProducts.size() : 0);
        
        return dto;
    }
}