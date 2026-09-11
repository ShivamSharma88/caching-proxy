package com.cachingproxy;

/**
 * Data class for parsed command-line arguments
 */
public class CommandLineArgs {
    private int port;
    private String origin;
    private boolean clearCache;

    public CommandLineArgs(int port, String origin, boolean clearCache) {
        this.port = port;
        this.origin = origin;
        this.clearCache = clearCache;
    }

    public int getPort() {
        return port;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean isClearCache() {
        return clearCache;
    }
}
