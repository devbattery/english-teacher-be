package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.common.exception.GeminiApiException;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningDailyContentCreator {

    private final LearningContentRepository learningContentRepository;
    private final GeminiDailyContentGeneratorService geminiDailyContentGeneratorService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LearningContent createDailyContent(String level, LocalDate date, String userName) {
        // 트랜잭션 시작 후 다시 한번 DB 확인
        Optional<LearningContent> existingContent = learningContentRepository.fetchByLevelAndDate(level, date);
        if (existingContent.isPresent()) {
            log.info("락 획득 후 확인하니 이미 컨텐츠 존재 (생성자: {})", existingContent.get().getGeneratedByUserName());
            return existingContent.get();
        }

        log.info("'{}' 레벨, {} 날짜의 컨텐츠가 없으므로 생성 (생성자: {})", level, date, userName);
        String articleJson = geminiDailyContentGeneratorService.generateDailyContent(level);

        try {
            JsonNode rootNode = objectMapper.readTree(articleJson);
            String title = rootNode.path("title").asText("No Title Provided");
            String contentText = rootNode.path("content").asText("No content available.");
            List<KeyExpression> expressions = Collections.emptyList();
            JsonNode expressionsNode = rootNode.path("keyExpressions");
            if (expressionsNode.isArray()) {
                expressions = objectMapper.convertValue(expressionsNode, new TypeReference<>() {
                });
            }

            LearningContent newContent = new LearningContent(null, level, title, contentText, expressions, userName,
                    date);
            learningContentRepository.save(newContent);
            log.info("'{}' 레벨의 글 성공적으로 생성 완료 (생성자: {})", level, userName);

            return newContent;
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 또는 DB 저장 중 오류 발생", e);
            throw new GeminiApiException();
        }
    }

}