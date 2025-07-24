package com.devbattery.englishteacher.vocabulary.presentation.dto;

import com.devbattery.englishteacher.vocabulary.domain.UserVocabulary;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VocabResponse {

    private Long id;
    private String englishExpression;
    private String koreanMeaning;

    // private 생성자로 외부에서 직접 생성을 막고, 정적 팩토리 메소드 사용을 유도
    private VocabResponse(Long id, String englishExpression, String koreanMeaning) {
        this.id = id;
        this.englishExpression = englishExpression;
        this.koreanMeaning = koreanMeaning;
    }

    /**
     * UserVocabulary 엔티티/도메인 객체를 VocabResponse DTO로 변환합니다.
     * @param vocab UserVocabulary 객체
     * @return 변환된 VocabResponse DTO
     */
    public static VocabResponse from(UserVocabulary vocab) {
        return new VocabResponse(
                vocab.getId(),
                vocab.getEnglishExpression(),
                vocab.getKoreanMeaning()
        );
    }

}