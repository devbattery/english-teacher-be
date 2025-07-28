package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.common.exception.ContentGenerationFailedException;
import com.devbattery.englishteacher.learning.application.dto.LearningContentResponse;
import com.devbattery.englishteacher.learning.domain.LearningContent;
import com.devbattery.englishteacher.learning.domain.repository.LearningContentRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningService {

    private final LearningContentRepository learningContentRepository;
    private final RedissonClient redissonClient;
    private final LearningContentCreator learningContentCreator; // [수정] 새로운 클래스 주입

    private static final String LOCK_PREFIX = "lock:learning-content:";
    private static final long WAIT_TIME_SECONDS = 10L;
    private static final long LEASE_TIME_SECONDS = 60L;

    // 이 메소드에는 @Transactional이 없습니다.
    public LearningContentResponse getDailyContent(String level, String userName) {
        LocalDate today = LocalDate.now();
        Optional<LearningContent> existingContent = learningContentRepository.fetchByLevelAndDate(level, today);
        if (existingContent.isPresent()) {
            log.info("'{}' 레벨, {} 날짜의 컨텐츠 확인 (기존 데이터)", level, today);
            return new LearningContentResponse("FOUND_EXISTING", existingContent.get());
        }

        String lockKey = LOCK_PREFIX + level + ":" + today;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean lockAcquired = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("'{}' 레벨 콘텐츠 생성 락 획득 실패. 다른 사용자가 생성중이거나 완료될 때까지 대기합니다.", level);
                return getDailyContentAfterWait(level, today);
            }

            // [수정] 분리된 클래스의 메소드를 호출합니다. 이제 트랜잭션이 정상적으로 적용됩니다.
            LearningContent content = learningContentCreator.createAndSaveContent(level, today, userName);

            // 생성자와 요청자가 같은지 확인하여 상태를 결정
            String status =
                    content.getGeneratedByUserName().equals(userName) ? "GENERATED_NEW" : "FOUND_EXISTING_AFTER_WAIT";
            return new LearningContentResponse(status, content);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("락 대기 중 스레드 인터럽트 발생", e);
            throw new ContentGenerationFailedException();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("'{}' 레벨 콘텐츠 생성 락 해제 (요청자: {})", level, userName);
            }
        }
    }

    private LearningContentResponse getDailyContentAfterWait(String level, LocalDate today) {
        // 락 획득 실패 시, 잠시 대기 후 DB 재조회 (다른 스레드가 커밋할 시간을 줌)
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<LearningContent> content = learningContentRepository.fetchByLevelAndDate(level, today);
        if (content.isPresent()) {
            log.info("대기 후 '{}' 레벨 콘텐츠 조회 성공", level);
            return new LearningContentResponse("FOUND_EXISTING_AFTER_WAIT", content.get());
        }

        // 대기 후에도 없으면 실패 처리
        log.warn("대기 후에도 '{}' 레벨 콘텐츠를 찾을 수 없습니다.", level);
        throw new ContentGenerationFailedException();
    }

}