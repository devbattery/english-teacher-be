
package com.devbattery.englishteacher.learning.application;

import com.devbattery.englishteacher.learning.domain.LearningContent;
import com.devbattery.englishteacher.learning.domain.repository.LearningContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {

    @InjectMocks
    private LearningService learningService;

    @Mock
    private LearningContentRepository learningContentRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private LearningDailyContentCreator learningDailyContentCreator;

    @Mock
    private RLock rLock;

    private final String level = "native";
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
    }

    @Test
    @DisplayName("100개의 스레드가 동시에 콘텐츠를 요청할 때 한 번만 생성되어야 한다.")
    void fetchDailyContent_Concurrency_Test() throws InterruptedException {
        // Given
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger creationCount = new AtomicInteger(0);

        LearningContent learningContent = new LearningContent(1L, level, "Test Title", "Test Content",
                Collections.emptyList(), "user-0", today);

        // Mocking
        when(learningContentRepository.fetchByLevelAndDate(level, today))
                .thenReturn(Optional.empty()) // 처음에는 콘텐츠가 없음
                .thenReturn(Optional.of(learningContent)); // 그 다음부터는 콘텐츠가 있음

        when(learningDailyContentCreator.createDailyContent(anyString(), any(LocalDate.class), anyString()))
                .thenAnswer(invocation -> {
                    creationCount.incrementAndGet();
                    return learningContent;
                });

        // 락 획득 시뮬레이션
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            // 첫 번째 스레드만 락 획득 성공
            return creationCount.get() == 0;
        });

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final String userName = "user-" + i;
            executorService.submit(() -> {
                try {
                    learningService.fetchDailyContent(level, userName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        assertThat(creationCount.get()).isEqualTo(1);
        verify(learningDailyContentCreator, times(1)).createDailyContent(anyString(), any(LocalDate.class),
                anyString());
        verify(learningContentRepository, atLeast(numberOfThreads)).fetchByLevelAndDate(level, today);
    }

}
