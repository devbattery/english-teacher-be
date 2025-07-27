package com.devbattery.englishteacher.vocabulary.domain.repository;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VocabularyRepository {

    List<UserVocabulary> fetchByUserId(Long userId);

    void save(UserVocabulary vocabulary);

    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void deleteById(Long id);

    UserVocabulary findByIdAndUserId(Long id, Long userId);

    void updateMemorizedStatus(UserVocabulary vocabulary);

}