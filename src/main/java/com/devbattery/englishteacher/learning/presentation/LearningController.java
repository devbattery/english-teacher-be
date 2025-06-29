package com.devbattery.englishteacher.learning.presentation;

import com.devbattery.englishteacher.learning.application.LearningService;
import com.devbattery.englishteacher.learning.application.dto.LearningContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/api/learning/today/{level}")
    public ResponseEntity<?> getDailyLearningContent(@PathVariable String level) {
        LearningContentResponse content = learningService.getDailyContent(level);
        return ResponseEntity.ok(content);
    }

}
