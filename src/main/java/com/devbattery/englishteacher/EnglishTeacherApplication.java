package com.devbattery.englishteacher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class EnglishTeacherApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishTeacherApplication.class, args);
    }

}
