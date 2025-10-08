package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.dto.SupplierDTO;
import com.inventory.inventory_system.entity.Supplier;
import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String listSuppliers(Model model,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "country", required = false) String country) {
        try {
            List<SupplierDTO> supplierDTOs;
            String searchTerm = (search != null) ? search.trim() : "";
            String selectedStatus = (status != null) ? status.trim() : "";
            String selectedCountry = (country != null) ? country.trim() : "";
            
            // Get all suppliers as DTOs first
            if (!searchTerm.isEmpty()) {
                supplierDTOs = supplierService.searchSupplierDTOs(searchTerm);
                model.addAttribute("searchTerm", searchTerm);
            } else {
                supplierDTOs = supplierService.getAllSupplierDTOs();
            }
            
            // Apply filters
            if (!selectedStatus.isEmpty()) {
                if ("active".equals(selectedStatus)) {
                    supplierDTOs = supplierDTOs.stream()
                        .filter(s -> s.getIsActive() != null && s.getIsActive())
                        .collect(Collectors.toList());
                } else if ("inactive".equals(selectedStatus)) {
                    supplierDTOs = supplierDTOs.stream()
                        .filter(s -> s.getIsActive() != null && !s.getIsActive())
                        .collect(Collectors.toList());
                }
            }
            
            if (!selectedCountry.isEmpty()) {
                supplierDTOs = supplierDTOs.stream()
                    .filter(s -> s.getCountry() != null && s.getCountry().equalsIgnoreCase(selectedCountry))
                    .collect(Collectors.toList());
            }
            
            // Calculate statistics
            long totalSuppliers = supplierService.getTotalSuppliersCount();
            long activeSuppliers = supplierService.getActiveSuppliersCount();
            
            // Get unique countries for filter dropdown from original entities
            List<String> countries = supplierService.getAllSuppliers().stream()
                .filter(s -> s.getCountry() != null && !s.getCountry().trim().isEmpty())
                .map(Supplier::getCountry)
                .distinct()
                .limit(20)
                .collect(Collectors.toList());

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();
            
            // Add all required attributes to model
            model.addAttribute("suppliers", supplierDTOs);
            model.addAttribute("totalSuppliers", totalSuppliers);
            model.addAttribute("activeSuppliers", activeSuppliers);
            model.addAttribute("pendingOrders", 0);
            model.addAttribute("topRatedSuppliers", (int) activeSuppliers);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("selectedStatus", selectedStatus);
            model.addAttribute("selectedCountry", selectedCountry);
            model.addAttribute("countries", countries);
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            model.addAttribute("title", "Suppliers");
            
            return "suppliers/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading suppliers: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/new")
    public String showSupplierForm(Model model) {
        try {
            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("supplier", new Supplier());
            model.addAttribute("title", "Add Supplier");
            model.addAttribute("countries", Arrays.asList("USA", "UK", "Canada", "India", "China", "Germany", "Japan", "Australia"));
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            return "suppliers/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading supplier form: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping
    public String saveSupplier(@ModelAttribute Supplier supplier, Model model) {
        try {
            supplierService.saveSupplier(supplier);
            return "redirect:/suppliers";
        } catch (Exception e) {
            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("error", "Error saving supplier: " + e.getMessage());
            model.addAttribute("countries", Arrays.asList("USA", "UK", "Canada", "India", "China", "Germany", "Japan", "Australia"));
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            return "suppliers/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("supplier", supplier);
            model.addAttribute("title", "Edit Supplier");
            model.addAttribute("countries", Arrays.asList("USA", "UK", "Canada", "India", "China", "Germany", "Japan", "Australia"));
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            return "suppliers/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading supplier: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return "redirect:/suppliers";
        } catch (Exception e) {
            return "redirect:/suppliers?error=" + e.getMessage();
        }
    }

    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        try {
            Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));

            // Get products from this supplier
            List<com.inventory.inventory_system.entity.Product> supplierProducts = 
                productService.getProductsBySupplier(id.toString());

            // Add statistics for the sidebar
            long totalProducts = productService.getTotalProductsCount();
            long inStockProducts = productService.getInStockProductsCount();
            long lowStockProducts = productService.getLowStockProductsCount();
            long outOfStockProducts = productService.getOutOfStockProductsCount();
            BigDecimal totalInventoryValue = productService.getTotalInventoryValue();

            model.addAttribute("supplier", supplier);
            model.addAttribute("supplierProducts", supplierProducts);
            
            // Sidebar statistics
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("inStockProducts", inStockProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);
            model.addAttribute("totalInventoryValue", totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO);
            
            model.addAttribute("title", "Supplier Details - " + supplier.getName());

            return "suppliers/view";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading supplier: " + e.getMessage());
            return "error";
        }
    }
}