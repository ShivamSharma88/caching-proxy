package com.cachingproxy;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Forwards HTTP requests to the origin server
 */
public class HttpForwarder {
    private static final Logger logger = LoggerFactory.getLogger(HttpForwarder.class);
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Forward HTTP request to origin server
     */
    public static ForwardedResponse forward(String method, String url, Map<String, String> headers, byte[] body) {
        try {
            ClassicHttpRequest request = createRequest(method, url, headers, body);
            if (request == null) {
                return null;
            }

            // Execute request
            ClassicHttpResponse response = httpClient.executeOpen(null, request, null);

            // Extract response data
            int statusCode = response.getCode();
            Map<String, String> responseHeaders = extractHeaders(response);
            byte[] responseBody = extractBody(response.getEntity());

            logger.debug("Received response from origin: {} {}", statusCode, url);

            return new ForwardedResponse(statusCode, responseHeaders, responseBody);

        } catch (IOException e) {
            logger.error("Error forwarding request to {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Create appropriate HTTP request based on method
     */
    private static ClassicHttpRequest createRequest(String method, String url, Map<String, String> headers, byte[] body) {
        ClassicHttpRequest request;

        switch (method.toUpperCase()) {
            case "GET":
                request = new HttpGet(url);
                break;
            case "POST":
                request = new HttpPost(url);
                if (body != null && body.length > 0) {
                    ((HttpPost) request).setEntity(new ByteArrayEntity(body));
                }
                break;
            case "PUT":
                request = new HttpPut(url);
                if (body != null && body.length > 0) {
                    ((HttpPut) request).setEntity(new ByteArrayEntity(body));
                }
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            case "PATCH":
                request = new HttpPatch(url);
                if (body != null && body.length > 0) {
                    ((HttpPatch) request).setEntity(new ByteArrayEntity(body));
                }
                break;
            case "HEAD":
                request = new HttpHead(url);
                break;
            case "OPTIONS":
                request = new HttpOptions(url);
                break;
            default:
                logger.warn("Unsupported HTTP method: {}", method);
                return null;
        }

        // Add headers to request
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                // Skip Host and Connection headers as they are handled by the HTTP client
                if (!isSystemHeader(header.getKey())) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
        }

        return request;
    }

    /**
     * Check if header is a system header that should not be forwarded
     */
    private static boolean isSystemHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("host") || 
               lowerName.equals("connection") || 
               lowerName.equals("transfer-encoding");
    }

    /**
     * Extract headers from response
     */
    private static Map<String, String> extractHeaders(ClassicHttpResponse response) {
        Map<String, String> headers = new HashMap<>();
        for (Header header : response.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

    /**
     * Extract body from response entity
     */
    private static byte[] extractBody(HttpEntity entity) throws IOException {
        if (entity == null) {
            return new byte[0];
        }

        return entity.getContent().readAllBytes();
    }
}
