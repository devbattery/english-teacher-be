package com.devbattery.englishteacher.vocabulary.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.vocabulary.application.VocabularyService;
import com.devbattery.englishteacher.vocabulary.domain.UserVocabulary;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabResponse;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabSaveRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /**
     * 현재 로그인한 사용자의 전체 단어장을 조회합니다.
     */
    @GetMapping
    public ResponseEntity<List<VocabResponse>> getMyVocabulary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 인증된 사용자가 없으면 401 Unauthorized 응답
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        log.info("Fetching vocabulary for user ID: {}", userId);

        List<UserVocabulary> vocabularyList = vocabularyService.getMyVocabulary(userId);

        // Entity List를 DTO List로 변환
        List<VocabResponse> responseList = vocabularyList.stream()
                .map(VocabResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    /**
     * 새로운 단어를 저장합니다.
     */
    @PostMapping
    public ResponseEntity<VocabResponse> saveWord(
            @RequestBody VocabSaveRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        log.info("User ID {} is saving a new word: '{}'", userId, request.getExpression());

        try {
            UserVocabulary savedWord = vocabularyService.saveNewWord(request, userId);
            // 저장 후 생성된 객체를 DTO로 변환하여 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(VocabResponse.from(savedWord));
        } catch (Exception e) {
            log.error("Error saving word for user ID {}: {}", userId, e.getMessage());
            // 서버 내부 오류 발생 시 500 에러 반환
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "단어 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 단어를 삭제합니다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(
            @PathVariable("id") Long wordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        log.info("User ID {} is attempting to delete word ID: {}", userId, wordId);

        try {
            vocabularyService.deleteWord(wordId, userId);
            // 성공적으로 삭제되면 내용 없이 204 No Content 상태 코드 반환
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            // 권한이 없는 경우 403 Forbidden 에러 반환
            log.warn("Access denied for user ID {} trying to delete word ID {}", userId, wordId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 단어를 삭제할 권한이 없습니다.");
        } catch (Exception e) {
            log.error("Error deleting word ID {} for user ID {}: {}", wordId, userId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "단어 삭제 중 오류가 발생했습니다.");
        }
    }

}