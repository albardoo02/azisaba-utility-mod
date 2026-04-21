package net.azisaba.azisabautilitymod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MixinMinecraftClientAccessor {

    @Accessor("interactionManager")
    ClientPlayerInteractionManager getInteractionManager();
}