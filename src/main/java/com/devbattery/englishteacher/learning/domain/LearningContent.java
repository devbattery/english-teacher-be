package com.devbattery.englishteacher.learning.domain;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningContent {

    private Long id;
    private String level;
    private String title;
    private String content;
    private LocalDate createdDate;

    public LearningContent(Long id, String level, String title, String content, LocalDate createdDate) {
        this.id = id;
        this.level = level;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
    }

}
