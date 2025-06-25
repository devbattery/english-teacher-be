package com.devbattery.englishteacher.chat.infra.persistence.mongodb;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatConversationMongoRepository extends MongoRepository<ChatConversation, String> {

    Optional<ChatConversation> findByUserIdAndTeacherLevel(Long userId, String teacherLevel);

}
