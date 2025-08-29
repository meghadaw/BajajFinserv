package com.bajajfinserv.qualifier.service;

import com.bajajfinserv.qualifier.dto.SolutionRequest;
import com.bajajfinserv.qualifier.dto.WebhookRequest;
import com.bajajfinserv.qualifier.dto.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QualifierService {

    private static final Logger logger = LoggerFactory.getLogger(QualifierService.class);

    @Autowired
    private RestTemplate restTemplate;

    // Replace these with your actual details
    private static final String NAME = "Megha Daw";
    private static final String REG_NO = "22BEE0058"; // Even number for Question 2
    private static final String EMAIL = "meghadaw48954@gmail.com";

    private static final String WEBHOOK_GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String WEBHOOK_TEST_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    @EventListener(ApplicationReadyEvent.class)
    public void executeQualifierFlow() {
        logger.info("=== STARTING BAJAJ FINSERV QUALIFIER TEST FLOW ===");
        logger.info("Application Details:");
        logger.info("- Name: {}", NAME);
        logger.info("- Registration Number: {}", REG_NO);
        logger.info("- Email: {}", EMAIL);
        logger.info("- Target URL: {}", WEBHOOK_GENERATE_URL);

        try {
            // Add a small delay to ensure all beans are initialized
            Thread.sleep(2000);

            // Step 1: Generate webhook
            logger.info("Step 1: Attempting to generate webhook...");
            WebhookResponse webhookResponse = generateWebhook();

            if (webhookResponse != null && webhookResponse.getAccessToken() != null) {
                logger.info("SUCCESS: Webhook generated successfully!");
                logger.info("Access Token received: {}", webhookResponse.getAccessToken());
                logger.info("Webhook URL: {}", webhookResponse.getWebhook());

                // Step 2: Solve SQL problem (Question 2)
                logger.info("Step 2: Preparing SQL solution...");
                String sqlSolution = solveSQLProblem();

                // Step 3: Submit solution
                logger.info("Step 3: Submitting solution...");
                submitSolution(webhookResponse.getAccessToken(), sqlSolution);

                logger.info("=== QUALIFIER TEST FLOW COMPLETED SUCCESSFULLY ===");
            } else {
                logger.error("FAILED: Webhook generation failed or returned null response");
                logger.error("This could be due to:");
                logger.error("1. Network connectivity issues");
                logger.error("2. API endpoint temporarily unavailable");
                logger.error("3. Invalid request format");
                logger.error("4. Registration number or email validation issues");
                logger.info("💡 The SQL solution has been prepared and logged below for manual submission:");

                // Still show the SQL solution for manual submission
                String sqlSolution = solveSQLProblem();
                logger.info("=== MANUAL SUBMISSION SOLUTION ===");
                logger.info("Final SQL Query: {}", sqlSolution);
                logger.info("=== END MANUAL SUBMISSION SOLUTION ===");
            }
        } catch (Exception e) {
            logger.info("💡 Preparing SQL solution for manual submission...");
            try {
                String sqlSolution = solveSQLProblem();
                logger.info("=== MANUAL SUBMISSION SOLUTION ===");
                logger.info("Final SQL Query: {}", sqlSolution);
                logger.info("=== END MANUAL SUBMISSION SOLUTION ===");
            } catch (Exception sqlError) {
                logger.error("Error preparing SQL solution: ", sqlError);
            }
        }
    }

    private WebhookResponse generateWebhook() {
        try {
            logger.info("Generating webhook for regNo: {}", REG_NO);
            logger.info("Request details - Name: {}, Email: {}", NAME, EMAIL);
            logger.info("Making POST request to: {}", WEBHOOK_GENERATE_URL);

            WebhookRequest request = new WebhookRequest(NAME, REG_NO, EMAIL);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Spring-Boot-Qualifier-App/1.0");

            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);

            logger.info("Request payload: Name={}, RegNo={}, Email={}", NAME, REG_NO, EMAIL);

            ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                WEBHOOK_GENERATE_URL,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
            );

            logger.info("Webhook generation response status: {}", response.getStatusCode());
            logger.info("Response headers: {}", response.getHeaders());

            WebhookResponse responseBody = response.getBody();
            if (responseBody != null) {
                logger.info("Received webhook URL: {}", responseBody.getWebhook());
                logger.info("Received access token length: {}",
                    responseBody.getAccessToken() != null ? responseBody.getAccessToken().length() : "null");
                return responseBody;
            } else {
                logger.error("Response body is null even though status was: {}", response.getStatusCode());
                return null;
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP Client Error (4xx): Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            logger.error("HTTP Server Error (5xx): Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Network/Connection Error: {}", e.getMessage());
            logger.error("Please check your internet connection and firewall settings");
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error generating webhook: {}", e.getMessage(), e);
            return null;
        }
    }

    private String solveSQLProblem() {
        // SQL solution for Question 2: Calculate younger employees count by department
        String sqlQuery = "SELECT " +
                "e1.EMP_ID, " +
                "e1.FIRST_NAME, " +
                "e1.LAST_NAME, " +
                "d.DEPARTMENT_NAME, " +
                "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                "AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC";

        logger.info("SQL Solution prepared: {}", sqlQuery);
        return sqlQuery.trim();
    }

    private void submitSolution(String accessToken, String sqlQuery) {
        try {
            logger.info("Submitting solution to webhook...");
            SolutionRequest solutionRequest = new SolutionRequest(sqlQuery);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<SolutionRequest> entity = new HttpEntity<>(solutionRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                WEBHOOK_TEST_URL,
                HttpMethod.POST,
                entity,
                String.class
            );

            logger.info("Solution submitted successfully. Status: {}", response.getStatusCode());
            logger.info("Response: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Error submitting solution: ", e);
        }
    }
}
