package com.devbattery.englishteacher.vocabulary.presentation.dto;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VocabResponse {

    private Long id;
    private String englishExpression;
    private String koreanMeaning;

    @JsonProperty("isMemorized")
    private boolean isMemorized;

    private LocalDateTime createdAt;

    public VocabResponse(Long id, String englishExpression, String koreanMeaning, boolean isMemorized,
                         LocalDateTime createdAt) {
        this.id = id;
        this.englishExpression = englishExpression;
        this.koreanMeaning = koreanMeaning;
        this.isMemorized = isMemorized;
        this.createdAt = createdAt;
    }

    public static VocabResponse from(UserVocabulary vocab) {
        return new VocabResponse(
                vocab.getId(),
                vocab.getEnglishExpression(),
                vocab.getKoreanMeaning(),
                vocab.isMemorized(),
                vocab.getCreatedAt()
        );
    }

}