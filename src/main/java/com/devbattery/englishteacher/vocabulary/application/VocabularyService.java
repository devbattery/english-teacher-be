package com.devbattery.englishteacher.vocabulary.application;

import com.devbattery.englishteacher.common.exception.UserUnauthorizedException;
import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.devbattery.englishteacher.vocabulary.domain.repository.VocabularyRepository;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabSaveRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final GeminiTranslationService translationService;

    @Transactional(readOnly = true)
    public List<UserVocabulary> fetchMyVocabulary(Long userId) {
        return vocabularyRepository.fetchByUserId(userId);
    }

    @Transactional
    public UserVocabulary saveNewWord(VocabSaveRequest request, Long userId) {
        String koreanMeaning = translationService.translateToKorean(request.getExpression());
        UserVocabulary newVocab = new UserVocabulary(userId, request.getExpression(), koreanMeaning);
        vocabularyRepository.save(newVocab);
        return newVocab;
    }

    @Transactional
    public void deleteWord(Long wordId, Long userId) throws AccessDeniedException {
        if (!vocabularyRepository.existsByIdAndUserId(wordId, userId)) {
            throw new UserUnauthorizedException();
        }

        vocabularyRepository.deleteById(wordId);
    }

}