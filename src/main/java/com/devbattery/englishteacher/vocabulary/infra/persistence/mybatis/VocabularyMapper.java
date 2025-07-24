package com.devbattery.englishteacher.vocabulary.infra.persistence.mybatis;

import com.devbattery.englishteacher.vocabulary.domain.UserVocabulary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper // Spring이 이 인터페이스를 MyBatis 매퍼 빈으로 등록합니다.
public interface VocabularyMapper {

    // XML의 <select id="findByUserId">와 매핑
    List<UserVocabulary> findByUserId(Long userId);

    // XML의 <insert id="save">와 매핑
    // UserVocabulary 객체의 id 필드에 생성된 키가 자동으로 채워집니다.
    void save(UserVocabulary vocabulary);

    // XML의 <select id="existsByIdAndUserId">와 매핑
    // 파라미터가 2개 이상일 때는 @Param을 사용해 이름을 명시해주는 것이 좋습니다.
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    // XML의 <delete id="deleteById">와 매핑
    void deleteById(Long id);
}