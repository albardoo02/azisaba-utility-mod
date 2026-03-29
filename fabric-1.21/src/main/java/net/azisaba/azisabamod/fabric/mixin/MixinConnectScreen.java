package net.azisaba.azisabamod.fabric.mixin;

import net.azisaba.azisabamod.fabric.Mod;
import net.azisaba.azisabamod.fabric.connection.ws.LocalWebSocketProxyServer;
import net.azisaba.azisabamod.fabric.connection.ws.WebSocketAddressUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen {
    private static final ThreadLocal<Boolean> azisaba$rerouting = ThreadLocal.withInitial(() -> false);

    @Inject(method = "startConnecting", at = @At("HEAD"), cancellable = true)
    private static void azisaba$startWebSocketProxy(Screen parent, Minecraft minecraft, ServerAddress hostAndPort, ServerData serverData, boolean quickPlayLog, TransferState transferState, CallbackInfo ci) {
        if (azisaba$rerouting.get()) {
            return;
        }

        String rawAddress = serverData != null ? serverData.ip : hostAndPort.toString();
        URI uri = WebSocketAddressUtil.parseWebSocketUri(rawAddress);
        if (uri == null) {
            return;
        }

        try {
            int localPort = new LocalWebSocketProxyServer(uri).start();
            azisaba$rerouting.set(true);
            ConnectScreen.startConnecting(parent, minecraft, new ServerAddress("127.0.0.1", localPort), serverData, quickPlayLog, transferState);
            ci.cancel();
        } catch (Exception e) {
            Mod.LOGGER.error("Failed to start WebSocket proxy for {}", rawAddress, e);
        } finally {
            azisaba$rerouting.set(false);
        }
    }
}
