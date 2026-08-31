package org.java.spring_04.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            new RequestIpResolver("127.0.0.1,::1")
    );

    @Test
    void authenticationFailureRemainsUnauthorizedInsteadOfBecomingServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/boards");

        var response = handler.responseStatus(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login is required."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("success", false)
                .containsEntry("message", "Authentication is required.");
    }

    @Test
    void authorizationFailureRemainsForbiddenInsteadOfBecomingServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/boards");

        var response = handler.responseStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin login is required."),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("success", false)
                .containsEntry("message", "Access is forbidden.");
    }
}
