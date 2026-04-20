package com.example.webhook_app1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            
            // STEP 1: Generate Webhook
           
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> body = new HashMap<>();
            body.put("name", "Meghan Bhonsle");
            body.put("regNo", "ADT24SOCBD070");
            body.put("email", "meghanbhonsle0@gmail.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map responseData = response.getBody();

            String webhookUrl = (String) responseData.get("webhook");
            String accessToken = (String) responseData.get("accessToken");

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            
            // STEP 2: Determine Question
            
            String regNo = body.get("regNo");

            String digits = regNo.replaceAll("\\D", "");

            if (digits.length() < 2) {
                throw new RuntimeException("Invalid regNo: " + regNo);
            }

            int lastTwo = Integer.parseInt(
                    digits.substring(digits.length() - 2)
            );

            boolean isOdd = lastTwo % 2 != 0;

            System.out.println("Last Two Digits: " + lastTwo);
            System.out.println("Is Odd: " + isOdd);

            
            // STEP 3: FINAL SQL QUERY
           
            String finalQuery;

            if (isOdd) {
                // Not my case
                finalQuery = "SELECT * FROM table1;";
            } else {
                // My Case (EVEN → Question 2)
                finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC;";
            }

            System.out.println("Final SQL Query: " + finalQuery);

            
            // STEP 4: Submit Answer
            
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);

            
            submitHeaders.set("Authorization", accessToken);

            Map<String, String> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> submitRequest =
                    new HttpEntity<>(submitBody, submitHeaders);

            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

            System.out.println("Final Response: " + submitResponse.getBody());

        } catch (Exception e) {
            System.out.println("ERROR OCCURRED:");
            e.printStackTrace();
        }
    }
}