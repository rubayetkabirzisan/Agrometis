package com.rubaet.agri.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("${app.api.base-path}/weather")
public class WeatherController {

    @Value("${app.weather.api-key}")
    private String apiKey;

    @Value("${app.weather.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public WeatherController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping
    public String getWeather(@RequestParam String city) {
        if ("demo_key".equals(apiKey)) {
            try {
                String mockData = java.nio.file.Files.readString(java.nio.file.Paths.get("../next30days.json"));
                return mockData;
            } catch (Exception e) {
                // Fallback handled below
            }
        }

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/forecast")
                .queryParam("q", city)
                .queryParam("cnt", 50)
                .queryParam("appid", apiKey)
                .encode()
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}
