package com.cachingproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Caching Proxy application
 */
public class CachingProxy {
    private static final Logger logger = LoggerFactory.getLogger(CachingProxy.class);

    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            CommandLineArgs cliArgs = CommandLineParser.parse(args);

            if (cliArgs.isClearCache()) {
                // Clear cache and exit
                CacheManager.getInstance().clearCache();
                logger.info("Cache cleared successfully");
                System.exit(0);
            }

            if (cliArgs.getPort() <= 0 || cliArgs.getOrigin() == null) {
                logger.error("Error: --port and --origin are required arguments");
                printUsage();
                System.exit(1);
            }

            // Start the proxy server
            ProxyServer proxyServer = new ProxyServer(cliArgs.getPort(), cliArgs.getOrigin());
            proxyServer.start();

        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            printUsage();
            System.exit(1);
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("\nUsage:");
        System.out.println("  caching-proxy --port <number> --origin <url>");
        System.out.println("  caching-proxy --clear-cache");
        System.out.println("\nOptions:");
        System.out.println("  --port <number>      Port on which the proxy server will run");
        System.out.println("  --origin <url>       URL of the origin server to forward requests to");
        System.out.println("  --clear-cache        Clear the cache and exit");
        System.out.println("\nExample:");
        System.out.println("  caching-proxy --port 3000 --origin http://dummyjson.com");
    }
}
