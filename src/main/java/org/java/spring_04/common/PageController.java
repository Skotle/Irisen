package org.java.spring_04.common;

import jakarta.servlet.http.HttpSession;
import org.java.spring_04.board.BoardService;
import org.java.spring_04.post.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {
    private final AdminAccessService adminAccessService;
    private final BoardService boardService;
    private final PostService postService;

    public PageController(AdminAccessService adminAccessService, BoardService boardService, PostService postService) {
        this.adminAccessService = adminAccessService;
        this.boardService = boardService;
        this.postService = postService;
    }

    private void logRequest(String pageName) {
        System.out.printf("[%s] GET %s PAGE%n", LocalDateTime.now(), pageName);
    }

    @GetMapping("/")
    public String index(Model model) {
        logRequest("INDEX");
        addCrawlerSnapshot(model);
        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        logRequest("LOGIN");
        return "index";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        logRequest("ADMIN LOGIN");
        return "index";
    }

    @GetMapping("/m")
    public String mobileIndex(Model model) {
        logRequest("MOBILE INDEX");
        addCrawlerSnapshot(model);
        return "mobile";
    }

    @GetMapping("/m/signin")
    public String mobileLogin() {
        logRequest("MOBILE LOGIN");
        return "mobile";
    }

    @GetMapping("/alarms")
    public String alarms() {
        logRequest("ALARMS");
        return "index";
    }

    @GetMapping("/board-requests")
    public String boardRequests() {
        logRequest("BOARD REQUESTS");
        return "index";
    }

    @GetMapping("/feed")
    public String feed(Model model) {
        logRequest("FEED");
        addCrawlerSnapshot(model);
        return "index";
    }

    @GetMapping("/search")
    public String search() {
        logRequest("SEARCH");
        return "index";
    }

    @GetMapping("/profile")
    public String profile() {
        logRequest("PROFILE");
        return "index";
    }

    @GetMapping("/profile/{uid}")
    public String publicProfile() {
        logRequest("PROFILE USER");
        return "index";
    }

    @GetMapping("/nid")
    public String nid() {
        logRequest("SIGNUP");
        return "index";
    }

    @GetMapping("/account-recovery")
    public String accountRecovery() {
        logRequest("ACCOUNT RECOVERY");
        return "index";
    }

    @GetMapping("/m/nid")
    public String mobileSignup() {
        logRequest("MOBILE SIGNUP");
        return "mobile";
    }

    @GetMapping("/m/board-request")
    public String mobileBoardRequest() {
        logRequest("MOBILE BOARD REQUEST");
        return "mobile";
    }

    @GetMapping("/m/account-recovery")
    public String mobileAccountRecovery() {
        logRequest("MOBILE ACCOUNT RECOVERY");
        return "mobile";
    }

    @GetMapping("/m/feed")
    public String mobileFeed(Model model) {
        logRequest("MOBILE FEED");
        addCrawlerSnapshot(model);
        return "mobile";
    }

    @GetMapping({"/m/search", "/m/profile", "/m/profile/{uid}", "/m/alarms"})
    public String mobileUtilityPage() {
        logRequest("MOBILE UTILITY");
        return "mobile";
    }

    @GetMapping("/policy.html")
    public String policy() {
        logRequest("POLICY");
        return "policy";
    }

    @GetMapping("/test")
    public String legacyApiTest(HttpSession session) {
        adminAccessService.assertAdmin(session);
        return "redirect:/admin/api-test";
    }

    @GetMapping("/admin/api-test")
    public String apiTest(HttpSession session) {
        adminAccessService.assertAdmin(session);
        logRequest("ADMIN API TEST");
        return "test";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        adminAccessService.assertAdmin(session);
        logRequest("ADMIN");
        return "admin";
    }

    private void addCrawlerSnapshot(Model model) {
        try {
            List<Map<String, Object>> boards = boardService.getBoardList();
            model.addAttribute("crawlerBoards", boards.stream().limit(120).toList());
        } catch (Exception e) {
            model.addAttribute("crawlerBoards", List.of());
        }
        try {
            model.addAttribute("crawlerPosts", postService.getTopRecommendedPosts().stream().limit(50).toList());
        } catch (Exception e) {
            model.addAttribute("crawlerPosts", List.of());
        }
    }
}
