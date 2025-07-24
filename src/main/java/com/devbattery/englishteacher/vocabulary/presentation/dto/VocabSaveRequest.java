package com.devbattery.englishteacher.vocabulary.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프론트엔드에서 단어/숙어 저장을 요청할 때 사용하는 DTO 입니다.
 * 사용자가 드래그한 영어 표현을 담습니다.
 */
@Getter
@Setter // JSON 직렬화/역직렬화를 위해 Setter가 필요합니다.
@NoArgsConstructor // 기본 생성자도 필요합니다.
public class VocabSaveRequest {
    
    // 사용자가 저장하려는 영어 표현 (예: "look forward to")
    private String expression;
    
}