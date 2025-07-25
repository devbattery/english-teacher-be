package com.devbattery.englishteacher.chat.application.service;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.common.exception.ChatRoomNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatConversationService {

    private final ChatConversationRepository chatConversationRepository;

    public ChatConversation findById(String conversationId) {
        return chatConversationRepository.findById(conversationId)
                .orElseThrow(ChatRoomNotFoundException::new);
    }

    public List<ChatConversation> fetchAllByUserIdAndTeacherLevel(Long userId, String teacherLevel) {
        return chatConversationRepository.findAllByUserIdAndTeacherLevel(userId, teacherLevel);
    }

    public long countByUserIdAndTeacherLevel(Long userId, String teacherLevel) {
        return chatConversationRepository.countByUserIdAndTeacherLevel(userId, teacherLevel);
    }

    public void save(ChatConversation chatConversation) {
        chatConversationRepository.save(chatConversation);
    }

    public void deleteById(String conversationId) {
        chatConversationRepository.deleteById(conversationId);
    }

}
