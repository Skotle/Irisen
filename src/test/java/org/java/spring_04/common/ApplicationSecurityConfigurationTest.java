package org.java.spring_04.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSecurityConfigurationTest {
    @Test
    void productionDefaultsAreFailClosed() throws IOException {
        String properties = Files.readString(Path.of("src/main/resources/application.properties"));

        assertThat(properties)
                .contains("server.servlet.session.cookie.secure=true")
                .contains("server.servlet.session.cookie.same-site=strict")
                .contains("app.security.admin-login-code=${APP_ADMIN_LOGIN_CODE:}")
                .contains("app.seed.bulk-guest-content.enabled=false")
                .contains("app.seed.bulk-guest-content.allow-destructive-reset=false")
                .doesNotContain("app.security.admin-login-code=1234");
    }
}
