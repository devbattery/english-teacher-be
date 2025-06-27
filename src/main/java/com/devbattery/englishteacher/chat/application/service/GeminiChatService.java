// src/main/java/com/devbattery/englishteacher/chat/application/service/GeminiChatService.java
package com.devbattery.englishteacher.chat.application.service;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.user.application.service.UserReadService;
import com.devbattery.englishteacher.user.domain.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class GeminiChatService {

    private final UserReadService userReadService;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatConversationRepository chatConversationRepository; // 리포지토리 주입

    /**
     * [신규] 사용자의 이전 대화 기록을 불러옵니다.
     * @param userId      사용자 ID
     * @param level       선생님 레벨
     * @return 메시지 목록
     */
    public List<ChatMessage> getConversationHistory(Long userId, String level) {
        return chatConversationRepository.fetchByUserIdAndTeacherLevel(userId, level)
                .map(ChatConversation::getMessages)
                .orElseGet(() -> {
                    User user = userReadService.fetchById(userId);
                    ChatMessage firstMessage = createFirstMessageForLevel(level, user.getName());
                    return List.of(firstMessage);
                });
    }

    private ChatMessage createFirstMessageForLevel(String level, String userName) {
        String text = switch (level) {
            case "beginner" ->
                    String.format("Hello, %s!. Let's start slowly. What did you do today?", userName);
            case "intermediate" ->
                    String.format("Hey %s, welcome! Ready to level up your English? What's on your mind?", userName);
            case "advanced" ->
                    String.format("Good to see you, %s. Let's delve into a stimulating discussion. What's our topic?",
                            userName);
            case "ielts" ->
                    "This is the IELTS speaking test simulation. Could you tell me your full name, please?";
            default -> String.format("Hi %s! Let's have a great conversation. What would you like to talk about?",
                    userName);
        };
        // 첫 메시지를 ChatMessage 객체로 만들어 반환
        return new ChatMessage("ai", text, LocalDateTime.now());
    }

    /**
     * [수정] 사용자의 메시지를 받아 DB에 저장하고, 전체 대화 맥락을 포함하여 Gemini API에 응답을 요청합니다.
     * @param userId      사용자 ID
     * @param level       선생님 레벨
     * @param userMessage 사용자 메시지
     * @return Gemini API가 생성한 응답 텍스트
     */
    @Transactional // DB 저장과 API 호출을 하나의 트랜잭션으로 묶음
    public String getChatResponse(Long userId, String level, String userMessage) {
        // 1. 사용자 ID와 레벨로 기존 대화내역을 찾거나 새로 생성
        ChatConversation conversation = chatConversationRepository
                .fetchByUserIdAndTeacherLevel(userId, level)
                .orElseGet(() -> new ChatConversation(userId, level));

        // 2. 현재 사용자의 메시지를 대화에 추가
        conversation.addMessage("user", userMessage);

        // 3. API 요청 본문 생성 (시스템 프롬프트 + 전체 대화 기록)
        String systemPrompt = createSystemPrompt(level);
        String requestBody = createRequestBodyWithHistory(systemPrompt, conversation.getMessages());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String apiUrl = String.format(API_URL_TEMPLATE, apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String aiResponseText = parseResponse(response.getBody());

            // 4. AI의 응답을 대화에 추가
            conversation.addMessage("ai", aiResponseText);

            // 5. 업데이트된 대화 내용을 DB에 저장
            chatConversationRepository.save(conversation);

            return aiResponseText;

        } catch (HttpClientErrorException e) {
            log.error("Gemini API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Sorry, I encountered an error. Please try again.";
        } catch (Exception e) {
            log.error("An unexpected error occurred", e);
            return "An unexpected error occurred. Please check the server logs.";
        }
    }

    // ... createSystemPrompt, parseResponse 메소드는 기존과 동일 ...
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

    /**
     * [수정] 전체 대화 기록을 포함하여 Gemini API 요청 본문을 생성합니다.
     * @param systemPrompt 시스템 지시사항
     * @param messages     전체 메시지 목록
     * @return JSON 형식의 문자열
     */
    private String createRequestBodyWithHistory(String systemPrompt, List<ChatMessage> messages) {
        Map<String, Object> requestMap = new HashMap<>();

        List<Map<String, Object>> contents = new ArrayList<>();

        // 1. 시스템 프롬프트(역할 부여)를 첫 번째 'user' 메시지로 추가
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt))));
        // 2. 모델이 역할을 인지했다는 가상의 응답 추가
        contents.add(Map.of("role", "model", "parts",
                List.of(Map.of("text", "Okay, I'm ready. I will act as the specified English teacher."))));

        // 3. 실제 대화 기록을 'user'와 'model' 역할에 맞게 추가
        for (ChatMessage message : messages) {
            // 프론트엔드에서 사용하는 'ai'를 API가 이해하는 'model'로 변환
            String role = "ai".equalsIgnoreCase(message.getSender()) ? "model" : "user";
            contents.add(Map.of("role", role, "parts", List.of(Map.of("text", message.getText()))));
        }

        requestMap.put("contents", contents);

        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            log.error("Error creating request body for Gemini API", e);
            throw new RuntimeException("Error creating request body for Gemini API", e);
        }
    }

}