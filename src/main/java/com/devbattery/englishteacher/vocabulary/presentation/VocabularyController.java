package com.devbattery.englishteacher.vocabulary.presentation;

import com.devbattery.englishteacher.auth.domain.UserPrincipal;
import com.devbattery.englishteacher.common.exception.ServerErrorException;
import com.devbattery.englishteacher.common.exception.UserUnauthorizedException;
import com.devbattery.englishteacher.vocabulary.application.VocabularyService;
import com.devbattery.englishteacher.vocabulary.domain.entity.UserVocabulary;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public ResponseEntity<List<VocabResponse>> getMyVocabulary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();
        List<VocabResponse> responseList = vocabularyService.fetchMyVocabulary(userId)
                .stream()
                .map(VocabResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @PostMapping
    public ResponseEntity<VocabResponse> saveWord(
            @RequestBody VocabSaveRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();

        try {
            UserVocabulary savedWord = vocabularyService.saveNewWord(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(VocabResponse.from(savedWord));
        } catch (Exception e) {
            throw new ServerErrorException();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(
            @PathVariable("id") Long wordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userPrincipal.getId();

        try {
            vocabularyService.deleteWord(wordId, userId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            throw new UserUnauthorizedException();
        } catch (Exception e) {
            throw new ServerErrorException();
        }
    }

    @PatchMapping("/{id}/toggle-memorized")
    public ResponseEntity<Void> toggleMemorized(
            @PathVariable("id") Long wordId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            vocabularyService.toggleMemorizedStatus(wordId, userPrincipal.getId());
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            throw new UserUnauthorizedException();
        } catch (Exception e) {
            throw new ServerErrorException();
        }
    }

}