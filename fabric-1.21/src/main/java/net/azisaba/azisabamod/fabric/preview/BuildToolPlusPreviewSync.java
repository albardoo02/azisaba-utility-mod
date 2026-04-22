package net.azisaba.azisabamod.fabric.preview;

import net.azisaba.azisabamod.fabric.Mod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public final class BuildToolPlusPreviewSync {
    private static final AtomicReference<BuildToolPlusPreviewState> STATE = new AtomicReference<>(BuildToolPlusPreviewState.EMPTY);

    public BuildToolPlusPreviewSync() {
        PayloadTypeRegistry.playC2S().register(PreviewHelloPayload.ID, PreviewHelloPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PreviewStatePayload.ID, PreviewStatePayload.CODEC);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            STATE.set(BuildToolPlusPreviewState.EMPTY);
            sendHello();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STATE.set(BuildToolPlusPreviewState.EMPTY));

        ClientPlayNetworking.registerGlobalReceiver(PreviewStatePayload.ID, (payload, context) -> {
            BuildToolPlusPreviewState parsed = parseServerState(payload.data());
            if (!parsed.isValid()) {
                return;
            }
            STATE.set(parsed);
        });
    }

    public static BuildToolPlusPreviewState getState() {
        return STATE.get();
    }

    private void sendHello() {
        ClientPlayNetworking.send(new PreviewHelloPayload(BuildToolPlusPreviewApi.PROTOCOL_V2));
    }

    private BuildToolPlusPreviewState parseServerState(FriendlyByteBuf buf) {
        try {
            byte packetType = buf.readByte();
            if (packetType != BuildToolPlusPreviewApi.SERVER_STATE) {
                return BuildToolPlusPreviewState.EMPTY;
            }

            int protocol = buf.readInt();
            if (protocol != BuildToolPlusPreviewApi.PROTOCOL_V1 && protocol != BuildToolPlusPreviewApi.PROTOCOL_V2) {
                return BuildToolPlusPreviewState.EMPTY;
            }

            BuildToolPlusPreviewState.Mode mode = BuildToolPlusPreviewState.Mode.fromOrdinal(buf.readInt());
            int buildAmount = Math.max(1, Math.min(64, buf.readInt()));
            boolean surfacePlacement = buf.readBoolean();
            boolean replaceMode = buf.readBoolean();
            boolean offHandEnabled = buf.readBoolean();
            boolean undoEnabled = buf.readBoolean();
            int nextAlignmentOrdinal = buf.readInt();
            BlockPos pos1 = readNullablePos(buf);
            BlockPos pos2 = readNullablePos(buf);

            boolean templatePlacementPending = false;
            BuildToolPlusPreviewState.TemplatePreview templatePreview = null;
            if (protocol >= BuildToolPlusPreviewApi.PROTOCOL_V2) {
                templatePlacementPending = buf.readBoolean();
                if (templatePlacementPending) {
                    String templateName = buf.readUtf(256);
                    int sizeX = Math.max(0, buf.readInt());
                    int sizeY = Math.max(0, buf.readInt());
                    int sizeZ = Math.max(0, buf.readInt());
                    int blockCount = Math.max(0, buf.readInt());
                    templatePreview = new BuildToolPlusPreviewState.TemplatePreview(templateName, sizeX, sizeY, sizeZ, blockCount);
                }
            }

            return new BuildToolPlusPreviewState(
                    protocol,
                    mode,
                    buildAmount,
                    surfacePlacement,
                    replaceMode,
                    offHandEnabled,
                    undoEnabled,
                    nextAlignmentOrdinal,
                    pos1,
                    pos2,
                    templatePlacementPending,
                    templatePreview
            );
        } catch (Exception e) {
            Mod.LOGGER.warn("Failed to parse BuildToolPlus preview packet", e);
            return BuildToolPlusPreviewState.EMPTY;
        }
    }

    private @Nullable BlockPos readNullablePos(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    private BuildToolPlusPreviewState parseServerState(byte[] data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data));
        return parseServerState(buf);
    }

    private record PreviewHelloPayload(int protocol) implements CustomPacketPayload {
        private static final Type<PreviewHelloPayload> ID = new Type<>(BuildToolPlusPreviewApi.CHANNEL);
        private static final StreamCodec<FriendlyByteBuf, PreviewHelloPayload> CODEC =
                CustomPacketPayload.codec(PreviewHelloPayload::write, PreviewHelloPayload::read);

        private static PreviewHelloPayload read(FriendlyByteBuf buf) {
            byte packetType = buf.readByte();
            int protocol = buf.readInt();
            if (packetType != BuildToolPlusPreviewApi.CLIENT_HELLO) {
                return new PreviewHelloPayload(BuildToolPlusPreviewApi.PROTOCOL_V1);
            }
            return new PreviewHelloPayload(protocol);
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeByte(BuildToolPlusPreviewApi.CLIENT_HELLO);
            buf.writeInt(protocol);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    private record PreviewStatePayload(byte[] data) implements CustomPacketPayload {
        private static final Type<PreviewStatePayload> ID = new Type<>(BuildToolPlusPreviewApi.CHANNEL);
        private static final StreamCodec<FriendlyByteBuf, PreviewStatePayload> CODEC =
                CustomPacketPayload.codec(PreviewStatePayload::write, PreviewStatePayload::read);

        private static PreviewStatePayload read(FriendlyByteBuf buf) {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return new PreviewStatePayload(data);
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeBytes(data);
        }

        @Override
        public byte[] data() {
            return Arrays.copyOf(data, data.length);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}

