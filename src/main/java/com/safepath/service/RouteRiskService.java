package com.safepath.service;

import com.safepath.dto.GoogleRouteRequest;
import com.safepath.dto.RouteRiskResponse;

public interface RouteRiskService {

    /**
     * Analyzes multiple routes from Google Directions API and computes risk scores
     * for each step and overall route based on historical crime data.
     *
     * @param request Google Directions API response containing routes/legs/steps
     * @return RouteRiskResponse with risk scores per route and step
     */
    RouteRiskResponse analyzeRoutes(GoogleRouteRequest request);
}

