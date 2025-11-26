package com.safepath.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.safepath.dto.StreetSegmentRiskDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class SafetyRecommendationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getSingleSegmentRisk() {
        ResponseEntity<StreetSegmentRiskDto> response =
            restTemplate.getForEntity("/api/safety/segments/SEG001", StreetSegmentRiskDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StreetSegmentRiskDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("SEG001", dto.getUnitid());
        assertEquals("HIGH", dto.getRiskLabel());
        assertTrue(dto.getRiskScore() >= 0.0 && dto.getRiskScore() <= 1.0);
    }

    @Test
    void getSegmentsWithinBoundingBox() {
        ResponseEntity<StreetSegmentRiskDto[]> response =
            restTemplate.getForEntity(
                "/api/safety/segments?west=-122.35&south=47.60&east=-122.33&north=47.62",
                StreetSegmentRiskDto[].class
            );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StreetSegmentRiskDto[] body = response.getBody();
        assertNotNull(body);
        assertTrue(body.length >= 1);
    }
}

