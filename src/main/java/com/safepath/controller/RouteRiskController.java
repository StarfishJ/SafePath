package com.safepath.controller;

import com.safepath.dto.GoogleRouteRequest;
import com.safepath.dto.RouteRiskResponse;
import com.safepath.service.RouteRiskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for analyzing route safety.
 * Accepts Google Directions responses and returns risk scores per route/step.
 */
@RestController
@RequestMapping("/api/routes/risk")
public class RouteRiskController {

    private final RouteRiskService routeRiskService;

    public RouteRiskController(RouteRiskService routeRiskService) {
        this.routeRiskService = routeRiskService;
    }

    /**
     * Accepts Google Directions API response and returns risk scores per route.
     * Frontend can POST multiple candidate routes and get back which is safest.
     *
     * @param request Google Directions response with routes/legs/steps
     * @return RouteRiskResponse with per-route and per-step risk scores
     */
    @PostMapping
    public ResponseEntity<RouteRiskResponse> analyzeRouteRisk(@RequestBody GoogleRouteRequest request) {
        RouteRiskResponse response = routeRiskService.analyzeRoutes(request);
        return ResponseEntity.ok(response);
    }
}

