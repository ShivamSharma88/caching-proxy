package com.cachingproxy;

import java.util.Map;

/**
 * Represents an HTTP request
 */
public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private byte[] body;

    public HttpRequest(String method, String path, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
