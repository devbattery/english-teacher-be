package com.devbattery.englishteacher.chat.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collection = "conversations")
public class ChatConversation {

    @Id
    private String id;

    private Long userId;
    private String teacherLevel;

    private List<ChatMessage> messages = new ArrayList<>();

    @LastModifiedDate
    public LocalDateTime lastModifiedAt;

    public ChatConversation(Long userId, String teacherLevel) {
        this.userId = userId;
        this.teacherLevel = teacherLevel;
    }

    public void addMessage(String sender, String text) {
        this.messages.add(new ChatMessage(sender, text, LocalDateTime.now()));
    }

    public void addMessage(String sender, String text, String imageUrl) {
        this.messages.add(new ChatMessage(sender, text, LocalDateTime.now(), imageUrl));
    }

}
