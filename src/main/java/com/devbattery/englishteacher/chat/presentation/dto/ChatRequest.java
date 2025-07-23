package com.devbattery.englishteacher.chat.presentation.dto;

import org.springframework.lang.Nullable;

public record ChatRequest(String level, @Nullable String conversationId, String message) {

}
