package net.azisaba.azisabamod.fabric.mixin.gui.components;

import net.azisaba.azisabamod.fabric.connection.UpdateTimePacketHandler;
import net.azisaba.azisabamod.fabric.debug.AzisabaDebugScreenEntries;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class MixinPlayerListHud {
    @Inject(at = @At("RETURN"), method = "getNameForDisplay")
    public void rewriteComponent(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        if (!AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.INDICATOR_IN_PLAYER_LIST)) return;
        String s = UpdateTimePacketHandler.admin.get(entry.getProfile().id());
        if (s == null || s.isBlank()) return;
        var component = cir.getReturnValue();
        if (!(component instanceof MutableComponent text)) return;
        text.append(" §8<" + s + "§8>");
    }
}
