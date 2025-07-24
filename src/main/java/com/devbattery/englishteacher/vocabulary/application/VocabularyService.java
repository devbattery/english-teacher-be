package com.devbattery.englishteacher.vocabulary.application;

// import com.devbattery.englishteacher.user.domain.User; <-- User Entity 직접 참조 불필요
// import com.devbattery.englishteacher.user.domain.repository.UserRepository; <-- User Repository 불필요
import com.devbattery.englishteacher.vocabulary.domain.UserVocabulary;
// import com.devbattery.englishteacher.vocabulary.domain.repository.UserVocabularyRepository; <-- JPA Repository 대신 Mapper 사용
import com.devbattery.englishteacher.vocabulary.infra.persistence.mybatis.VocabularyMapper; // MyBatis Mapper 임포트
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabSaveRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    // JPA Repository 대신 MyBatis Mapper를 주입받습니다.
    private final VocabularyMapper vocabularyMapper; 
    private final GeminiTranslationService translationService;

    @Transactional(readOnly = true)
    public List<UserVocabulary> getMyVocabulary(Long userId) {
        return vocabularyMapper.findByUserId(userId);
    }

    @Transactional
    public UserVocabulary saveNewWord(VocabSaveRequest request, Long userId) {
        // User 객체를 찾을 필요 없이, userId만 있으면 됩니다.
        
        // Gemini를 통해 번역
        String koreanMeaning = translationService.translateToKorean(request.getExpression());

        // UserVocabulary 객체 생성
        UserVocabulary newVocab = new UserVocabulary(userId, request.getExpression(), koreanMeaning);

        // Mapper를 통해 DB에 저장
        vocabularyMapper.save(newVocab);
        
        // save 메소드 호출 후 newVocab 객체에는 DB에서 생성된 id가 채워져 있습니다.
        return newVocab;
    }

    @Transactional
    public void deleteWord(Long wordId, Long userId) throws AccessDeniedException {
        // 본인의 단어인지 확인
        if (!vocabularyMapper.existsByIdAndUserId(wordId, userId)) {
            throw new AccessDeniedException("You do not have permission to delete this word.");
        }
        vocabularyMapper.deleteById(wordId);
    }
}