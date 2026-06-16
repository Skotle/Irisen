package org.java.spring_04.common;

import com.google.cloud.storage.StorageException;
import org.java.spring_04.board.BoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final GcsImageService gcsImageService;
    private final BoardService boardService;

    public UploadController(GcsImageService gcsImageService, BoardService boardService) {
        this.gcsImageService = gcsImageService;
        this.boardService = boardService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "gallId", required = false) String gallId,
                                                           @SessionAttribute(name = "uid", required = false) String uid) {
        String normalizedGallId = gallId == null ? "" : gallId.trim();
        if (normalizedGallId.isEmpty()) {
            if (uid == null || uid.isBlank()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }
        } else if (!boardService.canUploadImage(normalizedGallId, uid)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", uid == null || uid.isBlank()
                            ? "이 보드는 비회원 이미지 업로드가 비활성화되어 있습니다."
                            : "이 보드는 회원 이미지 업로드가 비활성화되어 있습니다."
            ));
        } else {
            try {
                boardService.assertAttachmentPolicy(normalizedGallId, file.getContentType(), file.getSize());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
            }
        }
        try {
            String url = gcsImageService.uploadImage(file);
            log.info("Image uploaded. actor={} gallId={}", uid == null || uid.isBlank() ? "guest" : uid, normalizedGallId);
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (StorageException e) {
            log.warn("Image upload storage failed. actor={} gallId={} code={} reason={}",
                    uid == null || uid.isBlank() ? "guest" : uid,
                    normalizedGallId,
                    e.getCode(),
                    e.getMessage());
            String message = e.getCode() == 403
                    ? "이미지 저장소 권한이 없어 업로드할 수 없습니다. GCS 서비스 계정 권한 또는 접근 범위를 확인하세요."
                    : "이미지 저장소에 업로드하지 못했습니다.";
            return ResponseEntity.status(502).body(Map.of("success", false, "message", message));
        } catch (Exception e) {
            log.warn("Image upload failed. actor={} gallId={} reason={}", uid == null || uid.isBlank() ? "guest" : uid, normalizedGallId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "이미지 업로드에 실패했습니다."));
        }
    }
}
