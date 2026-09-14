import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.java.spring_04.auth.*;
import org.java.spring_04.board.*;
import org.java.spring_04.common.*;
import org.java.spring_04.feature.*;
import org.java.spring_04.post.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;

// Local-only regression probes. No server, database, email, or network is used.
public class SecurityAuditHarness {
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        System.out.println("CONFIRMED: " + message);
    }
    public static void main(String[] args) throws Exception {
        privateBoardRead();
        resetSession();
        crawlerLimit();
        replayCapacity();
    }
    static void privateBoardRead() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForMap("SELECT * FROM gallery_setting WHERE gall_id = ?", "audit-private"))
            .thenReturn(Map.of("visibility", "private", "read_visibility", "inherit"));
        FeatureService feature = new FeatureService();
        ReflectionTestUtils.setField(feature, "jdbcTemplate", jdbc);
        boolean denied = false;
        try { feature.assertBoardReadable("audit-private", null, null); }
        catch (RuntimeException expected) { denied = true; }
        check(denied, "private-board policy denies anonymous viewer");
        Map<String,Object> post = new HashMap<>(Map.of("id", 1L, "gall_id", "audit-private",
            "post_no", 1L, "is_draft", 0, "is_secret", 0, "review_status", "normal", "content", "synthetic-private-marker"));
        BoardService boards = mock(BoardService.class);
        when(boards.getPostDetail("audit-private", 1L)).thenReturn(post);
        BoardController boardController = new BoardController();
        ReflectionTestUtils.setField(boardController, "boardService", boards);
        ReflectionTestUtils.setField(boardController, "featureService", feature);
        ReflectionTestUtils.setField(boardController, "contentResponsePolicy", new ContentResponsePolicy(boards));
        check(Boolean.TRUE.equals(boardController.getPostDetail("audit-private", 1L, null, null).get("success")),
            "BoardController detail returns synthetic private post anonymously");
        PostService posts = mock(PostService.class);
        when(posts.getPostDetail("audit-private", 1L)).thenReturn(post);
        when(posts.getComments("audit-private", 1L)).thenReturn(List.of());
        when(posts.getVoteState("audit-private", 1L, null, "198.51.100.10")).thenReturn(Map.of());
        PostController postController = new PostController();
        ReflectionTestUtils.setField(postController, "postService", posts);
        ReflectionTestUtils.setField(postController, "featureService", feature);
        ReflectionTestUtils.setField(postController, "contentResponsePolicy", new ContentResponsePolicy(boards));
        ReflectionTestUtils.setField(postController, "requestIpResolver", new RequestIpResolver("127.0.0.1"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.10");
        check(Boolean.TRUE.equals(postController.getPostByGalleryAndNumber("audit-private", 1L, request, null, null).get("success")),
            "PostController detail returns synthetic private post anonymously");
    }
    static void resetSession() {
        AuthController controller = new AuthController();
        AuthService auth = mock(AuthService.class);
        Map<String,String> body = Map.of("email", "audit@example.invalid", "action", "password_reset", "code", "synthetic");
        when(auth.confirmAccountAction(body)).thenReturn("audit-member");
        ReflectionTestUtils.setField(controller, "authService", auth);
        ReflectionTestUtils.setField(controller, "rateLimiter", new SecurityRateLimiter());
        ReflectionTestUtils.setField(controller, "requestIpResolver", new RequestIpResolver("127.0.0.1"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("uid", "audit-member");
        check(Boolean.TRUE.equals(controller.confirmAccountAction(body, request).get("success")), "mocked password reset succeeds");
        check(Boolean.TRUE.equals(controller.checkLogin(request).get("loggedIn")), "existing session remains logged in after password reset");
    }
    static void crawlerLimit() throws Exception {
        PageRequestRateLimitFilter filter = new PageRequestRateLimitFilter(new RequestIpResolver("127.0.0.1"),
            mock(IpLocationService.class), new SecurityRateLimiter(), true, 10);
        int last = 0;
        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
            request.setRemoteAddr("198.51.100.11");
            request.addHeader("User-Agent", "Local audit");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            last = response.getStatus();
        }
        check(last == 429, "ordinary page request is limited after configured quota");
        MockHttpServletRequest crawler = new MockHttpServletRequest("GET", "/");
        crawler.setRemoteAddr("198.51.100.11");
        crawler.addHeader("User-Agent", "Googlebot");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(crawler, response, new MockFilterChain());
        check(response.getStatus() == 200, "unverified crawler user-agent bypasses exhausted page limit");
    }
    @SuppressWarnings("unchecked")
    static void replayCapacity() throws Exception {
        RequestReplayGuard guard = new RequestReplayGuard();
        ApiRequestSecurityFilter filter = new ApiRequestSecurityFilter(new ObjectMapper(), guard);
        MockHttpServletRequest malformed = new MockHttpServletRequest("POST", "/api/audit-nonexistent");
        malformed.addHeader("X-Security-Request", "1");
        malformed.addHeader("X-Requested-With", "XMLHttpRequest");
        malformed.addHeader("X-Request-Id", "local-audit-request-001");
        malformed.addHeader("X-Request-Timestamp", Long.toString(System.currentTimeMillis()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(malformed, response, new MockFilterChain());
        ConcurrentHashMap<String,Long> entries = (ConcurrentHashMap<String,Long>) ReflectionTestUtils.getField(guard, "seenRequests");
        check(response.getStatus() == 403 && entries.size() == 1, "rejected unauthenticated request occupies replay capacity before remaining validation");
        // Seed state directly; do not simulate a request flood, even locally.
        for (int i = entries.size(); i < 20000; i++) entries.put("synthetic:" + i, System.currentTimeMillis() + 600000);
        MockHttpServletRequest other = new MockHttpServletRequest();
        other.getSession().setAttribute("uid", "unrelated-member");
        check(!guard.markIfNew(other, "unrelated-valid-id-001", Duration.ofMinutes(11)),
            "full global replay cache rejects unrelated authenticated user");
    }
}
