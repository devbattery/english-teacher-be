package com.devbattery.englishteacher.vocabulary.application;

import com.devbattery.englishteacher.common.exception.UserUnauthorizedException;
import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.devbattery.englishteacher.vocabulary.domain.repository.VocabularyRepository;
import com.devbattery.englishteacher.vocabulary.presentation.dto.PagedVocabResponse;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabResponse;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabSaveRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final GeminiTranslationService translationService;

    @Transactional(readOnly = true)
    public PagedVocabResponse fetchMyVocabularyPaginated(Long userId, String searchTerm, int page, int size) {
        long offset = (long) page * size;
        List<UserVocabulary> vocabList = vocabularyRepository.findPaginatedByUserIdAndSearchTerm(userId, searchTerm,
                size, offset);
        long totalElements = vocabularyRepository.countByUserIdAndSearchTerm(userId, searchTerm);

        List<VocabResponse> content = vocabList.stream()
                .map(VocabResponse::from)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = page >= totalPages - 1;

        return new PagedVocabResponse(content, page, size, totalElements, totalPages, isLast);
    }

    @Transactional
    public UserVocabulary saveNewWord(VocabSaveRequest request, Long userId) {
        String koreanMeaning = translationService.translateToKorean(request.getExpression());
        UserVocabulary newVocab = new UserVocabulary(userId, request.getExpression(), koreanMeaning, false);
        vocabularyRepository.save(newVocab);
        return newVocab;
    }

    @Transactional
    public void deleteWord(Long wordId, Long userId) {
        if (!vocabularyRepository.existsByIdAndUserId(wordId, userId)) {
            throw new UserUnauthorizedException();
        }

        vocabularyRepository.deleteById(wordId);
    }

    @Transactional
    public void toggleMemorizedStatus(Long wordId, Long userId) {
        UserVocabulary vocab = vocabularyRepository.findByIdAndUserId(wordId, userId);
        if (vocab == null) {
            throw new UserUnauthorizedException();
        }

        vocab.updateMemorized(!vocab.isMemorized());
        vocabularyRepository.updateMemorizedStatus(vocab);
    }

}