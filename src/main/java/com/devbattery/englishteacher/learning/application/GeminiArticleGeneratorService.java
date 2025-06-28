package com.devbattery.englishteacher.learning.application;

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
public class GeminiArticleGeneratorService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * [수정됨] 이제 이 메소드는 API 응답에서 핵심 JSON 문자열만 추출하여 반환합니다.
     */
    public String generateArticleJson(String level) {
        String systemPrompt = createPromptForLevel(level);
        String requestBody = createRequestBody(systemPrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String apiUrl = String.format(API_URL_TEMPLATE, apiKey);

        try {
            log.info("Requesting learning article from Gemini for level: {}", level);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            // [핵심 변경] 응답 본문 전체가 아닌, 내부 텍스트만 파싱해서 반환하도록 변경
            return parseContentFromResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Gemini API Error for learning article - Status: {}, Body: {}", e.getStatusCode(),
                    e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to generate learning article from Gemini API.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while generating learning article", e);
            throw new RuntimeException("An unexpected error occurred while communicating with AI.", e);
        }
    }

    /**
     * [신규] API의 전체 JSON 응답에서 우리가 필요한 내부 JSON 문자열만 추출하는 파서 메소드입니다.
     */
    private String parseContentFromResponse(String fullJsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(fullJsonResponse);
            // JSON 경로: candidates -> [0] -> content -> parts -> [0] -> text
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode.isMissingNode()) {
                log.error("Could not find 'text' field in Gemini response: {}", fullJsonResponse);
                // LearningService가 처리할 수 있도록 빈 JSON 객체 문자열을 반환
                return "{}";
            }

            // textNode.asText()는 이미 완벽한 JSON 문자열이므로 그대로 반환합니다.
            return textNode.asText();

        } catch (Exception e) {
            log.error("Error parsing the outer structure of Gemini JSON response: {}", fullJsonResponse, e);
            // 파싱 실패 시에도 빈 JSON 객체 문자열을 반환하여 NullPointerException을 방지
            return "{}";
        }
    }

    // --- 아래 메소드들은 변경할 필요 없습니다 ---

    private String createPromptForLevel(String level) {
        String levelDescription = switch (level) {
            // ... (levelDescription 생성 부분은 동일) ...
            case "beginner" -> "for absolute beginners. Use very simple vocabulary and short sentences.";
            case "intermediate" -> "for intermediate learners. Use common idioms and slightly more complex sentences.";
            case "advanced" -> "for advanced learners. Use sophisticated vocabulary and complex sentence structures.";
            case "ielts" -> "for students preparing for the IELTS test. Focus on a topic relevant to the speaking or writing test, using academic vocabulary.";
            default -> "for general English learners.";
        };

        return "You are an expert English teacher creating learning materials. " +
                "Your task is to write a short English article " + levelDescription + ". " +
                // [핵심 수정] "3개" 라는 제한을 없애고 "모든 관련 표현"을 요청합니다.
                "After writing the article, you MUST identify all relevant key expressions from the article that would be helpful for the learner. The number of expressions can vary depending on the article's content. " +
                "Your entire response MUST be a single, valid JSON object. Do not add any text outside of the JSON object. " +
                "The JSON object must have exactly three keys: " +
                "1. 'title': a string for the article's title. " +
                "2. 'content': a string for the full article content. " +
                // [핵심 수정] keyExpressions에 대한 설명은 동일하게 유지합니다.
                "3. 'keyExpressions': an array of JSON objects. Each object in the array must have two keys: 'expression' (the English phrase) and 'meaning' (its Korean translation). If there are no key expressions, return an empty array [].";
    }

    private String createRequestBody(String prompt) {
        Map<String, Object> requestMap = new HashMap<>();

        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
        );
        requestMap.put("contents", contents);

        Map<String, Object> generationConfig = Map.of("response_mime_type", "application/json");
        requestMap.put("generationConfig", generationConfig);

        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            log.error("Error creating request body for Gemini API", e);
            throw new RuntimeException("Error creating request body for AI API", e);
        }
    }

}
