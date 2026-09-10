package org.java.spring_04.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MemorySqliteDataSourceTest {
    @TempDir Path directory;

    @Test
    void sharesFreshTablesAcrossConnectionsAndExportsOnlyOnClose() throws Exception {
        Path file = directory.resolve("saved.sqlite");
        String url = "jdbc:sqlite:file:" + UUID.randomUUID() + "?mode=memory&cache=shared";
        try (var source = new MemorySqliteDataSource(url, file.toString())) {
            try (var connection = source.getConnection(); var statement = connection.createStatement()) {
                try (var result = statement.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table'")) {
                    assertTrue(result.next());
                    assertEquals(39, result.getInt(1));
                }
                statement.executeUpdate("INSERT INTO board(gall_id,gall_name) VALUES ('test','메모리 테스트')");
            }
            try (var connection = source.getConnection(); var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT gall_name FROM board")) {
                assertTrue(result.next());
                assertEquals("메모리 테스트", result.getString(1));
            }
            assertFalse(Files.exists(file));
        }
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file);
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA integrity_check")) {
                assertEquals("ok", result.getString(1));
            }
            try (var result = statement.executeQuery("SELECT gall_name FROM board")) {
                assertEquals("메모리 테스트", result.getString(1));
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new MemorySqliteDataSource(url, file.toString()));
    }
}
