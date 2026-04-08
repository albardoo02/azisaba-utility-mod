package net.azisaba.azisabamod.fabric.util;

import net.azisaba.azisabamod.fabric.Mod;
import net.azisaba.azisabamod.fabric.chat.ChatImage;
import net.azisaba.azisabamod.fabric.chat.ChatImageHoverEvent;
import net.azisaba.azisabamod.fabric.chat.contents.ImageContents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public class MessageUtil {
    public static boolean processSiblings(Component component) {
        boolean modified = false;
        for (int i = 0; i < component.getSiblings().size(); i++) {
            Component sibling = component.getSiblings().get(i);
            Component replaced = checkComponent(sibling.copy());
            if (replaced != null) {
                component.getSiblings().set(i, replaced);
                modified = true;
            }
        }
        return modified;
    }

    public static @NotNull MutableComponent replaceContents(@NotNull Component component, @NotNull ComponentContents contents) {
        MutableComponent newComponent = MutableComponent.create(contents);
        newComponent.getSiblings().addAll(component.getSiblings());
        newComponent.withStyle(component.getStyle());
        return newComponent;
    }

    public static @Nullable Component checkComponent(@NotNull Component component) {
        if (component.getContents() instanceof PlainTextContents plainTextContents) {
            String text = plainTextContents.text();
            if (!text.isEmpty()) {
                MutableComponent newComponent = replaceContents(component, new PlainTextContents.LiteralContents(""));
                newComponent.getSiblings().clear();
                for (UrlUtil.MatchResult result : UrlUtil.matchUrls(text)) {
                    if (result.isUrl()) {
                        ChatImage image = new ChatImage(result.text());
                        MutableComponent innerComponent = MutableComponent.create(new ImageContents(image));
                        innerComponent.withStyle(style -> style.withHoverEvent(new ChatImageHoverEvent(image)).withClickEvent(new ClickEvent.OpenUrl(URI.create(result.text()))));
                        if (UrlUtil.isShouldBeImage(result.text())) {
                            innerComponent.withStyle(ChatFormatting.AQUA);
                        }
                        newComponent.append(innerComponent);
                    } else {
                        newComponent.append(Component.literal(result.text()));
                    }
                }
                newComponent.getSiblings().addAll(component.getSiblings());
                return newComponent;
            }
        }
        if (processSiblings(component)) return component;
        return null;
    }
}
