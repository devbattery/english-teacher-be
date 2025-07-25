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
    public List<UserVocabulary> fetchByUserId(Long userId) {
        return vocabularyMapper.findByUserId(userId);
    }

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

}
