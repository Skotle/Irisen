package org.java.spring_04.common;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

@Component
public class ImageFingerprintService {
    private static final int HASH_WIDTH = 9;
    private static final int HASH_HEIGHT = 8;
    private static final int MAX_DECODE_DIMENSION = 1024;
    private static final long MAX_SOURCE_PIXELS = 100_000_000L;

    public Optional<Fingerprint> fingerprint(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0 || !supportsPerceptualHash(contentType)) {
            return Optional.empty();
        }
        try {
            DecodedImage decoded = decodeForFingerprint(bytes);
            if (decoded == null) {
                return Optional.empty();
            }
            BufferedImage source = decoded.image();
            BufferedImage normalized = new BufferedImage(HASH_WIDTH, HASH_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = normalized.createGraphics();
            try {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, HASH_WIDTH, HASH_HEIGHT);
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(source, 0, 0, HASH_WIDTH, HASH_HEIGHT, null);
            } finally {
                graphics.dispose();
            }

            long differenceHash = 0L;
            long red = 0;
            long green = 0;
            long blue = 0;
            for (int y = 0; y < HASH_HEIGHT; y++) {
                for (int x = 0; x < HASH_WIDTH; x++) {
                    int rgb = normalized.getRGB(x, y);
                    red += (rgb >>> 16) & 0xff;
                    green += (rgb >>> 8) & 0xff;
                    blue += rgb & 0xff;
                    if (x < HASH_WIDTH - 1) {
                        differenceHash <<= 1;
                        if (luminance(rgb) > luminance(normalized.getRGB(x + 1, y))) {
                            differenceHash |= 1L;
                        }
                    }
                }
            }

            int sampleCount = HASH_WIDTH * HASH_HEIGHT;
            return Optional.of(new Fingerprint(
                    differenceHash,
                    (int) (red / sampleCount),
                    (int) (green / sampleCount),
                    (int) (blue / sampleCount),
                    decoded.originalWidth(),
                    decoded.originalHeight()
            ));
        } catch (IOException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private DecodedImage decodeForFingerprint(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_SOURCE_PIXELS) {
                    return null;
                }
                int largestDimension = Math.max(width, height);
                int subsampling = (int) Math.max(
                        1L,
                        ((long) largestDimension + MAX_DECODE_DIMENSION - 1) / MAX_DECODE_DIMENSION
                );
                ImageReadParam readParam = reader.getDefaultReadParam();
                readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
                BufferedImage image = reader.read(0, readParam);
                return image == null ? null : new DecodedImage(image, width, height);
            } finally {
                reader.dispose();
            }
        }
    }

    public boolean isSimilar(Fingerprint first,
                             Fingerprint second,
                             int hammingThreshold,
                             int colorDistanceThreshold,
                             double aspectRatioTolerance) {
        if (first == null || second == null) {
            return false;
        }
        if (Long.bitCount(first.differenceHash() ^ second.differenceHash()) > hammingThreshold) {
            return false;
        }
        if (first.colorDistance(second) > colorDistanceThreshold) {
            return false;
        }
        double firstRatio = first.aspectRatio();
        double secondRatio = second.aspectRatio();
        double ratioDifference = Math.abs(firstRatio - secondRatio) / Math.max(firstRatio, secondRatio);
        return ratioDifference <= aspectRatioTolerance;
    }

    private boolean supportsPerceptualHash(String contentType) {
        String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        return "image/jpeg".equals(normalized) || "image/png".equals(normalized);
    }

    private int luminance(int rgb) {
        int red = (rgb >>> 16) & 0xff;
        int green = (rgb >>> 8) & 0xff;
        int blue = rgb & 0xff;
        return (299 * red + 587 * green + 114 * blue) / 1000;
    }

    private record DecodedImage(BufferedImage image, int originalWidth, int originalHeight) {
    }

    public record Fingerprint(long differenceHash,
                              int averageRed,
                              int averageGreen,
                              int averageBlue,
                              int width,
                              int height) {
        public String hashHex() {
            return String.format(Locale.ROOT, "%016x", differenceHash);
        }

        public String averageColorHex() {
            return String.format(Locale.ROOT, "%02x%02x%02x", averageRed, averageGreen, averageBlue);
        }

        public int band(int index) {
            if (index < 0 || index > 7) {
                throw new IllegalArgumentException("해시 밴드 인덱스는 0~7이어야 합니다.");
            }
            return (int) ((differenceHash >>> ((7 - index) * 8)) & 0xff);
        }

        public double aspectRatio() {
            return (double) width / (double) height;
        }

        public double colorDistance(Fingerprint other) {
            int redDifference = averageRed - other.averageRed;
            int greenDifference = averageGreen - other.averageGreen;
            int blueDifference = averageBlue - other.averageBlue;
            return Math.sqrt(redDifference * redDifference
                    + greenDifference * greenDifference
                    + blueDifference * blueDifference);
        }

        public static Optional<Fingerprint> fromMetadata(String hashHex,
                                                         String averageColorHex,
                                                         String width,
                                                         String height) {
            try {
                if (hashHex == null || hashHex.length() != 16
                        || averageColorHex == null || averageColorHex.length() != 6) {
                    return Optional.empty();
                }
                long hash = Long.parseUnsignedLong(hashHex, 16);
                int red = Integer.parseInt(averageColorHex.substring(0, 2), 16);
                int green = Integer.parseInt(averageColorHex.substring(2, 4), 16);
                int blue = Integer.parseInt(averageColorHex.substring(4, 6), 16);
                int parsedWidth = Integer.parseInt(width);
                int parsedHeight = Integer.parseInt(height);
                if (parsedWidth <= 0 || parsedHeight <= 0) {
                    return Optional.empty();
                }
                return Optional.of(new Fingerprint(hash, red, green, blue, parsedWidth, parsedHeight));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
    }
}
