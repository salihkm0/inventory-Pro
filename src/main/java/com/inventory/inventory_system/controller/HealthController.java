package com.inventory.inventory_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "✅ Inventory Management System is running!";
    }

    @GetMapping("/api")
    public String apiInfo() {
        return "🚀 Inventory Management System API is running! " +
               "<br/>📊 Products: /products" +
               "<br/>📈 Reports: /products/reports" +
               "<br/>❤️ Health: /health" +
               "<br/>🗄️ H2 Console: /h2-console";
    }
}