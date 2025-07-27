package com.devbattery.englishteacher.vocabulary.domain.repository;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VocabularyRepository {

    void save(UserVocabulary vocabulary);

    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void deleteById(Long id);

    UserVocabulary findByIdAndUserId(Long id, Long userId);

    void updateMemorizedStatus(UserVocabulary vocabulary);

    List<UserVocabulary> findPaginatedByUserIdAndSearchTerm(Long userId, String searchTerm, int limit,
                                                            long offset); // [추가]

    long countByUserIdAndSearchTerm(Long userId, String searchTerm); // [추가]

}