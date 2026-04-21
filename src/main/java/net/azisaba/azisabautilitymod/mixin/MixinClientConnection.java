package net.azisaba.azisabautilitymod.mixin;

import io.netty.channel.Channel;
import net.azisaba.azisabautilitymod.connection.UpdateTimePacketHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

    public MixinClientConnection() {
    }

    @Inject(method = "channelActive", at = @At("TAIL"))
    public void azisaba$addHandlers(CallbackInfo ci) {
        Channel channel = ((MixinClientConnectionAccessor) this).getChannel();

        if (channel != null && channel.pipeline() != null) {
            if (channel.pipeline().get("azisabautilitymod_time_handler") == null) {
                channel.pipeline().addBefore("packet_handler", "azisabautilitymod_time_handler", new UpdateTimePacketHandler());
            }
        }
    }
}