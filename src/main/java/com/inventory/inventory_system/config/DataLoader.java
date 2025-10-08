package com.inventory.inventory_system.config;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.entity.Sale;
import com.inventory.inventory_system.entity.Supplier;
import com.inventory.inventory_system.repository.ProductRepository;
import com.inventory.inventory_system.repository.SaleRepository;
import com.inventory.inventory_system.repository.SupplierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;

    public DataLoader(ProductRepository productRepository, SupplierRepository supplierRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting Inventory Management System...");
        
        // Load sample data
        loadSampleData();
        
        System.out.println("✅ Application started successfully!");
        System.out.println("🌐 Access your application at: http://localhost:8080");
        System.out.println("📊 Products page: http://localhost:8080/products");
        System.out.println("📈 Reports page: http://localhost:8080/products/reports");
        System.out.println("📋 Dashboard: http://localhost:8080/dashboard");
        System.out.println("❤️ Health check: http://localhost:8080/health");
        System.out.println("🗄️ H2 Console: http://localhost:8080/h2-console");
    }

    private void loadSampleData() {
        // Create sample suppliers
        Supplier supplier1 = new Supplier("Tech Supplies Inc", "John Doe", "john@techsupplies.com", "123-456-7890");
        supplier1.setSupplierCode("TECH001");
        supplier1.setAddress("123 Tech Street");
        supplier1.setCity("San Francisco");
        supplier1.setCountry("USA");
        
        Supplier supplier2 = new Supplier("Office World", "Jane Smith", "jane@officeworld.com", "987-654-3210");
        supplier2.setSupplierCode("OFFICE001");
        supplier2.setAddress("456 Office Ave");
        supplier2.setCity("New York");
        supplier2.setCountry("USA");
        
        supplierRepository.save(supplier1);
        supplierRepository.save(supplier2);

        // Create sample products
        Product laptop = new Product("Laptop", "High-performance laptop with 16GB RAM", "LAPTOP-001", 
                                   new BigDecimal("999.99"), 15, "Electronics");
        laptop.setSupplierId(supplier1.getId().toString());

        Product mouse = new Product("Wireless Mouse", "Ergonomic wireless mouse", "MOUSE-001", 
                                  new BigDecimal("29.99"), 50, "Electronics");
        mouse.setSupplierId(supplier1.getId().toString());

        Product notebook = new Product("Notebook", "Premium quality notebook", "NOTE-001", 
                                     new BigDecimal("4.99"), 100, "Stationery");
        notebook.setSupplierId(supplier2.getId().toString());

        Product pen = new Product("Ballpoint Pen", "Smooth writing ballpoint pen", "PEN-001", 
                                new BigDecimal("1.99"), 200, "Stationery");
        pen.setSupplierId(supplier2.getId().toString());

        Product chair = new Product("Office Chair", "Comfortable office chair", "CHAIR-001", 
                                  new BigDecimal("149.99"), 8, "Furniture");
        chair.setReorderLevel(5);

        // Low stock product
        Product keyboard = new Product("Mechanical Keyboard", "RGB mechanical keyboard", "KEYBOARD-001", 
                                     new BigDecimal("79.99"), 3, "Electronics");
        keyboard.setSupplierId(supplier1.getId().toString());
        keyboard.setReorderLevel(5);

        // Save products first
        List<Product> savedProducts = productRepository.saveAll(List.of(laptop, mouse, notebook, pen, chair, keyboard));

        System.out.println("✅ Sample data loaded: 6 products, 2 suppliers");

        // Create dummy sales data
        createDummySales(savedProducts);
    }

    private void createDummySales(List<Product> products) {
        System.out.println("🛒 Creating dummy sales data...");
        
        Random random = new Random();
        String[] customers = {"John Smith", "Emma Wilson", "Michael Brown", "Sarah Johnson", "David Lee", 
                            "Lisa Garcia", "Robert Davis", "Maria Martinez", "James Wilson", "Jennifer Taylor"};
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer"};
        
        // Create sales for the last 3 months
        LocalDateTime now = LocalDateTime.now();
        
        // Sales for current month
        for (int i = 0; i < 8; i++) {
            Product product = products.get(random.nextInt(products.size()));
            int quantity = random.nextInt(3) + 1; // 1-3 items
            LocalDateTime saleDate = now.minusDays(random.nextInt(30));
            
            Sale sale = new Sale(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice(),
                customers[random.nextInt(customers.length)]
            );
            sale.setProductSku(product.getSku());
            sale.setProductCategory(product.getCategory());
            sale.setSaleDate(saleDate);
            sale.setPaymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]);
            sale.setCustomerEmail(sale.getCustomerName().toLowerCase().replace(" ", ".") + "@example.com");
            
            saleRepository.save(sale);
        }
        
        // Sales for previous month
        for (int i = 0; i < 12; i++) {
            Product product = products.get(random.nextInt(products.size()));
            int quantity = random.nextInt(3) + 1;
            LocalDateTime saleDate = now.minusMonths(1).minusDays(random.nextInt(30));
            
            Sale sale = new Sale(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice(),
                customers[random.nextInt(customers.length)]
            );
            sale.setProductSku(product.getSku());
            sale.setProductCategory(product.getCategory());
            sale.setSaleDate(saleDate);
            sale.setPaymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]);
            sale.setCustomerEmail(sale.getCustomerName().toLowerCase().replace(" ", ".") + "@example.com");
            
            saleRepository.save(sale);
        }
        
        // Sales for month before last
        for (int i = 0; i < 10; i++) {
            Product product = products.get(random.nextInt(products.size()));
            int quantity = random.nextInt(3) + 1;
            LocalDateTime saleDate = now.minusMonths(2).minusDays(random.nextInt(30));
            
            Sale sale = new Sale(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice(),
                customers[random.nextInt(customers.length)]
            );
            sale.setProductSku(product.getSku());
            sale.setProductCategory(product.getCategory());
            sale.setSaleDate(saleDate);
            sale.setPaymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]);
            sale.setCustomerEmail(sale.getCustomerName().toLowerCase().replace(" ", ".") + "@example.com");
            
            saleRepository.save(sale);
        }
        
        // Update product quantities based on sales (simulate stock reduction)
        updateProductQuantities(products);
        
        System.out.println("✅ Dummy sales data created: " + saleRepository.count() + " sales records");
    }

    private void updateProductQuantities(List<Product> products) {
        // Reduce product quantities based on sales to make it realistic
        for (Product product : products) {
            long totalSold = saleRepository.findAll().stream()
                .filter(sale -> sale.getProductId().equals(product.getId()))
                .mapToLong(Sale::getQuantity)
                .sum();
            
            // Ensure we don't go below 0
            int newQuantity = Math.max(0, product.getQuantity() - (int) totalSold);
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }
        
        System.out.println("📦 Updated product quantities based on sales");
    }
}