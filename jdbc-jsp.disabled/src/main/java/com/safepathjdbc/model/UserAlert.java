package com.safepathjdbc.model;

public class UserAlert {
    private Long id;
    private Long userId;
    private String message;

    public UserAlert() {}

    public UserAlert(Long id, Long userId, String message) {
        this.id = id;
        this.userId = userId;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
