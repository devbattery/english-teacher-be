package com.devbattery.englishteacher.learning.application.dto;

import com.devbattery.englishteacher.learning.domain.LearningContent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LearningContentResponse {

    private String status;
    private LearningContent content;

}
