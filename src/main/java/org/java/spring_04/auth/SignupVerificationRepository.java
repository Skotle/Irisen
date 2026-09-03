package org.java.spring_04.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Repository
public class SignupVerificationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        if (isSqliteRuntime()) {
            return;
        }
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS signup_verification (
                    request_id BIGINT NOT NULL AUTO_INCREMENT,
                    uid VARCHAR(50) NOT NULL,
                    nick VARCHAR(100) NOT NULL,
                    email VARCHAR(191) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    nick_type VARCHAR(20) NOT NULL DEFAULT 'variable',
                    verification_code VARCHAR(20) NOT NULL,
                    expires_at DATETIME NOT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (request_id),
                    UNIQUE KEY uk_signup_verification_uid (uid),
                    UNIQUE KEY uk_signup_verification_email (email),
                    INDEX idx_signup_verification_expires (expires_at)
                )
                """);
        try {
            jdbcTemplate.execute("ALTER TABLE signup_verification ADD COLUMN nick_type VARCHAR(20) NOT NULL DEFAULT 'variable'");
        } catch (Exception ignored) {
        }
        ensureIndex("idx_signup_verification_expires", "expires_at");
    }

    private boolean isSqliteRuntime() {
        String datasourceUrl = System.getProperty("spring.datasource.url", "");
        return datasourceUrl.toLowerCase().startsWith("jdbc:sqlite:");
    }

    public void upsertPendingSignup(String uid, String nick, String email, String passwordHash, String nickType, String code, LocalDateTime expiresAt) {
        String normalizedEmail = normalizeEmail(email);
        jdbcTemplate.update("""
                INSERT INTO signup_verification (uid, nick, email, password_hash, nick_type, verification_code, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    nick = VALUES(nick),
                    email = VALUES(email),
                    password_hash = VALUES(password_hash),
                    nick_type = VALUES(nick_type),
                    verification_code = VALUES(verification_code),
                    expires_at = VALUES(expires_at),
                    created_at = CURRENT_TIMESTAMP
                """, uid, nick, normalizedEmail, passwordHash, nickType, code, expiresAt);
    }

    public Map<String, Object> findByUidAndEmail(String uid, String email) {
        try {
            return jdbcTemplate.queryForMap("""
                    SELECT request_id, uid, nick, email, password_hash, nick_type, verification_code, expires_at
                    FROM signup_verification
                    WHERE uid = ? AND email = ?
                    """, uid, normalizeEmail(email));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteByUid(String uid) {
        jdbcTemplate.update("DELETE FROM signup_verification WHERE uid = ?", uid);
    }

    public int deleteExpired() {
        return jdbcTemplate.update("DELETE FROM signup_verification WHERE expires_at < ?", LocalDateTime.now());
    }

    private void ensureIndex(String indexName, String columns) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'signup_verification'
                  AND index_name = ?
                """, Integer.class, indexName);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE signup_verification ADD INDEX " + indexName + " (" + columns + ")");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
