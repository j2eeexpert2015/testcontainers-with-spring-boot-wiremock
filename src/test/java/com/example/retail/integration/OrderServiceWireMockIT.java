package com.example.retail.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*; // Includes verify, *RequestedFor, equalToJson etc.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

class OrderServiceWireMockIT {

    private static WireMockServer wireMockServer;
    private static RestTemplate restTemplate;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        // Configure the static WireMock client to talk to our server instance
        WireMock.configureFor("localhost", wireMockServer.port());
        restTemplate = new RestTemplate();
    }

    @AfterAll
    static void teardown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        // Reset mappings and request journal before each test
        wireMockServer.resetAll();
        // Alternatives:
        // wireMockServer.resetRequests(); // Only clear received requests, keep stubs
        // wireMockServer.resetMappings(); // Only clear stubs, keep request log
        // wireMockServer.resetScenarios(); // Only reset scenario states
    }


    @Test
    void shouldReturnOrderDetails() {
        // Stub
        stubFor(get(urlEqualTo("/orders/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "orderId": "123",
                                  "product": "Laptop",
                                  "quantity": 1
                                }
                                """)));

        String url = "http://localhost:" + wireMockServer.port() + "/orders/123";
        // Action
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Laptop"));

        // Verify
        verify(1, getRequestedFor(urlEqualTo("/orders/123")));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() {
        // Stub
        stubFor(get(urlEqualTo("/orders/9999"))
                .willReturn(aResponse().withStatus(404)));

        String url = "http://localhost:" + wireMockServer.port() + "/orders/9999";

        try {
            // Action
            restTemplate.getForEntity(url, String.class);
            fail("Expected HttpClientErrorException.NotFound");
        } catch (HttpClientErrorException.NotFound ex) {
            // Assertion
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

            // Verify
            verify(1, getRequestedFor(urlEqualTo("/orders/9999")));
        }
    }


    @Test
    void shouldCreateOrderSuccessfully() {
        // Stub
        stubFor(post(urlEqualTo("/orders"))
                .withRequestBody(containing("\"productId\": \"123\""))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "orderId": "abc-001",
                                  "status": "CREATED"
                                }
                                """)));

        String url = "http://localhost:" + wireMockServer.port() + "/orders";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Define the exact request body JSON
        String requestBodyJson = """
                {
                  "productId": "123",
                  "quantity": 2
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(requestBodyJson, headers);

        // Action
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // Assertions
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("abc-001"));

        // Verify
        verify(1, postRequestedFor(urlEqualTo("/orders"))
                .withRequestBody(equalToJson(requestBodyJson, true, true)) // ignoreArrayOrder=true, ignoreExtraElements=true
                .withHeader("Content-Type", containing("application/json")));
    }
}