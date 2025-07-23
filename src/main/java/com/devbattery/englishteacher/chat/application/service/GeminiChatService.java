package com.devbattery.englishteacher.chat.application.service;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.chat.presentation.dto.ChatResponse;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRoomSummaryResponse;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
@RequiredArgsConstructor
public class GeminiChatService {

    private final UserReadService userReadService;
    private final GeminiPromptProperties promptProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatConversationRepository chatConversationRepository;
    private final FileStorageProperties fileStorageProperties;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${url.api}")
    private String apiUrl;

    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";
    private static final int MAX_CHAT_ROOMS_PER_LEVEL = 10; // 레벨당 채팅방 생성 제한

    /**
     * [신규] 특정 레벨에 속한 사용자의 모든 채팅방 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param level  선생님 레벨
     * @return 채팅방 요약 정보 리스트 (최신순 정렬)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getConversationListByLevel(Long userId, String level) {
        return chatConversationRepository.findAllByUserIdAndTeacherLevel(userId, level)
                .stream()
                // 최근에 대화한 방이 위로 오도록 정렬
                .sorted(Comparator.comparing(ChatConversation::getLastModifiedAt).reversed())
                .map(ChatRoomSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * [수정] 특정 채팅방의 대화 기록을 조회합니다.
     * @param userId         사용자 ID (소유권 확인용)
     * @param conversationId 조회할 채팅방의 고유 ID
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationHistory(Long userId, String conversationId) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + conversationId));

        // 해당 채팅방이 요청한 사용자의 소유인지 확인 (보안)
        if (!conversation.getUserId().equals(userId)) {
            throw new SecurityException("User does not have permission to access this chat room.");
        }

        // 채팅방이 비어있다면, 첫 AI 메시지를 생성해서 보여줍니다.
        if (conversation.getMessages().isEmpty()) {
            User user = userReadService.fetchById(userId);
            ChatMessage firstMessage = createFirstMessageForLevel(conversation.getTeacherLevel(), user.getName());
            return List.of(firstMessage);
        }

        return conversation.getMessages();
    }

    /**
     * [수정] 채팅 응답을 생성하고 대화를 저장합니다.
     * conversationId가 없으면 새 채팅방을 생성하고, 있으면 기존 채팅방에 메시지를 추가합니다.
     *
     * @param userId         사용자 ID
     * @param level          선생님 레벨 (새 채팅방 생성 시 필요)
     * @param conversationId (Nullable) 기존 채팅방의 ID
     * @param userMessage    사용자 메시지
     * @param imageFile      (Nullable) 사용자가 업로드한 이미지 파일
     * @return AI의 응답과 채팅방 ID가 포함된 ChatResponse 객체
     */
    @Transactional
    public ChatResponse getChatResponse(Long userId, String level, @Nullable String conversationId, String userMessage, @Nullable MultipartFile imageFile) {
        ChatConversation conversation;
        boolean isFirstMessageInRoom = false;

        // 1. 대화 객체 가져오기 (기존 또는 신규)
        if (conversationId == null || conversationId.isBlank()) {
            // [신규 채팅방 생성 로직]
            long roomCount = chatConversationRepository.countByUserIdAndTeacherLevel(userId, level);
            if (roomCount >= MAX_CHAT_ROOMS_PER_LEVEL) {
                throw new IllegalStateException("You have reached the maximum number of " + MAX_CHAT_ROOMS_PER_LEVEL + " chat rooms for the " + level + " level.");
            }
            conversation = new ChatConversation(userId, level);
            isFirstMessageInRoom = true;
        } else {
            // [기존 채팅방 조회 로직]
            conversation = chatConversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + conversationId));

            if (!conversation.getUserId().equals(userId)) {
                throw new SecurityException("User does not have permission to access this chat room.");
            }
        }

        // 2. 첫 AI 인사 메시지 추가 (필요 시)
        if (isFirstMessageInRoom || conversation.getMessages().isEmpty()) {
            User user = userReadService.fetchById(userId);
            ChatMessage firstAiMessage = createFirstMessageForLevel(level, user.getName());
            conversation.addMessage(firstAiMessage.getSender(), firstAiMessage.getText());
        }

        // 3. 사용자 메시지 추가 (이미지 포함)
        String imageBase64 = null;
        String imageMimeType = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String savedFilename = storeFile(imageFile);
                String imageUrl = apiUrl + fileStorageProperties.getUploadUrlPrefix() + savedFilename;
                imageBase64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
                imageMimeType = imageFile.getContentType();
                conversation.addMessage("user", userMessage, imageUrl);
            } catch (IOException e) {
                log.error("Failed to store or encode image file", e);
                throw new RuntimeException("Sorry, I had a problem processing your image. Please try again.");
            }
        } else {
            conversation.addMessage("user", userMessage);
        }

        // 4. Gemini API 요청 및 응답 처리
        String systemPrompt = createSystemPrompt(level);
        String requestBody = createRequestBodyWithHistory(systemPrompt, conversation.getMessages(), imageBase64, imageMimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String fullApiUrl = String.format(API_URL_TEMPLATE, apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullApiUrl, entity, String.class);
            String aiResponseText = parseResponse(response.getBody());

            // 5. AI 응답 메시지 추가 및 대화 저장
            conversation.addMessage("ai", aiResponseText);
            chatConversationRepository.save(conversation);

            // 6. 클라이언트에 응답 전달
            return new ChatResponse(aiResponseText, conversation.getId());

        } catch (HttpClientErrorException e) {
            log.error("Gemini API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Sorry, I encountered an error with the AI service. Please try again.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing", e);
            throw new RuntimeException("An unexpected error occurred. Please check the server logs.");
        }
    }

    /**
     * [수정] 특정 채팅방을 삭제합니다.
     * @param userId         사용자 ID (소유권 확인용)
     * @param conversationId 삭제할 채팅방 ID
     */
    @Transactional
    public void deleteConversation(Long userId, String conversationId) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new SecurityException("User does not have permission to delete this chat room.");
        }

        chatConversationRepository.deleteById(conversationId);
        log.info("Chat room with id '{}' for user {} has been deleted.", conversationId, userId);
    }

    // --- 아래는 변경되지 않은 private 헬퍼 메소드들 ---

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
        return new ChatMessage("ai", text, LocalDateTime.now());
    }

    private String storeFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = Paths.get(fileStorageProperties.getUploadDir()).resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return storedFilename;
    }

    private String createSystemPrompt(String level) {
        return promptProperties.getChat().getOrDefault(
                level, promptProperties.getChat().get("default")
        );
    }

    private String parseResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            if (textNode.isMissingNode()) {
                log.error("Could not find 'text' field in Gemini response: {}", jsonResponse);
                return "I'm sorry, I couldn't generate a proper response. The structure of the AI's reply was unexpected.";
            }
            return textNode.asText();
        } catch (Exception e) {
            log.error("Error parsing Gemini JSON response: {}", jsonResponse, e);
            return "There was an issue processing the AI's response.";
        }
    }

    private String createRequestBodyWithHistory(String systemPrompt, List<ChatMessage> messages,
                                                @Nullable String imageBase64, @Nullable String imageMimeType) {
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", systemPrompt))));
        contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", "Okay, I'm ready..."))));

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            String role = "ai".equalsIgnoreCase(message.getSender()) ? "model" : "user";

            if (i == messages.size() - 1 && "user".equals(role) && imageBase64 != null) {
                List<Map<String, Object>> parts = new ArrayList<>();
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", imageMimeType,
                                "data", imageBase64
                        )
                ));
                if (message.getText() != null && !message.getText().isBlank()) {
                    parts.add(Map.of("text", message.getText()));
                }
                contents.add(Map.of("role", role, "parts", parts));
            } else {
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
}