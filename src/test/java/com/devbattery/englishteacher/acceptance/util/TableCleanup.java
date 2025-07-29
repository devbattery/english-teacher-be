package com.devbattery.englishteacher.acceptance.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableCleanup {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String tableName;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Transactional
    public void execute() {
        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
    }
}