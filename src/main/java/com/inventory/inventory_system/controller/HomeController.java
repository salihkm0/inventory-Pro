package com.inventory.inventory_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Inventory Management System");
        model.addAttribute("subtitle", "Manage your inventory efficiently");
        return "home";
    }
    
    @GetMapping("/sample-page")
    public String samplePage(Model model) {
        model.addAttribute("title", "Sample Page");
        model.addAttribute("subtitle", "This is a sample page description");
        return "sample-page";
    }
}