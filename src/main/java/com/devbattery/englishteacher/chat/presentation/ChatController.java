package com.devbattery.englishteacher.chat.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.chat.application.service.GeminiChatService;
import com.devbattery.englishteacher.chat.presentation.dto.ChatRequest;
import com.devbattery.englishteacher.chat.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final GeminiChatService geminiChatService;

    @PostMapping("/api/chat/send")
    public ResponseEntity<ChatResponse> sendChatMessage(@RequestBody ChatRequest request) {
        String reply = geminiChatService.getChatResponse(request.level(), request.message());
        return ResponseEntity.ok(new ChatResponse(reply));
    }

}
