package net.azisaba.azisabamod.fabric.connection.ws;

import net.azisaba.azisabamod.fabric.Mod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalWebSocketProxyServer {
    private final URI targetUri;
    private final ServerSocket serverSocket;

    public LocalWebSocketProxyServer(URI targetUri) throws IOException {
        this.targetUri = targetUri;
        this.serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
    }

    public int start() {
        Thread.startVirtualThread(this::acceptOnce);
        return serverSocket.getLocalPort();
    }

    private void acceptOnce() {
        try (serverSocket; Socket clientSocket = serverSocket.accept()) {
            clientSocket.setTcpNoDelay(true);
            bridge(clientSocket);
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to run local WebSocket proxy for {}", targetUri, e);
        }
    }

    private void bridge(Socket clientSocket) throws Exception {
        OutputStream output = clientSocket.getOutputStream();
        AtomicBoolean closed = new AtomicBoolean(false);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ProxyWebSocketListener listener = new ProxyWebSocketListener(output, closed, clientSocket);
        WebSocket webSocket = httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(targetUri, listener)
                .join();

        Thread upstreamThread = Thread.startVirtualThread(() -> forwardClientToWebSocket(clientSocket, webSocket, closed));
        upstreamThread.join();
    }

    private void forwardClientToWebSocket(Socket clientSocket, WebSocket webSocket, AtomicBoolean closed) {
        byte[] buffer = new byte[8192];
        try (InputStream input = clientSocket.getInputStream()) {
            int read;
            while (!closed.get() && (read = input.read(buffer)) >= 0) {
                if (read == 0) {
                    continue;
                }
                webSocket.sendBinary(ByteBuffer.wrap(Arrays.copyOf(buffer, read)), true).join();
            }
        } catch (Exception e) {
            if (!closed.get()) {
                Mod.LOGGER.debug("Closing WebSocket proxy upstream for {}", targetUri, e);
            }
        } finally {
            closeQuietly(clientSocket, closed);
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "minecraft client disconnected").join();
            } catch (Exception ignored) {
            }
        }
    }

    private static void closeQuietly(Socket socket, AtomicBoolean closed) {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private static final class ProxyWebSocketListener implements WebSocket.Listener {
        private final OutputStream output;
        private final AtomicBoolean closed;
        private final Socket clientSocket;
        private final ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

        private ProxyWebSocketListener(OutputStream output, AtomicBoolean closed, Socket clientSocket) {
            this.output = output;
            this.closed = closed;
            this.clientSocket = clientSocket;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            try {
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                synchronized (frameBuffer) {
                    frameBuffer.write(bytes);
                    if (last) {
                        output.write(frameBuffer.toByteArray());
                        output.flush();
                        frameBuffer.reset();
                    }
                }
            } catch (IOException e) {
                closeQuietly(clientSocket, closed);
            } finally {
                webSocket.request(1);
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            closeQuietly(clientSocket, closed);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            Mod.LOGGER.error("WebSocket proxy downstream failed", error);
            closeQuietly(clientSocket, closed);
        }
    }
}
