package org.java.spring_04.common;

import jakarta.servlet.http.HttpSession;
import org.java.spring_04.board.BoardService;
import org.java.spring_04.post.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminApiTestPageTest {
    @Test
    void adminApiTestRouteRequiresAdminAndReturnsTemplate() {
        AdminAccessService adminAccessService = mock(AdminAccessService.class);
        PageController controller = controller(adminAccessService);
        MockHttpSession session = new MockHttpSession();

        String template = controller.apiTest(session);

        assertThat(template).isEqualTo("test");
        verify(adminAccessService).assertAdmin(session);
    }

    @Test
    void rejectedAdminSessionCannotOpenApiTestPage() {
        AdminAccessService adminAccessService = mock(AdminAccessService.class);
        PageController controller = controller(adminAccessService);
        MockHttpSession session = new MockHttpSession();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin login is required."))
                .when(adminAccessService).assertAdmin(session);

        assertThatThrownBy(() -> controller.apiTest(session))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void routeIsAvailableUnderAdminPath() throws Exception {
        GetMapping mapping = PageController.class
                .getDeclaredMethod("apiTest", HttpSession.class)
                .getAnnotation(GetMapping.class);

        assertThat(mapping.value()).containsExactly("/admin/api-test");
    }

    @Test
    void legacyTestPathRequiresAdminAndRedirectsToRestrictedPath() {
        AdminAccessService adminAccessService = mock(AdminAccessService.class);
        PageController controller = controller(adminAccessService);
        MockHttpSession session = new MockHttpSession();

        assertThat(controller.legacyApiTest(session)).isEqualTo("redirect:/admin/api-test");
        verify(adminAccessService).assertAdmin(session);
    }

    private PageController controller(AdminAccessService adminAccessService) {
        return new PageController(
                adminAccessService,
                mock(BoardService.class),
                mock(PostService.class)
        );
    }
}
