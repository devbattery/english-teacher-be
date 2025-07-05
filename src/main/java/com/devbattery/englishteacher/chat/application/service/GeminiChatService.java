package com.devbattery.englishteacher.chat.application.service;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.common.config.FileStorageProperties;
import com.devbattery.englishteacher.common.config.GeminiPromptProperties;
import com.devbattery.englishteacher.user.application.service.UserReadService;
import com.devbattery.englishteacher.user.domain.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor // 생성자 주입을 위한 Lombok 어노테이션
public class GeminiChatService {

    private final UserReadService userReadService;
    private final GeminiPromptProperties promptProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatConversationRepository chatConversationRepository; // 리포지토리 주입
    private final FileStorageProperties fileStorageProperties;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${url.api}")
    private String apiUrl;

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

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
            case "beginner" -> String.format("Hello, %s!. Let's start slowly. What did you do today?", userName);
            case "intermediate" ->
                    String.format("Hey %s, welcome! Ready to level up your English? What's on your mind?", userName);
            case "advanced" ->
                    String.format("Good to see you, %s. Let's delve into a stimulating discussion. What's our topic?",
                            userName);
            case "ielts" -> "This is the IELTS test simulation. Could you tell me your full name, please?";
            default -> String.format("Hi %s! Let's have a great conversation. What would you like to talk about?",
                    userName);
        };
        // 첫 메시지를 ChatMessage 객체로 만들어 반환
        return new ChatMessage("ai", text, LocalDateTime.now());
    }

    /**
     * [수정] 이미지 파일을 포함할 수 있도록 getChatResponse 메소드 시그니처 변경
     * @param userId 사용자 ID
     * @param level 선생님 레벨
     * @param userMessage 사용자 메시지
     * @param imageFile (Nullable) 사용자가 업로드한 이미지 파일
     * @return AI의 응답
     */
    @Transactional
    public String getChatResponse(Long userId, String level, String userMessage, @Nullable MultipartFile imageFile) {
        AtomicBoolean isFirstConversation = new AtomicBoolean(false);

        ChatConversation conversation = chatConversationRepository
                .fetchByUserIdAndTeacherLevel(userId, level)
                .orElseGet(() -> {
                    isFirstConversation.set(true);
                    return new ChatConversation(userId, level);
                });

        if (isFirstConversation.get()) {
            User user = userReadService.fetchById(userId);
            ChatMessage firstAiMessage = createFirstMessageForLevel(level, user.getName());
            conversation.addMessage(firstAiMessage.getSender(), firstAiMessage.getText());
        }

        String imageUrl = null;
        String imageBase64 = null;
        String imageMimeType = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String savedFilename = storeFile(imageFile);

                // --- [핵심 수정] ---
                // 기존 코드: 상대 경로 생성
                // imageUrl = fileStorageProperties.getUploadUrlPrefix() + savedFilename;

                // 수정된 코드: 절대 경로(Full URL) 생성
                imageUrl = apiUrl + fileStorageProperties.getUploadUrlPrefix() + savedFilename;
                // 결과 예: "https://englishteacher.store/uploads/some-uuid.jpg"
                // -------------------

                imageBase64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
                imageMimeType = imageFile.getContentType();

                conversation.addMessage("user", userMessage, imageUrl);
            } catch (IOException e) {
                log.error("Failed to store or encode image file", e);
                return "Sorry, I had a problem processing your image. Please try again.";
            }
        } else {
            conversation.addMessage("user", userMessage);
        }

        String systemPrompt = createSystemPrompt(level);
        String requestBody = createRequestBodyWithHistory(systemPrompt, conversation.getMessages(), imageBase64,
                imageMimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String apiUrl = String.format(API_URL_TEMPLATE, apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            String aiResponseText = parseResponse(response.getBody());
            conversation.addMessage("ai", aiResponseText);
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

    /**
     * [신규] MultipartFile을 서버에 저장하는 메소드
     * @param file 업로드된 파일
     * @return 서버에 저장된 파일 이름
     * @throws IOException 파일 저장 실패 시
     */
    private String storeFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // 보안을 위해 파일 이름에 UUID 추가하여 중복 및 잠재적 공격 방지
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = Paths.get(fileStorageProperties.getUploadDir()).resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return storedFilename;
    }

    // ... createSystemPrompt, parseResponse 메소드는 기존과 동일 ...
    private String createSystemPrompt(String level) {
        return promptProperties.getChat().getOrDefault(
                level, promptProperties.getChat().get("default")
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

    /**
     * [수정] Gemini API 요청 본문을 생성. 이미지(멀티모달) 데이터를 포함할 수 있도록 변경.
     * @param systemPrompt 시스템 지시사항
     * @param messages 전체 메시지 목록
     * @param imageBase64 (Nullable) Base64로 인코딩된 이미지 데이터
     * @param imageMimeType (Nullable) 이미지의 MIME 타입 (e.g., "image/jpeg")
     * @return JSON 형식의 문자열
     */
    private String createRequestBodyWithHistory(String systemPrompt, List<ChatMessage> messages,
                                                @Nullable String imageBase64, @Nullable String imageMimeType) {
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt))));
        contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", "Okay, I'm ready..."))));

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            String role = "ai".equalsIgnoreCase(message.getSender()) ? "model" : "user";

            // [핵심] 마지막 사용자 메시지이고, 이미지가 있는 경우 멀티모달 형식으로 구성
            if (i == messages.size() - 1 && "user".equals(role) && imageBase64 != null) {
                List<Map<String, Object>> parts = new ArrayList<>();
                // 1. 이미지 데이터 추가
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", imageMimeType,
                                "data", imageBase64
                        )
                ));
                // 2. 텍스트 데이터 추가
                if (message.getText() != null && !message.getText().isBlank()) {
                    parts.add(Map.of("text", message.getText()));
                }
                contents.add(Map.of("role", role, "parts", parts));
            } else {
                // 이미지가 없는 일반 메시지
                // TODO: 나중에 대화 기록에 있는 이미지도 다시 보여주려면 이 부분 수정 필요
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", message.getText()))));
            }
        }

        requestMap.put("contents", contents);

        try {
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            log.error("Error creating request body for Gemini API", e);
            throw new RuntimeException("Error creating request body", e);
        }
    }

    /**
     * [신규] 특정 선생님과의 대화 기록을 초기화(삭제)합니다.
     * @param userId 사용자 ID
     * @param level  선생님 레벨
     */
    @Transactional
    public void resetChatHistory(Long userId, String level) {
        chatConversationRepository.deleteByUserIdAndTeacherLevel(userId, level);
        log.info("Chat history for user {} with teacher level '{}' has been reset.", userId, level);
    }

}