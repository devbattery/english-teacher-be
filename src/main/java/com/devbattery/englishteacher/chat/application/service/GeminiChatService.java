package com.devbattery.englishteacher.chat.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j // Lombok 어노테이션을 통해 'log' 변수가 자동으로 생성됩니다.
public class GeminiChatService {

    // 1. application.yml에서 API 키 주입
    @Value("${gemini.api.key}")
    private String apiKey;

    // 2. Gemini API 엔드포인트 URL 정의 (gemini-pro 모델 사용)
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

    // 3. 외부 API와 통신하기 위한 RestTemplate
    private final RestTemplate restTemplate;

    // 4. JSON 파싱을 위한 ObjectMapper
    private final ObjectMapper objectMapper;

    // 5. 생성자를 통한 의존성 주입 (RestTemplate, ObjectMapper)
    public GeminiChatService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 사용자의 메시지와 선택된 레벨을 기반으로 Gemini API에 응답을 요청하고 결과를 반환합니다.
     * @param level       프론트엔드에서 선택한 선생님 레벨 (e.g., "beginner", "intermediate")
     * @param userMessage 사용자가 입력한 메시지
     * @return Gemini API가 생성한 응답 텍스트
     */
    public String getChatResponse(String level, String userMessage) {
        // 6. 선생님 레벨에 맞는 시스템 프롬프트(지시사항) 생성
        String systemPrompt = createSystemPrompt(level);

        // 7. Gemini API가 요구하는 JSON 형식에 맞춰 요청 본문 생성
        String requestBody = createRequestBody(systemPrompt, userMessage);

        // 8. HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 9. HTTP 요청 객체(HttpEntity) 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String apiUrl = String.format(API_URL_TEMPLATE, apiKey);

        try {
            // 10. RestTemplate을 사용하여 Gemini API에 POST 요청 전송
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            // 11. API 응답(JSON)에서 실제 텍스트 내용만 추출하여 반환
            return parseResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            // 12. API 요청 중 에러 발생 시 처리 (log 사용으로 변경)
            log.error("Gemini API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Sorry, I encountered an error while contacting my brain. Please try again.";
        } catch (Exception e) {
            // 13. 기타 예외 발생 시 처리 (log 사용으로 변경)
            log.error("An unexpected error occurred while communicating with Gemini API", e);
            return "An unexpected error occurred. Please check the server logs.";
        }
    }

    /**
     * 선생님 레벨에 따라 Gemini에게 역할과 행동 지침을 부여하는 시스템 프롬프트를 생성합니다.
     * 이것이 "프롬프트 엔지니어링"의 핵심입니다.
     * @param level 선생님 레벨
     * @return Gemini에게 전달될 지시사항 텍스트
     */
    private String createSystemPrompt(String level) {
        return switch (level) {
            case "beginner" -> "You are a friendly and extremely patient English teacher for absolute beginners. " +
                    "Your name is 'Tutorbot'. Use very simple words, short sentences, and basic grammar. " +
                    "If the user makes a mistake, gently correct it in a simple way and always be encouraging. " +
                    "For example, if a user says 'I go to school yesterday', you can say 'Great try! A better way to say that is: I *went* to school yesterday. We use 'went' for the past.'";
            case "intermediate" -> "You are a helpful and engaging English coach for intermediate learners. " +
                    "Talk to the user like a friend. Focus on improving their fluency and introducing more natural-sounding expressions. "
                    +
                    "When you correct them, suggest better vocabulary or alternative sentence structures, and briefly explain the nuance. "
                    +
                    "For example, instead of just correcting grammar, if a user says 'I am happy', you could suggest 'That's great! You could also say 'I'm thrilled' or 'I'm over the moon' to sound more expressive.'";
            case "advanced" ->
                    "You are an expert English tutor and a sophisticated conversation partner for advanced learners. " +
                            "Engage in deep and complex conversations on various topics. Use a rich range of vocabulary, idioms, and complex sentence structures. "
                            +
                            "Feel free to challenge the user with thought-provoking questions and provide feedback on their style, tone, and rhetorical effectiveness as if you were a university professor.";
            case "ielts" ->
                    "You are a professional and strict IELTS speaking test examiner. Your goal is to simulate a real IELTS test. "
                            +
                            "Start by saying 'This is the speaking part of the International English Language Testing System. My name is [Your Examiner Name]. Can you tell me your full name, please?'. "
                            +
                            "Then, ask questions typical of Part 1, 2, and 3. Be formal. After the user's response to a full part, provide a band score estimate and detailed, critical feedback on Fluency and Coherence, Lexical Resource, Grammatical Range and Accuracy, and Pronunciation.";
            default -> "You are a general English conversation partner. Be friendly and natural.";
        };
    }

    /**
     * Gemini API의 generateContent 메서드가 요구하는 형식에 맞춰 JSON 요청 본문을 생성합니다.
     * @param systemPrompt 시스템 지시사항
     * @param userMessage 사용자의 현재 메시지
     * @return JSON 형식의 문자열
     */
    private String createRequestBody(String systemPrompt, String userMessage) {
        // Gemini는 역할(role) 기반의 대화 형식을 잘 이해합니다.
        // 시스템 프롬프트는 첫 번째 메시지로, 사용자 메시지는 그 다음 메시지로 구성합니다.
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, Object>> contents = getContents(systemPrompt, userMessage);
        requestMap.put("contents", contents);

        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            // 런타임 예외를 발생시키기 전에 로그를 남기는 것이 좋습니다.
            log.error("Error creating request body for Gemini API", e);
            throw new RuntimeException("Error creating request body for Gemini API", e);
        }
    }

    private static List<Map<String, Object>> getContents(String systemPrompt, String userMessage) {
        Map<String, String> systemPart = Map.of("text", systemPrompt);
        Map<String, String> userPart = Map.of("text",
                "Okay, I understand my role. Now, here is the user's first message:\n\n" + userMessage);

        // 모델이 역할을 인지했다는 가상의 응답
        return List.of(
                Map.of("role", "user", "parts", List.of(systemPart)),
                Map.of("role", "model", "parts", List.of(Map.of("text", "Okay, I'm ready."))), // 모델이 역할을 인지했다는 가상의 응답
                Map.of("role", "user", "parts", List.of(userPart))
        );
    }

    /**
     * Gemini API의 JSON 응답에서 모델이 생성한 텍스트 부분만 파싱하여 추출합니다.
     * @param jsonResponse Gemini API로부터 받은 전체 JSON 응답 문자열
     * @return 추출된 응답 텍스트
     */
    private String parseResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            // JSON 경로: candidates -> [0] -> content -> parts -> [0] -> text
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            if (textNode.isMissingNode()) {
                // 어떤 응답에서 텍스트를 찾지 못했는지 로그로 기록 (log 사용으로 변경)
                log.error("Could not find 'text' field in Gemini response: {}", jsonResponse);
                return "I'm sorry, I couldn't generate a proper response. The structure of the AI's reply was unexpected.";
            }
            return textNode.asText();
        } catch (Exception e) {
            // JSON 파싱 에러와 원본 JSON을 로그로 기록 (log 사용으로 변경)
            log.error("Error parsing Gemini JSON response: {}", jsonResponse, e);
            return "There was an issue processing the AI's response.";
        }
    }

}