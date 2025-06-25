package com.devbattery.englishteacher.chat.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.chat.application.service.GeminiChatService;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.chat.domain.ChatMessage;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRequest;
import com.devbattery.englishteacher.chat.presentation.dto.ChatResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;
    private final ChatConversationRepository chatConversationRepository;

    @PostMapping("/api/chat/send")
    public ResponseEntity<?> sendChatMessage(@RequestBody ChatRequest request,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        String reply = geminiChatService.getChatResponse(userId, request.level(), request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @GetMapping("/api/chat/history/{level}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String level,
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 실제로는 Spring Security 등을 통해 인증된 사용자 정보를 가져와야 합니다.
        Long userId = userPrincipal.getId();
        List<ChatMessage> history = geminiChatService.getConversationHistory(userId, level);
        return ResponseEntity.ok(history);
    }

}
