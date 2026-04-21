package net.azisaba.azisabautilitymod.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface MixinClientCommonNetworkHandlerAccessor {

    @Accessor
    ClientConnection getConnection();
}