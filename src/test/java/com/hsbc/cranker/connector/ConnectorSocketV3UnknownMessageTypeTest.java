package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectorSocketV3UnknownMessageTypeTest {

    @Test
    void testUnknownMessageTypeClosesConnection() throws Exception {
        CountDownLatch closeLatch = new CountDownLatch(1);
        final Throwable[] capturedError = new Throwable[1];
        
        ConnectorSocketListener listener = new ConnectorSocketListener() {
            @Override
            public void onClose(ConnectorSocket socket, Throwable error) {
                capturedError[0] = error;
                closeLatch.countDown();
            }
            
            @Override
            public void onConnectionAcquired(ConnectorSocket socket) {
            }
        };
        
        HttpClient httpClient = HttpClient.newHttpClient();
        URI targetURI = URI.create("http://localhost:8080");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        
        ConnectorSocketV3 socket = new ConnectorSocketV3(
            targetURI,
            httpClient,
            listener,
            new ProxyEventListener() {},
            executor
        );
        
        final int[] closeStatusCode = new int[1];
        final String[] closeReason = new String[1];
        
        WebSocket mockWebSocket = createMockWebSocket(closeStatusCode, closeReason);
        
        socket.onOpen(mockWebSocket);
        
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put((byte) 5);
        buffer.put((byte) 0);
        buffer.putInt(1);
        buffer.flip();
        
        socket.onBinary(mockWebSocket, buffer, true);
        
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS), "Close callback should be called");
        
        assertNotNull(capturedError[0], "Error should be captured");
        assertTrue(capturedError[0] instanceof IllegalStateException, "Error should be IllegalStateException");
        assertTrue(capturedError[0].getMessage().contains("Unknown message type: 5"), 
                   "Error message should indicate unknown message type: " + capturedError[0].getMessage());
        assertTrue(capturedError[0].getMessage().contains("requestId=1"),
                   "Error message should include request ID: " + capturedError[0].getMessage());
        
        assertEquals(1002, closeStatusCode[0], "WebSocket should be closed with status code 1002");
        assertTrue(closeReason[0].contains("Unknown message type"), 
                   "Close reason should mention unknown message type: " + closeReason[0]);
        
        executor.shutdown();
    }

    @Test
    void testOnErrorHandling() throws Exception {
        CountDownLatch closeLatch = new CountDownLatch(1);
        final Throwable[] capturedError = new Throwable[1];
        
        ConnectorSocketListener listener = new ConnectorSocketListener() {
            @Override
            public void onClose(ConnectorSocket socket, Throwable error) {
                capturedError[0] = error;
                closeLatch.countDown();
            }
            
            @Override
            public void onConnectionAcquired(ConnectorSocket socket) {
            }
        };
        
        HttpClient httpClient = HttpClient.newHttpClient();
        URI targetURI = URI.create("http://localhost:8080");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        
        ConnectorSocketV3 socket = new ConnectorSocketV3(
            targetURI,
            httpClient,
            listener,
            new ProxyEventListener() {},
            executor
        );
        
        final int[] closeStatusCode = new int[1];
        
        WebSocket mockWebSocket = createMockWebSocket(closeStatusCode, new String[1]);
        
        socket.onOpen(mockWebSocket);
        
        Exception testError = new Exception("Test error");
        socket.onError(mockWebSocket, testError);
        
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS), "Close callback should be called");
        
        assertNotNull(capturedError[0], "Error should be captured");
        assertEquals(testError, capturedError[0], "Captured error should match the test error");
        assertEquals(1011, closeStatusCode[0], "WebSocket should be closed with status code 1011");
        
        executor.shutdown();
    }

    @Test
    void testOnPingHandling() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        URI targetURI = URI.create("http://localhost:8080");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        
        ConnectorSocketV3 socket = new ConnectorSocketV3(
            targetURI,
            httpClient,
            new ConnectorSocketListener() {
                @Override
                public void onClose(ConnectorSocket socket, Throwable error) {}
                
                @Override
                public void onConnectionAcquired(ConnectorSocket socket) {}
            },
            new ProxyEventListener() {},
            executor
        );
        
        final boolean[] requestCalled = new boolean[1];
        
        WebSocket mockWebSocket = new WebSocket() {
            @Override
            public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public void request(long n) {
                requestCalled[0] = true;
            }
            
            @Override
            public String getSubprotocol() {
                return "";
            }
            
            @Override
            public boolean isOutputClosed() {
                return false;
            }
            
            @Override
            public boolean isInputClosed() {
                return false;
            }
            
            @Override
            public void abort() {}
        };
        
        socket.onOpen(mockWebSocket);
        socket.onPing(mockWebSocket, ByteBuffer.allocate(0));
        
        assertTrue(requestCalled[0], "WebSocket.request(1) should be called after onPing");
        
        executor.shutdown();
    }

    private WebSocket createMockWebSocket(final int[] closeStatusCode, final String[] closeReason) {
        return new WebSocket() {
            @Override
            public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
                closeStatusCode[0] = statusCode;
                if (closeReason != null && closeReason.length > 0) {
                    closeReason[0] = reason;
                }
                return CompletableFuture.completedFuture(this);
            }
            
            @Override
            public void request(long n) {}
            
            @Override
            public String getSubprotocol() {
                return "";
            }
            
            @Override
            public boolean isOutputClosed() {
                return false;
            }
            
            @Override
            public boolean isInputClosed() {
                return false;
            }
            
            @Override
            public void abort() {}
        };
    }
}
