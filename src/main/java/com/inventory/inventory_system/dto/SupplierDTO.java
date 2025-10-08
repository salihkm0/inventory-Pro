package com.inventory.inventory_system.dto;

import java.time.LocalDateTime;

public class SupplierDTO {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String supplierCode;
    private Boolean isActive;
    private int productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public SupplierDTO() {}

    public SupplierDTO(Long id, String name, String contactPerson, String email, String phone, 
                      String address, String city, String country, String supplierCode, 
                      Boolean isActive, int productCount) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.country = country;
        this.supplierCode = supplierCode;
        this.isActive = isActive;
        this.productCount = productCount;
    }

    public SupplierDTO(Long id, String name, String contactPerson, String email, String phone, 
                      String address, String city, String country, String supplierCode, 
                      Boolean isActive, int productCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.country = country;
        this.supplierCode = supplierCode;
        this.isActive = isActive;
        this.productCount = productCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getStatusBadge() {
        if (isActive == null) return "secondary";
        return isActive ? "success" : "danger";
    }

    public String getStatusText() {
        if (isActive == null) return "Unknown";
        return isActive ? "Active" : "Inactive";
    }

    public String getFormattedPhone() {
        if (phone == null || phone.trim().isEmpty()) {
            return "N/A";
        }
        return phone;
    }

    public String getShortAddress() {
        if (address == null || address.trim().isEmpty()) {
            return "No address provided";
        }
        if (address.length() > 50) {
            return address.substring(0, 47) + "...";
        }
        return address;
    }

    public boolean hasContactInfo() {
        return (email != null && !email.trim().isEmpty()) || 
               (phone != null && !phone.trim().isEmpty());
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "SU";
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}