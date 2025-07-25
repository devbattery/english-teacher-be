package com.devbattery.englishteacher.chat.infra.persistence.mongodb;

import com.devbattery.englishteacher.chat.domain.ChatConversation;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatConversationMongoRepository extends MongoRepository<ChatConversation, String> {

    // [신규] Spring Data MongoDB가 메소드 이름을 분석하여 쿼리를 자동 생성합니다.
    List<ChatConversation> findAllByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    // [신규] 개수를 세는 쿼리 자동 생성
    long countByUserIdAndTeacherLevel(Long userId, String teacherLevel);

    // findById, save, deleteById는 MongoRepository에 이미 내장되어 있습니다.

    // 아래 메소드들은 더 이상 필요 없습니다.
    // Optional<ChatConversation> findByUserIdAndTeacherLevel(Long userId, String teacherLevel);
    // void deleteByUserIdAndTeacherLevel(Long userId, String level);
}