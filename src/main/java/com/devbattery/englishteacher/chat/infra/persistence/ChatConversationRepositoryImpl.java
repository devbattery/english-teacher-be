package com.devbattery.englishteacher.chat.infra.persistence;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import com.devbattery.englishteacher.chat.domain.repository.ChatConversationRepository;
import com.devbattery.englishteacher.chat.infra.persistence.mongodb.ChatConversationMongoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatConversationRepositoryImpl implements ChatConversationRepository {

    private final ChatConversationMongoRepository mongoRepository;

    @Override
    public Optional<ChatConversation> findById(String conversationId) {
        return mongoRepository.findById(conversationId);
    }

    @Override
    public List<ChatConversation> findAllByUserIdAndTeacherLevel(Long userId, String teacherLevel) {
        return mongoRepository.findAllByUserIdAndTeacherLevel(userId, teacherLevel);
    }

    @Override
    public long countByUserIdAndTeacherLevel(Long userId, String teacherLevel) {
        return mongoRepository.countByUserIdAndTeacherLevel(userId, teacherLevel);
    }

    @Override
    public void save(ChatConversation chatConversation) {
        mongoRepository.save(chatConversation);
    }

    @Override
    public void deleteById(String conversationId) {
        mongoRepository.deleteById(conversationId);
    }

}