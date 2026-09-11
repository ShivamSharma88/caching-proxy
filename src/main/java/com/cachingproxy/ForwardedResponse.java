package com.cachingproxy;

import java.util.Map;

/**
 * Represents an HTTP response received from the origin server
 */
public class ForwardedResponse {
    private int statusCode;
    private Map<String, String> headers;
    private byte[] body;

    public ForwardedResponse(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
