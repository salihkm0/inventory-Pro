package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.service.SaleService;
import com.inventory.inventory_system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
            
            // Chart data - use the enhanced methods
            Map<String, BigDecimal> salesByCategory = saleService.getSalesByCategoryForChart();
            Map<String, BigDecimal> monthlySalesData = saleService.getMonthlySalesForChart(6);
            List<Map<String, Object>> topSellingProducts = saleService.getTopSellingProducts(5);
            
            // Debug output
            System.out.println("📈 Monthly Sales Data: " + monthlySalesData);
            System.out.println("📈 Monthly Sales Data Size: " + monthlySalesData.size());
            System.out.println("📊 Sales by Category: " + salesByCategory);
            System.out.println("📊 Sales by Category Size: " + salesByCategory.size());
            System.out.println("🔥 Top Selling Products: " + topSellingProducts.size());
            
            // Check if data is actually being populated
            if (monthlySalesData.isEmpty()) {
                System.out.println("⚠️ Monthly sales data is EMPTY!");
            }
            if (salesByCategory.isEmpty()) {
                System.out.println("⚠️ Sales by category data is EMPTY!");
            }
            
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
            model.addAttribute("monthlySales", monthlySales != null ? monthlySales : BigDecimal.ZERO);
            model.addAttribute("totalSuppliers", totalSuppliers);
            model.addAttribute("salesByCategory", salesByCategory);
            model.addAttribute("monthlySalesData", monthlySalesData);
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
    
    // Simple debug endpoint that doesn't use problematic queries
    @GetMapping("/debug/sales")
    public String debugSales(Model model) {
        try {
            List<Object[]> allSales = saleService.getAllSalesWithDetails();
            Map<String, BigDecimal> monthlyData = saleService.getMonthlySalesForChart(6);
            Map<String, BigDecimal> categoryData = saleService.getSalesByCategoryForChart();
            
            model.addAttribute("allSales", allSales);
            model.addAttribute("monthlyData", monthlyData);
            model.addAttribute("categoryData", categoryData);
            model.addAttribute("title", "Sales Debug");
            
            return "debug/sales";
        } catch (Exception e) {
            model.addAttribute("error", "Debug error: " + e.getMessage());
            return "error";
        }
    }
}