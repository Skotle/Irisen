package org.java.spring_04.common;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageFingerprintServiceTest {
    private final ImageFingerprintService service = new ImageFingerprintService();

    @Test
    void resizedCopyIsSimilar() throws Exception {
        byte[] original = createPatternImage(320, 180, Color.BLUE, Color.ORANGE);
        byte[] resized = createPatternImage(1280, 720, Color.BLUE, Color.ORANGE);

        ImageFingerprintService.Fingerprint first = service.fingerprint(original, "image/png").orElseThrow();
        ImageFingerprintService.Fingerprint second = service.fingerprint(resized, "image/png").orElseThrow();

        assertTrue(service.isSimilar(first, second, 5, 32, 0.02));
        assertTrue(Long.bitCount(first.differenceHash() ^ second.differenceHash()) <= 5);
    }

    @Test
    void differentColorsAreNotTreatedAsSimilar() throws Exception {
        byte[] blueOrange = createPatternImage(320, 180, Color.BLUE, Color.ORANGE);
        byte[] redGreen = createPatternImage(320, 180, Color.RED, Color.GREEN);

        ImageFingerprintService.Fingerprint first = service.fingerprint(blueOrange, "image/png").orElseThrow();
        ImageFingerprintService.Fingerprint second = service.fingerprint(redGreen, "image/png").orElseThrow();

        assertFalse(service.isSimilar(first, second, 5, 32, 0.02));
    }

    @Test
    void unsupportedCodecSkipsPerceptualFingerprint() {
        Optional<ImageFingerprintService.Fingerprint> fingerprint = service.fingerprint(new byte[]{1, 2, 3}, "image/avif");

        assertTrue(fingerprint.isEmpty());
    }

    @Test
    void fingerprintMetadataRoundTripsUnsignedHash() {
        ImageFingerprintService.Fingerprint original = new ImageFingerprintService.Fingerprint(
                0xfedcba9876543210L, 12, 34, 56, 1920, 1080
        );

        ImageFingerprintService.Fingerprint restored = ImageFingerprintService.Fingerprint.fromMetadata(
                original.hashHex(), original.averageColorHex(), "1920", "1080"
        ).orElseThrow();

        assertEquals(original, restored);
    }

    private byte[] createPatternImage(int width,
                                      int height,
                                      Color background,
                                      Color foreground) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(background);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(foreground);
            graphics.fillOval(width / 5, height / 5, width * 3 / 5, height * 3 / 5);
            graphics.setColor(Color.WHITE);
            graphics.drawLine(0, 0, width - 1, height - 1);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
