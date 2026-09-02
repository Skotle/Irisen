package org.java.spring_04.auth;

import jakarta.annotation.PostConstruct;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeUserSchema() {
        if (isSqliteRuntime()) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE user ADD COLUMN nick_type VARCHAR(20) NOT NULL DEFAULT 'variable'");
        } catch (Exception ignored) {
        }
    }

    private boolean isSqliteRuntime() {
        String datasourceUrl = System.getProperty("spring.datasource.url", "");
        return datasourceUrl.toLowerCase().startsWith("jdbc:sqlite:");
    }

    public Optional<UserEntity> findByIdentifier(String identifier) {
        String sql = """
                SELECT uid, nick, nick_type, nick_icon_type, password_hash, email, member_division
                FROM user
                WHERE uid = ? OR LOWER(email) = LOWER(?)
                """;
        try {
            UserEntity user = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(UserEntity.class),
                    identifier,
                    identifier
            );
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        String sql = """
                SELECT uid, nick, nick_type, nick_icon_type, password_hash, email, member_division
                FROM user
                WHERE LOWER(email) = LOWER(?)
                ORDER BY created_at ASC
                """;
        List<UserEntity> users = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(UserEntity.class),
                email
        );
        return users.size() == 1 ? Optional.of(users.get(0)) : Optional.empty();
    }

    public boolean existsByUid(String uid) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE uid = ?",
                Integer.class,
                uid
        );
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE LOWER(email) = LOWER(?)",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }

    public boolean existsByNick(String nick) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE nick = ?",
                Integer.class,
                nick
        );
        return count != null && count > 0;
    }

    public boolean existsByFixedNick(String nick) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE nick = ? AND COALESCE(nick_type, 'variable') = 'fixed'",
                Integer.class,
                nick
        );
        return count != null && count > 0;
    }

    public void save(UserEntity user) {
        String normalizedNickType = user.getNickType() == null || user.getNickType().isBlank()
                ? "variable"
                : user.getNickType().trim().toLowerCase();
        String normalizedNickIconType = user.getNickIconType() == null || user.getNickIconType().isBlank()
                ? "default"
                : user.getNickIconType().trim().toLowerCase();
        String sql = """
                INSERT INTO user (uid, nick, password_hash, email, nick_type, nick_icon_type, member_division)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                user.getUid(),
                user.getNick(),
                user.getPasswordHash(),
                user.getEmail() == null ? null : user.getEmail().trim().toLowerCase(),
                normalizedNickType,
                normalizedNickIconType,
                "user"
        );
    }

    public int updatePassword(String uid, String passwordHash) {
        return jdbcTemplate.update("UPDATE user SET password_hash = ? WHERE uid = ?", passwordHash, uid);
    }

    public String findMemberDivision(String uid) {
        List<String> rows = jdbcTemplate.query(
                "SELECT member_division FROM user WHERE uid = ?",
                (resultSet, rowNum) -> resultSet.getString(1),
                uid
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Removes private account data while preserving public discussions under an anonymized author.
     * This method is called inside AuthService's account-action transaction.
     */
    public int deleteAccount(String uid) {
        updateIfTableExists("board", "UPDATE board SET manager_uid = NULL WHERE manager_uid = ?", uid);
        updateIfTableExists("gallery_setting", "UPDATE gallery_setting SET updated_by = NULL WHERE updated_by = ?", uid);
        updateIfTableExists("post", "UPDATE post SET concept_cancelled_by = NULL WHERE concept_cancelled_by = ?", uid);
        updateIfTableExists("post", "UPDATE post SET writer_uid = NULL, name = '탈퇴한 사용자' WHERE writer_uid = ?", uid);
        updateIfTableExists("comment", "UPDATE comment SET writer_uid = NULL, name = '탈퇴한 사용자' WHERE writer_uid = ?", uid);

        updateIfTableExists("board_member", "UPDATE board_member SET approved_by = NULL WHERE approved_by = ?", uid);
        updateIfTableExists("board_join_request", "UPDATE board_join_request SET reviewed_by = NULL WHERE reviewed_by = ?", uid);
        updateIfTableExists("board_request", "UPDATE board_request SET reviewed_by = NULL WHERE reviewed_by = ?", uid);
        updateIfTableExists("board_submanager", "UPDATE board_submanager SET appointed_by = NULL WHERE appointed_by = ?", uid);
        updateIfTableExists("forbidden_word", "UPDATE forbidden_word SET created_by = NULL WHERE created_by = ?", uid);
        updateIfTableExists("moderation_log", "UPDATE moderation_log SET actor_uid = NULL WHERE actor_uid = ?", uid);
        updateIfTableExists("moderation_log", "UPDATE moderation_log SET target_uid = NULL WHERE target_uid = ?", uid);
        updateIfTableExists("user_suspension", "UPDATE user_suspension SET suspended_by = NULL WHERE suspended_by = ?", uid);

        deleteIfTableExists("alarm", "DELETE FROM alarm WHERE uid = ?", uid);
        deleteIfTableExists("board_ban", "DELETE FROM board_ban WHERE target_uid = ? OR banned_by = ?", uid, uid);
        deleteIfTableExists("board_join_request", "DELETE FROM board_join_request WHERE uid = ?", uid);
        deleteIfTableExists("board_member", "DELETE FROM board_member WHERE uid = ?", uid);
        deleteIfTableExists("board_request", "DELETE FROM board_request WHERE requester_uid = ?", uid);
        deleteIfTableExists("board_submanager", "DELETE FROM board_submanager WHERE uid = ?", uid);
        deleteIfTableExists("board_transfer", "DELETE FROM board_transfer WHERE from_uid = ? OR to_uid = ?", uid, uid);
        deleteIfTableExists("post_scrap", "DELETE FROM post_scrap WHERE uid = ?", uid);
        deleteIfTableExists("user_block", "DELETE FROM user_block WHERE blocker_uid = ? OR blocked_uid = ?", uid, uid);
        deleteIfTableExists("user_follow", "DELETE FROM user_follow WHERE follower_uid = ? OR following_uid = ?", uid, uid);
        deleteIfTableExists("user_notification_setting", "DELETE FROM user_notification_setting WHERE uid = ?", uid);
        deleteIfTableExists("user_profile_setting", "DELETE FROM user_profile_setting WHERE uid = ?", uid);
        deleteIfTableExists("user_profile", "DELETE FROM user_profile WHERE uid = ?", uid);
        deleteIfTableExists("user_suspension", "DELETE FROM user_suspension WHERE uid = ?", uid);
        deleteIfTableExists("account_verification", "DELETE FROM account_verification WHERE uid = ?", uid);
        deleteIfTableExists("signup_verification", "DELETE FROM signup_verification WHERE uid = ? OR LOWER(email) = LOWER((SELECT email FROM user WHERE uid = ?))", uid, uid);

        String actorKey = "uid:" + uid;
        deleteIfTableExists("comment_reaction", "DELETE FROM comment_reaction WHERE actor_key = ?", actorKey);
        deleteIfTableExists("comment_report", "DELETE FROM comment_report WHERE reporter_key = ?", actorKey);
        deleteIfTableExists("post_report", "DELETE FROM post_report WHERE reporter_key = ?", actorKey);
        deleteIfTableExists("post_vote", "DELETE FROM post_vote WHERE actor_key = ?", actorKey);

        return jdbcTemplate.update("DELETE FROM user WHERE uid = ?", uid);
    }

    private void updateIfTableExists(String tableName, String sql, Object... args) {
        if (tableExists(tableName)) {
            jdbcTemplate.update(sql, args);
        }
    }

    private void deleteIfTableExists(String tableName, String sql, Object... args) {
        updateIfTableExists(tableName, sql, args);
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet tables = metadata.getTables(null, null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    if (tableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        });
        return Boolean.TRUE.equals(exists);
    }
}
