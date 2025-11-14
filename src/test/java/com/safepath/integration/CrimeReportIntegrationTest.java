package com.safepath.integration;

import com.safepath.dto.CreateCrimeReportRequest;
import com.safepath.model.CrimeReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
public class CrimeReportIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void createAndRetrieveCrimeReport() {
        CreateCrimeReportRequest req = new CreateCrimeReportRequest();
        req.setReportNumber("IT-CR-1");
        req.setReportDatetime(LocalDateTime.now());
        req.setPrecinct("TestPrecinct");

        ResponseEntity<CrimeReport> post = rest.postForEntity("/api/crime-reports", req, CrimeReport.class);
        assertEquals(HttpStatus.CREATED, post.getStatusCode());
        CrimeReport created = post.getBody();
        assertNotNull(created);
        assertEquals("IT-CR-1", created.getReportNumber());

        ResponseEntity<CrimeReport> get = rest.getForEntity("/api/crime-reports/IT-CR-1", CrimeReport.class);
        assertEquals(HttpStatus.OK, get.getStatusCode());
        CrimeReport fetched = get.getBody();
        assertNotNull(fetched);
        assertEquals(created.getReportNumber(), fetched.getReportNumber());
    }
}
