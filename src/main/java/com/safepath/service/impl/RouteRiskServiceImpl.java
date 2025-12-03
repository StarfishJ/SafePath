package com.safepath.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.safepath.dto.GoogleRouteRequest;
import com.safepath.dto.RouteRiskResponse;
import com.safepath.repository.StreetSegmentRiskRepository;
import com.safepath.service.RouteRiskService;
import com.safepath.util.PolylineDecoder;

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

            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RouteRiskServiceImpl.class);
            logger.info("Route '{}' - Total steps: {}, Total risk: {}, Average risk score: {}", 
                routeName, totalSteps, totalRouteRisk, routeRisk.getTotalRiskScore());

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
        final int totalPoints = points.size();
        final int[] matchedPoints = {0}; // Use array to allow modification in lambda

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RouteRiskServiceImpl.class);
        
        // Log all points for debugging (first step only to avoid too much output)
        if (totalPoints > 0) {
            logger.debug("Step has {} points to match", totalPoints);
            // Log first and last few points
            int logCount = Math.min(5, totalPoints);
            for (int i = 0; i < logCount; i++) {
                PolylineDecoder.LatLng p = points.get(i);
                logger.debug("  Point {}: lat={}, lon={}", i+1, p.lat, p.lng);
            }
            if (totalPoints > logCount) {
                logger.debug("  ... ({} more points)", totalPoints - logCount);
                // Log last point
                PolylineDecoder.LatLng lastP = points.get(points.size() - 1);
                logger.debug("  Point {}: lat={}, lon={}", totalPoints, lastP.lat, lastP.lng);
            }
        }
        
        for (PolylineDecoder.LatLng point : points) {
            riskRepository.findNearestSegment(point.lat, point.lng).ifPresent(risk -> {
                riskScores.add(risk.getRiskScore());
                String label = risk.getRiskLabel() != null ? risk.getRiskLabel() : "UNKNOWN";
                labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                matchedPoints[0]++;
            });
        }
        
        // Log first few unmatched points for debugging
        if (matchedPoints[0] == 0 && totalPoints > 0) {
            logger.warn("⚠️ No matches found for any points in this step. Sample coordinates:");
            for (int i = 0; i < Math.min(5, points.size()); i++) {
                PolylineDecoder.LatLng p = points.get(i);
                logger.warn("  Unmatched Point {}: lat={}, lon={}", i+1, p.lat, p.lng);
            }
            // Also check what segments exist nearby
            if (!points.isEmpty()) {
                PolylineDecoder.LatLng samplePoint = points.get(0);
                logger.warn("  Checking for segments near sample point (lat={}, lon={}):", 
                    samplePoint.lat, samplePoint.lng);
                // Calculate bounding box (500m = 0.005 degrees)
                double latMin = samplePoint.lat - 0.005;
                double latMax = samplePoint.lat + 0.005;
                double lonMin = samplePoint.lng - 0.005;
                double lonMax = samplePoint.lng + 0.005;
                logger.warn("  Search box: lat [{}, {}], lon [{}, {}]", latMin, latMax, lonMin, lonMax);
                
                // Query nearby segments to see what's available
                try {
                    List<com.safepath.model.StreetSegmentRisk> nearbySegments = 
                        riskRepository.findWithinBounds(lonMin, latMin, lonMax, latMax);
                    if (nearbySegments.isEmpty()) {
                        logger.warn("  ❌ No street segments found in search box!");
                    } else {
                        logger.warn("  Found {} segments in search box:", nearbySegments.size());
                        for (int i = 0; i < Math.min(5, nearbySegments.size()); i++) {
                            com.safepath.model.StreetSegmentRisk seg = nearbySegments.get(i);
                            com.safepath.model.StreetSegment streetSeg = seg.getStreetSegment();
                            if (streetSeg != null) {
                                logger.warn("    Segment {}: unitid={}, lat={}, lon={}, risk={}", 
                                    i+1, seg.getUnitid(), streetSeg.getGisMidY(), 
                                    streetSeg.getGisMidX(), seg.getRiskScore());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error checking nearby segments", e);
                }
            }
        }
        
        // Log matching statistics for debugging
        if (totalPoints > 0) {
            int matchedCount = matchedPoints[0];
            int coveragePercent = (matchedCount * 100 / totalPoints);
            logger.info("Step risk analysis: {}/{} points matched to street segments ({}% coverage)", 
                matchedCount, totalPoints, coveragePercent);
            
            if (matchedCount > 0) {
                double minRisk = riskScores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                double maxRisk = riskScores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                logger.info("Matched risk scores - Min: {}, Max: {}, Count: {}", 
                    minRisk, maxRisk, riskScores.size());
            } else {
                logger.warn("⚠️ No points matched to street segments! This step will have risk score 0.");
            }
        }

        // Compute average risk across all sampled points
        // Only count matched points with non-zero risk scores for more accurate assessment
        double avgRisk;
        if (riskScores.isEmpty()) {
            avgRisk = 0.0;
            logger.warn("⚠️ No risk scores collected - all points either unmatched or have zero risk");
        } else {
            // Calculate average of matched risk scores
            avgRisk = riskScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            
            // If average is 0 but we have matched points, log a warning
            if (avgRisk == 0.0 && matchedPoints[0] > 0) {
                logger.warn("⚠️ All matched segments have risk score 0. Matched {} points but all risk scores are 0.", 
                    matchedPoints[0]);
            }
        }
        stepRisk.setAverageRiskScore(avgRisk);
        
        logger.info("Step average risk score: {} (from {} matched points out of {} total points)", 
            avgRisk, riskScores.size(), totalPoints);

        // Determine the most common risk label in this step
        String dominantLabel = labelCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");
        stepRisk.setDominantRiskLabel(dominantLabel);

        return stepRisk;
    }
}

