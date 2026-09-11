# Caching Proxy

A high-performance HTTP caching proxy server built in Java that forwards requests to other servers and caches responses for improved performance and reduced load on origin servers.

## Features

- **Request Forwarding**: Forwards HTTP requests to a specified origin server
- **Response Caching**: Caches responses from the origin server for subsequent identical requests
- **Cache Status Headers**: Adds `X-Cache` header to indicate cache hits (`HIT`) or misses (`MISS`)
- **Multi-threaded**: Handles multiple concurrent client connections efficiently
- **Cache Management**: Clear the cache on demand
- **Easy CLI Interface**: Simple command-line interface for configuration

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean package
```

This creates an executable JAR file at `target/caching-proxy.jar`.

## Usage

### Start the Proxy Server

```bash
java -jar target/caching-proxy.jar --port <number> --origin <url>
```

**Parameters:**
- `--port <number>`: Port on which the proxy server will run (1-65535)
- `--origin <url>`: URL of the origin server to forward requests to

**Example:**
```bash
java -jar target/caching-proxy.jar --port 3000 --origin http://dummyjson.com
```

### Clear the Cache

```bash
java -jar target/caching-proxy.jar --clear-cache
```

## How It Works

1. **Client Request**: A client sends an HTTP request to the proxy server
2. **Cache Lookup**: The proxy checks if the response for this request is already cached
3. **Cache Hit**: If cached, the proxy returns the cached response with `X-Cache: HIT` header
4. **Cache Miss**: If not cached, the proxy forwards the request to the origin server
5. **Response Caching**: The proxy caches the response from the origin server
6. **Response Delivery**: The proxy returns the response to the client with `X-Cache: MISS` header

### Example Request Flow

```bash
# Start the proxy
java -jar target/caching-proxy.jar --port 3000 --origin http://dummyjson.com

# In another terminal, make a request
curl -i http://localhost:3000/products

# First request - Cache MISS
# Response headers will include: X-Cache: MISS

# Second identical request
curl -i http://localhost:3000/products

# Second request - Cache HIT
# Response headers will include: X-Cache: HIT
```

## Architecture

### Core Components

1. **CachingProxy**: Main entry point that handles CLI arguments
2. **CommandLineParser**: Parses command-line arguments
3. **ProxyServer**: Manages the HTTP server and client connections
4. **RequestHandler**: Processes individual client requests
5. **HttpForwarder**: Forwards requests to the origin server
6. **CacheManager**: Manages cache storage and retrieval
7. **CacheEntry**: Represents a cached HTTP response

### Thread Safety

- The cache uses `ConcurrentHashMap` for thread-safe concurrent access
- The server uses a thread pool to handle multiple client connections efficiently

## Supported HTTP Methods

- GET
- POST
- PUT
- DELETE
- PATCH
- HEAD
- OPTIONS

## Cache Key Generation

Cache keys are generated using the format: `METHOD:URL`

For example:
- `GET:http://dummyjson.com/products`
- `POST:http://dummyjson.com/products`

This ensures that different HTTP methods for the same URL are cached separately.

## Logging

The proxy uses SLF4J with Simple Logger for logging. Logs are printed to the console and include:
- Server startup information
- Request forwarding details
- Cache hits and misses
- Error messages

## Example Usage with cURL

```bash
# Start the proxy server
java -jar target/caching-proxy.jar --port 3000 --origin http://dummyjson.com &

# Make first request (Cache MISS)
curl -i http://localhost:3000/products

# Output will include: X-Cache: MISS

# Make second identical request (Cache HIT)
curl -i http://localhost:3000/products

# Output will include: X-Cache: HIT

# Make a different request
curl -i http://localhost:3000/users

# Output will include: X-Cache: MISS

# Clear the cache
java -jar target/caching-proxy.jar --clear-cache

# Make request again (Cache MISS)
curl -i http://localhost:3000/products

# Output will include: X-Cache: MISS
```

## Error Handling

The proxy handles various error scenarios:
- **Bad Request (400)**: Invalid HTTP request format
- **Bad Gateway (502)**: Error forwarding request to origin server
- **Invalid Arguments**: Missing or invalid command-line arguments

## Future Enhancements

- **TTL Support**: Add time-to-live (TTL) for cached entries
- **Cache Expiration**: Implement automatic cache expiration based on HTTP headers
- **Cache Statistics**: Provide detailed cache statistics (hit rate, memory usage)
- **Selective Caching**: Allow caching only specific HTTP methods or status codes
- **Compression**: Support response compression to reduce bandwidth
- **Configuration File**: Support reading configuration from a file
- **REST API**: Add REST endpoints for cache management

