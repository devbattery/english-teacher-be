package com.devbattery.englishteacher.vocabulary.presentation.dto;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private VocabResponse(Long id, String englishExpression, String koreanMeaning, boolean isMemorized) {
        this.id = id;
        this.englishExpression = englishExpression;
        this.koreanMeaning = koreanMeaning;
        this.isMemorized = isMemorized;
    }

    public static VocabResponse from(UserVocabulary vocab) {
        return new VocabResponse(
                vocab.getId(),
                vocab.getEnglishExpression(),
                vocab.getKoreanMeaning(),
                vocab.isMemorized()
        );
    }

}