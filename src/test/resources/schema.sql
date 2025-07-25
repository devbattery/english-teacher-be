-- 기존 테이블이 있으면 삭제 (테스트 반복 실행 시 필요)
DROP TABLE IF EXISTS USERS;

-- USERS 테이블 생성
CREATE TABLE USERS
(
    ID        BIGINT AUTO_INCREMENT PRIMARY KEY,
    NAME      VARCHAR(255) NOT NULL,
    EMAIL     VARCHAR(255) NOT NULL UNIQUE,
    IMAGE_URL VARCHAR(255),
    ROLE      VARCHAR(50)  NOT NULL
);



DROP TABLE IF EXISTS LEARNING_CONTENT;

CREATE TABLE LEARNING_CONTENT
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    level        VARCHAR(50)  NOT NULL,            -- 'beginner', 'intermediate' 등 선생님 레벨
    title        VARCHAR(255) NOT NULL,            -- AI가 생성한 글의 제목
    content      TEXT         NOT NULL,            -- AI가 생성한 글의 내용
    created_date DATE         NOT NULL,            -- 생성된 날짜 (매일 콘텐츠를 구분하기 위함)
    UNIQUE KEY uk_level_date (level, created_date) -- 특정 레벨의 콘텐츠는 하루에 하나만 존재하도록 보장
);

ALTER TABLE LEARNING_CONTENT
    ADD COLUMN key_expressions JSON NULL;



CREATE TABLE `USER_VOCABULARY`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       NOT NULL,
    `english_expression` VARCHAR(255) NOT NULL,
    `korean_meaning`     VARCHAR(255) NOT NULL,
    `created_at`         DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    KEY `fk_user_vocabulary_user_id` (`user_id`),
    CONSTRAINT `fk_user_vocabulary_user_id` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;