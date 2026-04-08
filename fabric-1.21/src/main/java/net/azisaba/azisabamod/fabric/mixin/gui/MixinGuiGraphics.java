package net.azisaba.azisabamod.fabric.mixin.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.datafixers.util.Pair;
import net.azisaba.azisabamod.fabric.chat.ChatImage;
import net.azisaba.azisabamod.fabric.chat.ChatImageHoverEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.OptionalInt;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {
    @Shadow
    public abstract void setTooltipForNextFrame(Component component, int i, int j);

    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    public abstract int guiWidth();

    @Shadow
    public abstract int guiHeight();

    @Shadow
    public abstract void blit(RenderPipeline renderPipeline, Identifier identifier, int x, int y, float f, float g, int width, int height, int textureWidth, int textureHeight);

    @Inject(at = @At("HEAD"), method = "renderComponentHoverEffect")
    public void renderImageHoverEvent(Font font, Style style, int i, int j, CallbackInfo ci) {
        if (style != null && style.getHoverEvent() != null && style.getHoverEvent() instanceof ChatImageHoverEvent(ChatImage image)) {
            OptionalInt optWidth = image.getWidth();
            OptionalInt optHeight = image.getHeight();
            if (image.getTexture() == null || optWidth.isEmpty() || optHeight.isEmpty()) {
                setTooltipForNextFrame(Component.literal("↓画像が見つかりません"), i, j);
            } else {
                int marginWidth = guiWidth() / 16;
                int marginHeight = guiHeight() / 16;
                Pair<Integer, Integer> scaled = scaleToTarget(optWidth.getAsInt(), optHeight.getAsInt(), guiWidth() - marginWidth * 2, guiHeight() - marginHeight * 2);
                int width = scaled.getFirst();
                int height = scaled.getSecond();
                int padding = 2;
                int allWidth = width + padding * 2;
                int allHeight = height + padding * 2;
                ClientTooltipPositioner positioner = DefaultTooltipPositioner.INSTANCE;
                Vector2ic vec = positioner.positionTooltip(guiWidth(), guiHeight(), i, j, allWidth, allHeight);
                int x = vec.x();
                int y = vec.y();
                pose.pushMatrix();
                TooltipRenderUtil.renderTooltipBackground((GuiGraphics) (Object) this, x, y, allWidth, allHeight, null);
                pose.translate(0.0F, 0.0F);
                blit(RenderPipelines.GUI_TEXTURED, image.getIdentifier(), x + padding, y + padding, 0, 0, width, height, width, height);
                pose.popMatrix();
            }
        }
    }

    @Unique
    private Pair<Integer, Integer> scaleToTarget(int width, int height, int targetWidth, int targetHeight) {
        BigDecimal bigDecimal = new BigDecimal((float) height / width).setScale(2, RoundingMode.HALF_UP);
        double x = bigDecimal.doubleValue();
        if (width > targetWidth) {
            return Pair.of(targetWidth, (int) (targetWidth * x));
        }
        return Pair.of((int) (targetHeight / x), targetHeight);
    }
}
