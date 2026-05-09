package com.jdreamer.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class MediaUrlCache {

    // ------------------------------------------------------------
    // Singleton plumbing
    // ------------------------------------------------------------
    private static final MediaUrlCache INSTANCE = new MediaUrlCache();
    public static MediaUrlCache get() { return INSTANCE; }

    // ------------------------------------------------------------
    // Internal state
    // ------------------------------------------------------------
    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    private final Path cacheFile = Paths.get(System.getProperty("user.home"),
            ".islamic-studies",
            "mediaUrlCache.json");

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();   // registers JavaTimeModule for Instant

    // ------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------
    private MediaUrlCache() {
        loadFromDisk();

        cleaner.scheduleAtFixedRate(this::purgeExpired, 1, 1, TimeUnit.DAYS);
    }

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------
    public void put(String pageUrl, String mediaUrl) {
        if (pageUrl == null || mediaUrl == null) return;

        CacheEntry entry = new CacheEntry();
        entry.setMediaUrl(mediaUrl);
        entry.setInserted(Instant.now());

        store.put(pageUrl, entry);

        persistAsync();
    }

    public String get(String pageUrl) {
        if (pageUrl == null) return null;

        CacheEntry entry = store.get(pageUrl);

        if (entry == null) return null;

        if (entry.isExpired()) {
            store.remove(pageUrl);
            persistAsync();
            return null;
        }

        return entry.getMediaUrl();
    }

    public void shutdown() {
        cleaner.shutdownNow();

        persist();     // final sync write
    }

    // ------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------
    private void purgeExpired() {
        boolean changed = false;

        for (Map.Entry<String, CacheEntry> e : store.entrySet()) {
            if (e.getValue().isExpired()) {
                store.remove(e.getKey());
                changed = true;
            }
        }

        if (changed) persistAsync();
    }

    private void loadFromDisk() {
        if (!Files.exists(cacheFile)) return;

        try (Reader r = Files.newBufferedReader(cacheFile)) {
            Map<String, CacheEntry> loaded = mapper.readValue(
                    r, new TypeReference<Map<String, CacheEntry>>() {}
            );
            if (loaded != null) store.putAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persistAsync() {
        CompletableFuture.runAsync(this::persist);
    }

    private synchronized void persist() {
        try {
            Files.createDirectories(cacheFile.getParent());
            try (Writer w = Files.newBufferedWriter(cacheFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(w, store);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
