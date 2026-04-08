package net.azisaba.azisabamod.fabric.mixin.gui.screens;

import net.azisaba.azisabamod.fabric.Mod;
import net.azisaba.azisabamod.fabric.util.BuildToolUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {
    protected MixinAbstractContainerScreen(Component component) {
        super(component);
    }

    @Shadow
    public abstract AbstractContainerMenu getMenu();

    @Inject(at = @At("TAIL"), method = "onClose")
    public void onClose(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (getMenu() instanceof ChestMenu chestMenu && "Build Tool Options".equals(title.tryCollapseToString()) && BuildToolUtil.isBuildTool(player.getMainHandItem())) {
            Mod.CONFIG.buildTool = BuildToolUtil.identifyOptions(chestMenu);
            Mod.CONFIG.saveAsync();
        }
    }
}
