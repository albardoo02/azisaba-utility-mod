package net.azisaba.azisabamod.fabric.mixin.multiplayer;

import net.azisaba.azisabamod.fabric.connection.ws.WebSocketAddressUtil;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;

@Mixin(ServerAddress.class)
public abstract class MixinServerAddress {
    @Inject(method = "isValidAddress", at = @At("HEAD"), cancellable = true)
    private static void azisaba$allowWebSocketAddress(String address, CallbackInfoReturnable<Boolean> cir) {
        if (WebSocketAddressUtil.isWebSocketAddress(address)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "parseString", at = @At("HEAD"), cancellable = true)
    private static void azisaba$parseWebSocketAddress(String address, CallbackInfoReturnable<ServerAddress> cir) {
        URI uri = WebSocketAddressUtil.parseWebSocketUri(address);
        if (uri == null) {
            return;
        }
        cir.setReturnValue(new ServerAddress(uri.getHost(), WebSocketAddressUtil.getDefaultPort(uri)));
    }
}
