package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.entity.Sale;
import com.inventory.inventory_system.service.ProductService;
import com.inventory.inventory_system.service.SaleService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    @GetMapping("/products/excel")
    public ResponseEntity<InputStreamResource> exportProductsToExcel() {
        try {
            List<Product> products = productService.getAllProducts();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Products");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "SKU", "Category", "Price", "Quantity", "Reorder Level", "Stock Status", "Supplier ID", "Created Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId() != null ? product.getId().toString() : "N/A");
                row.createCell(1).setCellValue(product.getName() != null ? product.getName() : "N/A");
                row.createCell(2).setCellValue(product.getSku() != null ? product.getSku() : "N/A");
                row.createCell(3).setCellValue(product.getCategory() != null ? product.getCategory() : "N/A");
                row.createCell(4).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(product.getQuantity() != null ? product.getQuantity() : 0);
                row.createCell(6).setCellValue(product.getReorderLevel() != null ? product.getReorderLevel() : 10);
                row.createCell(7).setCellValue(product.getStockStatus());
                row.createCell(8).setCellValue(product.getSupplierId() != null ? product.getSupplierId() : "N/A");
                row.createCell(9).setCellValue(product.getCreatedAt() != null ? 
                    product.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Disposition", "attachment; filename=products_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/sales/excel")
    public ResponseEntity<InputStreamResource> exportSalesToExcel() {
        try {
            List<Sale> sales = saleService.getAllSales();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sales");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Product Name", "Product SKU", "Category", "Quantity", "Unit Price", "Total Amount", "Customer Name", "Sale Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (Sale sale : sales) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sale.getId() != null ? sale.getId().toString() : "N/A");
                row.createCell(1).setCellValue(sale.getProductName() != null ? sale.getProductName() : "N/A");
                row.createCell(2).setCellValue(sale.getProductSku() != null ? sale.getProductSku() : "N/A");
                row.createCell(3).setCellValue(sale.getProductCategory() != null ? sale.getProductCategory() : "N/A");
                row.createCell(4).setCellValue(sale.getQuantity() != null ? sale.getQuantity() : 0);
                row.createCell(5).setCellValue(sale.getUnitPrice() != null ? sale.getUnitPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(sale.getTotalAmount() != null ? sale.getTotalAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(sale.getCustomerName() != null ? sale.getCustomerName() : "N/A");
                row.createCell(8).setCellValue(sale.getSaleDate() != null ? 
                    sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            summaryRow.createCell(0).setCellValue("TOTAL SALES:");
            BigDecimal totalSales = sales.stream()
                .filter(s -> s.getTotalAmount() != null)
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summaryRow.createCell(6).setCellValue(totalSales.doubleValue());

            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryRow.getCell(0).setCellStyle(summaryStyle);
            summaryRow.getCell(6).setCellStyle(summaryStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Disposition", "attachment; filename=sales_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<InputStreamResource> exportReportsToExcel() {
        try {
            List<Product> products = productService.getAllProducts();
            List<Sale> sales = saleService.getAllSales();

            Workbook workbook = new XSSFWorkbook();
            
            // Products Sheet
            Sheet productsSheet = workbook.createSheet("Products Report");
            createProductsReportSheet(productsSheet, products);
            
            // Sales Sheet
            Sheet salesSheet = workbook.createSheet("Sales Report");
            createSalesReportSheet(salesSheet, sales);
            
            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, products, sales);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Disposition", "attachment; filename=inventory_report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report: " + e.getMessage());
        }
    }

    private void createProductsReportSheet(Sheet sheet, List<Product> products) {
        // Implementation for products report sheet
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product Report - Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))};
        headerRow.createCell(0).setCellValue(headers[0]);
        
        // Add product statistics
        Row statsRow1 = sheet.createRow(2);
        statsRow1.createCell(0).setCellValue("Total Products: " + products.size());
        
        long inStock = products.stream().filter(p -> p.getQuantity() != null && p.getQuantity() > 0).count();
        Row statsRow2 = sheet.createRow(3);
        statsRow2.createCell(0).setCellValue("In Stock: " + inStock);
        
        long lowStock = products.stream().filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() <= 5).count();
        Row statsRow3 = sheet.createRow(4);
        statsRow3.createCell(0).setCellValue("Low Stock: " + lowStock);
        
        long outOfStock = products.stream().filter(p -> p.getQuantity() == null || p.getQuantity() == 0).count();
        Row statsRow4 = sheet.createRow(5);
        statsRow4.createCell(0).setCellValue("Out of Stock: " + outOfStock);
    }

    private void createSalesReportSheet(Sheet sheet, List<Sale> sales) {
        // Implementation for sales report sheet
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Sales Report - Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        Row statsRow1 = sheet.createRow(2);
        statsRow1.createCell(0).setCellValue("Total Sales: " + sales.size());
        
        BigDecimal totalRevenue = sales.stream()
            .filter(s -> s.getTotalAmount() != null)
            .map(Sale::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Row statsRow2 = sheet.createRow(3);
        statsRow2.createCell(0).setCellValue("Total Revenue: $" + totalRevenue);
    }

    private void createSummarySheet(Sheet sheet, List<Product> products, List<Sale> sales) {
        // Implementation for summary sheet
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Inventory Management System - Summary Report");
        
        Row dateRow = sheet.createRow(1);
        dateRow.createCell(0).setCellValue("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        // Add key metrics
        int rowNum = 3;
        String[][] summaryData = {
            {"Total Products", String.valueOf(products.size())},
            {"Total Sales Records", String.valueOf(sales.size())},
            {"Inventory Value", "$" + productService.getTotalInventoryValue()},
            {"Today's Sales", "$" + saleService.getTotalSalesToday()},
            {"Monthly Sales", "$" + saleService.getTotalSalesThisMonth()}
        };
        
        for (String[] data : summaryData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data[0]);
            row.createCell(1).setCellValue(data[1]);
        }
    }
}