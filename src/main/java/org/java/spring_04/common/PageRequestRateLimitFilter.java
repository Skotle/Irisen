package org.java.spring_04.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 9)
public class PageRequestRateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(PageRequestRateLimitFilter.class);

    private final RequestIpResolver requestIpResolver;
    private final IpLocationService ipLocationService;
    private final SecurityRateLimiter rateLimiter;
    private final boolean enabled;
    private final int requestsPerMinute;

    public PageRequestRateLimitFilter(RequestIpResolver requestIpResolver,
                                      IpLocationService ipLocationService,
                                      SecurityRateLimiter rateLimiter,
                                      @Value("${app.security.page-rate-limit.enabled:true}") boolean enabled,
                                      @Value("${app.security.page-rate-limit.requests-per-minute:60}") int requestsPerMinute) {
        this.requestIpResolver = requestIpResolver;
        this.ipLocationService = ipLocationService;
        this.rateLimiter = rateLimiter;
        this.enabled = enabled;
        this.requestsPerMinute = Math.max(10, requestsPerMinute);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled || !isRateLimitedPageRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = requestIpResolver.resolve(request);
        if (isKnownCrawler(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (rateLimiter.allow("page-get", ip, requestsPerMinute, Duration.ofMinutes(1))) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("PAGE_RATE_LIMITED method={} path={} ip={} location={} ua={} limitPerMinute={}",
                request.getMethod(),
                request.getRequestURI(),
                ip,
                ipLocationService.resolveLabel(ip),
                trimHeader(request.getHeader("User-Agent"), 180),
                requestsPerMinute);
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"Too many requests.\"}");
    }

    private boolean isRateLimitedPageRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = cleanRequestPath(request.getRequestURI()).toLowerCase(Locale.ROOT);
        return "/".equals(path)
                || "/boards".equals(path)
                || "/signin".equals(path)
                || "/nid".equals(path)
                || "/profile".equals(path)
                || path.startsWith("/profile/")
                || path.startsWith("/board/")
                || path.startsWith("/m/board/");
    }

    private boolean isKnownCrawler(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            return false;
        }
        String normalized = ua.toLowerCase(Locale.ROOT);
        return normalized.contains("googlebot")
                || normalized.contains("bingbot")
                || normalized.contains("naverbot")
                || normalized.contains("yeti")
                || normalized.contains("daumoa");
    }

    private String cleanRequestPath(String uri) {
        if (uri == null || uri.isBlank()) {
            return "/";
        }
        String[] segments = uri.split("/");
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            int semicolon = segment.indexOf(';');
            String cleanSegment = semicolon >= 0 ? segment.substring(0, semicolon) : segment;
            builder.append('/').append(cleanSegment);
        }
        return builder.length() == 0 ? "/" : builder.toString();
    }

    private String trimHeader(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = value.replaceAll("[\\r\\n]", "").trim();
        return sanitized.length() <= maxLength ? sanitized : sanitized.substring(0, maxLength);
    }
}
