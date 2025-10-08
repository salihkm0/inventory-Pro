package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.Sale;
import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.service.SaleService;
import com.inventory.inventory_system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class DashboardController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SaleService saleService;
    
    @Autowired
    private SupplierService supplierService;
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        try {
            System.out.println("📊 Loading dashboard data...");
            
            // Basic statistics
            long totalProducts = productService.getTotalProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();
            BigDecimal todaySales = saleService.getTotalSalesToday();
            BigDecimal monthlySales = saleService.getTotalSalesThisMonth();
            long totalSuppliers = supplierService.getTotalSuppliersCount();
            
            // Get REAL chart data from database
            Map<String, BigDecimal> salesByCategory = getRealSalesByCategory();
            Map<String, BigDecimal> monthlySalesData = getRealMonthlySalesData();
            List<Map<String, Object>> topSellingProducts = saleService.getTopSellingProducts(5);
            
            // Debug: Print the actual data structure
            System.out.println("=== DEBUG: Monthly Sales Data Structure ===");
            monthlySalesData.forEach((key, value) -> {
                System.out.println("Key: '" + key + "', Value: " + value + ", Type: " + value.getClass().getSimpleName());
            });
            
            System.out.println("=== DEBUG: Category Sales Data Structure ===");
            salesByCategory.forEach((key, value) -> {
                System.out.println("Key: '" + key + "', Value: " + value + ", Type: " + value.getClass().getSimpleName());
            });
            
            // Convert BigDecimal to Double for JavaScript compatibility
            Map<String, Double> monthlySalesDouble = new LinkedHashMap<>();
            monthlySalesData.forEach((key, value) -> {
                monthlySalesDouble.put(key, value != null ? value.doubleValue() : 0.0);
            });
            
            Map<String, Double> categorySalesDouble = new HashMap<>();
            salesByCategory.forEach((key, value) -> {
                categorySalesDouble.put(key, value != null ? value.doubleValue() : 0.0);
            });
            
            // Add data to model
            model.addAttribute("totalSalesCount", saleService.getAllSales().size());
            model.addAttribute("hasMonthlyData", !monthlySalesData.isEmpty());
            model.addAttribute("hasCategoryData", !salesByCategory.isEmpty());
            
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
            model.addAttribute("monthlySales", monthlySales != null ? monthlySales : BigDecimal.ZERO);
            model.addAttribute("totalSuppliers", totalSuppliers);
            
            // Add the converted data
            model.addAttribute("salesByCategory", categorySalesDouble);
            model.addAttribute("monthlySalesData", monthlySalesDouble);
            model.addAttribute("topSellingProducts", topSellingProducts);
            model.addAttribute("title", "Dashboard");
            
            System.out.println("✅ Dashboard data loaded successfully");
            
            return "dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }
    
    // Get REAL sales by category from database
    private Map<String, BigDecimal> getRealSalesByCategory() {
        try {
            // Get all sales from database
            List<Sale> allSales = saleService.getAllSales();
            System.out.println("📊 Processing " + allSales.size() + " sales for category analysis");
            
            Map<String, BigDecimal> salesByCategory = new HashMap<>();
            
            for (Sale sale : allSales) {
                String category = sale.getProductCategory();
                BigDecimal amount = sale.getTotalAmount();
                
                if (amount != null) {
                    // Handle null or empty categories
                    String categoryKey = (category == null || category.trim().isEmpty()) ? "Uncategorized" : category;
                    
                    // Add to category total
                    salesByCategory.merge(categoryKey, amount, BigDecimal::add);
                }
            }
            
            // If no sales data, create sample structure from products
            if (salesByCategory.isEmpty()) {
                System.out.println("🔄 No sales data found, creating category structure from products...");
                var products = productService.getAllProducts();
                for (var product : products) {
                    String category = product.getCategory();
                    String categoryKey = (category == null || category.trim().isEmpty()) ? "Uncategorized" : category;
                    salesByCategory.putIfAbsent(categoryKey, BigDecimal.ZERO);
                }
            }
            
            System.out.println("✅ Final category sales data: " + salesByCategory);
            return salesByCategory;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting real sales by category: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    // Get REAL monthly sales data from database
    private Map<String, BigDecimal> getRealMonthlySalesData() {
        try {
            // Get all sales from database
            List<Sale> allSales = saleService.getAllSales();
            System.out.println("📈 Processing " + allSales.size() + " sales for monthly analysis");
            
            Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();
            
            // Initialize last 6 months with zeros
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                String monthKey = monthDate.format(formatter);
                monthlySales.put(monthKey, BigDecimal.ZERO);
            }
            
            System.out.println("📅 Initialized months: " + monthlySales.keySet());
            
            // Fill with actual data
            int salesWithDates = 0;
            for (Sale sale : allSales) {
                LocalDateTime saleDate = sale.getSaleDate();
                BigDecimal saleAmount = sale.getTotalAmount();
                
                if (saleDate != null && saleAmount != null) {
                    String monthKey = saleDate.format(formatter);
                    
                    if (monthlySales.containsKey(monthKey)) {
                        BigDecimal currentTotal = monthlySales.get(monthKey);
                        monthlySales.put(monthKey, currentTotal.add(saleAmount));
                        salesWithDates++;
                    }
                }
            }
            
            System.out.println("📊 Processed " + salesWithDates + " sales with valid dates");
            System.out.println("✅ Final monthly sales data: " + monthlySales);
            
            return monthlySales;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting real monthly sales data: " + e.getMessage());
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }
    
    // Debug endpoint to check sales data
    @GetMapping("/debug/sales")
    public String debugSales(Model model) {
        try {
            List<Sale> allSales = saleService.getAllSales();
            Map<String, BigDecimal> monthlyData = getRealMonthlySalesData();
            Map<String, BigDecimal> categoryData = getRealSalesByCategory();
            
            // Prepare detailed sales info for debugging
            List<Map<String, Object>> salesDetails = new ArrayList<>();
            for (Sale sale : allSales) {
                Map<String, Object> saleInfo = new HashMap<>();
                saleInfo.put("id", sale.getId());
                saleInfo.put("productName", sale.getProductName());
                saleInfo.put("category", sale.getProductCategory());
                saleInfo.put("amount", sale.getTotalAmount());
                saleInfo.put("date", sale.getSaleDate());
                salesDetails.add(saleInfo);
            }
            
            model.addAttribute("allSales", salesDetails);
            model.addAttribute("monthlyData", monthlyData);
            model.addAttribute("categoryData", categoryData);
            model.addAttribute("totalSales", allSales.size());
            model.addAttribute("title", "Sales Debug");
            
            return "debug/sales";
        } catch (Exception e) {
            model.addAttribute("error", "Debug error: " + e.getMessage());
            return "error";
        }
    }
    
    // API endpoint for chart data
    @GetMapping("/api/dashboard/charts")
    @ResponseBody
    public Map<String, Object> getChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            Map<String, BigDecimal> monthlySalesData = getRealMonthlySalesData();
            Map<String, BigDecimal> salesByCategory = getRealSalesByCategory();
            
            // Convert to Double for JSON
            Map<String, Double> monthlyDouble = new LinkedHashMap<>();
            monthlySalesData.forEach((k, v) -> monthlyDouble.put(k, v.doubleValue()));
            
            Map<String, Double> categoryDouble = new HashMap<>();
            salesByCategory.forEach((k, v) -> categoryDouble.put(k, v.doubleValue()));
            
            chartData.put("monthlySales", monthlyDouble);
            chartData.put("salesByCategory", categoryDouble);
            chartData.put("success", true);
            
        } catch (Exception e) {
            chartData.put("success", false);
            chartData.put("error", e.getMessage());
        }
        
        return chartData;
    }
    
    // REMOVE THIS DUPLICATE METHOD - Keep it only in SaleController
    // @PostMapping("/sales/create-sample")
    // @ResponseBody
    // public String createSampleSalesData(HttpServletRequest request) {
    //     try {
    //         saleService.createSampleSalesData();
    //         return "Sample sales data created successfully";
    //     } catch (Exception e) {
    //         return "Error creating sample data: " + e.getMessage();
    //     }
    // }
}