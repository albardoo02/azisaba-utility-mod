package net.azisaba.azisabautilitymod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class BuildToolPreviewNetworking {
    private static final Identifier CHANNEL = new Identifier("buildtoolplus", "preview");
    private static final int PROTOCOL_VERSION = 1;
    private static final byte CLIENT_HELLO = 0;
    private static final byte SERVER_STATE = 1;
    private static final Direction[] ALIGNMENT_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.DOWN,
            Direction.UP
    };

    private BuildToolPreviewNetworking() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, BuildToolPreviewNetworking::receiveState);
        ClientPlayConnectionEvents.JOIN.register(BuildToolPreviewNetworking::sendHello);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BuildToolPreviewState.clear());
    }

    private static void sendHello(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(CLIENT_HELLO);
        buf.writeInt(PROTOCOL_VERSION);
        sender.sendPacket(CHANNEL, buf);
    }

    private static void receiveState(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        byte packetType = buf.readByte();
        if (packetType != SERVER_STATE) {
            return;
        }
        int protocol = buf.readInt();
        if (protocol != PROTOCOL_VERSION) {
            return;
        }

        final BuildToolPreviewState.Mode mode = BuildToolPreviewState.Mode.fromOrdinal(buf.readInt());
        final int amount = buf.readInt();
        final boolean surfacePlacement = buf.readBoolean();
        final boolean replaceMode = buf.readBoolean();
        final boolean offHandEnabled = buf.readBoolean();
        final boolean undoEnabled = buf.readBoolean();
        final Direction nextAlignment = readAlignment(buf.readInt());
        final BlockPos pos1 = readBlockPos(buf);
        final BlockPos pos2 = readBlockPos(buf);

        client.execute(() -> BuildToolPreviewState.set(new BuildToolPreviewState.Snapshot(
                mode,
                amount,
                surfacePlacement,
                replaceMode,
                offHandEnabled,
                undoEnabled,
                nextAlignment,
                pos1,
                pos2
        )));
    }

    private static BlockPos readBlockPos(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    private static Direction readAlignment(int ordinal) {
        if (ordinal < 0 || ordinal >= ALIGNMENT_DIRECTIONS.length) {
            return null;
        }
        return ALIGNMENT_DIRECTIONS[ordinal];
    }
}