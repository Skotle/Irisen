package org.java.spring_04.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateSecurityTest {
    private static final Path TEMPLATE_ROOT = Path.of("src/main/resources/templates");
    private static final Pattern INLINE_EVENT = Pattern.compile(
            "\\s(onclick|oninput|onsubmit|onchange|onload|onerror|onfocus|onblur)\\s*=",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SCRIPT_TAG = Pattern.compile("<script\\b[^>]*>", Pattern.CASE_INSENSITIVE);

    @Test
    void templatesContainNoInlineEventHandlers() throws IOException {
        for (Path template : templates()) {
            assertThat(INLINE_EVENT.matcher(Files.readString(template)).find())
                    .as("inline event handler in %s", template)
                    .isFalse();
        }
    }

    @Test
    void inlineScriptsRequireTheResponseNonce() throws IOException {
        for (Path template : templates()) {
            String html = Files.readString(template);
            var matcher = SCRIPT_TAG.matcher(html);
            while (matcher.find()) {
                String tag = matcher.group().toLowerCase(Locale.ROOT);
                boolean external = tag.contains(" src=") || tag.contains(" th:src=");
                boolean nonceBound = tag.contains("th:attr=\"nonce=${cspnonce}\"")
                        || tag.contains("th:attr='nonce=${cspnonce}'");
                assertThat(external || nonceBound)
                        .as("script without src or CSP nonce in %s: %s", template, matcher.group())
                        .isTrue();
            }
        }
    }

    @Test
    void templatesDoNotLoadRemoteScripts() throws IOException {
        for (Path template : templates()) {
            String html = Files.readString(template).toLowerCase(Locale.ROOT);
            assertThat(html).as("remote script in %s", template)
                    .doesNotContain("<script src=\"http://", "<script src=\"https://",
                            "<script src='http://", "<script src='https://");
        }
    }

    private List<Path> templates() throws IOException {
        try (var paths = Files.walk(TEMPLATE_ROOT)) {
            return paths.filter(path -> path.toString().endsWith(".html")).toList();
        }
    }
}
