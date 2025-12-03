package com.safepath.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.safepath.dto.GoogleRouteRequest;
import com.safepath.dto.RouteRiskResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class RouteRiskIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void analyzeRouteRisk_withValidPolyline() {
        // Build a mock Google Directions response
        GoogleRouteRequest request = new GoogleRouteRequest();
        GoogleRouteRequest.Route route = new GoogleRouteRequest.Route();
        route.setSummary("via 1st Ave");

        GoogleRouteRequest.Leg leg = new GoogleRouteRequest.Leg();
        GoogleRouteRequest.Step step = new GoogleRouteRequest.Step();
        step.setInstructions("Head north on 1st Ave");

        // Encoded polyline for a short segment near SEG001 (47.609, -122.3406)
        GoogleRouteRequest.Polyline polyline = new GoogleRouteRequest.Polyline();
        polyline.setPoints("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
        step.setPolyline(polyline);

        leg.setSteps(List.of(step));
        route.setLegs(List.of(leg));
        request.setRoutes(List.of(route));

        ResponseEntity<RouteRiskResponse> response =
            restTemplate.postForEntity("/api/routes/risk", request, RouteRiskResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        RouteRiskResponse body = response.getBody();
        assertNotNull(body);
        assertFalse(body.getRoutes().isEmpty());

        RouteRiskResponse.RouteRisk routeRisk = body.getRoutes().get(0);
        assertEquals("via 1st Ave", routeRisk.getRouteName());
        assertTrue(routeRisk.getTotalRiskScore() >= 0.0);
        assertTrue(routeRisk.getTotalRiskScore() <= 1.0);

        assertFalse(routeRisk.getStepRisks().isEmpty());
        RouteRiskResponse.StepRisk stepRisk = routeRisk.getStepRisks().get(0);
        assertNotNull(stepRisk.getDominantRiskLabel());
    }

    @Test
    void analyzeRouteRisk_withEmptyRequest() {
        GoogleRouteRequest request = new GoogleRouteRequest();

        ResponseEntity<RouteRiskResponse> response =
            restTemplate.postForEntity("/api/routes/risk", request, RouteRiskResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        RouteRiskResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getRoutes().isEmpty());
    }
}

