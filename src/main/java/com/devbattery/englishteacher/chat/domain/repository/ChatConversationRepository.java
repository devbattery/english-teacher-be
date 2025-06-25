package com.devbattery.englishteacher.chat.domain.repository;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.Optional;

public interface ChatConversationRepository {

    Optional<ChatConversation> fetchByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    void save(ChatConversation chatConversation);

}
