package org.java.spring_04.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.Map;

@Repository
public class AccountVerificationRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountVerificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        if (isSqliteRuntime()) {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS account_verification (
                        request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uid VARCHAR(50) NOT NULL,
                        email VARCHAR(191) NOT NULL,
                        action_type VARCHAR(30) NOT NULL,
                        password_hash VARCHAR(255) NULL,
                        verification_code VARCHAR(255) NOT NULL,
                        expires_at DATETIME NOT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            return;
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS account_verification (
                    request_id BIGINT NOT NULL AUTO_INCREMENT,
                    uid VARCHAR(50) NOT NULL,
                    email VARCHAR(191) NOT NULL,
                    action_type VARCHAR(30) NOT NULL,
                    password_hash VARCHAR(255) NULL,
                    verification_code VARCHAR(255) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (request_id),
                    INDEX idx_account_verification_lookup (email, action_type, created_at)
                )
                """);
        try {
            jdbcTemplate.execute("ALTER TABLE account_verification MODIFY verification_code VARCHAR(255) NOT NULL");
        } catch (Exception ignored) {
            // The column is already large enough or the database does not require this migration.
        }
    }

    private boolean isSqliteRuntime() {
        Boolean sqlite = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            String productName = metadata.getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("sqlite");
        });
        return Boolean.TRUE.equals(sqlite);
    }

    @Transactional
    public void replace(String uid,
                        String email,
                        String actionType,
                        String passwordHash,
                        String verificationCodeHash,
                        LocalDateTime expiresAt) {
        deleteByEmailAndAction(email, actionType);
        jdbcTemplate.update("""
                INSERT INTO account_verification (
                    uid, email, action_type, password_hash, verification_code, expires_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, uid, email, actionType, passwordHash, verificationCodeHash, expiresAt);
    }

    public Map<String, Object> findLatest(String email, String actionType) {
        try {
            return jdbcTemplate.queryForMap("""
                    SELECT request_id, uid, email, action_type, password_hash, verification_code, expires_at
                    FROM account_verification
                    WHERE LOWER(email) = LOWER(?) AND action_type = ?
                    ORDER BY created_at DESC, request_id DESC
                    LIMIT 1
                    """, email, actionType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int deleteByRequestId(long requestId) {
        return jdbcTemplate.update("DELETE FROM account_verification WHERE request_id = ?", requestId);
    }

    public void deleteByEmailAndAction(String email, String actionType) {
        jdbcTemplate.update(
                "DELETE FROM account_verification WHERE LOWER(email) = LOWER(?) AND action_type = ?",
                email,
                actionType
        );
    }

    public void deleteExpired() {
        jdbcTemplate.update("DELETE FROM account_verification WHERE expires_at < ?", LocalDateTime.now());
    }
}
