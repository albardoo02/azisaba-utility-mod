package net.azisaba.azisabamod.fabric.mixin.multiplayer;

import net.azisaba.azisabamod.fabric.Mod;
import net.azisaba.azisabamod.fabric.connection.ws.LocalWebSocketProxyServer;
import net.azisaba.azisabamod.fabric.connection.ws.WebSocketAddressUtil;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ServerStatusPinger.class)
public abstract class MixinServerStatusPinger {
    @Unique
    private static final Map<ServerData, String> azisaba$originalAddresses = new ConcurrentHashMap<>();

    @Inject(method = "pingServer", at = @At("HEAD"))
    private void azisaba$routePingThroughProxy(ServerData data, Runnable onPingSuccess, Runnable onPingFailure, EventLoopGroupHolder eventLoopGroupHolder, CallbackInfo ci) {
        URI uri = WebSocketAddressUtil.parseWebSocketUri(data.ip);
        if (uri == null) {
            return;
        }

        try {
            int localPort = new LocalWebSocketProxyServer(uri).start();
            azisaba$originalAddresses.put(data, data.ip);
            data.ip = "127.0.0.1:" + localPort;
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to start WebSocket ping proxy for {}", data.ip, e);
        }
    }

    @Inject(method = "pingServer", at = @At("RETURN"))
    private void azisaba$restoreOriginalAddress(ServerData data, Runnable onPingSuccess, Runnable onPingFailure, EventLoopGroupHolder eventLoopGroupHolder, CallbackInfo ci) {
        String originalAddress = azisaba$originalAddresses.remove(data);
        if (originalAddress != null) {
            data.ip = originalAddress;
        }
    }
}
