package com.devbattery.englishteacher.learning.domain.repository;

import com.devbattery.englishteacher.learning.domain.LearningContent;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface LearningContentRepository {

    Optional<LearningContent> fetchByLevelAndDate(String level, LocalDate date);

    void save(LearningContent content);

}
