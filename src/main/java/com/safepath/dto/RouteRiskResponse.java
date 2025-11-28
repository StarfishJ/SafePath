package com.safepath.dto;

import java.util.List;

/**
 * Response containing risk scores for one or more routes.
 * Each route includes an overall risk score and per-step breakdown.
 */
public class RouteRiskResponse {

    private List<RouteRisk> routes;

    public RouteRiskResponse() {
    }

    public RouteRiskResponse(List<RouteRisk> routes) {
        this.routes = routes;
    }

    public List<RouteRisk> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteRisk> routes) {
        this.routes = routes;
    }

    /**
     * Risk analysis for a single route.
     */
    public static class RouteRisk {
        private String routeName;
        private double totalRiskScore;  // Average of all step scores (0-1)
        private int totalSteps;
        private List<StepRisk> stepRisks;

        public RouteRisk() {
        }

        public String getRouteName() {
            return routeName;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }

        public double getTotalRiskScore() {
            return totalRiskScore;
        }

        public void setTotalRiskScore(double totalRiskScore) {
            this.totalRiskScore = totalRiskScore;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public List<StepRisk> getStepRisks() {
            return stepRisks;
        }

        public void setStepRisks(List<StepRisk> stepRisks) {
            this.stepRisks = stepRisks;
        }
    }

    /**
     * Risk analysis for a single step within a route.
     */
    public static class StepRisk {
        private double averageRiskScore;  // Mean of all sampled points in this step
        private String dominantRiskLabel;  // LOW, MEDIUM, or HIGH

        public StepRisk() {
        }

        public double getAverageRiskScore() {
            return averageRiskScore;
        }

        public void setAverageRiskScore(double averageRiskScore) {
            this.averageRiskScore = averageRiskScore;
        }

        public String getDominantRiskLabel() {
            return dominantRiskLabel;
        }

        public void setDominantRiskLabel(String dominantRiskLabel) {
            this.dominantRiskLabel = dominantRiskLabel;
        }
    }
}

