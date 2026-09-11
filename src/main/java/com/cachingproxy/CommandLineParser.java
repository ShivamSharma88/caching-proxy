package com.cachingproxy;

/**
 * Parses command-line arguments
 */
public class CommandLineParser {
    public static CommandLineArgs parse(String[] args) throws IllegalArgumentException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        int port = -1;
        String origin = null;
        boolean clearCache = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("--port".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--port requires a value");
                }
                try {
                    port = Integer.parseInt(args[++i]);
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException("Port must be between 1 and 65535");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Port must be a valid integer");
                }
            } else if ("--origin".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("--origin requires a value");
                }
                origin = args[++i];
                if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
                    throw new IllegalArgumentException("Origin URL must start with http:// or https://");
                }
            } else if ("--clear-cache".equals(arg)) {
                clearCache = true;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        return new CommandLineArgs(port, origin, clearCache);
    }
}
