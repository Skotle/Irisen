package org.java.spring_04.common;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecurityRateLimiter {
    private static final int MAX_BUCKETS = 10_000;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allow(String scope, String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        String normalizedKey = (scope + ":" + (key == null ? "" : key))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\r\\n\\t ]+", "_");

        if (buckets.size() >= MAX_BUCKETS) {
            buckets.entrySet().removeIf((entry) -> now >= entry.getValue().resetAt());
            if (buckets.size() >= MAX_BUCKETS && !buckets.containsKey(normalizedKey)) {
                return false;
            }
        }

        Bucket bucket = buckets.compute(normalizedKey, (ignored, current) -> {
            if (current == null || now >= current.resetAt()) {
                return new Bucket(1, now + windowMillis);
            }
            return new Bucket(current.count() + 1, current.resetAt());
        });

        return bucket.count() <= limit;
    }

    private record Bucket(int count, long resetAt) {
    }
}
