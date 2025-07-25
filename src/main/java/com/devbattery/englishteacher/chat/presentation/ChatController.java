package com.devbattery.englishteacher.chat.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.chat.application.service.GeminiChatService;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRequest;
import com.devbattery.englishteacher.chat.presentation.dto.ChatResponse;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRoomSummaryResponse;
import com.devbattery.englishteacher.chat.presentation.dto.CreateChatRoomRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;

    @PostMapping("/api/chat/rooms")
    public ResponseEntity<ChatRoomSummaryResponse> createChatRoom(
            @RequestBody CreateChatRoomRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        ChatRoomSummaryResponse newRoom = geminiChatService.createChatRoom(userId, request.level());
        return ResponseEntity.ok(newRoom);
    }

    @PostMapping(value = "/api/chat/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> sendChatMessage(@RequestPart ChatRequest request,
                                                        @RequestPart(value = "image", required = false) @Nullable MultipartFile image,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        // 서비스 호출 시 conversationId도 함께 전달합니다.
        ChatResponse response = geminiChatService.fetchChatResponse(
                userId, request.level(), request.conversationId(), request.message(), image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chat/rooms/{level}")
    public ResponseEntity<List<ChatRoomSummaryResponse>> getChatRoomList(
            @PathVariable String level,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        List<ChatRoomSummaryResponse> rooms = geminiChatService.fetchConversationListByLevel(userId, level);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/api/chat/history/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String conversationId,
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        List<ChatMessage> history = geminiChatService.getConversationHistory(userId, conversationId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/api/chat/room/{conversationId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable String conversationId,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        geminiChatService.deleteConversation(userId, conversationId);
        return ResponseEntity.ok().build();
    }

}