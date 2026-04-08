package net.azisaba.azisabamod.fabric.debug;

import io.netty.channel.Channel;
import net.azisaba.azisabamod.fabric.connection.UpdateTimePacketHandler;
import net.azisaba.azisabamod.fabric.mixin.network.MixinClientCommonNetworkHandlerAccessor;
import net.azisaba.azisabamod.fabric.mixin.network.MixinClientConnectionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class DebugEntryTps implements DebugScreenEntry {
    private static final DecimalFormat TPS_FORMAT = new DecimalFormat("00.00");
    private long lastUpdated = 0;
    private String value = "";

    @Override
    public void display(@NonNull DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        Connection clientConnection = ((MixinClientCommonNetworkHandlerAccessor) minecraft.player.connection).getConnection();
        Channel channel = ((MixinClientConnectionAccessor) clientConnection).getChannel();
        UpdateTimePacketHandler updateTimePacketHandler = channel.pipeline().get(UpdateTimePacketHandler.class);
        if (updateTimePacketHandler != null && System.currentTimeMillis() - lastUpdated > 500) {
            lastUpdated = System.currentTimeMillis();
            double tps5s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(5000) / 20.0 / 50.0);
            double tps10s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(10000) / 20.0 / 50.0);
            double tps15s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(15000) / 20.0 / 50.0);
            double tps30s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(30000) / 20.0 / 50.0);
            double tps60s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(60000) / 20.0 / 50.0);
            double tps180s = 20.0 / (updateTimePacketHandler.getAverageMsPerSecond(180000) / 20.0 / 50.0);
            value = String.format(Locale.ROOT, "%s%s§r, %s%s§r, %s%s§r, %s%s§r, %s%s§r, %s%s",
                    tpsColor(tps5s),
                    formatDouble(tps5s),
                    tpsColor(tps10s),
                    formatDouble(tps10s),
                    tpsColor(tps15s),
                    formatDouble(tps15s),
                    tpsColor(tps30s),
                    formatDouble(tps30s),
                    tpsColor(tps60s),
                    formatDouble(tps60s),
                    tpsColor(tps180s),
                    formatDouble(tps180s)
            );
        }
        debugScreenDisplayer.addToGroup(Identifier.fromNamespaceAndPath("azisaba", "debug"), List.of("[Azisaba] TPS(§a5s§r/§a10s§r/§a15s§r/§a30s§r/§a1m§r/§a3m§r):", value));
    }

    @Override
    public boolean isAllowed(boolean bl) {
        return true;
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return AzisabaDebugScreenEntries.DEBUG_ENTRY_CATEGORY;
    }

    private static String formatDouble(double d) {
        return TPS_FORMAT.format(d);
    }

    private static @NotNull String tpsColor(double d) {
        if (d > 20.1) {
            return "§b";
        } else if (d >= 19) {
            return "§a";
        } else if (d >= 17) {
            return "§e";
        } else if (d >= 14) {
            return "§c";
        } else {
            return "§4";
        }
    }
}
