package com.devbattery.englishteacher.chat.domain.repository;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository {

    Optional<ChatConversation> findById(String conversationId);

    List<ChatConversation> findAllByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    long countByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    void save(ChatConversation chatConversation);

    void deleteById(String conversationId);

}