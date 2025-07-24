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
    // 이제 Controller에서는 Repository를 직접 사용할 필요가 없습니다.
    // private final ChatConversationRepository chatConversationRepository;

    /**
     * [신규] 새로운 빈 채팅방을 생성하는 엔드포인트
     * @param request 생성할 레벨 정보
     * @return 생성된 채팅방의 요약 정보
     */
    @PostMapping("/api/chat/rooms")
    public ResponseEntity<ChatRoomSummaryResponse> createChatRoom(
            @RequestBody CreateChatRoomRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        ChatRoomSummaryResponse newRoom = geminiChatService.createChatRoom(userId, request.level());
        return ResponseEntity.ok(newRoom);
    }

    /**
     * [수정] 채팅 메시지 전송 (기존 또는 신규 채팅방)
     * ChatRequest에 conversationId가 포함됩니다. (신규 채팅방의 경우 null)
     */
    @PostMapping(value = "/api/chat/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> sendChatMessage(@RequestPart ChatRequest request,
                                                        @RequestPart(value = "image", required = false) @Nullable MultipartFile image,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        // 서비스 호출 시 conversationId도 함께 전달합니다.
        ChatResponse response = geminiChatService.getChatResponse(
                userId, request.level(), request.conversationId(), request.message(), image);
        return ResponseEntity.ok(response);
    }

    /**
     * [신규] 특정 레벨의 모든 채팅방 목록 조회
     * @param level 선생님 레벨
     * @return 해당 레벨의 채팅방 요약 정보 리스트
     */
    @GetMapping("/api/chat/rooms/{level}")
    public ResponseEntity<List<ChatRoomSummaryResponse>> getChatRoomList(
            @PathVariable String level,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        List<ChatRoomSummaryResponse> rooms = geminiChatService.getConversationListByLevel(userId, level);
        return ResponseEntity.ok(rooms);
    }

    /**
     * [수정] 특정 채팅방의 전체 대화 기록 조회
     * @param conversationId 조회할 채팅방의 고유 ID
     * @return 메시지 목록
     */
    @GetMapping("/api/chat/history/{conversationId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String conversationId,
                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        List<ChatMessage> history = geminiChatService.getConversationHistory(userId, conversationId);
        return ResponseEntity.ok(history);
    }

    /**
     * [수정] 특정 채팅방 삭제
     * @param conversationId 삭제할 채팅방의 고유 ID
     * @return 성공 응답
     */
    @DeleteMapping("/api/chat/room/{conversationId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable String conversationId,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        geminiChatService.deleteConversation(userId, conversationId);
        return ResponseEntity.ok().build();
    }
}