package com.inventory.inventory_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {

    // This endpoint will redirect to Spring Security's logout
    @GetMapping("/logout")
    public String logoutRedirect() {
        return "redirect:/login?logout=true";
    }
    
    // Alternative: Handle logout with POST (more secure)
    @PostMapping("/logout")
    public String performLogout() {
        // Spring Security will handle the actual logout
        return "redirect:/login?logout=true";
    }
}