package net.azisaba.azisabautilitymod.mixin;

import net.azisaba.azisabautilitymod.connection.UpdateTimePacketHandler;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

    @Inject(at = @At("RETURN"), method = "getPlayerName", cancellable = true)
    public void rewriteComponent(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        String s = UpdateTimePacketHandler.admin.get(entry.getProfile().getId());
        if (s == null || s.trim().isEmpty()) return;
        Text originalComponent = cir.getReturnValue();
        Text newComponent = originalComponent.copy().append(new LiteralText(" §8<" + s + "§8>"));

        cir.setReturnValue(newComponent);
    }
}