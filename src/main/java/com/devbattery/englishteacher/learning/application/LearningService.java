package com.devbattery.englishteacher.learning.application;

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
    // GeminiChatService 대신 새로운 Article 생성 서비스를 주입받습니다.
    private final GeminiArticleGeneratorService geminiArticleGeneratorService;
    private final ObjectMapper objectMapper;

    /**
     * 오늘의 학습 콘텐츠를 가져옵니다.
     * 1. DB에서 오늘 날짜의 콘텐츠를 먼저 조회합니다.
     * 2. 있으면 즉시 반환합니다.
     * 3. 없으면, GeminiArticleGeneratorService를 통해 새로 생성하고 DB에 저장한 후 반환합니다.
     * @param level 선생님 레벨
     * @return 오늘의 학습 콘텐츠 (LearningContent 객체)
     */
    @Transactional
    public LearningContentResponse getDailyContent(String level) {
        LocalDate today = LocalDate.now();
        // 1. DB에서 기존 콘텐츠 확인
        Optional<LearningContent> existingContent = learningContentRepository.fetchByLevelAndDate(level, today);

        if (existingContent.isPresent()) {
            log.info("Found existing learning content for level '{}' on {}", level, today);
            return new LearningContentResponse("FOUND_EXISTING", existingContent.get());
        }

        // 3. 없으면 새로 생성
        log.info("No content found for level '{}' on {}. Generating new content...", level, today);

        // 3-1. AI 생성 서비스 호출하여 raw JSON 받기
        String articleJson = geminiArticleGeneratorService.generateArticleJson(level);

        // --- [핵심 디버깅 코드 추가] ---
        // Gemini API가 반환한 실제 응답을 로그로 출력합니다.
        log.info("Response from Gemini API for level '{}': {}", level, articleJson);
        // -----------------------------

        try {
            JsonNode rootNode = objectMapper.readTree(articleJson);
            String title = rootNode.path("title").asText("No Title Provided");
            String contentText = rootNode.path("content").asText("No content available.");

            // [핵심 추가] keyExpressions 파싱
            List<KeyExpression> expressions = Collections.emptyList();
            JsonNode expressionsNode = rootNode.path("keyExpressions");
            if (expressionsNode.isArray()) {
                try {
                    expressions = objectMapper.convertValue(expressionsNode, new TypeReference<List<KeyExpression>>() {
                    });
                } catch (IllegalArgumentException e) {
                    log.error("Error converting keyExpressions node for level {}: {}", level,
                            expressionsNode.toString(), e);
                }
            }

            // [수정] newContent 생성자에 expressions 추가
            LearningContent newContent = new LearningContent(null, level, title, contentText, expressions, today);
            learningContentRepository.save(newContent);
            log.info("Successfully generated and saved new content for level '{}'.", level);

            return new LearningContentResponse("GENERATED_NEW", newContent);
        } catch (Exception e) {
            log.error("Failed to parse AI-generated article JSON for level {}: {}", level, articleJson, e);
            throw new RuntimeException("Could not process the generated learning content.", e);
        }
    }

}
