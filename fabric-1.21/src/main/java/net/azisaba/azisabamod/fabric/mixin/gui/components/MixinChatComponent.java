package net.azisaba.azisabamod.fabric.mixin.gui.components;

import net.azisaba.azisabamod.fabric.util.MessageUtil;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public class MixinChatComponent {
    @Redirect(at = @At(value = "NEW", target = "(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)Lnet/minecraft/client/GuiMessage;"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    public GuiMessage addMessageToQueue(int i, Component component, MessageSignature messageSignature, GuiMessageTag guiMessageTag) {
        Component newComponent = component.copy();
        MessageUtil.processSiblings(newComponent);
        return new GuiMessage(i, newComponent, messageSignature, guiMessageTag);
    }
}
