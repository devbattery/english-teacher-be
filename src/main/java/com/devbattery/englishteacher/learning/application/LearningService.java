package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.common.exception.GeminiApiException;
import com.devbattery.englishteacher.learning.application.dto.LearningContentResponse;
import com.devbattery.englishteacher.learning.domain.KeyExpression;
import com.devbattery.englishteacher.learning.domain.LearningContent;
import com.devbattery.englishteacher.learning.domain.repository.LearningContentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningService {

    private final LearningContentRepository learningContentRepository;
    private final GeminiArticleGeneratorService geminiArticleGeneratorService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LearningContentResponse getDailyContent(String level) {
        LocalDate today = LocalDate.now();
        Optional<LearningContent> existingContent = learningContentRepository.fetchByLevelAndDate(level, today);

        if (existingContent.isPresent()) {
            log.info("'{}' 레벨, {} 날짜의 컨텐츠 확인", level, today);
            return new LearningContentResponse("FOUND_EXISTING", existingContent.get());
        }

        log.info("'{}' 레벨, {} 날짜의 컨텐츠가 없으므로 생성", level, today);

        String articleJson = geminiArticleGeneratorService.generateArticleJson(level);

        try {
            JsonNode rootNode = objectMapper.readTree(articleJson);
            String title = rootNode.path("title").asText("No Title Provided");
            String contentText = rootNode.path("content").asText("No content available.");

            List<KeyExpression> expressions = Collections.emptyList();
            JsonNode expressionsNode = rootNode.path("keyExpressions");
            if (expressionsNode.isArray()) {
                try {
                    expressions = objectMapper.convertValue(expressionsNode, new TypeReference<List<KeyExpression>>() {
                    });
                } catch (IllegalArgumentException e) {
                    log.error("expressions 파싱 오류 {}", expressionsNode);
                }
            }

            LearningContent newContent = new LearningContent(null, level, title, contentText, expressions, today);
            learningContentRepository.save(newContent);
            log.info("'{}' 레벨의 글 성공적으로 생성 완료", level);

            return new LearningContentResponse("GENERATED_NEW", newContent);
        } catch (Exception e) {
            throw new GeminiApiException();
        }
    }

}
