package com.devbattery.englishteacher.vocabulary.infra.persistence.mybatis;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper // Spring이 이 인터페이스를 MyBatis 매퍼 빈으로 등록합니다.
public interface VocabularyMapper {

    List<UserVocabulary> findByUserId(Long userId);

    void save(UserVocabulary vocabulary);

    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void deleteById(Long id);

    UserVocabulary findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void updateMemorizedStatus(UserVocabulary vocabulary);

}