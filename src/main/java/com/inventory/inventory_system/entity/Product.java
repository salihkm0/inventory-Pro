package com.inventory.inventory_system.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    private String category;
    
    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;
    
    @Column(name = "supplier_id")
    private String supplierId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Product(String name, String description, String sku, BigDecimal price, Integer quantity, String category) {
        this();
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { 
        this.sku = sku; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { 
        this.reorderLevel = reorderLevel; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { 
        this.supplierId = supplierId; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isLowStock() {
        return quantity != null && quantity > 0 && quantity <= (reorderLevel != null ? reorderLevel : 10);
    }
    
    public boolean isOutOfStock() {
        return quantity == null || quantity == 0;
    }
    
    public boolean isInStock() {
        return quantity != null && quantity > (reorderLevel != null ? reorderLevel : 10);
    }
    
    public String getStockStatus() {
        if (quantity == null) {
            return "OUT_OF_STOCK";
        }
        if (isOutOfStock()) {
            return "OUT_OF_STOCK";
        } else if (isLowStock()) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }
    
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               sku != null && !sku.trim().isEmpty() &&
               price != null && price.compareTo(BigDecimal.ZERO) >= 0 &&
               quantity != null && quantity >= 0;
    }
    
    // Override toString for debugging
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", category='" + category + '\'' +
                ", reorderLevel=" + reorderLevel +
                ", supplierId='" + supplierId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}