package org.java.spring_04.common;

import jakarta.annotation.PreDestroy;
import org.java.spring_04.auth.AccountVerificationRepository;
import org.java.spring_04.auth.SignupVerificationRepository;
import org.java.spring_04.board.BoardService;
import org.java.spring_04.feature.FeatureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ConsoleCommandRunner {
    private static final int DEFAULT_ALARM_RETENTION_DAYS = 90;
    private static final int MAX_ALARM_RETENTION_DAYS = 3650;
    private static final Map<String, EnvironmentVariable> EDITABLE_ENVIRONMENT = editableEnvironment();

    private final ConfigurableApplicationContext applicationContext;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final BoardService boardService;
    private final FeatureService featureService;
    private final AccountVerificationRepository accountVerificationRepository;
    private final SignupVerificationRepository signupVerificationRepository;
    private final boolean enabled;
    private final BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    private volatile boolean running;
    private ExecutorService executor;

    public ConsoleCommandRunner(ConfigurableApplicationContext applicationContext,
                                JdbcTemplate jdbcTemplate,
                                DataSource dataSource,
                                BoardService boardService,
                                FeatureService featureService,
                                AccountVerificationRepository accountVerificationRepository,
                                SignupVerificationRepository signupVerificationRepository,
                                @Value("${app.console.enabled:true}") boolean enabled) {
        this.applicationContext = applicationContext;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
        this.boardService = boardService;
        this.featureService = featureService;
        this.accountVerificationRepository = accountVerificationRepository;
        this.signupVerificationRepository = signupVerificationRepository;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!enabled || running) {
            return;
        }
        running = true;
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "console-command");
            thread.setDaemon(true);
            return thread;
        });
        executor.execute(this::readCommands);
    }

    private void readCommands() {
        PrintStream output = System.out;
        output.println("[CONSOLE] 명령 입력이 활성화되었습니다. 사용 가능한 명령은 help를 입력하세요.");
        while (running && applicationContext.isActive()) {
            try {
                output.print("irisen> ");
                output.flush();
                String line = input.readLine();
                if (line == null || !execute(line, output)) {
                    break;
                }
            } catch (IOException e) {
                output.println("[CONSOLE] 입력을 읽지 못했습니다: " + messageOf(e));
                break;
            } catch (RuntimeException e) {
                output.println("[CONSOLE] 명령 실행 실패: " + messageOf(e));
            }
        }
        running = false;
    }

    boolean execute(String rawCommand, PrintStream output) {
        String normalized = rawCommand == null ? "" : rawCommand.trim();
        if (normalized.isEmpty()) {
            return true;
        }

        String[] tokens = normalized.split("\\s+");
        String command = tokens[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "help", "commands", "도움말" -> printHelp(output);
            case "status", "health", "상태" -> printStatus(output);
            case "stats", "통계" -> printStats(output);
            case "sync-post-counts", "게시글수동기화" -> syncPostCounts(output);
            case "refresh-rankings", "랭킹갱신" -> refreshRankings(output);
            case "dormancy-check", "휴면점검" -> runDormancyCheck(output);
            case "cleanup-expired", "만료정리" -> cleanupExpired(output);
            case "cleanup-alarms", "알림정리" -> cleanupAlarms(tokens, output);
            case "env", "환경변수" -> manageEnvironment(normalized, tokens, output);
            case "exit", "quit", "종료" -> {
                output.println("[CONSOLE] 애플리케이션을 정상 종료합니다.");
                running = false;
                applicationContext.close();
                return false;
            }
            default -> output.println("[CONSOLE] 알 수 없는 명령입니다: " + tokens[0] + " (help로 목록 확인)");
        }
        return true;
    }

    private void printHelp(PrintStream output) {
        output.println("""
                사용 가능한 명령:
                  help                         명령 목록 출력
                  status                       애플리케이션과 DB 연결 상태 확인
                  stats                        보드/게시글/댓글/사용자/알림 건수 출력
                  sync-post-counts             보드별 게시글 수 재동기화
                  refresh-rankings             보드 랭킹 즉시 갱신 (MySQL 전용)
                  dormancy-check               휴면 보드 점검 즉시 실행 (MySQL 전용)
                  cleanup-expired              만료된 가입·계정 인증 요청 삭제
                  cleanup-alarms [보존일수]     지정 일수보다 오래된 알림 삭제 (기본 90일)
                  env list                     수정 가능한 환경변수와 현재 상태 출력
                  env get <이름>               환경변수 조회 (보안 값은 마스킹)
                  env set <이름> <값>          현재 프로세스에 런타임 오버라이드 적용
                  env unset <이름>              런타임 오버라이드 제거
                  exit                         애플리케이션 정상 종료

                env set 값은 현재 프로세스에서만 유지됩니다.
                DB/SMTP/포트처럼 시작 시 고정되는 항목은 현재 실행에 반영되지 않으며,
                다음 실행 환경에도 별도로 같은 값을 설정해야 합니다.

                한국어 별칭: 도움말, 상태, 통계, 게시글수동기화, 랭킹갱신,
                             휴면점검, 만료정리, 알림정리, 환경변수, 종료
                """);
    }

    private void printStatus(PrintStream output) {
        String databaseStatus = "DOWN";
        String databaseProduct = "unknown";
        try (Connection connection = dataSource.getConnection()) {
            databaseProduct = connection.getMetaData().getDatabaseProductName();
            databaseStatus = connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            databaseProduct = "unavailable (" + messageOf(e) + ")";
        }
        output.println("application=UP");
        output.println("database=" + databaseStatus);
        output.println("databaseProduct=" + databaseProduct);
        output.println("databaseMode=" + (isSqliteRuntime() ? "sqlite" : "mysql"));
        output.println("checkedAt=" + LocalDateTime.now());
    }

    private void printStats(PrintStream output) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("boards", count("board"));
        counts.put("posts", count("post"));
        counts.put("activePosts", countWhere("post", "is_deleted = 0"));
        counts.put("comments", count("comment"));
        counts.put("users", count("user"));
        counts.put("unreadAlarms", countWhere("alarm", "is_read = 0"));
        counts.forEach((name, value) -> output.println(name + "=" + value));
    }

    private void syncPostCounts(PrintStream output) {
        int changed = boardService.syncGalleryPostCount();
        output.println("[CONSOLE] 게시글 수 동기화 완료: 변경된 보드 " + changed + "개");
    }

    private void refreshRankings(PrintStream output) {
        requireMySql("refresh-rankings");
        Map<String, Object> result = boardService.refreshBoardRankings();
        output.println("[CONSOLE] 랭킹 갱신 완료: " + result.getOrDefault("items", "[]"));
    }

    private void runDormancyCheck(PrintStream output) {
        requireMySql("dormancy-check");
        featureService.runDormancyCheck();
        output.println("[CONSOLE] 휴면 보드 점검 완료");
    }

    private void cleanupExpired(PrintStream output) {
        int accountRequests = accountVerificationRepository.deleteExpired();
        int signupRequests = signupVerificationRepository.deleteExpired();
        output.println("[CONSOLE] 만료 인증 정리 완료: 계정 " + accountRequests + "건, 가입 " + signupRequests + "건");
    }

    private void cleanupAlarms(String[] tokens, PrintStream output) {
        if (tokens.length > 2) {
            throw new IllegalArgumentException("사용법: cleanup-alarms [보존일수]");
        }
        int days = tokens.length == 2 ? parseRetentionDays(tokens[1]) : DEFAULT_ALARM_RETENTION_DAYS;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = jdbcTemplate.update("DELETE FROM alarm WHERE created_at < ?", cutoff);
        output.println("[CONSOLE] 오래된 알림 정리 완료: " + deleted + "건 삭제, 보존기간 " + days + "일");
    }

    private void manageEnvironment(String normalizedCommand, String[] tokens, PrintStream output) {
        if (tokens.length < 2) {
            throw new IllegalArgumentException("사용법: env <list|get|set|unset> ...");
        }
        String operation = tokens[1].toLowerCase(Locale.ROOT);
        switch (operation) {
            case "list", "목록" -> listEnvironment(output);
            case "get", "조회" -> {
                requireTokenCount(tokens, 3, "사용법: env get <이름>");
                printEnvironment(tokens[2], output);
            }
            case "set", "설정" -> setEnvironment(normalizedCommand, tokens, output);
            case "unset", "해제" -> {
                requireTokenCount(tokens, 3, "사용법: env unset <이름>");
                unsetEnvironment(tokens[2], output);
            }
            default -> throw new IllegalArgumentException("지원하지 않는 env 작업입니다: " + tokens[1]);
        }
    }

    private void listEnvironment(PrintStream output) {
        EDITABLE_ENVIRONMENT.keySet().forEach(name -> printEnvironment(name, output));
    }

    private void printEnvironment(String rawName, PrintStream output) {
        String name = normalizeEnvironmentName(rawName);
        EnvironmentVariable definition = requireEnvironmentVariable(name);
        String runtimeValue = System.getProperty(name);
        String osValue = System.getenv(name);
        String effectiveValue = runtimeValue != null ? runtimeValue : osValue;
        String source = runtimeValue != null ? "runtime" : osValue != null ? "os" : "unset";
        String displayValue = definition.secret()
                ? effectiveValue == null ? "<unset>" : "<masked>"
                : effectiveValue == null ? "<unset>" : effectiveValue;
        output.println(name + "=" + displayValue
                + " source=" + source
                + " restartRequired=" + definition.restartRequired());
    }

    private void setEnvironment(String normalizedCommand, String[] tokens, PrintStream output) {
        if (tokens.length < 4) {
            throw new IllegalArgumentException("사용법: env set <이름> <값>");
        }
        String name = normalizeEnvironmentName(tokens[2]);
        EnvironmentVariable definition = requireEnvironmentVariable(name);
        int valueStart = nthTokenStart(normalizedCommand, 3);
        String value = normalizedCommand.substring(valueStart).trim();
        validateEnvironmentValue(name, value);
        System.setProperty(name, value);
        if (definition.runtimeProperty() != null) {
            System.setProperty(definition.runtimeProperty(), value);
        }
        output.println("[CONSOLE] " + name + " 런타임 오버라이드 적용 완료"
                + (definition.restartRequired()
                ? " (시작 시 고정 항목: 현재 실행에는 영향 없음, 다음 실행 환경에도 별도 설정 필요)"
                : " (즉시 반영)"));
    }

    private void unsetEnvironment(String rawName, PrintStream output) {
        String name = normalizeEnvironmentName(rawName);
        EnvironmentVariable definition = requireEnvironmentVariable(name);
        System.clearProperty(name);
        if (definition.runtimeProperty() != null) {
            System.clearProperty(definition.runtimeProperty());
        }
        output.println("[CONSOLE] " + name + " 런타임 오버라이드 제거 완료"
                + (System.getenv(name) == null ? " (현재 값 없음)" : " (OS 환경변수 값으로 복원)"));
    }

    private int nthTokenStart(String text, int tokenIndex) {
        int index = 0;
        boolean inToken = false;
        int currentToken = -1;
        while (index < text.length()) {
            char character = text.charAt(index);
            if (Character.isWhitespace(character)) {
                inToken = false;
            } else if (!inToken) {
                inToken = true;
                currentToken++;
                if (currentToken == tokenIndex) {
                    return index;
                }
            }
            index++;
        }
        throw new IllegalArgumentException("환경변수 값이 필요합니다.");
    }

    private void requireTokenCount(String[] tokens, int expected, String usage) {
        if (tokens.length != expected) {
            throw new IllegalArgumentException(usage);
        }
    }

    private String normalizeEnvironmentName(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    private EnvironmentVariable requireEnvironmentVariable(String name) {
        EnvironmentVariable definition = EDITABLE_ENVIRONMENT.get(name);
        if (definition == null) {
            throw new IllegalArgumentException("수정할 수 없는 환경변수입니다: " + name + " (env list로 확인)");
        }
        return definition;
    }

    private void validateEnvironmentValue(String name, String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException("환경변수 값은 비워 둘 수 없습니다. 제거하려면 env unset을 사용하세요.");
        }
        if ("APP_SERVER_PORT".equals(name)) {
            try {
                int port = Integer.parseInt(value);
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("APP_SERVER_PORT는 1~65535 사이의 정수여야 합니다.");
            }
        }
        if ("APP_ADMIN_LOGIN_CODE".equals(name) && value.length() < 32) {
            throw new IllegalArgumentException("APP_ADMIN_LOGIN_CODE는 32자 이상이어야 합니다.");
        }
        if (("APP_CONSOLE_ENABLED".equals(name)
                || "APP_SEED_BULK_GUEST_CONTENT_ENABLED".equals(name)
                || "APP_SEED_BULK_GUEST_CONTENT_ALLOW_DESTRUCTIVE_RESET".equals(name))
                && !"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(name + " 값은 true 또는 false여야 합니다.");
        }
    }

    private static Map<String, EnvironmentVariable> editableEnvironment() {
        Map<String, EnvironmentVariable> variables = new LinkedHashMap<>();
        variables.put("APP_SERVER_PORT", new EnvironmentVariable(false, true, null));
        variables.put("APP_DB_MODE", new EnvironmentVariable(false, true, null));
        variables.put("APP_SQLITE_DB_PATH", new EnvironmentVariable(false, true, null));
        variables.put("APP_DB_HOST", new EnvironmentVariable(false, true, null));
        variables.put("APP_DB_USER", new EnvironmentVariable(false, true, null));
        variables.put("APP_DB_PASSWORD", new EnvironmentVariable(true, true, null));
        variables.put("APP_SMTP_EMAIL", new EnvironmentVariable(false, true, null));
        variables.put("APP_SMTP_PASSWORD", new EnvironmentVariable(true, true, null));
        variables.put("APP_ADMIN_LOGIN_CODE", new EnvironmentVariable(true, false, "app.security.admin-login-code"));
        variables.put("APP_ADMIN_ALLOWED_ADDRESSES", new EnvironmentVariable(false, false, "app.security.admin-allowed-addresses"));
        variables.put("APP_BLOCKED_ADDRESSES", new EnvironmentVariable(false, false, "app.security.blocked-addresses"));
        variables.put("APP_CONSOLE_ENABLED", new EnvironmentVariable(false, true, null));
        variables.put("APP_BULK_SEED_PASSWORD", new EnvironmentVariable(true, true, null));
        variables.put("APP_SEED_BULK_GUEST_CONTENT_ENABLED", new EnvironmentVariable(false, true, null));
        variables.put("APP_SEED_BULK_GUEST_CONTENT_ALLOW_DESTRUCTIVE_RESET", new EnvironmentVariable(false, true, null));
        return Collections.unmodifiableMap(variables);
    }

    private int parseRetentionDays(String rawValue) {
        try {
            int days = Integer.parseInt(rawValue);
            if (days < 1 || days > MAX_ALARM_RETENTION_DAYS) {
                throw new IllegalArgumentException();
            }
            return days;
        } catch (Exception e) {
            throw new IllegalArgumentException("보존일수는 1~" + MAX_ALARM_RETENTION_DAYS + " 사이의 정수여야 합니다.");
        }
    }

    private long count(String table) {
        return countWhere(table, "1 = 1");
    }

    private long countWhere(String table, String predicate) {
        Long value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + predicate, Long.class);
        return value == null ? 0L : value;
    }

    private void requireMySql(String command) {
        if (isSqliteRuntime()) {
            throw new IllegalStateException(command + " 명령은 MySQL 실행 상태에서만 사용할 수 있습니다.");
        }
    }

    private boolean isSqliteRuntime() {
        String url = System.getProperty("spring.datasource.url", "");
        return url.toLowerCase().startsWith("jdbc:sqlite:");
    }

    private String messageOf(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private record EnvironmentVariable(boolean secret, boolean restartRequired, String runtimeProperty) {
    }
}
