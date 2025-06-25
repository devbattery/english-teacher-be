package com.devbattery.englishteacher.chat.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatMessage {

    private String sender; // "user", "ai"
    private String text;

    @CreatedDate
    private LocalDateTime timestamp;

}
