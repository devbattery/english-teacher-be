package com.devbattery.englishteacher.chat.infra.persistence.mongodb;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatConversationMongoRepository extends MongoRepository<ChatConversation, String> {

    List<ChatConversation> findAllByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    long countByUserIdAndTeacherLevel(Long userId, String teacherLevel);

}