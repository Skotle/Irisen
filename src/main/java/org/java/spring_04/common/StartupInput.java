package org.java.spring_04.common;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class StartupInput {
    private static final int INPUT_TIMEOUT_SECONDS = 20;
    private static final String DB_NAME = "service_schema";
    private static final String DB_PORT = "3306";
    private static final String COMMON_DB_PARAMS = "serverTimezone=%2B09:00"
            + "&characterEncoding=UTF-8"
            + "&useUnicode=true"
            + "&connectTimeout=20000"
            + "&socketTimeout=20000";
    private static final String CLOUD_DB_PARAMS = COMMON_DB_PARAMS
            + "&sslMode=REQUIRED"
            + "&enabledTLSProtocols=TLSv1.2";
    private static final String LOCAL_DB_PARAMS = COMMON_DB_PARAMS
            + "&useSSL=false"
            + "&allowPublicKeyRetrieval=true";
    private static final BufferedReader STDIN = new BufferedReader(new InputStreamReader(System.in));

    private StartupInput() {
    }

    public static void collectAndApply() {
        String dbMode = firstPresent(
                System.getProperty("app.db.mode"),
                System.getenv("APP_DB_MODE"),
                System.getProperty("APP_DB_MODE")
        ).trim().toLowerCase();
        if ("sqlite".equals(dbMode)) {
            applySqliteRuntime();
            return;
        }

        String serverPort = promptRequired("Server port", false);
        validatePort(serverPort);

        String dbIp = promptRequired("DB IP address", false);
        String dbUser = promptRequired("DB user", false);
        String dbPassword = promptRequired("DB password", true);
        String smtpEmail = promptRequired("SMTP email", false);
        String smtpPassword = promptRequired("SMTP password", true);

        System.setProperty("server.port", serverPort);
        System.setProperty("spring.datasource.url", buildJdbcUrl(dbIp));
        System.setProperty("spring.datasource.username", dbUser);
        System.setProperty("spring.datasource.password", dbPassword);
        System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        System.setProperty("spring.mail.username", smtpEmail);
        System.setProperty("spring.mail.password", smtpPassword);
    }

    private static void applySqliteRuntime() {
        String sqlitePath = firstPresent(
                System.getProperty("app.sqlite.path"),
                System.getenv("APP_SQLITE_DB_PATH"),
                "mydb.db"
        ).trim();
        if (sqlitePath.isEmpty()) {
            sqlitePath = "mydb.db";
        }

        String existingPort = System.getProperty("server.port", "");
        if (existingPort == null || existingPort.isBlank()) {
            System.setProperty("server.port", "8080");
        }

        System.setProperty("spring.profiles.active", appendProfile(System.getProperty("spring.profiles.active"), "sqlite"));
        System.setProperty("spring.datasource.url", sqliteJdbcUrl(sqlitePath));
        System.setProperty("spring.datasource.username", "");
        System.setProperty("spring.datasource.password", "");
        System.setProperty("spring.datasource.driver-class-name", "org.sqlite.JDBC");
    }

    private static String sqliteJdbcUrl(String path) {
        String normalized = path.trim().replace('\\', '/');
        return normalized.startsWith("jdbc:sqlite:") ? normalized : "jdbc:sqlite:" + normalized;
    }

    private static String appendProfile(String current, String profile) {
        String normalizedProfile = profile == null ? "" : profile.trim();
        if (normalizedProfile.isEmpty()) {
            return current == null ? "" : current.trim();
        }
        String normalizedCurrent = current == null ? "" : current.trim();
        if (normalizedCurrent.isEmpty()) {
            return normalizedProfile;
        }
        if (containsProfile(normalizedCurrent, normalizedProfile)) {
            return normalizedCurrent;
        }
        return normalizedCurrent + "," + normalizedProfile;
    }

    private static boolean containsProfile(String current, String profile) {
        for (String token : current.split(",")) {
            if (token != null && token.trim().equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String buildJdbcUrl(String dbIp) {
        String host = dbIp.trim();
        String params = isLocalHost(host) ? LOCAL_DB_PARAMS : CLOUD_DB_PARAMS;
        return "jdbc:mysql://" + host + ":" + DB_PORT + "/" + DB_NAME + "?" + params;
    }

    private static boolean isLocalHost(String host) {
        String normalized = host == null ? "" : host.trim().toLowerCase();
        return "localhost".equals(normalized)
                || "127.0.0.1".equals(normalized)
                || "::1".equals(normalized)
                || "0:0:0:0:0:0:0:1".equals(normalized);
    }

    private static String promptRequired(String label, boolean secret) {
        String value = readWithTimeout(label + ": ", secret);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(label + " is required.");
        }
        return value.trim();
    }

    private static String readWithTimeout(String prompt, boolean secret) {
        System.out.print(prompt);
        System.out.flush();

        ThreadFactory daemonFactory = runnable -> {
            Thread thread = new Thread(runnable, "startup-input");
            thread.setDaemon(true);
            return thread;
        };
        var executor = Executors.newSingleThreadExecutor(daemonFactory);
        try {
            CompletableFuture<String> input = CompletableFuture.supplyAsync(() -> readLine(secret), executor);
            return input.get(INPUT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Startup input timed out after " + INPUT_TIMEOUT_SECONDS + " seconds.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Startup input was interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Startup input failed.", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private static String readLine(boolean secret) {
        Console console = System.console();
        if (console != null) {
            if (secret) {
                char[] password = console.readPassword();
                return password == null ? "" : new String(password);
            }
            String line = console.readLine();
            return line == null ? "" : line;
        }

        try {
            return STDIN.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("Startup input read failed.", e);
        }
    }

    private static void validatePort(String rawPort) {
        try {
            int port = Integer.parseInt(rawPort);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Server port must be a number between 1 and 65535.");
        }
    }
}
