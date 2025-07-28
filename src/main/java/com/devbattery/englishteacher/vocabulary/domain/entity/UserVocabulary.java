package com.devbattery.englishteacher.vocabulary.domain.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserVocabulary {

    private Long id;
    private Long userId;
    private String englishExpression;
    private String koreanMeaning;
    private LocalDateTime createdAt;
    private boolean isMemorized;

    public UserVocabulary(Long userId, String englishExpression, String koreanMeaning, boolean isMemorized) {
        this.userId = userId;
        this.englishExpression = englishExpression;
        this.koreanMeaning = koreanMeaning;
        this.isMemorized = isMemorized;
    }

    public void updateMemorized(boolean isMemorized) {
        this.isMemorized = isMemorized;
    }

}