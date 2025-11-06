cranker-connector
=================

[![Build and test](https://github.com/chrisgroks/hsbc-cranker-connector/workflows/Build%20and%20test/badge.svg)](https://github.com/chrisgroks/hsbc-cranker-connector/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.hsbc.cranker/cranker-connector.svg)](https://central.sonatype.com/artifact/com.hsbc.cranker/cranker-connector)
[![Java Version](https://img.shields.io/badge/Java-11%2B-blue)](https://openjdk.java.net/)

A cranker connector that has no external dependencies. Requires JDK11 or later.

## Table of Contents
- [Background](#background)
- [Quick Start](#quick-start)
- [Configuration Options](#configuration-options)
  - [Basic Configuration](#basic-configuration)
  - [Advanced Configuration](#advanced-configuration)
- [Event Listeners](#event-listeners)
  - [Router Event Listener](#router-event-listener)
  - [Proxy Event Listener](#proxy-event-listener)
  - [Registration Event Listener](#registration-event-listener)
- [Protocol Versions](#protocol-versions)
- [Security Considerations](#security-considerations)
- [SSL Configuration](#ssl-configuration)
- [Zero Downtime Deployments](#zero-downtime-deployments)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Background

Cranker is a load-balancing reverse proxy designed for systems with many HTTP services that need to
be exposed at a single endpoint. It was designed for fast-moving teams that need to deploy early and often
with no central configuration needed when a new service is introduced or an existing one is upgraded.

### Key Benefits
- **Zero-downtime deployments**: Gracefully deregister services before shutdown
- **Self-configuring**: No central configuration required when adding or updating services
- **Dynamic scaling**: Services automatically register and deregister as they start and stop
- **Protocol flexibility**: Supports both simple (v1.0) and multiplexed (v3.0) protocols
- **No external dependencies**: Pure JDK 11+ implementation

### How It Works
The key difference with other reverse proxies and load balancers is that each service connects via a "connector"
to one or more routers. This connection between connector and router is a websocket, and crucially it means
the service is self-configuring; the router knows where a service is and if it is available by the fact that
it has an active websocket connection for a service.

```
Client → Cranker Router ← WebSocket ← Cranker Connector ← Your Service
         (public facing)                (embedded)         (localhost)
```

Although there are websockets in the middle, from the point of view of clients and the target microservices,
everything is standard HTTP, and any HTTP libraries can be used.

For Java-based services, a connector can be embedded into the service by using a connector library, such
as this one.

## Quick Start

In this scenario, you have a microservice that you want to expose on a list of existing cranker routers.

### 1. Add Dependency

Add this library as a dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.hsbc.cranker</groupId>
    <artifactId>cranker-connector</artifactId>
    <version>RELEASE</version>
</dependency>
```

### 2. Start Your Web Server

In your service, start a web server using your preferred framework. This will only be accessed over
a local connection, so the port number is not important (so port 0 is recommended), and you can bind to the
localhost network interface which is recommended for security reasons.

### 3. Create and Start a Connector

Create a `CrankerConnector` object, passing it the router URLs and your service's URL:

```java
URI targetServiceUri = startedWebServerUri();

CrankerConnector connector = CrankerConnectorBuilder.connector()
    .withRouterLookupByDNS(URI.create("wss://my-router.example.org:8008"))
    .withRoute("path-prefix")
    .withTarget(targetServiceUri)
    .start();
```

Your service will now be available on the cranker router at `/path-prefix/*`.

### Multiple Routers

If you have multiple routers then a single domain name can point to all instances of your router. The `withRouterLookupByDNS`
method will result in a periodic DNS lookup to resolve the router IP addresses. Note that using this approach will use
IP-address URLs which may affect SNI and/or hostname verification. Using fixed DNS names if that's an issue or see the SSL
section below for a workaround.

## Configuration Options

### Basic Configuration

The minimum required configuration consists of:

- **Route** (`withRoute`): The path prefix this service handles, e.g., `"my-service"` makes it available at `/my-service/*`
- **Target** (`withTarget`): The URI of your local backend service, e.g., `http://localhost:8080`
- **Router URIs** (`withRouterUris` or `withRouterLookupByDNS`): Where to find cranker routers

### Advanced Configuration

#### Sliding Window Size

Controls the number of idle WebSocket connections maintained per router. Higher values provide more concurrent request capacity:

```java
CrankerConnector connector = CrankerConnectorBuilder.connector()
    .withRouterLookupByDNS(URI.create("wss://router.example.org:8008"))
    .withRoute("my-service")
    .withTarget(URI.create("http://localhost:8080"))
    .withSlidingWindowSize(4)  // Default is 2
    .start();
```

#### Component Name

Sets a human-readable name for this connector instance, visible in router diagnostics and logs:

```java
.withComponentName("my-app-service")  // Default is "cranker-connector"
```

#### Domain Configuration

Specifies which domain this connector serves. Use `"*"` (default) to accept any domain:

```java
.withDomain("my-domain.com")  // Only serve requests for this domain
```

#### Router Update Interval

How frequently to check for router changes (useful with DNS-based discovery):

```java
.withRouterUpdateInterval(30, TimeUnit.SECONDS)  // Default is 1 minute
```

#### Router Deregister Timeout

Maximum time to wait for in-flight requests to complete during graceful shutdown:

```java
.withRouterDeregisterTimeout(2, TimeUnit.MINUTES)  // Default is 1 minute
```

#### Complete Advanced Example

```java
CrankerConnector connector = CrankerConnectorBuilder.connector()
    .withRouterLookupByDNS(URI.create("wss://router.example.org:8008"))
    .withRoute("my-service")
    .withTarget(URI.create("http://localhost:8080"))
    .withSlidingWindowSize(4)
    .withComponentName("my-app-service")
    .withDomain("my-domain.com")
    .withRouterUpdateInterval(30, TimeUnit.SECONDS)
    .withRouterDeregisterTimeout(2, TimeUnit.MINUTES)
    .start();
```

## Event Listeners

### Router Event Listener

Listen to router registration changes and connection errors:

```java
CrankerConnectorBuilder.connector()
    .withRouterRegistrationListener(new RouterEventListener() {
        @Override
        public void onRegistrationChanged(ChangeData data) {
            log.info("Router registration changed: " + data);
        }
        
        @Override
        public void onSocketConnectionError(RouterRegistration router, Throwable exception) {
            log.warn("Error connecting to " + router, exception);
        }
        
        @Override
        public void onRouterDnsLookupError(Throwable exception) {
            log.error("DNS lookup failed", exception);
        }
    })
    // ... other configuration
    .start();
```

### Proxy Event Listener

Intercept and modify requests before they are sent to your target service:

```java
CrankerConnectorBuilder.connector()
    .withProxyEventListener(new ProxyEventListener() {
        @Override
        public HttpRequest beforeProxyToTarget(HttpRequest request, HttpRequest.Builder builder) {
            // Add custom headers, modify the request, etc.
            return builder
                .header("X-Custom-Header", "value")
                .header("X-Request-ID", UUID.randomUUID().toString())
                .build();
        }
        
        @Override
        public void onProxyError(HttpRequest request, Throwable error) {
            log.error("Proxy error for request: " + request.uri(), error);
        }
    })
    // ... other configuration
    .start();
```

### Registration Event Listener

Inject authentication or custom headers into the WebSocket registration request. See the [Security Considerations](#security-considerations) section for details.

## Protocol Versions

Cranker Connector supports two protocol versions that are automatically negotiated with the router:

### cranker_1.0 (Simple Protocol)
- **One HTTP request per WebSocket connection**
- Text-based protocol for headers
- Binary for request/response bodies
- Stable and reliable
- Default fallback protocol

### cranker_3.0 (Multiplexing Protocol)
- **Multiple concurrent HTTP requests per WebSocket connection**
- Binary protocol with efficient framing
- Flow control to prevent memory issues
- More efficient for high-traffic scenarios
- Preferred protocol (negotiated first by default)

The connector automatically negotiates the best protocol supported by both the connector and router. The default preference is `["cranker_3.0", "cranker_1.0"]`.

### Customizing Protocol Preference

You can customize which protocols to use and their negotiation order:

```java
CrankerConnectorBuilder.connector()
    .withPreferredProtocols(List.of(
        CrankerConnectorBuilder.CRANKER_PROTOCOL_3,
        CrankerConnectorBuilder.CRANKER_PROTOCOL_1
    ))
    // ... other configuration
    .start();
```

To use only the simple protocol:

```java
.withPreferredProtocols(List.of(CrankerConnectorBuilder.CRANKER_PROTOCOL_1))
```

## Security Considerations

If the routers being connected to do not trust all connections on the cranker registration port, you must apply
authentication on the connectors to ensure only authorized connectors can serve traffic.

### Authentication Methods

Any TCP or HTTP authentication scheme can be used, including:
- **Token authentication** (Bearer tokens, API keys)
- **Basic authentication**
- **mTLS** (mutual TLS with client certificates)
- **IP validation** (network-level security)

### mTLS Authentication

For mTLS, supply an HTTP client configured with the client certificate:

```java
HttpClient client = HttpClient.newBuilder()
    .sslContext(sslContextWithClientCert)
    .build();

CrankerConnectorBuilder.connector()
    .withHttpClient(client)
    // ... other configuration
    .start();
```

### HTTP Header-Based Authentication

For HTTP header-based authentication schemes, use a registration listener to inject headers into the WebSocket registration request:

```java
CrankerConnectorBuilder.connector()
    .withRegistrationEventListener(new RegistrationEventListener() {
        @Override
        public void beforeRegisterToRouter(RouterRegistrationContext context) {
            context.getWebsocketBuilder()
                .header("Authorization", "Bearer " + getAuthToken())
                .header("X-API-Key", getApiKey());
        }
    })
    // ... other configuration
    .start();
```

## SSL Configuration

This library establishes connections to one or more routers, and one local target HTTP server. These connections can
use HTTPS/WSS or plain HTTP/WS. If using a secure connection, then the default JDK parameters are used.

### Self-Signed Certificates

If self-signed certificates are used (common for localhost communications or development), you can specify a client that trusts all SSL certs:

```java
CrankerConnector connector = CrankerConnectorBuilder.connector()
    .withHttpClient(CrankerConnectorBuilder.createHttpClient(true).build())
    .withRouterLookupByDNS(URI.create("wss://my-router.example.org:8008"))
    .withRoute("path-prefix")
    .withTarget(targetServiceUri)
    .start();
```

**Note:** The `createHttpClient(true)` helper creates an HTTP client that trusts all certificates and disables hostname verification. This is useful for:
- Development environments with self-signed certificates
- Connecting to routers via IP addresses (which affects SNI/hostname verification)
- Testing scenarios

**Warning:** Only use `createHttpClient(true)` in development or trusted environments. For production, use proper SSL certificates and validation.

### Custom SSL Configuration

For more control over SSL configuration, create your own `HttpClient`:

```java
SSLContext sslContext = SSLContext.getInstance("TLS");
// Configure sslContext with your truststore, keystore, etc.

HttpClient client = HttpClient.newBuilder()
    .sslContext(sslContext)
    .build();

CrankerConnector connector = CrankerConnectorBuilder.connector()
    .withHttpClient(client)
    // ... other configuration
    .start();
```

## Zero Downtime Deployments

As long as you have at least 2 instances of your connected service, you can perform a zero downtime
deployment by simply stopping and restarting one instance at a time.

### Graceful Shutdown

To ensure that any in-flight requests complete before your application shuts down, call `stop(long timeout, TimeUnit timeUnit)` on the connector object:

```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    log.info("Shutting down connector...");
    boolean cleanShutdown = connector.stop(10, TimeUnit.SECONDS);
    if (!cleanShutdown) {
        log.warn("Timeout waiting for requests to complete, forcing shutdown");
    }
    targetServer.stop();
}));
```

### How It Works

1. **Deregistration**: The connector deregisters from all routers, preventing new requests
2. **Wait for completion**: Waits for active requests to complete within the timeout
3. **Return value**:
   - `true`: All active requests completed successfully within timeout
   - `false`: Timeout expired with requests still in progress

If `stop()` returns `false`, you can choose to:
- Wait longer (call `stop()` again with a longer timeout)
- Force shutdown (shut down your web service, which will abort in-flight requests)

### Rolling Deployment Example

For a rolling deployment with Kubernetes or similar orchestration:

1. Deploy new version alongside old version
2. New instances register with routers automatically
3. Stop old instances one at a time (graceful shutdown)
4. Monitor that at least N instances are healthy before proceeding

## Troubleshooting

### Connection Failures

**Symptoms:** Connector cannot establish WebSocket connections to routers

**Possible causes and solutions:**
- **Incorrect router URLs**: Verify router URLs are correct and accessible
- **Network connectivity**: Check firewall rules and network connectivity to router hosts
- **SSL/TLS issues**: For self-signed certificates, use `withHttpClient(createHttpClient(true).build())`
- **Authentication required**: Implement authentication using `withRegistrationEventListener()`
- **Router not running**: Verify cranker routers are running and accepting connections

**Check logs for:**
```
RouterEventListener.onSocketConnectionError()
RouterEventListener.onRouterDnsLookupError()
```

### DNS Lookup Issues

**Symptoms:** When using `withRouterLookupByDNS()`, routers are not discovered

**Solutions:**
- Verify DNS resolution: `nslookup router.example.org`
- Check router update interval is appropriate: `withRouterUpdateInterval(30, TimeUnit.SECONDS)`
- Implement `RouterEventListener.onRouterDnsLookupError()` to log DNS errors
- Ensure DNS TTL is compatible with your update interval

### High Connection Count

**Symptoms:** Too many WebSocket connections to routers

**Solutions:**
- Reduce sliding window size: `withSlidingWindowSize(1)` (default is 2)
- Verify you don't have more router instances than expected
- Check if multiple connector instances are running unintentionally

### Requests Timing Out During Shutdown

**Symptoms:** Graceful shutdown (`stop()`) times out, returns `false`

**Solutions:**
- Increase deregister timeout: `withRouterDeregisterTimeout(2, TimeUnit.MINUTES)`
- Check if in-flight requests are completing normally (not hanging)
- Monitor your target service for stuck requests
- Verify your target service is still responsive during shutdown

### SSL Hostname Verification Failures

**Symptoms:** Connection errors related to hostname verification when using IP addresses

**Solutions:**
- Use `withHttpClient(createHttpClient(true).build())` to disable hostname verification
- Use DNS names instead of IP addresses for router URLs
- Configure custom SSL context with appropriate hostname verifier

### Memory or Performance Issues

**Symptoms:** High memory usage or slow request processing

**Solutions:**
- Protocol v3.0 has built-in flow control; ensure you're using it (default)
- Check sliding window size isn't too high for your traffic patterns
- Monitor router health and capacity
- Review target service performance (connector just proxies requests)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Building Locally

```bash
git clone https://github.com/hsbc/cranker-connector.git
cd cranker-connector
mvn clean verify
```

### Running Tests

```bash
mvn test
```

### Code Quality

This project uses:
- **Maven** for builds and dependency management
- **JUnit 5** for testing
- **GitHub Actions** for CI/CD
- **No external runtime dependencies** (core principle)

### Reporting Issues

Please report issues via [GitHub Issues](https://github.com/hsbc/cranker-connector/issues).

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

