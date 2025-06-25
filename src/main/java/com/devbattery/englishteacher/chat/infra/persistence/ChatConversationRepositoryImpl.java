package com.devbattery.englishteacher.chat.infra.persistence;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.chat.infra.persistence.mongodb.ChatConversationMongoRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatConversationRepositoryImpl implements ChatConversationRepository {

    private final ChatConversationMongoRepository chatConversationMongoRepository;

    @Override
    public Optional<ChatConversation> fetchByUserIdAndTeacherLevel(Long userId, String teacherLevel) {
        return chatConversationMongoRepository.findByUserIdAndTeacherLevel(userId, teacherLevel);
    }

    @Override
    public void save(ChatConversation chatConversation) {
        chatConversationMongoRepository.save(chatConversation);
    }

}
