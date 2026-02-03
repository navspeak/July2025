package com.example.multithreading.orderentry.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdempotencyRepository {

    private final JdbcTemplate jdbc;

    public IdempotencyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @return true if this is the first time we see (target, key). false if duplicate.
     */
    public boolean tryInsert(String target, String key) {
        try {
            jdbc.update(
                    "INSERT INTO target_idempotency(target, idempotency_key) VALUES (?, ?)",
                    target, key
            );
            return true;
        } catch (DuplicateKeyException dup) {
            return false;
        }
    }
}
