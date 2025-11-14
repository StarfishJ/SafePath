package com.safepath.integration;

import com.safepath.dto.CreateUserAlertRequest;
import com.safepath.dto.CreateUserRequest;
import com.safepath.model.AppUser;
import com.safepath.model.UserAlert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

 

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
public class UserAlertIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void createUserAndAlert_thenListByUser() {
        // create user
        CreateUserRequest ureq = new CreateUserRequest();
        ureq.setEmail("alertuser@example.com");
        ureq.setPassword("pwd123");
        ResponseEntity<AppUser> up = rest.postForEntity("/api/users", ureq, AppUser.class);
        assertEquals(HttpStatus.CREATED, up.getStatusCode());
        AppUser user = up.getBody();
        assertNotNull(user);

        // create alert
        CreateUserAlertRequest areq = new CreateUserAlertRequest();
        areq.setUserId(user.getUserId());
        areq.setRadiusM(300);
        areq.setCenterLat(47.61);
        areq.setCenterLon(-122.33);
        areq.setCrimeTypeFilter("THEFT");

        ResponseEntity<UserAlert> ap = rest.postForEntity("/api/user-alerts", areq, UserAlert.class);
        assertEquals(HttpStatus.CREATED, ap.getStatusCode());
        UserAlert alert = ap.getBody();
        assertNotNull(alert);

        // list by user
        ResponseEntity<UserAlert[]> listResp = rest.getForEntity("/api/user-alerts/user/" + user.getUserId(), UserAlert[].class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        UserAlert[] alerts = listResp.getBody();
        assertNotNull(alerts);
        boolean found = false;
        for (UserAlert a : alerts) {
            if (a.getAlertId().equals(alert.getAlertId())) {
                found = true; break;
            }
        }
        assertTrue(found);
    }
}
