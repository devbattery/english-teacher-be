package com.devbattery.englishteacher.learning.infra;

import com.devbattery.englishteacher.learning.domain.LearningContent;
import com.devbattery.englishteacher.learning.domain.repository.LearningContentRepository;
import com.devbattery.englishteacher.learning.infra.persistence.mybatis.LearningContentMapper;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LearningContentRepositoryImpl implements LearningContentRepository {

    private final LearningContentMapper learningContentMapper;

    @Override
    public Optional<LearningContent> fetchByLevelAndDate(String level, LocalDate date) {
        return learningContentMapper.findByLevelAndDate(level, date);
    }

    @Override
    public void save(LearningContent content) {
        learningContentMapper.save(content);
    }

}
