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

    @Value("${gemini.api.key-translation}")
    private String apiKey;

    @Value("${gemini.api.template}")
    private String apiTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String translateToKorean(String englishText) {
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
            String apiUrl = String.format(apiTemplate, apiKey);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return parseTranslationFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("{} 텍스트의 번역 실패", englishText, e);
            return "번역 실패"; // 실패 시 기본값
        }
    }

    private String parseTranslationFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String translatedText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
                    .asText();
            return translatedText.trim();
        } catch (Exception e) {
            log.error("{} 응답의 파싱 오류", jsonResponse, e);
            return "번역 파싱 오류";
        }
    }

}