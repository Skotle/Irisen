package org.java.spring_04.common;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GcsImageServiceTest {
    private static final String BUCKET = "test-bucket";

    @Test
    void newImageUsesContentAddressedObjectName() throws Exception {
        Storage storage = mock(Storage.class);
        byte[] bytes = createPatternImage(320, 180);
        String sha256 = sha256(bytes);
        GcsImageService service = service(storage, false);

        GcsImageService.ImageUploadResult result = service.uploadImage(file(bytes));

        assertEquals(GcsImageService.DuplicateType.NONE, result.duplicateType());
        assertFalse(result.duplicate());
        assertTrue(result.url().endsWith("/uploads%2Fimages%2Fby-sha256%2F"
                + sha256.substring(0, 2) + "%2F" + sha256 + ".png"));
    }

    @Test
    void exactImageReusesExistingObject() throws Exception {
        Storage storage = mock(Storage.class);
        byte[] bytes = createPatternImage(320, 180);
        String sha256 = sha256(bytes);
        String objectName = "uploads/images/by-sha256/" + sha256.substring(0, 2) + "/" + sha256 + ".png";
        when(storage.get(BUCKET, objectName)).thenReturn(mock(Blob.class));
        GcsImageService service = service(storage, true);

        GcsImageService.ImageUploadResult result = service.uploadImage(file(bytes));

        assertEquals(GcsImageService.DuplicateType.EXACT, result.duplicateType());
        assertTrue(result.duplicate());
        assertTrue(result.url().endsWith(sha256 + ".png"));
    }

    @Test
    void similarImageReusesIndexedObject() throws Exception {
        Storage storage = mock(Storage.class);
        byte[] bytes = createPatternImage(320, 180);
        ImageFingerprintService fingerprintService = new ImageFingerprintService();
        ImageFingerprintService.Fingerprint fingerprint = fingerprintService
                .fingerprint(bytes, "image/png")
                .orElseThrow();
        String candidateObjectName = "uploads/images/by-sha256/aa/candidate.png";

        Blob marker = mock(Blob.class);
        when(marker.getMetadata()).thenReturn(Map.of(
                "object-name", candidateObjectName,
                "dhash", fingerprint.hashHex(),
                "average-color", fingerprint.averageColorHex(),
                "width", Integer.toString(fingerprint.width()),
                "height", Integer.toString(fingerprint.height())
        ));
        @SuppressWarnings("unchecked")
        Page<Blob> page = mock(Page.class);
        when(page.iterateAll()).thenReturn(List.of(marker));
        when(storage.list(eq(BUCKET), any(Storage.BlobListOption.class))).thenReturn(page);
        when(storage.get(BUCKET, candidateObjectName)).thenReturn(mock(Blob.class));

        GcsImageService service = new GcsImageService(
                storage, fingerprintService, BUCKET, "https://storage.googleapis.com",
                true, 5, 32, 0.02
        );
        GcsImageService.ImageUploadResult result = service.uploadImage(file(bytes));

        assertEquals(GcsImageService.DuplicateType.SIMILAR, result.duplicateType());
        assertEquals("https://storage.googleapis.com/test-bucket/uploads%2Fimages%2Fby-sha256%2Faa%2Fcandidate.png", result.url());
    }

    private GcsImageService service(Storage storage, boolean similarityEnabled) {
        return new GcsImageService(
                storage,
                new ImageFingerprintService(),
                BUCKET,
                "https://storage.googleapis.com",
                similarityEnabled,
                5,
                32,
                0.02
        );
    }

    private MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("file", "image.png", "image/png", bytes);
    }

    private byte[] createPatternImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.ORANGE);
            graphics.fillOval(width / 5, height / 5, width * 3 / 5, height * 3 / 5);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
