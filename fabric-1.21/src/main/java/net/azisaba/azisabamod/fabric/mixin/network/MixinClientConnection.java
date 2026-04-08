package net.azisaba.azisabamod.fabric.mixin.network;

import io.netty.channel.ChannelPipeline;
import net.azisaba.azisabamod.fabric.connection.UpdateTimePacketHandler;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinClientConnection {
    @Inject(at = @At("TAIL"), method = "configurePacketHandler")
    public void addHandlers(ChannelPipeline pipeline, CallbackInfo ci) {
        pipeline.addBefore("packet_handler", "azisabamod_time_handler", new UpdateTimePacketHandler());
    }
}
