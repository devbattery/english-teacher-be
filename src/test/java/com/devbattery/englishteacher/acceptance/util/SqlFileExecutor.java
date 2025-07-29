package com.devbattery.englishteacher.acceptance.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SqlFileExecutor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void execute(String sqlFilePath) {
        try {
            Resource resource = new ClassPathResource(sqlFilePath);
            String sqlScript = readFromInputStream(resource.getInputStream());

            String[] queries = sqlScript.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    jdbcTemplate.execute(query);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute SQL file: " + sqlFilePath, e);
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

}