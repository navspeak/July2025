package com.example.hello.jpa.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AuditMappingRepository{

    private final JdbcTemplate jdbcTemplate;


    public AuditMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsert(Long auditId, List<Long> transactionIds){
        String sql = "INSERT INTO AUDIT_MAPPING (AUDIT_ID, TID, CREATED_TIMESTAMP) values (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, transactionIds, 500, ((ps, transactionId) -> {
            ps.setLong(1, auditId);
            ps.setLong(2, transactionId);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        }));
    }
}