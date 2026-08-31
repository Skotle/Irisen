package org.java.spring_04.common;

import org.java.spring_04.board.BoardService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContentResponsePolicy {
    private final BoardService boardService;

    public ContentResponsePolicy(BoardService boardService) {
        this.boardService = boardService;
    }

    public Map<String, Object> protectPost(Map<String, Object> row, String uid, String memberDivision) {
        if (row == null) {
            return null;
        }
        return protectRow(row, mayViewNetworkIdentity(text(row.get("gall_id")), uid, memberDivision));
    }

    public List<Map<String, Object>> protectComments(List<Map<String, Object>> rows,
                                                      String gallId,
                                                      String uid,
                                                      String memberDivision) {
        boolean exposeNetworkIdentity = mayViewNetworkIdentity(gallId, uid, memberDivision);
        List<Map<String, Object>> protectedRows = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            protectedRows.add(protectRow(row, exposeNetworkIdentity));
        }
        return protectedRows;
    }

    private Map<String, Object> protectRow(Map<String, Object> row, boolean exposeNetworkIdentity) {
        Map<String, Object> copy = new LinkedHashMap<>(row);
        copy.remove("password");
        copy.remove("password_hash");
        copy.remove("verification_code");
        if (!exposeNetworkIdentity) {
            copy.remove("ip");
        }
        return copy;
    }

    private boolean mayViewNetworkIdentity(String gallId, String uid, String memberDivision) {
        return gallId != null
                && !gallId.isBlank()
                && boardService.hasBoardPermission(gallId, uid, memberDivision, BoardService.PERMISSION_BAN_USER);
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
