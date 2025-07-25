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

    @Value("${gemini.api.key-chat}")
    private String apiKey;

    @Value("${url.api}")
    private String apiUrl;

    @Value("${gemini.api.template}")
    private String apiTemplate;

    private static final int MAX_CHAT_ROOMS_PER_LEVEL = 10;

    /**
     * [수정] 새로운 채팅방을 생성할 때, 첫 AI 인사말을 포함하여 생성합니다.
     * @param userId 사용자 ID
     * @param level  생성할 선생님 레벨
     * @return 생성된 채팅방의 요약 정보 DTO
     */
    @Transactional
    public ChatRoomSummaryResponse createChatRoom(Long userId, String level) {
        long roomCount = chatConversationRepository.countByUserIdAndTeacherLevel(userId, level);
        if (roomCount >= MAX_CHAT_ROOMS_PER_LEVEL) {
            throw new IllegalStateException(
                    "You have reached the maximum number of chat rooms for the " + level + " level.");
        }

        // [핵심 수정] 빈 대화가 아닌, 첫 인사말이 포함된 대화 객체를 생성합니다.
        ChatConversation conversation = new ChatConversation(userId, level);

        // 사용자 정보를 가져와서 인사말에 이름을 포함시킵니다.
        User user = userReadService.fetchById(userId);
        ChatMessage firstAiMessage = createFirstMessageForLevel(level, user.getName());

        // 생성된 인사말을 대화 목록에 추가합니다.
        conversation.addMessage(firstAiMessage.getSender(), firstAiMessage.getText());

        // 첫 메시지가 포함된 상태로 대화를 저장합니다.
        chatConversationRepository.save(conversation);
        log.info("New chat room with greeting created. ID: '{}' for user {}", conversation.getId(), userId);

        // 프론트엔드에 전달할 요약 정보 DTO로 변환하여 반환
        return ChatRoomSummaryResponse.from(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> getConversationListByLevel(Long userId, String level) {
        return chatConversationRepository.findAllByUserIdAndTeacherLevel(userId, level)
                .stream()
                .sorted(Comparator.comparing(
                        ChatConversation::getLastModifiedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .map(ChatRoomSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationHistory(Long userId, String conversationId) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new SecurityException("User does not have permission to access this chat room.");
        }

        return conversation.getMessages();
    }

    /**
     * [수정] 채팅 응답 로직. 이제 첫 메시지 생성 책임이 없습니다.
     * (단, 방어 코드로 남겨두는 것은 좋습니다.)
     */
    @Transactional
    public ChatResponse getChatResponse(Long userId, String level, String conversationId, String userMessage,
                                        @Nullable MultipartFile imageFile) {

        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId cannot be null or empty for sending a message.");
        }

        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + conversationId));

        if (!conversation.getUserId().equals(userId)) {
            throw new SecurityException("User does not have permission to access this chat room.");
        }

        // 이 코드는 이제 정상적인 흐름에서는 실행되지 않지만,
        // 혹시 모를 비어있는 방 데이터에 대한 방어 코드로써 유용합니다.
        if (conversation.getMessages().isEmpty()) {
            User user = userReadService.fetchById(userId);
            ChatMessage firstAiMessage = createFirstMessageForLevel(level, user.getName());
            conversation.addMessage(firstAiMessage.getSender(), firstAiMessage.getText());
        }

        // --- 이하 로직은 변경 없음 ---
        String imageUrl = null;
        String imageBase64 = null;
        String imageMimeType = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String savedFilename = storeFile(imageFile);
                imageUrl = apiUrl + fileStorageProperties.getUploadUrlPrefix() + savedFilename;
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

        String systemPrompt = createSystemPrompt(level);
        String requestBody = createRequestBodyWithHistory(systemPrompt, conversation.getMessages(), imageBase64,
                imageMimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String fullApiUrl = String.format(apiTemplate, apiKey);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullApiUrl, entity, String.class);
            String aiResponseText = parseResponse(response.getBody());
            conversation.addMessage("ai", aiResponseText);
            chatConversationRepository.save(conversation);
            return new ChatResponse(aiResponseText, conversation.getId());
        } catch (HttpClientErrorException e) {
            log.error("Gemini API Error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Sorry, I encountered an error with the AI service. Please try again.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during chat processing", e);
            throw new RuntimeException("An unexpected error occurred. Please check the server logs.");
        }
    }

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

    private ChatMessage createFirstMessageForLevel(String level, String userName) {
        String text = switch (level) {
            case "elementary" -> String.format("Hello, %s! I'm your English friend. What did you do today?", userName);
            case "highschool" ->
                    String.format("Hi %s. Welcome to your academic English session. What topic should we start with?",
                            userName);
            case "native" -> String.format(
                    "It's a pleasure to connect, %s. I'm ready for a deep and insightful conversation. What's on your mind?",
                    userName);
            case "toeic" -> String.format(
                    "Welcome, %s. This is your TOEIC preparation session. Let's begin. How may I help you today?",
                    userName);
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