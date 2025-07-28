package com.devbattery.englishteacher.vocabulary.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.common.exception.ServerErrorException;
import com.devbattery.englishteacher.common.exception.UserUnauthorizedException;
import com.devbattery.englishteacher.vocabulary.application.VocabularyService;
import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
import com.devbattery.englishteacher.vocabulary.presentation.dto.PagedVocabResponse;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabResponse;
import com.devbattery.englishteacher.vocabulary.presentation.dto.VocabSaveRequest;
import java.nio.file.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<PagedVocabResponse> fetchMyVocabulary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "searchTerm", required = false) String searchTerm) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PagedVocabResponse response = vocabularyService.fetchMyVocabularyPaginated(userPrincipal.getId(), searchTerm,
                page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<VocabResponse> saveWord(
            @RequestBody VocabSaveRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        UserVocabulary savedWord = vocabularyService.saveNewWord(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(VocabResponse.from(savedWord));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(
            @PathVariable("id") Long wordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        vocabularyService.deleteWord(wordId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-memorized")
    public ResponseEntity<Void> toggleMemorized(
            @PathVariable("id") Long wordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        vocabularyService.toggleMemorizedStatus(wordId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

}