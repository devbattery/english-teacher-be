package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.common.config.GeminiPromptProperties;
import com.devbattery.englishteacher.common.exception.GeminiApiException;
import com.devbattery.englishteacher.common.exception.ServerErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class GeminiDailyContentGeneratorService {

    @Value("${gemini.api.key-article}")
    private String apiKey;

    @Value("${gemini.api.template}")
    private String apiTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiPromptProperties promptProperties;

    public String generateDailyContent(String level) {
        String systemPrompt = createPromptForLevel(level);
        String requestBody = createRequestBody(systemPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String apiUrl = String.format(apiTemplate, apiKey);

        try {
            log.info("{} 레벨의 Gemini 컨텐츠 생성", level);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            return parseContentFromResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            throw new GeminiApiException();
        } catch (Exception e) {
            throw new ServerErrorException();
        }
    }

    private String parseContentFromResponse(String fullJsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(fullJsonResponse);
            // JSON 경로: candidates -> [0] -> content -> parts -> [0] -> text
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode.isMissingNode()) {
                log.error("text 필드를 찾을 수 없음: {}", fullJsonResponse);
                // LearningService가 처리할 수 있도록 빈 JSON 객체 문자열을 반환
                return "{}";
            }

            // textNode.asText()는 이미 완벽한 JSON 문자열이므로 그대로 반환
            return textNode.asText();

        } catch (Exception e) {
            log.error("Error parsing the outer structure of Gemini JSON response: {}", fullJsonResponse, e);
            // 파싱 실패 시에도 빈 JSON 객체 문자열을 반환하여 NullPointerException 방지
            return "{}";
        }
    }

    private String createPromptForLevel(String level) {
        String levelDescription = promptProperties.getLevelDescriptions()
                .getOrDefault(level, promptProperties.getLevelDescriptions().get("default"));
        String promptTemplate = promptProperties.getLearning();
        return promptTemplate.replace("{levelDescription}", levelDescription);
    }

    private String createRequestBody(String prompt) {
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
        );
        Map<String, Object> generationConfig = Map.of("response_mime_type", "application/json");

        requestMap.put("contents", contents);
        requestMap.put("generationConfig", generationConfig);

        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            throw new GeminiApiException();
        }
    }

}
