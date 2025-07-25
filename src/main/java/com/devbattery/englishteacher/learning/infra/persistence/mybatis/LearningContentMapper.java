package com.devbattery.englishteacher.learning.infra.persistence.mybatis;

import com.devbattery.englishteacher.learning.domain.LearningContent;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LearningContentMapper {

    Optional<LearningContent> findByLevelAndDate(String level, LocalDate date);

    void save(LearningContent content);

}
