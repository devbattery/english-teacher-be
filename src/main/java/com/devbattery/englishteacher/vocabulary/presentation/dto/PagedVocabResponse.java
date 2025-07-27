package com.devbattery.englishteacher.vocabulary.presentation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagedVocabResponse {

    private List<VocabResponse> content; // 현재 페이지의 단어 목록
    private int pageNumber;              // 현재 페이지 번호
    private int pageSize;                // 페이지 당 아이템 수
    private long totalElements;          // 전체 아이템 수
    private int totalPages;              // 전체 페이지 수
    private boolean last;                // 마지막 페이지 여부

}
