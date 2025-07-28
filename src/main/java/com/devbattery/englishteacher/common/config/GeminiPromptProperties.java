package com.devbattery.englishteacher.common.config;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "gemini.prompts")
public class GeminiPromptProperties {

    private String learning;
    private Map<String, String> chat;
    private Map<String, String> levelDescriptions;

}
