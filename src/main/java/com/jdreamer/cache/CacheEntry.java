package com.jdreamer.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public class CacheEntry {
    private String mediaUrl;          // The <video> src
    private Instant  inserted;        // When the entry was added

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Instant getInserted() {
        return inserted;
    }

    public void setInserted(Instant inserted) {
        this.inserted = inserted;
    }

    @JsonIgnore
    public boolean isExpired() {
        // 6h expiration
        return inserted.plusSeconds(6 * 60 * 60).isBefore(Instant.now());
    }
}
