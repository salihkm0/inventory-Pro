package com.inventory.inventory_system.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateProductPdf(com.inventory.inventory_system.entity.Product product) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Paragraph title = new Paragraph("Product Details Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(30);
            document.add(date);
            
            // Create table for product details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);
            
            // Add product details
            addTableRow(table, "Product Name:", product.getName());
            addTableRow(table, "SKU:", product.getSku());
            addTableRow(table, "Category:", product.getCategory() != null ? product.getCategory() : "Not specified");
            addTableRow(table, "Price:", "$" + String.format("%.2f", product.getPrice()));
            addTableRow(table, "Quantity:", String.valueOf(product.getQuantity()));
            addTableRow(table, "Reorder Level:", 
                product.getReorderLevel() != null ? String.valueOf(product.getReorderLevel()) : "10");
            addTableRow(table, "Stock Status:", getStockStatusText(product));
            addTableRow(table, "Inventory Value:", 
                "$" + String.format("%.2f", product.getPrice().doubleValue() * product.getQuantity()));
            
            document.add(table);
            
            // Add description if available
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                Font descFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Paragraph descTitle = new Paragraph("Description:", descFont);
                descTitle.setSpacingBefore(20f);
                descTitle.setSpacingAfter(10f);
                document.add(descTitle);
                
                Font descContentFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
                Paragraph description = new Paragraph(product.getDescription(), descContentFont);
                description.setSpacingAfter(20f);
                document.add(description);
            }
            
            // Add timestamps
            PdfPTable timestampTable = new PdfPTable(2);
            timestampTable.setWidthPercentage(100);
            timestampTable.setSpacingBefore(10f);
            
            if (product.getCreatedAt() != null) {
                addTableRow(timestampTable, "Created:", 
                    product.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            if (product.getUpdatedAt() != null) {
                addTableRow(timestampTable, "Last Updated:", 
                    product.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            
            if (timestampTable.size() > 0) {
                document.add(timestampTable);
            }
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating product PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateSalesPdf(com.inventory.inventory_system.entity.Sale sale) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Paragraph title = new Paragraph("Sales Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);
            
            // Add receipt number
            Font receiptFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph receiptNo = new Paragraph("Receipt #: " + sale.getId(), receiptFont);
            receiptNo.setAlignment(Element.ALIGN_CENTER);
            receiptNo.setSpacingAfter(10);
            document.add(receiptNo);
            
            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(30);
            document.add(date);
            
            // Create table for sale details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);
            
            // Add sale details
            if (sale.getSaleDate() != null) {
                addTableRow(table, "Sale Date:", 
                    sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            addTableRow(table, "Product Name:", sale.getProductName());
            addTableRow(table, "SKU:", sale.getProductSku() != null ? sale.getProductSku() : "N/A");
            addTableRow(table, "Category:", 
                sale.getProductCategory() != null ? sale.getProductCategory() : "Not specified");
            addTableRow(table, "Quantity:", String.valueOf(sale.getQuantity()));
            addTableRow(table, "Unit Price:", "$" + String.format("%.2f", sale.getUnitPrice()));
            
            // Highlight total amount
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Amount:"));
            totalLabelCell.setBackgroundColor(Color.LIGHT_GRAY);
            totalLabelCell.setPadding(8);
            totalLabelCell.setBorderWidth(1);
            
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.GREEN.darker());
            PdfPCell totalValueCell = new PdfPCell(new Phrase("$" + String.format("%.2f", sale.getTotalAmount()), totalFont));
            totalValueCell.setPadding(8);
            totalValueCell.setBorderWidth(1);
            
            table.addCell(totalLabelCell);
            table.addCell(totalValueCell);
            
            addTableRow(table, "Payment Method:", 
                sale.getPaymentMethod() != null ? sale.getPaymentMethod() : "Not specified");
            
            if (sale.getCustomerName() != null) {
                addTableRow(table, "Customer Name:", sale.getCustomerName());
            }
            if (sale.getCustomerEmail() != null) {
                addTableRow(table, "Customer Email:", sale.getCustomerEmail());
            }
            
            document.add(table);
            
            // Add footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
            Paragraph footer = new Paragraph("Thank you for your business!", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20f);
            document.add(footer);
            
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating sales PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateProductListPdf(List<com.inventory.inventory_system.entity.Product> products) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Landscape for better table view
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.BOLD);
            Paragraph title = new Paragraph("Product Inventory Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);
            
            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);
            
            // Add summary statistics
            long totalProducts = products.size();
            long inStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .count();
            long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() <= 5)
                .count();
            long outOfStockCount = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() == 0)
                .count();
            
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Paragraph summary = new Paragraph(
                String.format("Summary: Total: %d | In Stock: %d | Low Stock: %d | Out of Stock: %d", 
                    totalProducts, inStockCount, lowStockCount, outOfStockCount), summaryFont);
            summary.setAlignment(Element.ALIGN_CENTER);
            summary.setSpacingAfter(20);
            document.add(summary);
            
            // Create table for products
            PdfPTable table = new PdfPTable(7); // 7 columns
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);
            
            // Set column widths
            float[] columnWidths = {1.5f, 2.5f, 1.5f, 1f, 1f, 1.5f, 1.5f};
            table.setWidths(columnWidths);
            
            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            String[] headers = {"SKU", "Product Name", "Category", "Price", "Quantity", "Status", "Value"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setBorderWidth(1);
                table.addCell(cell);
            }
            
            // Add product rows
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (com.inventory.inventory_system.entity.Product product : products) {
                // SKU
                table.addCell(createCell(product.getSku(), contentFont));
                
                // Product Name
                table.addCell(createCell(product.getName(), contentFont));
                
                // Category
                table.addCell(createCell(product.getCategory() != null ? product.getCategory() : "Not specified", contentFont));
                
                // Price
                table.addCell(createCell("$" + String.format("%.2f", product.getPrice()), contentFont));
                
                // Quantity
                PdfPCell quantityCell = createCell(String.valueOf(product.getQuantity()), contentFont);
                if (product.getQuantity() == 0) {
                    quantityCell.setBackgroundColor(new Color(255, 200, 200)); // Red for out of stock
                } else if (product.getQuantity() <= 5) {
                    quantityCell.setBackgroundColor(new Color(255, 255, 200)); // Yellow for low stock
                }
                table.addCell(quantityCell);
                
                // Status
                String status = getStockStatusText(product);
                PdfPCell statusCell = createCell(status, contentFont);
                if (status.equals("Out of Stock")) {
                    statusCell.setBackgroundColor(new Color(255, 200, 200));
                } else if (status.equals("Low Stock")) {
                    statusCell.setBackgroundColor(new Color(255, 255, 200));
                } else {
                    statusCell.setBackgroundColor(new Color(200, 255, 200)); // Green for in stock
                }
                table.addCell(statusCell);
                
                // Value
                table.addCell(createCell("$" + String.format("%.2f", 
                    product.getPrice().doubleValue() * product.getQuantity()), contentFont));
            }
            
            document.add(table);
            
            // Add footer
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
            Paragraph footer = new Paragraph("Inventory Management System - Confidential Report", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating product list PDF: " + e.getMessage(), e);
        }
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(Color.LIGHT_GRAY);
        labelCell.setBorderWidth(1);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderWidth(1);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "N/A", font));
        cell.setPadding(4);
        cell.setBorderWidth(1);
        return cell;
    }
    
    private String getStockStatusText(com.inventory.inventory_system.entity.Product product) {
        if (product.getQuantity() == null || product.getQuantity() == 0) {
            return "Out of Stock";
        } else if (product.getQuantity() <= (product.getReorderLevel() != null ? product.getReorderLevel() : 10)) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }
}