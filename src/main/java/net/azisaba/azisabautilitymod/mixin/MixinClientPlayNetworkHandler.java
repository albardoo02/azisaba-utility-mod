package net.azisaba.azisabautilitymod.mixin;

import io.netty.channel.Channel;
import net.azisaba.azisabautilitymod.connection.UpdateTimePacketHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow @Final
    private ClientConnection connection;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        Channel channel = ((MixinClientConnectionAccessor) this.connection).getChannel();

        if (channel.pipeline().get("azisaba-utility-mod-handler") == null) {
            channel.pipeline().addLast("azisaba-utility-mod-handler", new UpdateTimePacketHandler());
        }
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onDisconnected(Text reason, CallbackInfo ci) {
        UpdateTimePacketHandler.uuidToNameMap.clear();
        UpdateTimePacketHandler.admin.clear();
    }
}