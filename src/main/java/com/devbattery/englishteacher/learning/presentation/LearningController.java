package com.devbattery.englishteacher.learning.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.learning.application.LearningService;
import com.devbattery.englishteacher.learning.application.dto.LearningContentResponse;
import com.devbattery.englishteacher.user.application.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;
    private final UserReadService userReadService;

    @GetMapping("/api/learning/today/{level}")
    public ResponseEntity<?> getDailyLearningContent(@PathVariable String level,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String userName = userReadService.fetchById(userPrincipal.getId()).getName();
        LearningContentResponse content = learningService.fetchDailyContent(level, userName);
        return ResponseEntity.ok(content);
    }

}