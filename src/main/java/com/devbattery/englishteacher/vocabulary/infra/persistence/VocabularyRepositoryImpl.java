package com.devbattery.englishteacher.vocabulary.infra.persistence;

import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.devbattery.englishteacher.vocabulary.domain.repository.VocabularyRepository;
import com.devbattery.englishteacher.vocabulary.infra.persistence.mybatis.VocabularyMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VocabularyRepositoryImpl implements VocabularyRepository {

    private final VocabularyMapper vocabularyMapper;

    @Override
    public void save(UserVocabulary vocabulary) {
        vocabularyMapper.save(vocabulary);
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return vocabularyMapper.existsByIdAndUserId(id, userId);
    }

    @Override
    public void deleteById(Long id) {
        vocabularyMapper.deleteById(id);
    }

    @Override
    public UserVocabulary findByIdAndUserId(Long id, Long userId) {
        return vocabularyMapper.findByIdAndUserId(id, userId);
    }

    @Override
    public void updateMemorizedStatus(UserVocabulary vocabulary) {
        vocabularyMapper.updateMemorizedStatus(vocabulary);
    }

    @Override
    public List<UserVocabulary> findPaginatedByUserIdAndSearchTerm(Long userId, String searchTerm, int limit,
                                                                   long offset) {
        return vocabularyMapper.findPaginatedByUserIdAndSearchTerm(userId, searchTerm, limit, offset);
    }

    @Override
    public long countByUserIdAndSearchTerm(Long userId, String searchTerm) {
        return vocabularyMapper.countByUserIdAndSearchTerm(userId, searchTerm);
    }

}
