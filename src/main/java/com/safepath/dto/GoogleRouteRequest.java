package com.safepath.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO matching Google Directions API response structure.
 * Frontend sends this directly after fetching routes from Google.
 * 
 * Note: Google Directions Service returns camelCase field names,
 * but we also support snake_case for compatibility.
 */
public class GoogleRouteRequest {

    private List<Route> routes;

    public GoogleRouteRequest() {
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public static class Route {
        private String summary;
        private List<Leg> legs;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }
    }

    public static class Leg {
        private List<Step> steps;

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }
    }

    public static class Step {
        private String instructions;
        private Distance distance;
        private Duration duration;
        
        @JsonProperty(value = "start_location", required = false)
        private LatLng startLocation;
        
        @JsonProperty(value = "end_location", required = false)
        private LatLng endLocation;
        
        private Polyline polyline;

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public LatLng getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(LatLng startLocation) {
            this.startLocation = startLocation;
        }

        public LatLng getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(LatLng endLocation) {
            this.endLocation = endLocation;
        }

        public Polyline getPolyline() {
            return polyline;
        }

        public void setPolyline(Polyline polyline) {
            this.polyline = polyline;
        }
    }

    public static class Distance {
        private String text;
        private int value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class Duration {
        private String text;
        private int value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class LatLng {
        private double lat;
        private double lng;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class Polyline {
        private String points;

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }
}

