package com.rubaet.agri.controller;

import com.rubaet.agri.dto.AiRequest;
import com.rubaet.agri.entity.FarmProfile;
import com.rubaet.agri.repository.FarmProfileRepository;
import com.rubaet.agri.repository.UserRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.*;

@RestController
@RequestMapping("${app.api.base-path}/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Value("${app.ai.gemini-api-key}")
    private String apiKey;

    private static final String GENERAL_SYSTEM_INSTRUCTION =
        "You are AgriBot, a highly knowledgeable agricultural AI assistant. " +
        "You specialise in crop management, disease identification, soil science, " +
        "intercropping, weather impact on farming, and best agricultural practices " +
        "for South Asian and tropical climates. " +
        "Always give practical, actionable advice. Keep responses concise and farmer-friendly. " +
        "If asked something outside agriculture, gently redirect to farming topics. " +
        "Do not use markdown formatting — respond in plain, readable paragraphs.";

    private final RestTemplate restTemplate;
    private final FarmProfileRepository farmProfileRepository;
    private final UserRepository userRepository;

    public AiController(FarmProfileRepository farmProfileRepository, UserRepository userRepository) {
        this.restTemplate = new RestTemplate();
        this.farmProfileRepository = farmProfileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/ask")
    @RateLimiter(name = "ai-endpoint", fallbackMethod = "rateLimitFallback")
    public String askAi(@Valid @RequestBody AiRequest request, Authentication auth) {
        String systemInstruction = GENERAL_SYSTEM_INSTRUCTION + buildFarmContext(auth);
        return callGemini(request, systemInstruction);
    }

    @PostMapping("/plan-crop")
    @RateLimiter(name = "ai-endpoint", fallbackMethod = "rateLimitFallback")
    public String planCrop(@Valid @RequestBody AiRequest request, Authentication auth) {
        String agronomistInstruction =
            "You are an expert Agronomist. Recommend two suitable main crops and an intercropping strategy " +
            "based on the provided season, soil, and region. Structure your response clearly with sections. " +
            "Keep responses practical, concise, and farmer-friendly. Do not use markdown asterisks or hashtags.";
        return callGemini(request, agronomistInstruction + buildFarmContext(auth));
    }

    @PostMapping("/identify-disease")
    @RateLimiter(name = "ai-endpoint", fallbackMethod = "rateLimitFallback")
    public String identifyDisease(@Valid @RequestBody AiRequest request, Authentication auth) {
        String pathologistInstruction =
            "You are an expert Plant Pathologist. Identify the most likely disease from the provided symptoms, " +
            "and provide clear treatment and prevention steps. Structure your response clearly with sections. " +
            "Keep responses practical, concise, and farmer-friendly. Do not use markdown asterisks or hashtags.";
        return callGemini(request, pathologistInstruction + buildFarmContext(auth));
    }

    /** Fallback method invoked when rate limit is exceeded. */
    public String rateLimitFallback(AiRequest request, Authentication auth, Exception ex) {
        return "{ \"title\": \"Rate Limit Exceeded\", \"detail\": \"You have sent too many requests. Please wait a minute before trying again.\" }";
    }

    /** Builds a farm context string by resolving the authenticated user's FarmProfile. */
    private String buildFarmContext(Authentication auth) {
        if (auth == null || auth.getName() == null) return "";
        return userRepository.findByEmail(auth.getName())
                .flatMap(user -> farmProfileRepository.findByUserId(user.getId()))
                .map(profile -> String.format(
                    " FARMER CONTEXT: This farmer has %s hectares of %s soil in %s. " +
                    "Farm name: %s. Primary crops: %s.",
                    profile.getAreaHectares(), profile.getSoilType(), profile.getLocation(),
                    profile.getFarmName(), profile.getPrimaryCrops()))
                .orElse("");
    }

    private String callGemini(AiRequest request, String systemInstruction) {
        if ("demo_key".equals(apiKey)) {
            return "{ \"candidates\": [ { \"content\": { \"parts\": [ { \"text\": \"" +
                "This is a mock AI response. Set GEMINI_API_KEY to use real AI." +
                "\" } ] } } ] }";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

        List<Map<String, Object>> contents = new ArrayList<>();

        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (Map<String, String> turn : request.getHistory()) {
                Map<String, Object> contentEntry = new HashMap<>();
                contentEntry.put("role", "user".equals(turn.getOrDefault("role", "user")) ? "user" : "model");
                contentEntry.put("parts", List.of(Map.of("text", turn.getOrDefault("content", ""))));
                contents.add(contentEntry);
            }
        }

        Map<String, Object> currentTurn = new HashMap<>();
        currentTurn.put("role", "user");
        currentTurn.put("parts", List.of(Map.of("text", request.getPrompt())));
        contents.add(currentTurn);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return "{ \"title\": \"Error\", \"detail\": \"AI service is temporarily unavailable. Please try again later.\" }";
        }
    }
}
