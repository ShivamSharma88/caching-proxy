package com.cachingproxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a cached HTTP response
 */
public class CacheEntry {
    private int statusCode;
    private Map<String, String> headers;
    private byte[] body;
    private long timestamp;

    public CacheEntry(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = new HashMap<>(headers);
        this.body = body;
        this.timestamp = System.currentTimeMillis();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public byte[] getBody() {
        return body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAgeInSeconds() {
        return (System.currentTimeMillis() - timestamp) / 1000;
    }
}
