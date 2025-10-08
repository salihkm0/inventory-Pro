package com.inventory.inventory_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ProfileController {
    
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            
            // User information
            model.addAttribute("username", user.getUsername());
            model.addAttribute("roles", user.getAuthorities());
            model.addAttribute("accountEnabled", user.isEnabled());
            model.addAttribute("accountNonExpired", user.isAccountNonExpired());
            model.addAttribute("credentialsNonExpired", user.isCredentialsNonExpired());
            model.addAttribute("accountNonLocked", user.isAccountNonLocked());
            
            // Profile statistics (you can customize these)
            model.addAttribute("memberSince", "January 2024");
            model.addAttribute("lastLogin", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            model.addAttribute("loginCount", "127");
        } else {
            // Fallback for unauthenticated users
            model.addAttribute("username", "Guest");
            model.addAttribute("roles", "ROLE_GUEST");
        }
        
        model.addAttribute("title", "User Profile");
        return "profile";
    }
    
    @GetMapping("/settings")
    public String showSettings(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getUsername() + "@inventorypro.com");
        }
        
        model.addAttribute("title", "Settings");
        return "settings";
    }
    
    @PostMapping("/profile/update-password")
    public String updatePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        
        // Basic validation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/settings";
        }
        
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long!");
            return "redirect:/settings";
        }
        
        // In a real application, you would update the password in the user store
        // For in-memory authentication, this would require recreating the user
        
        redirectAttributes.addFlashAttribute("success", "Password updated successfully!");
        return "redirect:/settings";
    }
}