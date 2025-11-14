package com.safepathjdbc.model;

import java.time.LocalDateTime;

public class AppUser {
    // Align to Spring entity (users table): user_id, email, password_hash, created_at
    private Integer userId;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    public AppUser() {}

    public AppUser(Integer userId, String email, String passwordHash, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
