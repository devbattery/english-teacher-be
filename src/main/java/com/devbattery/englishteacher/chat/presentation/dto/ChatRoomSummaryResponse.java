package com.devbattery.englishteacher.chat.presentation.dto;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.time.LocalDateTime;

public record ChatRoomSummaryResponse(
        String conversationId,
        String lastMessage,
        LocalDateTime lastModifiedAt
) {

    public static ChatRoomSummaryResponse from(ChatConversation conversation) {
        String lastMessageText = "새로운 대화";
        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            lastMessageText = conversation.getMessages()
                    .get(conversation.getMessages().size() - 1)
                    .getText();

            if (lastMessageText.length() > 30) {
                lastMessageText = lastMessageText.substring(0, 30) + "...";
            }
        }

        return new ChatRoomSummaryResponse(
                conversation.getId(),
                lastMessageText,
                conversation.getLastModifiedAt()
        );
    }

}