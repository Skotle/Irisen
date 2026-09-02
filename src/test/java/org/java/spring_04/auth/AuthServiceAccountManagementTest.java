package org.java.spring_04.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceAccountManagementTest {
    private AuthService authService;
    private UserDAO userDAO;
    private AccountVerificationRepository verificationRepository;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        userDAO = mock(UserDAO.class);
        verificationRepository = mock(AccountVerificationRepository.class);
        ReflectionTestUtils.setField(authService, "userDAO", userDAO);
        ReflectionTestUtils.setField(authService, "accountVerificationRepository", verificationRepository);
    }

    @Test
    void confirmedCodeChangesPasswordAndConsumesCode() {
        Map<String, Object> pending = pending("password_reset", "new-password-hash");
        when(verificationRepository.findLatest("member@example.com", "password_reset")).thenReturn(pending);
        when(verificationRepository.deleteByRequestId(42L)).thenReturn(1);
        when(userDAO.updatePassword("member1", "new-password-hash")).thenReturn(1);

        String uid = authService.confirmAccountAction(Map.of(
                "action", "password_reset",
                "email", "Member@Example.com",
                "code", "123456"
        ));

        assertThat(uid).isEqualTo("member1");
        verify(verificationRepository).deleteByRequestId(42L);
        verify(userDAO).updatePassword("member1", "new-password-hash");
    }

    @Test
    void incorrectCodeCannotChangePasswordOrConsumeRequest() {
        when(verificationRepository.findLatest("member@example.com", "password_reset"))
                .thenReturn(pending("password_reset", "new-password-hash"));

        assertThatThrownBy(() -> authService.confirmAccountAction(Map.of(
                "action", "password_reset",
                "email", "member@example.com",
                "code", "000000"
        ))).hasMessage("인증 정보가 올바르지 않거나 만료되었습니다. 인증 메일을 다시 요청해 주세요.");

        verify(verificationRepository, never()).deleteByRequestId(42L);
        verify(userDAO, never()).updatePassword("member1", "new-password-hash");
    }

    @Test
    void confirmedDeleteAnonymizesAndDeletesRegularAccount() {
        when(verificationRepository.findLatest("member@example.com", "account_delete"))
                .thenReturn(pending("account_delete", null));
        when(verificationRepository.deleteByRequestId(42L)).thenReturn(1);
        when(userDAO.findMemberDivision("member1")).thenReturn("user");
        when(userDAO.deleteAccount("member1")).thenReturn(1);

        String uid = authService.confirmAccountAction(Map.of(
                "action", "account_delete",
                "email", "member@example.com",
                "code", "123456",
                "confirmation", "DELETE"
        ));

        assertThat(uid).isEqualTo("member1");
        verify(userDAO).deleteAccount("member1");
    }

    @Test
    void privilegedAccountCannotBeDeleted() {
        when(verificationRepository.findLatest("member@example.com", "account_delete"))
                .thenReturn(pending("account_delete", null));
        when(userDAO.findMemberDivision("member1")).thenReturn("admin");

        assertThatThrownBy(() -> authService.confirmAccountAction(Map.of(
                "action", "account_delete",
                "email", "member@example.com",
                "code", "123456",
                "confirmation", "DELETE"
        ))).hasMessage("관리자 또는 운영자 계정은 이 화면에서 삭제할 수 없습니다.");

        verify(verificationRepository, never()).deleteByRequestId(42L);
        verify(userDAO, never()).deleteAccount("member1");
    }

    private Map<String, Object> pending(String action, String passwordHash) {
        Map<String, Object> pending = new HashMap<>();
        pending.put("request_id", 42L);
        pending.put("uid", "member1");
        pending.put("email", "member@example.com");
        pending.put("action_type", action);
        pending.put("password_hash", passwordHash);
        pending.put("verification_code", BCrypt.hashpw("123456", BCrypt.gensalt(4)));
        pending.put("expires_at", LocalDateTime.now().plusMinutes(5));
        return pending;
    }
}
