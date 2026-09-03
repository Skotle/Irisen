package org.java.spring_04.common;

import org.java.spring_04.auth.AccountVerificationRepository;
import org.java.spring_04.auth.SignupVerificationRepository;
import org.java.spring_04.board.BoardService;
import org.java.spring_04.feature.FeatureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleCommandRunnerTest {
    private ConfigurableApplicationContext context;
    private JdbcTemplate jdbcTemplate;
    private BoardService boardService;
    private AccountVerificationRepository accountVerificationRepository;
    private SignupVerificationRepository signupVerificationRepository;
    private ConsoleCommandRunner runner;
    private ByteArrayOutputStream outputBuffer;
    private PrintStream output;

    @AfterEach
    void clearRuntimeEnvironmentOverrides() {
        System.clearProperty("APP_ADMIN_ALLOWED_ADDRESSES");
        System.clearProperty("app.security.admin-allowed-addresses");
        System.clearProperty("APP_ADMIN_LOGIN_CODE");
        System.clearProperty("app.security.admin-login-code");
    }

    @BeforeEach
    void setUp() {
        context = mock(ConfigurableApplicationContext.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        boardService = mock(BoardService.class);
        accountVerificationRepository = mock(AccountVerificationRepository.class);
        signupVerificationRepository = mock(SignupVerificationRepository.class);
        runner = new ConsoleCommandRunner(
                context,
                jdbcTemplate,
                mock(DataSource.class),
                boardService,
                mock(FeatureService.class),
                accountVerificationRepository,
                signupVerificationRepository,
                false
        );
        outputBuffer = new ByteArrayOutputStream();
        output = new PrintStream(outputBuffer);
    }

    @Test
    void helpPrintsAvailableCommands() {
        assertTrue(runner.execute("help", output));

        String text = outputBuffer.toString();
        assertTrue(text.contains("sync-post-counts"));
        assertTrue(text.contains("cleanup-alarms"));
        assertTrue(text.contains("exit"));
    }

    @Test
    void syncPostCountsReportsChangedBoards() {
        when(boardService.syncGalleryPostCount()).thenReturn(3);

        assertTrue(runner.execute("sync-post-counts", output));

        verify(boardService).syncGalleryPostCount();
        assertTrue(outputBuffer.toString().contains("변경된 보드 3개"));
    }

    @Test
    void cleanupCommandsUseBoundedCutoffAndRepositories() {
        when(accountVerificationRepository.deleteExpired()).thenReturn(2);
        when(signupVerificationRepository.deleteExpired()).thenReturn(4);
        when(jdbcTemplate.update(eq("DELETE FROM alarm WHERE created_at < ?"), any(LocalDateTime.class))).thenReturn(7);

        assertTrue(runner.execute("cleanup-expired", output));
        assertTrue(runner.execute("cleanup-alarms 30", output));

        verify(accountVerificationRepository).deleteExpired();
        verify(signupVerificationRepository).deleteExpired();
        verify(jdbcTemplate).update(eq("DELETE FROM alarm WHERE created_at < ?"), any(LocalDateTime.class));
        String text = outputBuffer.toString();
        assertTrue(text.contains("계정 2건, 가입 4건"));
        assertTrue(text.contains("7건 삭제, 보존기간 30일"));
    }

    @Test
    void exitClosesApplicationContext() {
        assertFalse(runner.execute("exit", output));

        verify(context).close();
        assertTrue(outputBuffer.toString().contains("정상 종료"));
    }

    @Test
    void environmentCommandSetsAndUnsetsDynamicOverride() {
        assertTrue(runner.execute("env set APP_ADMIN_ALLOWED_ADDRESSES 127.0.0.1, 10.0.0.2", output));

        assertEquals("127.0.0.1, 10.0.0.2", System.getProperty("APP_ADMIN_ALLOWED_ADDRESSES"));
        assertEquals("127.0.0.1, 10.0.0.2", System.getProperty("app.security.admin-allowed-addresses"));

        assertTrue(runner.execute("env unset APP_ADMIN_ALLOWED_ADDRESSES", output));
        assertNull(System.getProperty("APP_ADMIN_ALLOWED_ADDRESSES"));
        assertNull(System.getProperty("app.security.admin-allowed-addresses"));
    }

    @Test
    void environmentCommandMasksSecrets() {
        String secret = "0123456789abcdef0123456789abcdef";
        assertTrue(runner.execute("env set APP_ADMIN_LOGIN_CODE " + secret, output));
        assertTrue(runner.execute("env get APP_ADMIN_LOGIN_CODE", output));

        String text = outputBuffer.toString();
        assertTrue(text.contains("APP_ADMIN_LOGIN_CODE=<masked>"));
        assertFalse(text.contains(secret));
    }
}
