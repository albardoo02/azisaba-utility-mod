package net.azisaba.azisabamod.fabric.mixin.network;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommonPacketListenerImpl.class)
public interface MixinClientCommonNetworkHandlerAccessor {
    @Accessor
    Connection getConnection();
}
