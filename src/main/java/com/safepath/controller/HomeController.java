package com.safepath.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "SafePath API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
            "users", "/api/users",
            "crimeReports", "/api/crime-reports",
            "userAlerts", "/api/user-alerts",
            "h2Console", "/h2-console"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "SafePath API Endpoints");
        response.put("baseUrl", "/api");
        response.put("endpoints", Map.of(
            "users", Map.of(
                "POST /api/users", "Create a new user",
                "GET /api/users/{id}", "Get user by ID"
            ),
            "crimeReports", Map.of(
                "GET /api/crime-reports", "Get all crime reports",
                "GET /api/crime-reports/{id}", "Get crime report by ID",
                "POST /api/crime-reports", "Create a new crime report",
                "DELETE /api/crime-reports/{id}", "Delete a crime report"
            ),
            "userAlerts", Map.of(
                "POST /api/user-alerts", "Create a new user alert",
                "GET /api/user-alerts/user/{userId}", "Get all alerts for a user"
            )
        ));
        return ResponseEntity.ok(response);
    }
}

