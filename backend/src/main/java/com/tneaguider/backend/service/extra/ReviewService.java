package com.tneaguider.backend.service.extra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.InetAddress;
import java.util.*;

@Service
public class ReviewService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public ReviewService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    private boolean checkInternetConnectivity() {
        try {
            InetAddress.getByName("generativelanguage.googleapis.com");
            System.out.println("Internet connectivity: OK");
            return true;
        } catch (Exception e) {
            System.err.println("Internet connectivity check failed: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getMultiReviews(String collegeName, String district) {
        System.out.println("=== GEMINI MULTI-REVIEW API DEBUG ===");
        System.out.println("Calling Gemini with: " + collegeName + " in " + district);
        
        if (!checkInternetConnectivity()) {
            System.err.println("No internet - using fallback");
            return getFallbackReviews(collegeName);
        }
        
        try {
            String prompt = "Generate 6 unique student reviews for " + collegeName + " engineering college in " 
                + (district != null ? district : "Tamil Nadu") + ".\n\n"
                + "Each review must be DIFFERENT and realistic, like real Google reviews.\n"
                + "Return ONLY a valid JSON array in this exact format:\n"
                + "[{\"name\":\"Student Name\",\"rating\":4,\"review\":\"Review text here\"},...]\n\n"
                + "Requirements:\n"
                + "- 6 reviews only\n"
                + "- Rating must be integer 1-5\n"
                + "- Use realistic Indian names\n"
                + "- Each review should mention different aspects (placements, faculty, infrastructure, campus life)\n"
                + "- NO additional text, ONLY JSON array";

            String jsonBody = buildGeminiRequestMulti(prompt);
            
            System.out.println("Request Body: " + jsonBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            System.out.println("Calling Gemini...");
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Raw Response: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String reviewText = parseGeminiResponse(response.getBody());
                System.out.println("Parsed text: " + reviewText);
                
                if (reviewText != null && !reviewText.isEmpty()) {
                    List<Map<String, Object>> reviews = parseReviewsJson(reviewText);
                    System.out.println("Parsed Reviews count: " + reviews.size());
                    
                    if (reviews.isEmpty()) {
                        System.err.println("No reviews parsed, using fallback");
                        return getFallbackReviews(collegeName);
                    }
                    
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("college", collegeName);
                    result.put("reviews", reviews);
                    return result;
                }
            }
        } catch (RestClientException e) {
            System.err.println("RestClientException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error generating reviews: " + e.getMessage());
            e.printStackTrace();
        }

        System.err.println("Using fallback response");
        return getFallbackReviews(collegeName);
    }

    private String buildGeminiRequestMulti(String prompt) {
        return String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}],\"generationConfig\":{\"temperature\":1.0,\"topP\":0.95,\"topK\":40,\"maxOutputTokens\":1000}}",
            prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );
    }

    private List<Map<String, Object>> parseReviewsJson(String response) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        
        try {
            response = response.trim();
            if (response.startsWith("```json")) {
                response = response.substring(7);
            }
            if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();
            
            if (response.startsWith("[")) {
                JsonNode jsonArray = mapper.readTree(response);
                if (jsonArray.isArray()) {
                    for (JsonNode node : jsonArray) {
                        Map<String, Object> review = new LinkedHashMap<>();
                        review.put("name", node.path("name").asText("Anonymous"));
                        review.put("rating", node.path("rating").asInt(3));
                        review.put("review", node.path("review").asText("Good college."));
                        reviews.add(review);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JSON parse error: " + e.getMessage() + ", trying line parsing");
        }
        
        if (reviews.isEmpty()) {
            reviews = parseReviewsFromText(response);
        }
        
        return reviews;
    }

    private List<Map<String, Object>> parseReviewsFromText(String response) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            line = line.replaceAll("^\\d+[\\.\\)]\\s*", "");
            
            String[] parts = line.split("\\|", 3);
            
            if (parts.length >= 2) {
                String name = parts[0].trim();
                String ratingStr = parts[1].trim().replaceAll("[^0-9]", "");
                String review = parts.length > 2 ? parts[2].trim() : "Good college.";
                
                int rating = 3;
                try {
                    rating = Integer.parseInt(ratingStr);
                    if (rating < 1 || rating > 5) rating = 3;
                } catch (NumberFormatException e) {
                    rating = 3;
                }
                
                Map<String, Object> reviewMap = new LinkedHashMap<>();
                reviewMap.put("name", name);
                reviewMap.put("rating", rating);
                reviewMap.put("review", review);
                reviews.add(reviewMap);
            }
        }
        
        return reviews;
    }

    private Map<String, Object> getFallbackReviews(String collegeName) {
        List<Map<String, Object>> fallbackReviews = new ArrayList<>();
        
        Map<String, Object> r1 = new LinkedHashMap<>();
        r1.put("name", "Rahul Kumar");
        r1.put("rating", 4);
        r1.put("review", "Good placements and decent infrastructure. Faculty is supportive.");
        fallbackReviews.add(r1);
        
        Map<String, Object> r2 = new LinkedHashMap<>();
        r2.put("name", "Priya Sharma");
        r2.put("rating", 5);
        r2.put("review", "Excellent faculty and great placement opportunities!");
        fallbackReviews.add(r2);
        
        Map<String, Object> r3 = new LinkedHashMap<>();
        r3.put("name", "Arun Venkatesh");
        r3.put("rating", 3);
        r3.put("review", "Average college but good for placements. Infrastructure could be better.");
        fallbackReviews.add(r3);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("college", collegeName);
        result.put("reviews", fallbackReviews);
        return result;
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            
            if (root.has("error")) {
                JsonNode error = root.path("error");
                String errorMessage = error.path("message").asText();
                System.err.println("Gemini API Error: " + errorMessage);
                return null;
            }
            
            JsonNode candidates = root.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            System.err.println("Response structure unexpected. Full response: " + responseBody);
        } catch (Exception e) {
            System.err.println("JSON Parse Error: " + e.getMessage());
        }
        return null;
    }

    public Map<String, String> getWhyThisCollege(String collegeName, String cutoff, String category) {
        System.out.println("=== GEMINI WHY API DEBUG ===");
        
        if (!checkInternetConnectivity()) {
            return getFallbackWhy(collegeName, cutoff, category);
        }
        
        try {
            String userInfo = "";
            if (cutoff != null && !cutoff.isEmpty()) {
                userInfo += " with cutoff " + cutoff;
            }
            if (category != null && !category.isEmpty()) {
                userInfo += ", category " + category;
            }

            String prompt = "Act as a helpful education counselor.\n"
                + "Explain why " + collegeName + " is suitable for a student" + userInfo + " in Tamil Nadu.\n"
                + "Focus on placement records, fee structure, reputation, and why this college is a good fit.\n"
                + "Keep it under 80 words, concise and helpful.\n";

            String jsonBody = buildGeminiRequestMulti(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String url = GEMINI_API_URL + "?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("Why Response Status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String reasonText = parseGeminiResponse(response.getBody());
                if (reasonText != null && !reasonText.isEmpty()) {
                    return Map.of("college", collegeName, "reason", reasonText);
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating reason: " + e.getMessage());
        }

        return getFallbackWhy(collegeName, cutoff, category);
    }

    private Map<String, String> getFallbackWhy(String collegeName, String cutoff, String category) {
        String reason = collegeName + " is recommended because it offers good placement records, quality education, and fits well within the " + (category != null ? category : "BC") + " category cutoff range. The college has strong industry connections and modern infrastructure.";
        return Map.of("college", collegeName, "reason", reason);
    }
}