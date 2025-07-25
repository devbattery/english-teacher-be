package com.devbattery.englishteacher.chat.domain.repository;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository {

    // [수정] 이제 level이 아닌 conversationId로 채팅방을 찾습니다.
    Optional<ChatConversation> findById(String conversationId);

    // [신규] 특정 사용자의 레벨별 모든 채팅방을 찾습니다.
    List<ChatConversation> findAllByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    // [신규] 특정 사용자의 레벨별 채팅방 개수를 셉니다. (10개 제한용)
    long countByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    void save(ChatConversation chatConversation);

    // [수정] ID를 기준으로 채팅방을 삭제합니다.
    void deleteById(String conversationId);

    // 아래 두 메소드는 더 이상 직접적으로 사용되지 않거나, 다른 메소드로 대체됩니다.
    // Optional<ChatConversation> fetchByUserIdAndTeacherLevel(Long userId, String teacherLevel);
    // void deleteByUserIdAndTeacherLevel(Long userId, String level);
}