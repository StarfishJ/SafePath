package com.safepath.service.impl;

import com.safepath.dto.GoogleRouteRequest;
import com.safepath.dto.RouteRiskResponse;
import com.safepath.model.StreetSegmentRisk;
import com.safepath.repository.StreetSegmentRiskRepository;
import com.safepath.service.RouteRiskService;
import com.safepath.util.PolylineDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Implements route risk scoring by decoding Google polylines and mapping
 * each coordinate to the nearest street segment risk score.
 */
@Service
public class RouteRiskServiceImpl implements RouteRiskService {

    private final StreetSegmentRiskRepository riskRepository;

    public RouteRiskServiceImpl(StreetSegmentRiskRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    @Override
    public RouteRiskResponse analyzeRoutes(GoogleRouteRequest request) {
        List<RouteRiskResponse.RouteRisk> routeRisks = new ArrayList<>();

        if (request.getRoutes() == null) {
            return new RouteRiskResponse(routeRisks);
        }

        int routeIndex = 0;
        for (GoogleRouteRequest.Route route : request.getRoutes()) {
            routeIndex++;
            String routeName = route.getSummary() != null && !route.getSummary().isEmpty()
                ? route.getSummary()
                : "Route " + routeIndex;

            RouteRiskResponse.RouteRisk routeRisk = new RouteRiskResponse.RouteRisk();
            routeRisk.setRouteName(routeName);

            List<RouteRiskResponse.StepRisk> stepRisks = new ArrayList<>();
            double totalRouteRisk = 0.0;
            int totalSteps = 0;

            if (route.getLegs() != null) {
                for (GoogleRouteRequest.Leg leg : route.getLegs()) {
                    if (leg.getSteps() != null) {
                        for (GoogleRouteRequest.Step step : leg.getSteps()) {
                            RouteRiskResponse.StepRisk stepRisk = analyzeStep(step);
                            stepRisks.add(stepRisk);
                            totalRouteRisk += stepRisk.getAverageRiskScore();
                            totalSteps++;
                        }
                    }
                }
            }

            routeRisk.setStepRisks(stepRisks);
            routeRisk.setTotalSteps(totalSteps);
            routeRisk.setTotalRiskScore(totalSteps > 0 ? totalRouteRisk / totalSteps : 0.0);

            routeRisks.add(routeRisk);
        }

        return new RouteRiskResponse(routeRisks);
    }

    /**
     * Analyzes a single step by decoding its polyline and sampling risk at each point.
     */
    private RouteRiskResponse.StepRisk analyzeStep(GoogleRouteRequest.Step step) {
        RouteRiskResponse.StepRisk stepRisk = new RouteRiskResponse.StepRisk();

        // Decode polyline into lat/lng coordinates
        List<PolylineDecoder.LatLng> points = new ArrayList<>();
        if (step.getPolyline() != null && step.getPolyline().getPoints() != null) {
            points = PolylineDecoder.decode(step.getPolyline().getPoints());
        }

        // Fallback: if no polyline, sample start/end points
        if (points.isEmpty()) {
            if (step.getStartLocation() != null) {
                points.add(new PolylineDecoder.LatLng(
                    step.getStartLocation().getLat(),
                    step.getStartLocation().getLng()
                ));
            }
            if (step.getEndLocation() != null) {
                points.add(new PolylineDecoder.LatLng(
                    step.getEndLocation().getLat(),
                    step.getEndLocation().getLng()
                ));
            }
        }

        if (points.isEmpty()) {
            stepRisk.setAverageRiskScore(0.0);
            stepRisk.setDominantRiskLabel("UNKNOWN");
            return stepRisk;
        }

        // Query nearest segment for each point and collect risk scores
        List<Double> riskScores = new ArrayList<>();
        Map<String, Integer> labelCounts = new HashMap<>();

        for (PolylineDecoder.LatLng point : points) {
            riskRepository.findNearestSegment(point.lat, point.lng).ifPresent(risk -> {
                riskScores.add(risk.getRiskScore());
                String label = risk.getRiskLabel() != null ? risk.getRiskLabel() : "UNKNOWN";
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
            });
        }

        // Compute average risk across all sampled points
        double avgRisk = riskScores.isEmpty()
            ? 0.0
            : riskScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        stepRisk.setAverageRiskScore(avgRisk);

        // Determine the most common risk label in this step
        String dominantLabel = labelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
        stepRisk.setDominantRiskLabel(dominantLabel);

        return stepRisk;
    }
}

