package com.devbattery.englishteacher.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileStorageProperties {

    private String uploadDir;
    private String uploadUrlPrefix;

}
