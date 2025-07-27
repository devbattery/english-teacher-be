package com.devbattery.englishteacher.vocabulary.domain.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserVocabulary {

    private Long id;
    private Long userId; // User 객체 대신 userId를 직접 관리
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

    public void updateMemorized(boolean memorized) {
        isMemorized = memorized;
    }

}