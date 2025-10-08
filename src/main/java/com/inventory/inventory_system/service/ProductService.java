package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    // Basic CRUD operations
    public List<Product> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            System.out.println("📦 Retrieved " + products.size() + " products from database");
            return products;
        } catch (Exception e) {
            System.err.println("⚠️ Could not retrieve products from database: " + e.getMessage());
            System.out.println("📝 Returning empty list - application will continue working");
            return Collections.emptyList();
        }
    }
    
    public Optional<Product> getProductById(Long id) {
        if (id == null) {
            System.out.println("Invalid product ID: null");
            return Optional.empty();
        }
        Optional<Product> product = productRepository.findById(id);
        System.out.println("Retrieved product by ID " + id + ": " + (product.isPresent() ? product.get().toString() : "Not found"));
        return product;
    }
    
    public Product saveProduct(Product product) {
        System.out.println("Saving product: " + product.toString());
        
        // Validate product
        if (!product.isValid()) {
            throw new RuntimeException("Invalid product data. Please check name, SKU, price, and quantity.");
        }
        
        // Ensure timestamps are set
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        
        // Ensure SKU is unique
        String sku = product.getSku();
        if (sku != null && !sku.trim().isEmpty()) {
            // Check if SKU already exists (excluding current product when updating)
            Product existingProduct = productRepository.findBySku(sku);
            if (existingProduct != null && !existingProduct.getId().equals(product.getId())) {
                throw new RuntimeException("SKU already exists: " + sku);
            }
        }
        
        Product savedProduct = productRepository.save(product);
        System.out.println("Saved product with ID: " + savedProduct.getId());
        System.out.println("Saved product details: " + savedProduct.toString());
        
        return savedProduct;
    }
    
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new RuntimeException("Invalid product ID");
        }
        System.out.println("Deleting product with ID: " + id);
        productRepository.deleteById(id);
        System.out.println("Product deleted successfully");
    }
    
    // Search functionality
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        List<Product> results = productRepository.findByNameContainingIgnoreCase(keyword);
        System.out.println("Search for '" + keyword + "' returned " + results.size() + " products");
        return results;
    }
    
    public List<Product> getProductsByCategory(String category) {
        List<Product> results = productRepository.findByCategory(category);
        System.out.println("Category '" + category + "' returned " + results.size() + " products");
        return results;
    }
    
    // Stock management
    public List<Product> getLowStockProducts() {
        List<Product> results = productRepository.findLowStockProducts(10);
        System.out.println("Low stock products: " + results.size());
        return results;
    }
    
    public List<Product> getOutOfStockProducts() {
        List<Product> results = productRepository.findByQuantity(0);
        System.out.println("Out of stock products: " + results.size());
        return results;
    }
    
    public List<Product> getInStockProducts() {
        List<Product> results = productRepository.findInStockProducts();
        System.out.println("In stock products: " + results.size());
        return results;
    }
    
    // Statistics
    public long getTotalProductsCount() {
        long count = productRepository.count();
        System.out.println("Total products count: " + count);
        return count;
    }
    
    public long getInStockProductsCount() {
        long count = productRepository.countByQuantityGreaterThan(0);
        System.out.println("In stock products count: " + count);
        return count;
    }
    
    public long getLowStockProductsCount() {
        List<Product> lowStock = productRepository.findLowStockProducts(10);
        long count = lowStock.size();
        System.out.println("Low stock products count: " + count);
        return count;
    }
    
    public long getOutOfStockProductsCount() {
        long count = productRepository.countByQuantity(0);
        System.out.println("Out of stock products count: " + count);
        return count;
    }
    
    // Category management
    public List<String> getAllCategories() {
        return Arrays.asList("Electronics", "Clothing", "Books", "Home & Garden", "Sports", 
                      "Beauty", "Toys", "Automotive", "Furniture", "Stationery", "Kitchen");
    }

    public List<Product> getProductsBySupplier(String supplierId) {
        List<Product> results = productRepository.findBySupplierId(supplierId);
        System.out.println("Products for supplier " + supplierId + ": " + results.size());
        return results;
    }
    
    // Price analysis
    public BigDecimal getTotalInventoryValue() {
        try {
            List<Product> products = getAllProducts();
            BigDecimal total = BigDecimal.ZERO;
            for (Product product : products) {
                if (product.getPrice() != null && product.getQuantity() != null) {
                    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())));
                }
            }
            System.out.println("Total inventory value: $" + total);
            return total;
        } catch (Exception e) {
            System.err.println("Error calculating total inventory value: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    // Generate unique SKU
    public String generateSku(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        
        String baseSku = productName.toUpperCase()
            .replaceAll("[^A-Z0-9]", "")
            .substring(0, Math.min(6, productName.length()));
        
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        String sku = baseSku + "-" + uniquePart;
        
        // Ensure SKU is unique
        int counter = 1;
        String finalSku = sku;
        while (productRepository.existsBySku(finalSku)) {
            finalSku = baseSku + "-" + uniquePart + "-" + counter;
            counter++;
        }
        
        return finalSku;
    }
}