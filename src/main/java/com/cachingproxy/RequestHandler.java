package com.cachingproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles individual client requests
 */
public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket clientSocket;
    private final String originUrl;

    public RequestHandler(Socket clientSocket, String originUrl) {
        this.clientSocket = clientSocket;
        this.originUrl = originUrl;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Parse HTTP request
            HttpRequest httpRequest = parseHttpRequest(inputStream);
            if (httpRequest == null) {
                sendErrorResponse(outputStream, 400, "Bad Request");
                return;
            }

            logger.info("{} {}", httpRequest.getMethod(), httpRequest.getPath());

            // Generate cache key
            String fullUrl = originUrl + httpRequest.getPath();
            String cacheKey = CacheManager.getInstance().generateCacheKey(httpRequest.getMethod(), fullUrl);

            // Check if response is cached
            CacheEntry cachedEntry = CacheManager.getInstance().getCached(cacheKey);

            if (cachedEntry != null) {
                // Return cached response
                logger.debug("Cache HIT: {}", cacheKey);
                sendCachedResponse(outputStream, cachedEntry, true);
            } else {
                // Forward request to origin server
                logger.debug("Cache MISS: {}", cacheKey);
                ForwardedResponse forwardedResponse = HttpForwarder.forward(
                        httpRequest.getMethod(),
                        fullUrl,
                        httpRequest.getHeaders(),
                        httpRequest.getBody()
                );

                if (forwardedResponse != null) {
                    // Cache the response
                    CacheEntry cacheEntry = new CacheEntry(
                            forwardedResponse.getStatusCode(),
                            forwardedResponse.getHeaders(),
                            forwardedResponse.getBody()
                    );
                    CacheManager.getInstance().cache(cacheKey, cacheEntry);

                    // Send response to client
                    sendCachedResponse(outputStream, cacheEntry, false);
                } else {
                    sendErrorResponse(outputStream, 502, "Bad Gateway");
                }
            }

        } catch (IOException e) {
            logger.error("Error handling request: {}", e.getMessage());
        }
    }

    /**
     * Parse incoming HTTP request
     */
    private HttpRequest parseHttpRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            return null;
        }

        String method = parts[0];
        String path = parts[1];

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        int contentLength = 0;

        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                String headerName = headerParts[0];
                String headerValue = headerParts[1];
                headers.put(headerName, headerValue);

                if ("Content-Length".equalsIgnoreCase(headerName)) {
                    contentLength = Integer.parseInt(headerValue);
                }
            }
        }

        // Parse body
        byte[] body = null;
        if (contentLength > 0) {
            body = new byte[contentLength];
            int bytesRead = 0;
            while (bytesRead < contentLength) {
                bytesRead += inputStream.read(body, bytesRead, contentLength - bytesRead);
            }
        }

        return new HttpRequest(method, path, headers, body);
    }

    /**
     * Send cached response to client with cache header
     */
    private void sendCachedResponse(OutputStream outputStream, CacheEntry cacheEntry, boolean isHit) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(cacheEntry.getStatusCode()).append(" OK\r\n");

        // Add cache status header
        response.append("X-Cache: ").append(isHit ? "HIT" : "MISS").append("\r\n");

        // Add original headers
        for (Map.Entry<String, String> header : cacheEntry.getHeaders().entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // Ensure Content-Length is set
        byte[] body = cacheEntry.getBody();
        response.append("Content-Length: ").append(body != null ? body.length : 0).append("\r\n");
        response.append("\r\n");

        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));
        if (body != null && body.length > 0) {
            outputStream.write(body);
        }
        outputStream.flush();
    }

    /**
     * Send error response to client
     */
    private void sendErrorResponse(OutputStream outputStream, int statusCode, String statusText) throws IOException {
        String errorBody = "Error: " + statusCode + " " + statusText;
        byte[] bodyBytes = errorBody.getBytes(StandardCharsets.UTF_8);

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        response.append("Content-Type: text/plain\r\n");
        response.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        response.append("X-Cache: MISS\r\n");
        response.append("\r\n");

        outputStream.write(response.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.write(bodyBytes);
        outputStream.flush();
    }
}
