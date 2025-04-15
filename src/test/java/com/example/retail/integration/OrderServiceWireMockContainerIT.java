package com.example.retail.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach; // Import BeforeEach
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

// Static imports should cover verify, *RequestedFor, equalToJson etc.
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class OrderServiceWireMockContainerIT {

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.3.1");

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach // Add this method
    void setup() {
        // Configure WireMock client to talk to the container
        WireMock.configureFor(wiremock.getHost(), wiremock.getPort()); // Use getPort() for dynamic port
        // Reset WireMock state (stubs and request logs) before each test
        WireMock.reset();
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

        String url = wiremock.getBaseUrl() + "/orders/123";
        // Action
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Laptop"));

        // Verification
        verify(1, getRequestedFor(urlEqualTo("/orders/123")));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() {
        // Stub
        stubFor(get(urlEqualTo("/orders/9999"))
                .willReturn(aResponse().withStatus(404)));

        String url = wiremock.getBaseUrl() + "/orders/9999";

        try {
            // Action
            restTemplate.getForEntity(url, String.class);
            fail("Expected HttpClientErrorException.NotFound");
        } catch (HttpClientErrorException.NotFound ex) {
            // Assertion
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

            // Verification
            // Verify the request was made, even though it resulted in 404
            verify(1, getRequestedFor(urlEqualTo("/orders/9999")));
        }
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Stub
        stubFor(post(urlEqualTo("/orders"))
                // You can still use a simpler matcher like 'containing' in the stub if needed
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

        String url = wiremock.getBaseUrl() + "/orders";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Define the exact JSON body that will be sent
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

        // *** FIXED VERIFICATION using equalToJson ***
        verify(1, postRequestedFor(urlEqualTo("/orders"))
                // Use equalToJson to match the exact request body structure and content
                .withRequestBody(equalToJson(requestBodyJson, true, true)) // ignoreArrayOrder=true, ignoreExtraElements=true
                .withHeader("Content-Type", containing("application/json")));
    }
}