package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.common.exception.ContentGenerationFailedException;
import com.devbattery.englishteacher.common.exception.GeminiApiException;
import com.devbattery.englishteacher.learning.application.dto.LearningContentResponse;
import com.devbattery.englishteacher.learning.domain.KeyExpression;
import com.devbattery.englishteacher.learning.domain.LearningContent;
import com.devbattery.englishteacher.learning.domain.repository.LearningContentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningService {

    private final LearningContentRepository learningContentRepository;
    private final GeminiArticleGeneratorService geminiArticleGeneratorService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate; // [추가] RedisTemplate 주입

    private static final String LOCK_PREFIX = "lock:learning-content:";
    private static final int MAX_RETRY_COUNT = 5;
    private static final long RETRY_DELAY_MS = 500;

    /**
     * [수정] 오늘의 학습 콘텐츠를 가져오는 메서드
     * @param level 요청 레벨
     * @param userName 요청한 사용자의 이름 (인증 정보에서 추출)
     * @return 학습 콘텐츠 응답 DTO
     */
    @Transactional
    public LearningContentResponse getDailyContent(String level, String userName) {
        LocalDate today = LocalDate.now();
        Optional<LearningContent> existingContent = learningContentRepository.fetchByLevelAndDate(level, today);

        if (existingContent.isPresent()) {
            log.info("'{}' 레벨, {} 날짜의 컨텐츠 확인 (기존 데이터)", level, today);
            return new LearningContentResponse("FOUND_EXISTING", existingContent.get());
        }

        // --- 여기서부터 Redis Lock 로직 시작 ---
        String lockKey = LOCK_PREFIX + level + ":" + today;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userName, Duration.ofMinutes(2)); // 2분 후 자동 만료

        if (Boolean.TRUE.equals(lockAcquired)) {
            // 락 획득 성공 (내가 첫 번째 요청자)
            log.info("'{}' 사용자가 '{}' 레벨 콘텐츠 생성 락 획득", userName, level);
            try {
                // 콘텐츠 생성 및 저장
                LearningContent newContent = generateAndSaveContent(level, today, userName);
                return new LearningContentResponse("GENERATED_NEW", newContent);
            } finally {
                // 작업 완료 후 락 해제
                redisTemplate.delete(lockKey);
                log.info("'{}' 레벨 콘텐츠 생성 락 해제", level);
            }
        } else {
            // 락 획득 실패 (다른 사용자가 생성 중)
            log.warn("'{}' 레벨 콘텐츠는 다른 사용자에 의해 생성 중입니다. 대기 후 재시도합니다.", level);
            for (int i = 0; i < MAX_RETRY_COUNT; i++) {
                try {
                    Thread.sleep(RETRY_DELAY_MS); // 잠시 대기
                    existingContent = learningContentRepository.fetchByLevelAndDate(level, today);
                    if (existingContent.isPresent()) {
                        log.info("대기 후 '{}' 레벨 콘텐츠 조회 성공", level);
                        return new LearningContentResponse("FOUND_EXISTING_AFTER_WAIT", existingContent.get());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ContentGenerationFailedException();
                }
            }
            // 최대 재시도 후에도 콘텐츠가 없으면 실패 처리
            throw new ContentGenerationFailedException();
        }
    }

    // 콘텐츠 생성 및 저장 로직을 별도 메서드로 분리
    private LearningContent generateAndSaveContent(String level, LocalDate date, String userName) {
        log.info("'{}' 레벨, {} 날짜의 컨텐츠가 없으므로 생성 (생성자: {})", level, date, userName);
        String articleJson = geminiArticleGeneratorService.generateArticleJson(level);

        try {
            JsonNode rootNode = objectMapper.readTree(articleJson);
            String title = rootNode.path("title").asText("No Title Provided");
            String contentText = rootNode.path("content").asText("No content available.");

            List<KeyExpression> expressions = Collections.emptyList();
            JsonNode expressionsNode = rootNode.path("keyExpressions");
            if (expressionsNode.isArray()) {
                try {
                    expressions = objectMapper.convertValue(expressionsNode, new TypeReference<>() {});
                } catch (IllegalArgumentException e) {
                    log.error("expressions 파싱 오류 {}", expressionsNode);
                }
            }

            // [수정] 생성자 이름을 포함하여 LearningContent 객체 생성
            LearningContent newContent = new LearningContent(null, level, title, contentText, expressions, userName, date);
            learningContentRepository.save(newContent);
            log.info("'{}' 레벨의 글 성공적으로 생성 완료 (생성자: {})", level, userName);

            return newContent;
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 또는 DB 저장 중 오류 발생", e);
            throw new GeminiApiException();
        }
    }
}