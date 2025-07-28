package com.devbattery.englishteacher.chat.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@NoArgsConstructor
@Getter
public class ChatMessage {

    private String sender;
    private String text;

    @CreatedDate
    private LocalDateTime timestamp;

    private String imageUrl;

    public ChatMessage(String sender, String text, LocalDateTime timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public ChatMessage(String sender, String text, LocalDateTime timestamp, String imageUrl) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

}
