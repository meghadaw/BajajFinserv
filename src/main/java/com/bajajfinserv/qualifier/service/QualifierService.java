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
        logger.info("Starting qualifier test flow...");

        try {
            // Step 1: Generate webhook
            WebhookResponse webhookResponse = generateWebhook();

            if (webhookResponse != null && webhookResponse.getAccessToken() != null) {
                logger.info("Webhook generated successfully");
                logger.info("Access Token received: {}", webhookResponse.getAccessToken());
                logger.info("Webhook URL: {}", webhookResponse.getWebhook());

                // Step 2: Solve SQL problem (Question 2)
                String sqlSolution = solveSQLProblem();

                // Step 3: Submit solution
                submitSolution(webhookResponse.getAccessToken(), sqlSolution);
            } else {
                logger.error("Failed to generate webhook or received null response");
            }
        } catch (Exception e) {
            logger.error("Error in qualifier flow: ", e);
        }
    }

    private WebhookResponse generateWebhook() {
        try {
            logger.info("Generating webhook for regNo: {}", REG_NO);
            WebhookRequest request = new WebhookRequest(NAME, REG_NO, EMAIL);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                WEBHOOK_GENERATE_URL,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
            );

            logger.info("Webhook generation response status: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error generating webhook: ", e);
            return null;
        }
    }

    private String solveSQLProblem() {
        // SQL solution for Question 2: Calculate younger employees count by department
        String sqlQuery = """
            SELECT 
                e1.EMP_ID,
                e1.FIRST_NAME,
                e1.LAST_NAME,
                d.DEPARTMENT_NAME,
                COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
            FROM EMPLOYEE e1
            JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
            LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT 
                AND e2.DOB > e1.DOB
            GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
            ORDER BY e1.EMP_ID DESC
            """;
        
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
