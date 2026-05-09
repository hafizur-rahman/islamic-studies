package com.jdreamer.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CacheEntry {
    private String mediaUrl;          // The <video> src
    private Instant  inserted;        // When the entry was added

    @JsonIgnore
    public boolean isExpired() {
        // 6h expiration
        return inserted.plusSeconds(6 * 60 * 60).isBefore(Instant.now());
    }
}
