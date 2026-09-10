package org.java.spring_04.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** The anchor keeps the shared in-process database alive between JDBC requests. */
public final class MemorySqliteDataSource extends DriverManagerDataSource implements AutoCloseable {
    private final Connection anchor;
    private final Path exportPath;
    private boolean closed;

    public MemorySqliteDataSource(String url, String destination) throws Exception {
        if (!url.startsWith("jdbc:sqlite:file:") || !url.contains("mode=memory")
                || !url.contains("cache=shared")) {
            throw new IllegalArgumentException("A shared SQLite memory URL is required.");
        }
        exportPath = Path.of(destination == null || destination.isBlank()
                ? "snapshots/irisen-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + "-" + UUID.randomUUID() + ".sqlite"
                : destination).toAbsolutePath().normalize();
        if (Files.exists(exportPath)) {
            throw new IllegalArgumentException("Export file already exists: " + exportPath);
        }
        Files.createDirectories(exportPath.getParent());
        setDriverClassName("org.sqlite.JDBC");
        setUrl(url);
        anchor = getConnection();
        try {
            ScriptUtils.executeSqlScript(anchor, new ClassPathResource("schema-memory.sql"));
        } catch (Exception failure) {
            anchor.close();
            throw failure;
        }
        System.out.println("[MEMORY DB] Fresh schema ready. Shutdown export: " + exportPath);
    }

    // Spring destroys dependent services before closing this DataSource.
    @Override
    public synchronized void close() throws Exception {
        if (closed) return;
        try {
            // VACUUM INTO creates a consistent SQLite file and refuses to replace existing data.
            try (var statement = anchor.prepareStatement("VACUUM main INTO ?")) {
                statement.setString(1, exportPath.toString());
                statement.execute();
            }
            System.out.println("[MEMORY DB] Saved: " + exportPath);
        } catch (Exception failure) {
            System.err.println("[MEMORY DB] EXPORT FAILED: " + exportPath + " (" + failure.getMessage() + ")");
            throw failure;
        } finally {
            closed = true;
            anchor.close();
        }
    }
}
