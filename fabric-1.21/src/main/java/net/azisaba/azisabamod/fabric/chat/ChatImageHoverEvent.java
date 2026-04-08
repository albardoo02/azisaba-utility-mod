package net.azisaba.azisabamod.fabric.chat;

import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

public record ChatImageHoverEvent(@NotNull ChatImage image) implements HoverEvent {
    @Override
    public @NotNull Action action() {
        throw new UnsupportedOperationException();
    }
}
