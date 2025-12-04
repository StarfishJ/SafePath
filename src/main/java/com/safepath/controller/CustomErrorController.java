package com.safepath.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        Map<String, Object> errorResponse = new HashMap<>();
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            errorResponse.put("status", statusCode);
            errorResponse.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        } else {
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
        }

        errorResponse.put("message", message != null ? message.toString() : "An unexpected error occurred");
        errorResponse.put("path", path != null ? path.toString() : request.getRequestURI());
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());

        // provide useful information
        if (status != null && Integer.parseInt(status.toString()) == 404) {
            errorResponse.put("suggestion", "The requested endpoint does not exist. Available endpoints: /api/users, /api/crime-reports, /api/user-alerts");
            errorResponse.put("documentation", "/api");
        }

        HttpStatus httpStatus = status != null ? 
            HttpStatus.valueOf(Integer.parseInt(status.toString())) : 
            HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}

