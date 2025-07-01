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
    private final ChatConversationRepository chatConversationRepository;

    @PostMapping(value = "/api/chat/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendChatMessage(@RequestPart ChatRequest request,
                                             @RequestPart(value = "image", required = false) @Nullable MultipartFile image,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        String reply = geminiChatService.getChatResponse(userId, request.level(), request.message(), image);
        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @GetMapping("/api/chat/history/{level}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String level,
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        List<ChatMessage> history = geminiChatService.getConversationHistory(userId, level);
        return ResponseEntity.ok(history);
    }

    /**
     * [신규] 특정 선생님과의 채팅 기록을 초기화하는 엔드포인트
     * @param level 초기화할 선생님 레벨
     * @return 성공 응답
     */
    @DeleteMapping("/api/chat/history/{level}")
    public ResponseEntity<Void> resetChatHistory(@PathVariable String level,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        geminiChatService.resetChatHistory(userId, level);
        return ResponseEntity.ok().build();
    }

}
