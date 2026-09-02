package org.java.spring_04.common;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class GcsImageService {
    private static final Logger log = LoggerFactory.getLogger(GcsImageService.class);
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    // With at most five changed bits, at least three of eight byte bands are unchanged.
    // Pairing within each half guarantees one searchable unchanged pair without scanning the bucket.
    private static final int[][] HASH_BAND_PAIRS = {
            {0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3},
            {4, 5}, {4, 6}, {4, 7}, {5, 6}, {5, 7}, {6, 7}
    };
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/avif", "image/heic", "image/heif"
    );
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/gif", "gif"),
            Map.entry("image/webp", "webp"),
            Map.entry("image/avif", "avif"),
            Map.entry("image/heic", "heic"),
            Map.entry("image/heif", "heif")
    );

    private final Storage storage;
    private final ImageFingerprintService fingerprintService;
    private final String bucketName;
    private final String publicBaseUrl;
    private final boolean similarityEnabled;
    private final int hammingThreshold;
    private final int colorDistanceThreshold;
    private final double aspectRatioTolerance;

    @Autowired
    public GcsImageService(
            ImageFingerprintService fingerprintService,
            @Value("${app.gcs.bucket-name:}") String bucketName,
            @Value("${app.gcs.public-base-url:https://storage.googleapis.com}") String publicBaseUrl,
            @Value("${app.gcs.image-dedup.similarity-enabled:true}") boolean similarityEnabled,
            @Value("${app.gcs.image-dedup.hamming-threshold:5}") int hammingThreshold,
            @Value("${app.gcs.image-dedup.color-distance-threshold:32}") int colorDistanceThreshold,
            @Value("${app.gcs.image-dedup.aspect-ratio-tolerance:0.02}") double aspectRatioTolerance
    ) {
        this(
                StorageOptions.getDefaultInstance().getService(),
                fingerprintService,
                bucketName,
                publicBaseUrl,
                similarityEnabled,
                hammingThreshold,
                colorDistanceThreshold,
                aspectRatioTolerance
        );
    }

    GcsImageService(Storage storage,
                    ImageFingerprintService fingerprintService,
                    String bucketName,
                    String publicBaseUrl,
                    boolean similarityEnabled,
                    int hammingThreshold,
                    int colorDistanceThreshold,
                    double aspectRatioTolerance) {
        this.storage = storage;
        this.fingerprintService = fingerprintService;
        this.bucketName = bucketName == null ? "" : bucketName.trim();
        this.publicBaseUrl = publicBaseUrl == null ? "https://storage.googleapis.com" : publicBaseUrl.replaceAll("/+$", "");
        this.similarityEnabled = similarityEnabled;
        this.hammingThreshold = Math.max(0, Math.min(5, hammingThreshold));
        this.colorDistanceThreshold = Math.max(0, colorDistanceThreshold);
        this.aspectRatioTolerance = Math.max(0.0, aspectRatioTolerance);
    }

    public ImageUploadResult uploadImage(MultipartFile file) throws IOException {
        if (bucketName.isBlank()) {
            throw new IllegalStateException("GCS 버킷 이름이 설정되지 않았습니다.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 필요합니다.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("이미지는 10MB 이하만 업로드할 수 있습니다.");
        }
        byte[] bytes = file.getBytes();
        String contentType = detectContentType(bytes);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG, PNG, GIF, WEBP, AVIF, HEIC, HEIF 이미지만 업로드할 수 있습니다.");
        }

        String sha256 = sha256(bytes);
        String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
        String objectName = canonicalObjectName(sha256, extension);
        Optional<ImageFingerprintService.Fingerprint> fingerprint = similarityEnabled
                ? fingerprintService.fingerprint(bytes, contentType)
                : Optional.empty();

        Optional<String> exactAlias = findExactAlias(sha256);
        if (exactAlias.isPresent()) {
            return new ImageUploadResult(buildPublicUrl(exactAlias.get()), DuplicateType.EXACT);
        }

        Blob existing = getBlobIfReadable(objectName);
        if (existing != null) {
            createExactAliasBestEffort(sha256, objectName);
            fingerprint.ifPresent(value -> createSimilarityIndexBestEffort(sha256, objectName, value));
            return new ImageUploadResult(buildPublicUrl(objectName), DuplicateType.EXACT);
        }

        if (fingerprint.isPresent()) {
            Optional<String> similarObject = findSimilarObject(fingerprint.get());
            if (similarObject.isPresent()) {
                createExactAliasBestEffort(sha256, similarObject.get());
                return new ImageUploadResult(buildPublicUrl(similarObject.get()), DuplicateType.SIMILAR);
            }
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sha256", sha256);
        fingerprint.ifPresent(value -> addFingerprintMetadata(metadata, value));
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000, immutable")
                .setMetadata(metadata)
                .build();

        try {
            storage.create(blobInfo, bytes, Storage.BlobTargetOption.doesNotExist());
        } catch (StorageException e) {
            if (e.getCode() == 412) {
                createExactAliasBestEffort(sha256, objectName);
                return new ImageUploadResult(buildPublicUrl(objectName), DuplicateType.EXACT);
            }
            throw e;
        }

        createExactAliasBestEffort(sha256, objectName);
        fingerprint.ifPresent(value -> createSimilarityIndexBestEffort(sha256, objectName, value));
        return new ImageUploadResult(buildPublicUrl(objectName), DuplicateType.NONE);
    }

    private Optional<String> findExactAlias(String sha256) {
        Blob alias = getBlobIfReadable(exactAliasName(sha256));
        if (alias == null || alias.getMetadata() == null) {
            return Optional.empty();
        }
        String targetObjectName = alias.getMetadata().get("object-name");
        if (targetObjectName == null || targetObjectName.isBlank()) {
            return Optional.empty();
        }
        return getBlobIfReadable(targetObjectName) == null ? Optional.empty() : Optional.of(targetObjectName);
    }

    private Optional<String> findSimilarObject(ImageFingerprintService.Fingerprint incoming) {
        Set<String> inspectedObjects = new HashSet<>();
        try {
            for (int[] pair : HASH_BAND_PAIRS) {
                Page<Blob> markers = storage.list(
                        bucketName,
                        Storage.BlobListOption.prefix(similarityIndexPrefix(pair, incoming))
                );
                for (Blob marker : markers.iterateAll()) {
                    Map<String, String> metadata = marker.getMetadata();
                    if (metadata == null) {
                        continue;
                    }
                    String candidateObjectName = metadata.get("object-name");
                    if (candidateObjectName == null || !inspectedObjects.add(candidateObjectName)) {
                        continue;
                    }
                    Optional<ImageFingerprintService.Fingerprint> candidate = ImageFingerprintService.Fingerprint.fromMetadata(
                            metadata.get("dhash"), metadata.get("average-color"),
                            metadata.get("width"), metadata.get("height")
                    );
                    if (candidate.isPresent() && fingerprintService.isSimilar(
                            incoming, candidate.get(), hammingThreshold,
                            colorDistanceThreshold, aspectRatioTolerance
                    ) && getBlobIfReadable(candidateObjectName) != null) {
                        return Optional.of(candidateObjectName);
                    }
                }
            }
        } catch (StorageException e) {
            log.warn("Image similarity lookup skipped. bucket={} code={} reason={}", bucketName, e.getCode(), e.getMessage());
        }
        return Optional.empty();
    }

    private void createExactAliasBestEffort(String sha256, String objectName) {
        createMarkerBestEffort(
                exactAliasName(sha256),
                Map.of("object-name", objectName, "sha256", sha256)
        );
    }

    private void createSimilarityIndexBestEffort(String sha256,
                                                  String objectName,
                                                  ImageFingerprintService.Fingerprint fingerprint) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("object-name", objectName);
        metadata.put("sha256", sha256);
        addFingerprintMetadata(metadata, fingerprint);
        for (int[] pair : HASH_BAND_PAIRS) {
            createMarkerBestEffort(similarityIndexName(pair, fingerprint, sha256), metadata);
        }
    }

    private void createMarkerBestEffort(String markerName, Map<String, String> metadata) {
        BlobInfo marker = BlobInfo.newBuilder(bucketName, markerName)
                .setContentType("application/octet-stream")
                .setCacheControl("private, no-store")
                .setMetadata(metadata)
                .build();
        try {
            storage.create(marker, new byte[0], Storage.BlobTargetOption.doesNotExist());
        } catch (StorageException e) {
            if (e.getCode() != 412) {
                log.warn("Image deduplication index write skipped. bucket={} marker={} code={} reason={}",
                        bucketName, markerName, e.getCode(), e.getMessage());
            }
        }
    }

    private Blob getBlobIfReadable(String objectName) {
        try {
            return storage.get(bucketName, objectName);
        } catch (StorageException e) {
            log.warn("Image deduplication lookup skipped. bucket={} object={} code={} reason={}",
                    bucketName, objectName, e.getCode(), e.getMessage());
            return null;
        }
    }

    private void addFingerprintMetadata(Map<String, String> metadata,
                                        ImageFingerprintService.Fingerprint fingerprint) {
        metadata.put("dhash", fingerprint.hashHex());
        metadata.put("average-color", fingerprint.averageColorHex());
        metadata.put("width", Integer.toString(fingerprint.width()));
        metadata.put("height", Integer.toString(fingerprint.height()));
    }

    private String canonicalObjectName(String sha256, String extension) {
        return "uploads/images/by-sha256/" + sha256.substring(0, 2) + "/" + sha256 + "." + extension;
    }

    private String exactAliasName(String sha256) {
        return "_dedup/images/exact/" + sha256.substring(0, 2) + "/" + sha256;
    }

    private String similarityIndexName(int[] pair,
                                       ImageFingerprintService.Fingerprint fingerprint,
                                       String sha256) {
        return similarityIndexPrefix(pair, fingerprint) + sha256;
    }

    private String similarityIndexPrefix(int[] pair, ImageFingerprintService.Fingerprint fingerprint) {
        return String.format(
                Locale.ROOT,
                "_dedup/images/similar/%d-%d/%02x%02x/",
                pair[0], pair[1], fingerprint.band(pair[0]), fingerprint.band(pair[1])
        );
    }

    private String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }

    private String detectContentType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return "";
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return "image/gif";
        }
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        String brand = isoBaseMediaBrand(bytes);
        if ("avif".equals(brand) || "avis".equals(brand)) {
            return "image/avif";
        }
        if (List.of("heic", "heix", "hevc", "hevx", "heim", "heis", "hevm", "hevs").contains(brand)) {
            return "image/heic";
        }
        if (List.of("mif1", "msf1").contains(brand)) {
            return "image/heif";
        }
        return "";
    }

    private String isoBaseMediaBrand(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return null;
        }
        if (bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p') {
            return new String(bytes, 8, 4, StandardCharsets.US_ASCII).toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private String buildPublicUrl(String objectName) {
        String encodedObjectName = URLEncoder.encode(objectName, StandardCharsets.UTF_8).replace("+", "%20");
        if (publicBaseUrl.contains("storage.googleapis.com")) {
            return publicBaseUrl + "/" + bucketName + "/" + encodedObjectName;
        }
        return publicBaseUrl + "/" + encodedObjectName;
    }

    public record ImageUploadResult(String url, DuplicateType duplicateType) {
        public boolean duplicate() {
            return duplicateType != DuplicateType.NONE;
        }
    }

    public enum DuplicateType {
        NONE,
        EXACT,
        SIMILAR;

        public String apiValue() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
