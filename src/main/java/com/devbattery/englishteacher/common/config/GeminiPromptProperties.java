package com.devbattery.englishteacher.common.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "gemini.prompts")
public class GeminiPromptProperties {

    private String learning;
    private Map<String, String> chat;
    private Map<String, String> levelDescriptions; // [추가]

}
