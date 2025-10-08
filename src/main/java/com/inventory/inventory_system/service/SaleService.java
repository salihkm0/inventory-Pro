package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.entity.Sale;
import com.inventory.inventory_system.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private ProductService productService;
    
    public List<Sale> getAllSales() {
        try {
            List<Sale> sales = saleRepository.findAll();
            System.out.println("📈 Retrieved " + sales.size() + " sales from database");
            return sales;
        } catch (Exception e) {
            System.err.println("⚠️ Could not retrieve sales from database: " + e.getMessage());
            System.out.println("📝 Returning empty list - application will continue working");
            return Collections.emptyList();
        }
    }
    
    // New method for debug purposes
    public List<Object[]> getAllSalesWithDetails() {
        List<Sale> sales = getAllSales();
        List<Object[]> result = new ArrayList<>();
        
        for (Sale sale : sales) {
            Object[] saleDetails = {
                sale.getId(),
                sale.getProductName(),
                sale.getProductCategory(),
                sale.getTotalAmount(),
                sale.getSaleDate(),
                sale.getQuantity()
            };
            result.add(saleDetails);
        }
        return result;
    }
    
    public Sale saveSale(Sale sale) {
        // Update product stock and get product details
        Optional<Product> productOpt = productService.getProductById(sale.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            // Check stock
            if (product.getQuantity() < sale.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Update product quantity
            product.setQuantity(product.getQuantity() - sale.getQuantity());
            productService.saveProduct(product);
            
            // Set product details in sale for quick access
            sale.setProductName(product.getName());
            sale.setProductSku(product.getSku());
            sale.setProductCategory(product.getCategory());
            
            // Set timestamps if not set
            if (sale.getSaleDate() == null) {
                sale.setSaleDate(LocalDateTime.now());
            }
            if (sale.getCreatedAt() == null) {
                sale.setCreatedAt(LocalDateTime.now());
            }
            
            // Calculate total amount if not set
            if (sale.getTotalAmount() == null && sale.getUnitPrice() != null && sale.getQuantity() != null) {
                sale.setTotalAmount(sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
            }
        } else {
            throw new RuntimeException("Product not found with id: " + sale.getProductId());
        }
        
        Sale savedSale = saleRepository.save(sale);
        System.out.println("💾 Saved sale: " + savedSale.getId() + " for product: " + savedSale.getProductName());
        return savedSale;
    }
    
    public void deleteSale(Long id) {
        try {
            saleRepository.deleteById(id);
            System.out.println("🗑️ Deleted sale: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting sale: " + e.getMessage());
        }
    }
    
    public BigDecimal getTotalSalesToday() {
        try {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
            List<Sale> todaySales = saleRepository.findBySaleDateBetween(startOfDay, endOfDay);
            
            BigDecimal total = BigDecimal.ZERO;
            for (Sale sale : todaySales) {
                if (sale.getTotalAmount() != null) {
                    total = total.add(sale.getTotalAmount());
                }
            }
            System.out.println("💰 Today's sales total: $" + total);
            return total;
        } catch (Exception e) {
            System.err.println("Error calculating today's sales: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getTotalSalesThisMonth() {
        try {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            List<Sale> monthSales = saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth);
            
            BigDecimal total = BigDecimal.ZERO;
            for (Sale sale : monthSales) {
                if (sale.getTotalAmount() != null) {
                    total = total.add(sale.getTotalAmount());
                }
            }
            System.out.println("💰 Monthly sales total: $" + total);
            return total;
        } catch (Exception e) {
            System.err.println("Error calculating monthly sales: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    public Map<String, BigDecimal> getSalesByCategory() {
        Map<String, BigDecimal> salesByCategory = new HashMap<>();
        
        try {
            List<Sale> allSales = getAllSales();
            System.out.println("📊 Processing " + allSales.size() + " sales for category analysis");
            
            for (Sale sale : allSales) {
                String category = sale.getProductCategory();
                BigDecimal amount = sale.getTotalAmount();
                
                if (amount != null && category != null) {
                    salesByCategory.merge(category, amount, BigDecimal::add);
                    System.out.println("📦 Category: " + category + ", Amount: $" + amount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating sales by category: " + e.getMessage());
        }
        
        return salesByCategory;
    }
    
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> topProducts = new ArrayList<>();
        
        try {
            List<Sale> allSales = getAllSales();
            Map<Long, Map<String, Object>> productSales = new HashMap<>();
            
            for (Sale sale : allSales) {
                Long productId = sale.getProductId();
                String productName = sale.getProductName();
                Long quantity = sale.getQuantity().longValue();
                BigDecimal price = sale.getUnitPrice();
                
                if (productSales.containsKey(productId)) {
                    Map<String, Object> existing = productSales.get(productId);
                    Long currentQuantity = (Long) existing.get("totalSold");
                    existing.put("totalSold", currentQuantity + quantity);
                    
                    // Update revenue
                    BigDecimal currentRevenue = (BigDecimal) existing.get("revenue");
                    BigDecimal saleRevenue = price.multiply(BigDecimal.valueOf(quantity));
                    existing.put("revenue", currentRevenue.add(saleRevenue));
                } else {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("productId", productId);
                    productData.put("productName", productName);
                    productData.put("totalSold", quantity);
                    productData.put("price", price);
                    productData.put("revenue", price.multiply(BigDecimal.valueOf(quantity)));
                    productSales.put(productId, productData);
                }
            }
            
            productSales.values().stream()
                .sorted((a, b) -> Long.compare((Long) b.get("totalSold"), (Long) a.get("totalSold")))
                .limit(limit)
                .forEach(topProducts::add);
                
            System.out.println("🔥 Top selling products: " + topProducts.size());
                
        } catch (Exception e) {
            System.err.println("Error calculating top selling products: " + e.getMessage());
        }
        
        return topProducts;
    }
    
    public Map<String, BigDecimal> getMonthlySalesForChart(int months) {
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();
        
        try {
            // Get current date and go backwards
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            
            // Initialize with last 'months' months
            for (int i = months - 1; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                String monthKey = monthDate.format(formatter);
                monthlySales.put(monthKey, BigDecimal.ZERO);
            }
            
            // Fill with actual data
            List<Sale> allSales = getAllSales();
            System.out.println("📈 Total sales found for monthly chart: " + allSales.size());
            
            for (Sale sale : allSales) {
                LocalDateTime saleDate = sale.getSaleDate();
                if (saleDate != null) {
                    String monthKey = saleDate.format(formatter);
                    
                    if (monthlySales.containsKey(monthKey)) {
                        BigDecimal currentTotal = monthlySales.get(monthKey);
                        BigDecimal saleAmount = sale.getTotalAmount();
                        if (saleAmount != null) {
                            monthlySales.put(monthKey, currentTotal.add(saleAmount));
                            System.out.println("➕ Added sale: $" + saleAmount + " to " + monthKey);
                        }
                    }
                }
            }
            
            System.out.println("📈 Final monthly sales data: " + monthlySales);
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating monthly sales for chart: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Generate sample data for demonstration
            return generateSampleMonthlyData(months);
        }
        
        return monthlySales;
    }

    public Map<String, BigDecimal> getSalesByCategoryForChart() {
        Map<String, BigDecimal> salesByCategory = new HashMap<>();
        
        try {
            List<Sale> allSales = getAllSales();
            System.out.println("📊 Processing " + allSales.size() + " sales for category chart");
            
            for (Sale sale : allSales) {
                String category = sale.getProductCategory();
                BigDecimal amount = sale.getTotalAmount();
                
                if (amount != null && category != null && !category.trim().isEmpty()) {
                    salesByCategory.merge(category, amount, BigDecimal::add);
                    System.out.println("📦 Category: " + category + ", Amount: $" + amount);
                } else {
                    System.out.println("⏭️ Skipping sale - missing category or amount: " + sale);
                }
            }
            
            // If no sales data or all categories are null, provide sample categories
            if (salesByCategory.isEmpty()) {
                System.out.println("📝 No sales data found - using sample categories");
                return generateSampleCategoryData();
            }
            
            System.out.println("📊 Final category sales data: " + salesByCategory);
            
        } catch (Exception e) {
            System.err.println("❌ Error in getSalesByCategoryForChart: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback data in case of error
            return generateSampleCategoryData();
        }
        
        return salesByCategory;
    }
    
    // Helper method to generate sample monthly data
    private Map<String, BigDecimal> generateSampleMonthlyData(int months) {
        Map<String, BigDecimal> sampleData = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        Random random = new Random();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthKey = monthDate.format(formatter);
            // Generate random sales between $500 and $3000
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 2500 + 500).setScale(2, BigDecimal.ROUND_HALF_UP);
            sampleData.put(monthKey, amount);
        }
        
        System.out.println("🎯 Generated sample monthly data: " + sampleData);
        return sampleData;
    }
    
    // Helper method to generate sample category data
    private Map<String, BigDecimal> generateSampleCategoryData() {
        Map<String, BigDecimal> sampleData = new HashMap<>();
        Random random = new Random();
        
        sampleData.put("Electronics", BigDecimal.valueOf(random.nextDouble() * 2000 + 1000).setScale(2, BigDecimal.ROUND_HALF_UP));
        sampleData.put("Furniture", BigDecimal.valueOf(random.nextDouble() * 1500 + 500).setScale(2, BigDecimal.ROUND_HALF_UP));
        sampleData.put("Stationery", BigDecimal.valueOf(random.nextDouble() * 800 + 200).setScale(2, BigDecimal.ROUND_HALF_UP));
        sampleData.put("Other", BigDecimal.valueOf(random.nextDouble() * 1000 + 300).setScale(2, BigDecimal.ROUND_HALF_UP));
        
        System.out.println("🎯 Generated sample category data: " + sampleData);
        return sampleData;
    }
    
    // Method to create sample sales data for testing
    public void createSampleSalesData() {
        try {
            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                System.out.println("⚠️ No products available to create sample sales");
                return;
            }
            
            System.out.println("🎯 Creating sample sales data...");
            
            // Create sales for the last 6 months
            LocalDateTime now = LocalDateTime.now();
            Random random = new Random();
            
            for (int i = 0; i < 20; i++) {
                Product product = products.get(random.nextInt(products.size()));
                Sale sale = new Sale();
                
                sale.setProductId(product.getId());
                sale.setProductName(product.getName());
                sale.setProductCategory(product.getCategory());
                sale.setProductSku(product.getSku());
                sale.setQuantity(random.nextInt(5) + 1); // 1-5 quantity
                sale.setUnitPrice(product.getPrice());
                sale.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
                
                // Random date in the last 6 months
                int monthsAgo = random.nextInt(6);
                int daysAgo = random.nextInt(30);
                sale.setSaleDate(now.minusMonths(monthsAgo).minusDays(daysAgo));
                sale.setCreatedAt(LocalDateTime.now());
                
                saleRepository.save(sale);
                System.out.println("✅ Created sample sale: " + product.getName() + " - $" + sale.getTotalAmount());
            }
            
            System.out.println("🎉 Sample sales data created successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error creating sample sales data: " + e.getMessage());
        }
    }
    
    // Alternative method using native SQL query (if you need database-level aggregation)
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    
    public Map<String, BigDecimal> getMonthlySalesNative(int months) {
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();
        
        try {
            String sql = "SELECT DATE_FORMAT(sale_date, '%b %Y') as month, SUM(total_amount) as total " +
                        "FROM sale " +
                        "WHERE sale_date >= DATE_SUB(NOW(), INTERVAL ? MONTH) " +
                        "GROUP BY DATE_FORMAT(sale_date, '%b %Y') " +
                        "ORDER BY MIN(sale_date)";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, months);
            
            for (Map<String, Object> row : results) {
                String month = (String) row.get("month");
                BigDecimal total = (BigDecimal) row.get("total");
                monthlySales.put(month, total != null ? total : BigDecimal.ZERO);
            }
            
        } catch (Exception e) {
            System.err.println("Error in native monthly sales query: " + e.getMessage());
        }
        
        return monthlySales;
    }
}