package org.java.spring_04.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestSecurityFilterTest {

    @Test
    void originPolicyRejectsUnsafeRequestWithoutOriginEvidence() throws Exception {
        OriginPolicyFilter filter = new OriginPolicyFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setScheme("https");
        request.setServerName("example.test");
        request.setServerPort(443);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void originPolicyAllowsExactSameOrigin() throws Exception {
        OriginPolicyFilter filter = new OriginPolicyFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setScheme("https");
        request.setServerName("example.test");
        request.setServerPort(443);
        request.addHeader("Origin", "https://example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void clientIpUsesRightmostUntrustedHop() {
        RequestIpResolver resolver = new RequestIpResolver("127.0.0.1,::1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "122.38.33.23, 198.51.100.42");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.42");
    }

    @Test
    void contentSecurityPolicyUsesNonceAndNoUnsafeInlineScript() throws Exception {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute("cspNonce")).isNotNull();
        assertThat(response.getHeader("Content-Security-Policy"))
                .contains("script-src 'self' 'nonce-")
                .doesNotContain("script-src 'self' 'unsafe-inline'");
    }
}
