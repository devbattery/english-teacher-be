package com.devbattery.englishteacher.acceptance.util;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseCleanup {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<String> tableNames;

    @PostConstruct
    public void init() {
        tableNames = jdbcTemplate.queryForList("SHOW TABLES", String.class);
    }

    @Transactional
    public void execute() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

}