-- 기존 테이블이 있으면 삭제 (테스트 반복 실행 시 필요)
DROP TABLE IF EXISTS USERS;

-- USERS 테이블 생성
CREATE TABLE USERS (
                       ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                       NAME VARCHAR(255) NOT NULL,
                       EMAIL VARCHAR(255) NOT NULL UNIQUE,
                       IMAGE_URL VARCHAR(255),
                       ROLE VARCHAR(50) NOT NULL
);