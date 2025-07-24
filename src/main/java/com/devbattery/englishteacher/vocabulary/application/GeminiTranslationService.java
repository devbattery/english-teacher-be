package com.devbattery.englishteacher.vocabulary.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTranslationService {

    @Value("${gemini.api.key}")
    private String apiKey;
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String translateToKorean(String englishText) {
        // Gemini에게 번역만 하도록 명확하게 지시하는 프롬프트
        String prompt = String.format(
            "Translate the following English phrase to Korean. Provide ONLY the Korean translation and nothing else. Phrase: \"%s\"",
            englishText
        );

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBodyMap = Map.of("contents", List.of(content));

        try {
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String apiUrl = String.format(API_URL_TEMPLATE, apiKey);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            return parseTranslationFromResponse(response.getBody());

        } catch (Exception e) {
            log.error("Error translating text with Gemini: {}", englishText, e);
            return "번역 실패"; // 실패 시 기본값
        }
    }

    private String parseTranslationFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String translatedText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            return translatedText.trim();
        } catch (Exception e) {
            log.error("Error parsing Gemini translation response: {}", jsonResponse, e);
            return "번역 파싱 오류";
        }
    }
}