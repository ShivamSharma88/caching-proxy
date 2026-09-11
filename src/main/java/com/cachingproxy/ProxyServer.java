package com.cachingproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP Proxy Server that handles incoming requests
 */
public class ProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private final int port;
    private final String originUrl;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    public ProxyServer(int port, String originUrl) {
        this.port = port;
        this.originUrl = originUrl;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Start the proxy server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        logger.info("Caching Proxy Server started on port: {}", port);
        logger.info("Forwarding requests to: {}", originUrl);
        logger.info("Press Ctrl+C to stop the server");

        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(new RequestHandler(clientSocket, originUrl));
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Server error: {}", e.getMessage(), e);
            }
        } finally {
            stop();
        }
    }

    /**
     * Stop the proxy server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: {}", e.getMessage());
        }
        executorService.shutdown();
        logger.info("Caching Proxy Server stopped");
    }
}
