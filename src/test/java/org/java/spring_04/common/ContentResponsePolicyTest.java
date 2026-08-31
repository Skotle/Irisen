package org.java.spring_04.common;

import org.java.spring_04.board.BoardService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ContentResponsePolicyTest {

    @Test
    void publicResponseRemovesSecretsAndNetworkIdentity() {
        ContentResponsePolicy policy = new ContentResponsePolicy(new PermissionBoardService(false));
        Map<String, Object> source = sensitiveRow();

        Map<String, Object> result = policy.protectPost(source, null, null);

        assertThat(result).doesNotContainKeys("password", "password_hash", "verification_code", "ip");
        assertThat(result).containsEntry("title", "safe");
        assertThat(source).containsKeys("password", "ip");
    }

    @Test
    void moderatorMaySeeIpButNeverStoredSecrets() {
        ContentResponsePolicy policy = new ContentResponsePolicy(new PermissionBoardService(true));

        Map<String, Object> result = policy.protectPost(sensitiveRow(), "manager", "user");

        assertThat(result).containsEntry("ip", "203.0.113.10");
        assertThat(result).doesNotContainKeys("password", "password_hash", "verification_code");
    }

    @Test
    void commentProtectionCopiesEveryRow() {
        ContentResponsePolicy policy = new ContentResponsePolicy(new PermissionBoardService(false));
        Map<String, Object> source = sensitiveRow();

        List<Map<String, Object>> result = policy.protectComments(List.of(source), "board", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).doesNotContainKeys("password", "ip");
        assertThat(source).containsKeys("password", "ip");
    }

    private Map<String, Object> sensitiveRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("gall_id", "board");
        row.put("title", "safe");
        row.put("password", "$2a$10$hash");
        row.put("password_hash", "$2a$10$account");
        row.put("verification_code", "123456");
        row.put("ip", "203.0.113.10");
        return row;
    }

    private static final class PermissionBoardService extends BoardService {
        private final boolean allowed;

        private PermissionBoardService(boolean allowed) {
            this.allowed = allowed;
        }

        @Override
        public boolean hasBoardPermission(String gallId, String uid, String memberDivision, String permissionColumn) {
            return allowed;
        }
    }
}
