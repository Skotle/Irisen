package org.java.spring_04.common;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Configuration
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final int STARTUP_TIMEOUT_SECONDS = 20;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name:}")
    private String datasourceDriverClassName;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String configuredUrl = normalizeDatasourceUrl(datasourceUrl);
        if (configuredUrl.isEmpty()) {
            throw new IllegalStateException("spring.datasource.url is required when running with MySQL.");
        }

        String driverClassName = datasourceDriverClassName == null || datasourceDriverClassName.isBlank()
                ? "com.mysql.cj.jdbc.Driver"
                : datasourceDriverClassName.trim();
        String configuredUsername = datasourceUsername == null ? "" : datasourceUsername.trim();
        if (configuredUsername.isEmpty()) {
            throw new IllegalStateException("spring.datasource.username is required when running with MySQL.");
        }
        String configuredPassword = datasourcePassword == null ? "" : datasourcePassword;
        if (configuredPassword.isEmpty()) {
            configuredPassword = promptDatabasePassword();
        }

        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(configuredUrl);
        dataSource.setUsername(configuredUsername);
        dataSource.setPassword(configuredPassword);

        if (driverClassName.contains("mysql")) {
            Properties properties = new Properties();
            properties.setProperty("useUnicode", "true");
            properties.setProperty("characterEncoding", "UTF-8");
            properties.setProperty("characterSetResults", "utf8mb4");
            properties.setProperty("connectionCollation", "utf8mb4_unicode_ci");
            properties.setProperty("serverTimezone", "+09:00");
            properties.setProperty("connectTimeout", String.valueOf(STARTUP_TIMEOUT_SECONDS * 1000));
            properties.setProperty("socketTimeout", String.valueOf(STARTUP_TIMEOUT_SECONDS * 1000));
            if (isLocalDatasource(configuredUrl)) {
                properties.setProperty("useSSL", "false");
                properties.setProperty("allowPublicKeyRetrieval", "true");
            } else {
                properties.setProperty("sslMode", "REQUIRED");
                properties.setProperty("enabledTLSProtocols", "TLSv1.2");
            }
            dataSource.setConnectionProperties(properties);
        }

        log.info("Datasource configured. driver={} database={}", driverClassName, databaseName(configuredUrl));
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private String databaseName(String jdbcUrl) {
        try {
            String uriText = jdbcUrl.replaceFirst("^jdbc:", "");
            URI uri = URI.create(uriText);
            String path = uri.getPath();
            if (path == null || path.length() <= 1) {
                return "unknown";
            }
            return path.substring(1);
        } catch (Exception ignored) {
            return "unknown";
        }
    }

    private String normalizeDatasourceUrl(String rawUrl) {
        String value = rawUrl == null ? "" : rawUrl.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("jdbc:")) {
            return value;
        }
        if (value.startsWith("mysql://")) {
            return "jdbc:" + value;
        }
        throw new IllegalStateException("spring.datasource.url must be a JDBC URL: " + value);
    }

    private boolean isLocalDatasource(String jdbcUrl) {
        String normalized = jdbcUrl == null ? "" : jdbcUrl.toLowerCase();
        return normalized.contains("//localhost:")
                || normalized.contains("//127.0.0.1:")
                || normalized.contains("//[::1]:");
    }

    private String promptDatabasePassword() {
        System.out.print("DB password for " + datasourceUsername + ": ");
        System.out.flush();

        ThreadFactory daemonFactory = runnable -> {
            Thread thread = new Thread(runnable, "db-password-input");
            thread.setDaemon(true);
            return thread;
        };
        var executor = Executors.newSingleThreadExecutor(daemonFactory);
        try {
            CompletableFuture<String> input = CompletableFuture.supplyAsync(this::readDatabasePassword, executor);
            String password = input.get(STARTUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (password == null || password.isEmpty()) {
                throw new IllegalStateException("DB password was not provided.");
            }
            return password;
        } catch (TimeoutException e) {
            throw new IllegalStateException("DB password input timed out after " + STARTUP_TIMEOUT_SECONDS + " seconds.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DB password input was interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("DB password input failed.", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private String readDatabasePassword() {
        if (System.console() != null) {
            char[] password = System.console().readPassword();
            return password == null ? "" : new String(password);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String password = reader.readLine();
            return password == null ? "" : password;
        } catch (IOException e) {
            throw new IllegalStateException("DB password input failed.", e);
        }
    }
}
