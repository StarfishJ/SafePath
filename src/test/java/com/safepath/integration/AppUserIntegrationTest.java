package com.safepath.integration;

import com.safepath.dto.CreateUserRequest;
import com.safepath.model.AppUser;
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
public class AppUserIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    public void createAndGetUser() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("inttest@example.com");
        req.setPassword("p@ssw0rd");

        ResponseEntity<AppUser> post = rest.postForEntity("/api/users", req, AppUser.class);
        assertEquals(HttpStatus.CREATED, post.getStatusCode());
        AppUser created = post.getBody();
        assertNotNull(created);
        assertNotNull(created.getUserId());

        ResponseEntity<AppUser> get = rest.getForEntity("/api/users/" + created.getUserId(), AppUser.class);
        assertEquals(HttpStatus.OK, get.getStatusCode());
        AppUser fetched = get.getBody();
        assertNotNull(fetched);
        assertEquals(created.getEmail(), fetched.getEmail());
    }
}
