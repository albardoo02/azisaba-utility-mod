package net.azisaba.azisabamod.fabric.commands;

import io.netty.channel.Channel;
import net.azisaba.azisabamod.fabric.connection.UpdateTimePacketHandler;
import net.azisaba.azisabamod.fabric.mixin.network.MixinClientCommonNetworkHandlerAccessor;
import net.azisaba.azisabamod.fabric.mixin.network.MixinClientConnectionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClearTPSCommand implements Command {
    @Override
    public void execute(@NotNull LocalPlayer player, @NotNull String[] args) throws Exception {
        assert Minecraft.getInstance().player != null;
        Connection clientConnection = ((MixinClientCommonNetworkHandlerAccessor) Minecraft.getInstance().player.connection).getConnection();
        Channel channel = ((MixinClientConnectionAccessor) clientConnection).getChannel();
        UpdateTimePacketHandler updateTimePacketHandler = Objects.requireNonNull(channel, "channel").pipeline().get(UpdateTimePacketHandler.class);
        if (updateTimePacketHandler != null) {
            updateTimePacketHandler.times.clear();
            UpdateTimePacketHandler.admin.clear();
        }
    }

    @Override
    public @NotNull String getName() {
        return "clearTps";
    }

    @Override
    public @NotNull String getDescription() {
        return "\"TPS\" 表示をリセットします";
    }
}
