package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // MAIN PRODUCTS LIST PAGE
    @GetMapping
    public String listProducts(Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category) {

        try {
            List<Product> products;
            String searchTerm = (search != null) ? search.trim() : "";
            String selectedCategory = (category != null) ? category.trim() : "";

            if (!searchTerm.isEmpty()) {
                products = productService.searchProducts(searchTerm);
                model.addAttribute("searchTerm", searchTerm);
            } else if (!selectedCategory.isEmpty()) {
                products = productService.getProductsByCategory(selectedCategory);
                model.addAttribute("selectedCategory", selectedCategory);
            } else {
                products = productService.getAllProducts();
            }

            // Handle null products
            if (products == null) {
                products = Collections.emptyList();
            }

            // Statistics
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();
            List<String> categories = productService.getAllCategories();

            model.addAttribute("products", products);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue",
                    totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("categories", categories != null ? categories : List.of());
            model.addAttribute("title", "All Products");

            return "products/list";
        } catch (Exception e) {
            e.printStackTrace();
            // Provide safe fallback values
            model.addAttribute("products", Collections.emptyList());
            model.addAttribute("totalProducts", 0);
            model.addAttribute("inStockProducts", 0);
            model.addAttribute("lowStockProducts", 0);
            model.addAttribute("outOfStockProducts", 0);
            model.addAttribute("totalInventoryValue", BigDecimal.ZERO);
            model.addAttribute("categories", List.of());
            model.addAttribute("error", "Error loading products: " + e.getMessage());
            model.addAttribute("title", "All Products");
            return "products/list";
        }
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        try {
            Product product = new Product();
            // Generate a default SKU
            product.setSku(productService.generateSku(""));

            List<String> categories = productService.getAllCategories();
            model.addAttribute("product", product);
            model.addAttribute("categories", categories);
            model.addAttribute("title", "Add Product");
            return "products/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String saveProduct(@ModelAttribute Product product, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Generate SKU if empty
            if (product.getSku() == null || product.getSku().trim().isEmpty()) {
                product.setSku(productService.generateSku(product.getName()));
            }

            Product savedProduct = productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product saved successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", "Error saving product: " + e.getMessage());
            List<String> categories = productService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("product", product);
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            List<String> categories = productService.getAllCategories();

            model.addAttribute("product", product);
            model.addAttribute("categories", categories);
            model.addAttribute("title", "Edit Product");
            return "products/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading product: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/low-stock")
    public String showLowStock(Model model) {
        try {
            List<Product> lowStockProducts = productService.getLowStockProducts();

            if (lowStockProducts == null) {
                lowStockProducts = Collections.emptyList();
            }

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProductsCount = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("products", lowStockProducts);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProductsCount);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue",
                    totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("title", "Low Stock Alert");
            return "products/low-stock";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("products", Collections.emptyList());
            model.addAttribute("error", "Error loading low stock products: " + e.getMessage());
            return "products/low-stock";
        }
    }

    @GetMapping("/out-of-stock")
    public String showOutOfStock(Model model) {
        try {
            List<Product> outOfStockProducts = productService.getOutOfStockProducts();

            if (outOfStockProducts == null) {
                outOfStockProducts = Collections.emptyList();
            }

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProductsCount = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("products", outOfStockProducts);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProductsCount);
            model.addAttribute("totalInventoryValue",
                    totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("title", "Out of Stock");
            return "products/out-of-stock";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("products", Collections.emptyList());
            model.addAttribute("error", "Error loading out of stock products: " + e.getMessage());
            return "products/out-of-stock";
        }
    }

    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("product", product);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue",
                    totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            model.addAttribute("title", "Product Details - " + product.getName());

            return "products/view";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading product: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/reports")
public String showReports(Model model) {
    try {
        System.out.println("📊 Loading reports page...");

        // Get all products with error handling
        List<Product> products;
        try {
            products = productService.getAllProducts();
        } catch (Exception e) {
            System.err.println("⚠️ Could not load products for reports: " + e.getMessage());
            products = Collections.emptyList();
        }

        // Handle null products
        if (products == null) {
            products = Collections.emptyList();
        }

        // Calculate statistics with null safety
        long totalProductsCount = products.size();
        long inStockCount = products.stream()
                .filter(p -> p != null && p.getQuantity() != null && p.getQuantity() > 0)
                .count();
        long lowStockCount = products.stream()
                .filter(p -> p != null && p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() <= 5)
                .count();
        long outOfStockCount = products.stream()
                .filter(p -> p != null && (p.getQuantity() == null || p.getQuantity() == 0))
                .count();

        // Calculate total inventory value with null safety
        double totalInventoryValueDouble = products.stream()
                .filter(p -> p != null && p.getPrice() != null && p.getQuantity() != null)
                .mapToDouble(p -> p.getPrice().doubleValue() * p.getQuantity())
                .sum();

        // Get Top Products by Inventory Value (sorted by price * quantity)
        List<Product> topProductsByValue = products.stream()
                .filter(p -> p != null && p.getPrice() != null && p.getQuantity() != null)
                .sorted((p1, p2) -> {
                    BigDecimal value1 = p1.getPrice().multiply(BigDecimal.valueOf(p1.getQuantity()));
                    BigDecimal value2 = p2.getPrice().multiply(BigDecimal.valueOf(p2.getQuantity()));
                    return value2.compareTo(value1); // Descending order
                })
                .limit(10) // Top 10 products
                .collect(Collectors.toList());

        // Get Low Stock Products (quantity <= 5 and > 0)
        List<Product> lowStockProductsList = products.stream()
                .filter(p -> p != null && p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() <= 5)
                .sorted((p1, p2) -> p1.getQuantity().compareTo(p2.getQuantity())) // Sort by quantity ascending
                .collect(Collectors.toList());

        // Add attributes to model
        model.addAttribute("totalProductsReport", totalProductsCount);
        model.addAttribute("inStockCount", inStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("totalInventoryValueDouble", totalInventoryValueDouble);
        model.addAttribute("topProductsByValue", topProductsByValue);
        model.addAttribute("lowStockProductsList", lowStockProductsList);

        // Sidebar statistics
        model.addAttribute("totalProducts", totalProductsCount);
        model.addAttribute("inStockProducts", inStockCount);
        model.addAttribute("lowStockProducts", lowStockCount);
        model.addAttribute("outOfStockProducts", outOfStockCount);
        model.addAttribute("totalInventoryValue", BigDecimal.valueOf(totalInventoryValueDouble));

        model.addAttribute("title", "Reports & Analytics");

        System.out.println("✅ Reports data loaded successfully");
        System.out.println("📈 Top Products: " + topProductsByValue.size());
        System.out.println("⚠️ Low Stock Items: " + lowStockProductsList.size());

        return "products/reports";

    } catch (Exception e) {
        System.err.println("❌ Error in reports: " + e.getMessage());

        // Provide safe fallback values
        model.addAttribute("totalProductsReport", 0);
        model.addAttribute("inStockCount", 0);
        model.addAttribute("lowStockCount", 0);
        model.addAttribute("outOfStockCount", 0);
        model.addAttribute("totalInventoryValueDouble", 0.0);
        model.addAttribute("topProductsByValue", Collections.emptyList());
        model.addAttribute("lowStockProductsList", Collections.emptyList());

        model.addAttribute("error", "Reports are temporarily unavailable. Please try again later.");
        model.addAttribute("title", "Reports & Analytics");

        return "products/reports";
    }
}
}