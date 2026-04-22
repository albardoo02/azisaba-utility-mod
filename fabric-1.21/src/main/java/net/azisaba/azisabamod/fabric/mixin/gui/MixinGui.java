package net.azisaba.azisabamod.fabric.mixin.gui;

import net.azisaba.azisabamod.fabric.preview.BuildToolPlusPreviewHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderBuildToolPlusHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        BuildToolPlusPreviewHudRenderer.render(guiGraphics);
    }
}

