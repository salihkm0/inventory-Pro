package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.entity.Sale;
import com.inventory.inventory_system.service.PdfService;
import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/sales")
public class SaleController {
    
    @Autowired
    private SaleService saleService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private PdfService pdfService;

    // Add this to your existing SaleController class
    @PostMapping("/create-sample")
    @ResponseBody
    public String createSampleSalesData() {
        try {
            saleService.createSampleSalesData();
            return "Sample sales data created successfully";
        } catch (Exception e) {
            return "Error creating sample data: " + e.getMessage();
        }
    }
    
    @GetMapping
    public String listSales(Model model,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "startDate", required = false) String startDate,
                          @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            List<Sale> sales = saleService.getAllSales();
            
            // Handle null sales list
            if (sales == null) {
                sales = Collections.emptyList();
            }
            
            // Calculate statistics with null safety
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalSalesCount = 0;
            long totalItemsSold = 0;
            BigDecimal averageSale = BigDecimal.ZERO;
            
            if (!sales.isEmpty()) {
                totalRevenue = sales.stream()
                    .filter(sale -> sale != null && sale.getTotalAmount() != null)
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                totalSalesCount = sales.size();
                
                totalItemsSold = sales.stream()
                    .filter(sale -> sale != null && sale.getQuantity() != null)
                    .mapToLong(Sale::getQuantity)
                    .sum();
                
                averageSale = totalSalesCount > 0 ? 
                    totalRevenue.divide(BigDecimal.valueOf(totalSalesCount), 2, BigDecimal.ROUND_HALF_UP) : 
                    BigDecimal.ZERO;
            }
            
            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            // Add attributes to model
            model.addAttribute("sales", sales);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalSalesCount", totalSalesCount);
            model.addAttribute("totalItemsSold", totalItemsSold);
            model.addAttribute("averageSale", averageSale);
            model.addAttribute("searchTerm", search);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            model.addAttribute("title", "Sales History");
            
            return "sales/list";
        } catch (Exception e) {
            e.printStackTrace();
            // Provide safe fallback values
            model.addAttribute("sales", Collections.emptyList());
            model.addAttribute("totalRevenue", BigDecimal.ZERO);
            model.addAttribute("totalSalesCount", 0);
            model.addAttribute("totalItemsSold", 0);
            model.addAttribute("averageSale", BigDecimal.ZERO);
            model.addAttribute("error", "Error loading sales: " + e.getMessage());
            return "sales/list"; // Return to same page with error
        }
    }
    
    @GetMapping("/new")
    public String showSaleForm(Model model) {
        try {
            List<Product> products = productService.getInStockProducts();
            
            // Handle null products
            if (products == null) {
                products = Collections.emptyList();
            }
            
            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("sale", new Sale());
            model.addAttribute("products", products);
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            model.addAttribute("title", "New Sale");
            return "sales/form";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading sale form: " + e.getMessage());
            model.addAttribute("products", Collections.emptyList());
            return "sales/form";
        }
    }
    
    @PostMapping
    public String saveSale(@ModelAttribute Sale sale, 
                          @RequestParam Long productId,
                          Model model) {
        try {
            if (productId == null) {
                throw new RuntimeException("Product ID is required");
            }
            
            Optional<Product> product = productService.getProductById(productId);
            if (product.isPresent()) {
                Product productEntity = product.get();
                
                // Validate stock
                if (productEntity.getQuantity() == null || productEntity.getQuantity() < sale.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + productEntity.getName() + 
                                             ". Available: " + productEntity.getQuantity());
                }
                
                // Set product information in the sale
                sale.setProductId(productId);
                sale.setProductName(productEntity.getName());
                sale.setProductSku(productEntity.getSku());
                sale.setProductCategory(productEntity.getCategory());
                
                // Set unit price from product if not provided
                if (sale.getUnitPrice() == null) {
                    sale.setUnitPrice(productEntity.getPrice());
                }
                
                // Validate unit price
                if (sale.getUnitPrice() == null) {
                    throw new RuntimeException("Unit price is required");
                }
                
                // Validate quantity
                if (sale.getQuantity() == null || sale.getQuantity() <= 0) {
                    throw new RuntimeException("Quantity must be greater than 0");
                }
                
                // Calculate total amount
                sale.setTotalAmount(sale.getUnitPrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
                
                // Set timestamps
                if (sale.getSaleDate() == null) {
                    sale.setSaleDate(LocalDateTime.now());
                }
                if (sale.getCreatedAt() == null) {
                    sale.setCreatedAt(LocalDateTime.now());
                }
                
                saleService.saveSale(sale);
                return "redirect:/sales?success=Sale+recorded+successfully";
            } else {
                throw new RuntimeException("Product not found with ID: " + productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            List<Product> products = productService.getInStockProducts();
            
            // Handle null products
            if (products == null) {
                products = Collections.emptyList();
            }
            
            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("products", products);
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            return "sales/form";
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        try {
            saleService.deleteSale(id);
            return "redirect:/sales?success=Sale+deleted+successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/sales?error=" + e.getMessage();
        }
    }

    // PDF Generation Endpoint for Sales
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateSalePdf(@PathVariable Long id) {
        try {
            Sale sale = saleService.getAllSales().stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
            
            byte[] pdfBytes = pdfService.generateSalesPdf(sale);
            
            String filename = "sale-" + id + "-" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage());
        }
    }
}