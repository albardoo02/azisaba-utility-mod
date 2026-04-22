package net.azisaba.azisabamod.fabric.preview;

import net.azisaba.azisabamod.fabric.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class BuildToolPlusPreviewHudRenderer {
    private static final int HUD_X = 8;
    private static final int HUD_Y = 8;
    private static final int HUD_PADDING = 4;
    private static final int HUD_LINE_HEIGHT = 10;
    private static final int HUD_BACKGROUND = 0xA0000000;
    private static final int HUD_TEXT_COLOR = 0xFFE8E8FF;

    private BuildToolPlusPreviewHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        if (!Mod.CONFIG.blockPlacementPreview) {
            return;
        }

        BuildToolPlusPreviewState state = BuildToolPlusPreviewSync.getState();
        if (!state.isValid() || !state.templatePlacementPending() || state.templatePreview() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        BuildToolPlusPreviewState.TemplatePreview preview = state.templatePreview();
        List<String> lines = new ArrayList<>();
        lines.add(Component.translatable("text.azisabamod.preview.template.ready").getString());
        lines.add(Component.translatable("text.azisabamod.preview.template.name", preview.name()).getString());
        lines.add(Component.translatable(
                "text.azisabamod.preview.template.size",
                preview.sizeX(),
                preview.sizeY(),
                preview.sizeZ(),
                preview.blockCount()
        ).getString());

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, minecraft.font.width(line));
        }

        int width = maxWidth + HUD_PADDING * 2;
        int height = lines.size() * HUD_LINE_HEIGHT + HUD_PADDING * 2;
        graphics.fill(HUD_X, HUD_Y, HUD_X + width, HUD_Y + height, HUD_BACKGROUND);

        int y = HUD_Y + HUD_PADDING;
        for (String line : lines) {
            graphics.drawString(minecraft.font, line, HUD_X + HUD_PADDING, y, HUD_TEXT_COLOR, false);
            y += HUD_LINE_HEIGHT;
        }
    }
}

