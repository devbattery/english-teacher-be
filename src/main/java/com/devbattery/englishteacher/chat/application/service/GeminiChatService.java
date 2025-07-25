package com.devbattery.englishteacher.chat.application.service;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.presentation.dto.ChatResponse;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRoomSummaryResponse;
import com.devbattery.englishteacher.common.config.FileStorageProperties;
import com.devbattery.englishteacher.common.config.GeminiPromptProperties;
import com.devbattery.englishteacher.common.exception.ChatMessageNotFoundException;
import com.devbattery.englishteacher.common.exception.ChatRoomOverException;
import com.devbattery.englishteacher.common.exception.FileStorageException;
import com.devbattery.englishteacher.common.exception.GeminiApiException;
import com.devbattery.englishteacher.common.exception.ServerErrorException;
import com.devbattery.englishteacher.common.exception.UserUnauthorizedException;
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

    private static final int MAX_CHAT_ROOMS_PER_LEVEL = 10;

    @Value("${gemini.api.key-chat}")
    private String apiKey;

    @Value("${url.api}")
    private String apiUrl;

    @Value("${gemini.api.template}")
    private String apiTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final FileStorageProperties fileStorageProperties;
    private final UserReadService userReadService;
    private final ChatConversationService chatConversationService;
    private final GeminiPromptProperties promptProperties;

    /**
     * 새로운 채팅방을 생성할 때, 첫 AI 인사말을 포함하여 생성
     */
    @Transactional
    public ChatRoomSummaryResponse createChatRoom(Long userId, String level) {
        long roomCount = chatConversationService.countByUserIdAndTeacherLevel(userId, level);
        if (roomCount >= MAX_CHAT_ROOMS_PER_LEVEL) {
            throw new ChatRoomOverException();
        }

        User user = userReadService.fetchById(userId);
        ChatMessage firstAiMessage = createFirstMessageForLevel(level, user.getName());

        ChatConversation conversation = new ChatConversation(userId, level);
        conversation.addMessage(firstAiMessage.getSender(), firstAiMessage.getText());
        chatConversationService.save(conversation);

        return ChatRoomSummaryResponse.from(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomSummaryResponse> fetchConversationListByLevel(Long userId, String level) {
        return chatConversationService.fetchAllByUserIdAndTeacherLevel(userId, level)
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
        ChatConversation conversation = chatConversationService.findById(conversationId);

        if (!conversation.getUserId().equals(userId)) {
            throw new UserUnauthorizedException();
        }

        return conversation.getMessages();
    }

    @Transactional
    public ChatResponse fetchChatResponse(Long userId, String level, String conversationId, String userMessage,
                                          @Nullable MultipartFile imageFile) {

        if (conversationId == null || conversationId.isBlank()) {
            throw new ChatMessageNotFoundException();
        }

        ChatConversation conversation = chatConversationService.findById(conversationId);

        if (!conversation.getUserId().equals(userId)) {
            throw new UserUnauthorizedException();
        }

        if (conversation.getMessages().isEmpty()) {
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
                imageUrl = apiUrl + fileStorageProperties.getUploadUrlPrefix() + savedFilename;
                imageBase64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
                imageMimeType = imageFile.getContentType();
                conversation.addMessage("user", userMessage, imageUrl);
            } catch (IOException e) {
                throw new FileStorageException();
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
            chatConversationService.save(conversation);
            return new ChatResponse(aiResponseText, conversation.getId());
        } catch (HttpClientErrorException e) {
            throw new GeminiApiException();
        } catch (Exception e) {
            throw new ServerErrorException();
        }
    }

    @Transactional
    public void deleteConversation(Long userId, String conversationId) {
        ChatConversation conversation = chatConversationService.findById(conversationId);

        if (!conversation.getUserId().equals(userId)) {
            throw new UserUnauthorizedException();
        }

        chatConversationService.deleteById(conversationId);
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
            throw new ServerErrorException();
        }
    }

}