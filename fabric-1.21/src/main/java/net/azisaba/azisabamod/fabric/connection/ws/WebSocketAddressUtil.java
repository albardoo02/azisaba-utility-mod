package net.azisaba.azisabamod.fabric.connection.ws;

import org.jetbrains.annotations.Nullable;

import java.net.URI;

public final class WebSocketAddressUtil {
    private WebSocketAddressUtil() {}

    public static boolean isWebSocketAddress(@Nullable String address) {
        return parseWebSocketUri(address) != null;
    }

    public static @Nullable URI parseWebSocketUri(@Nullable String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(address.trim());
            String scheme = uri.getScheme();
            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static int getDefaultPort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "wss".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
