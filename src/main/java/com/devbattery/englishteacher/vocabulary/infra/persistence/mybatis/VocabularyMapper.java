package com.devbattery.englishteacher.vocabulary.infra.persistence.mybatis;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VocabularyMapper {

    void save(UserVocabulary vocabulary);

    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void deleteById(Long id);

    UserVocabulary findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    void updateMemorizedStatus(UserVocabulary vocabulary);

    List<UserVocabulary> findPaginatedByUserIdAndSearchTerm(@Param("userId") Long userId,
                                                            @Param("searchTerm") String searchTerm,
                                                            @Param("limit") int limit,
                                                            @Param("offset") long offset);

    long countByUserIdAndSearchTerm(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

}