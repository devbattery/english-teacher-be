package com.devbattery.englishteacher.common.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestDocsController {

    @GetMapping("/docs")
    public ResponseEntity<?> getDocs() {
        Resource resource = new ClassPathResource("static/docs/index.html");

        if (!resource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML).body(resource);
    }

}
