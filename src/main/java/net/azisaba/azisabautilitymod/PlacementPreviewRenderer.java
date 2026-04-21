package net.azisaba.azisabautilitymod;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class PlacementPreviewRenderer {
    private static final double RAYCAST_DISTANCE = 5.0D;

    private PlacementPreviewRenderer() {
    }

    public static void render(MatrixStack matrices, Camera camera) {
        if (!Azisabautilitymod.CONFIG.blockPlacementPreview()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || player.isSpectator()) {
            return;
        }

        BuildToolPreviewState.Snapshot snapshot = BuildToolPreviewState.get();
        Hand activeToolHand = BuildToolPlusUtil.getActiveToolHand(player, snapshot.offHandEnabled());
        if (snapshot.isEmpty() || activeToolHand == null) {
            return;
        }

        HitResult hitResult = getHitResult(client, player);
        switch (snapshot.mode()) {
            case LINE:
                renderLine(matrices, camera, player, snapshot, hitResult);
                break;
            case SQUARE:
                renderSquare(matrices, camera, player, snapshot, hitResult);
                break;
            case DIAGONAL:
                renderDiagonal(matrices, camera, player, snapshot, hitResult);
                break;
            case AREA_PLACEMENT:
                if (BuildToolPlusUtil.getAreaPlacementSourceStack(player, activeToolHand) != null) {
                    renderSelection(matrices, camera, snapshot, 0.20F, 0.85F, 1.00F);
                }
                break;
            case AREA_DELETE:
                renderSelection(matrices, camera, snapshot, 1.00F, 0.30F, 0.30F);
                break;
            case AREA_SELECT:
                renderSelection(matrices, camera, snapshot, 1.00F, 0.85F, 0.25F);
                break;
            case DEBUG_STICK:
                renderDebugStick(matrices, camera, hitResult);
                break;
            default:
                break;
        }
    }

    public static String getHudText(MinecraftClient client) {
        BuildToolPreviewState.Snapshot snapshot = BuildToolPreviewState.get();
        if (snapshot.isEmpty() || client.player == null) {
            return null;
        }
        Hand activeToolHand = BuildToolPlusUtil.getActiveToolHand(client.player, snapshot.offHandEnabled());
        if (activeToolHand == null) {
            return null;
        }

        switch (snapshot.mode()) {
            case AREA_SELECT:
                return "BuildTool: " + snapshot.mode().displayName() + " -> " + formatDirection(snapshot.nextAlignment());
            case AREA_PLACEMENT:
            case AREA_DELETE:
                if (snapshot.pos1() != null || snapshot.pos2() != null) {
                    return "BuildTool: " + snapshot.mode().displayName();
                }
                return null;
            case DEBUG_STICK:
                HitResult hitResult = getHitResult(client, client.player);
                if (hitResult instanceof BlockHitResult) {
                    BlockState state = client.world.getBlockState(((BlockHitResult) hitResult).getBlockPos());
                    String debug = getDebugPreviewLabel(state);
                    return debug == null ? "BuildTool: Debug Stick" : "DebugStick: " + debug;
                }
                return "BuildTool: Debug Stick";
            default:
                return "BuildTool: " + snapshot.mode().displayName() + " x" + snapshot.amount();
        }
    }

    private static void renderLine(MatrixStack matrices, Camera camera, ClientPlayerEntity player, BuildToolPreviewState.Snapshot snapshot, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockState targetState = player.world.getBlockState(blockHitResult.getBlockPos());
        for (int i = 1; i <= snapshot.amount(); i++) {
            BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getSide(), i);
            if (shouldRenderPlacement(player.world.getBlockState(pos), targetState, snapshot.replaceMode())) {
                renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), pos, 0.33F, 1.0F, 0.33F);
            }
        }
    }

    private static void renderSquare(MatrixStack matrices, Camera camera, ClientPlayerEntity player, BuildToolPreviewState.Snapshot snapshot, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        Direction face = blockHitResult.getSide();
        BlockPos base = snapshot.surfacePlacement() ? blockHitResult.getBlockPos().offset(face) : blockHitResult.getBlockPos();
        BlockState targetState = player.world.getBlockState(blockHitResult.getBlockPos());
        int amount = snapshot.amount();
        int start;
        int end;
        if (amount % 2 != 0) {
            int radius = (amount - 1) / 2;
            start = -radius;
            end = radius;
        } else {
            int half = amount / 2;
            start = -half;
            end = half - 1;
        }

        int modX = face.getOffsetX();
        int modY = face.getOffsetY();
        int modZ = face.getOffsetZ();
        for (int a = start; a <= end; a++) {
            for (int b = start; b <= end; b++) {
                BlockPos pos;
                if (modY != 0) {
                    pos = base.add(a, 0, b);
                } else if (modX != 0) {
                    pos = base.add(0, b, a);
                } else {
                    pos = base.add(a, b, 0);
                }
                if (shouldRenderPlacement(player.world.getBlockState(pos), targetState, snapshot.replaceMode())) {
                    renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), pos, 0.33F, 1.0F, 0.33F);
                }
            }
        }
    }

    private static void renderDiagonal(MatrixStack matrices, Camera camera, ClientPlayerEntity player, BuildToolPreviewState.Snapshot snapshot, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        BlockState targetState = player.world.getBlockState(blockHitResult.getBlockPos());
        Vec3d look = player.getRotationVec(1.0F);
        int stepX = Math.abs(look.x) > 0.5D ? (look.x > 0 ? 1 : -1) : 0;
        int stepZ = Math.abs(look.z) > 0.5D ? (look.z > 0 ? 1 : -1) : 0;
        int stepY = player.pitch > 60.0F ? -1 : 1;
        for (int i = 1; i <= snapshot.amount(); i++) {
            BlockPos pos = blockHitResult.getBlockPos().add(stepX * i, stepY * i, stepZ * i);
            if (shouldRenderPlacement(player.world.getBlockState(pos), targetState, snapshot.replaceMode())) {
                renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), pos, 0.33F, 1.0F, 0.33F);
            }
        }
    }

    private static void renderSelection(MatrixStack matrices, Camera camera, BuildToolPreviewState.Snapshot snapshot, float red, float green, float blue) {
        if (snapshot.pos1() != null && snapshot.pos2() != null) {
            BlockPos min = new BlockPos(
                    Math.min(snapshot.pos1().getX(), snapshot.pos2().getX()),
                    Math.min(snapshot.pos1().getY(), snapshot.pos2().getY()),
                    Math.min(snapshot.pos1().getZ(), snapshot.pos2().getZ())
            );
            BlockPos max = new BlockPos(
                    Math.max(snapshot.pos1().getX(), snapshot.pos2().getX()),
                    Math.max(snapshot.pos1().getY(), snapshot.pos2().getY()),
                    Math.max(snapshot.pos1().getZ(), snapshot.pos2().getZ())
            );
            Box box = new Box(0.0D, 0.0D, 0.0D, max.getX() - min.getX() + 1.0D, max.getY() - min.getY() + 1.0D, max.getZ() - min.getZ() + 1.0D);
            renderSingleBox(matrices, camera, box, min, red, green, blue);
            return;
        }
        if (snapshot.pos1() != null) {
            renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), snapshot.pos1(), red, green, blue);
        }
        if (snapshot.pos2() != null) {
            renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), snapshot.pos2(), red, green, blue);
        }
    }

    private static void renderDebugStick(MatrixStack matrices, Camera camera, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        renderSingleBox(matrices, camera, new Box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), ((BlockHitResult) hitResult).getBlockPos(), 1.0F, 0.65F, 0.20F);
    }

    private static boolean shouldRenderPlacement(BlockState current, BlockState targetState, boolean replaceMode) {
        if (current.getBlock() == targetState.getBlock()) {
            return false;
        }
        if (replaceMode) {
            return true;
        }
        return current.isAir() || current.getMaterial().isLiquid();
    }

    private static void renderSingleBox(MatrixStack matrices, Camera camera, Box box, BlockPos pos, float red, float green, float blue) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        matrices.push();
        Vec3d cameraPos = camera.getPos();
        matrices.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

        VertexConsumer filled = immediate.getBuffer(RenderLayer.getTranslucent());
        VertexConsumer lines = immediate.getBuffer(RenderLayer.getLines());
        WorldRenderer.drawBox(matrices, filled, box, red, green, blue, 0.20F);
        WorldRenderer.drawBox(matrices, lines, box, red, green, blue, 1.0F);
        matrices.pop();
        immediate.draw();
    }

    private static String getDebugPreviewLabel(BlockState state) {
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            Direction current = state.get(Properties.HORIZONTAL_FACING);
            Direction next = nextFrom(current, horizontalDirections());
            String result = "Facing -> " + formatDirection(next);
            if (state.contains(Properties.BLOCK_HALF) && next == Direction.NORTH && current == Direction.WEST) {
                result += " / " + nextHalf(state.get(Properties.BLOCK_HALF)).name();
            }
            return result;
        }
        if (state.contains(Properties.FACING)) {
            Collection<Direction> values = state.get(Properties.FACING).getAxis().isHorizontal()
                    ? horizontalDirections()
                    : directionValues(Properties.FACING);
            Direction next = nextFrom(state.get(Properties.FACING), values);
            return "Facing -> " + formatDirection(next);
        }
        if (state.contains(Properties.AXIS)) {
            Direction.Axis axis = state.cycle(Properties.AXIS).get(Properties.AXIS);
            return "Axis -> " + axis.name();
        }
        if (state.contains(Properties.ROTATION)) {
            IntProperty property = Properties.ROTATION;
            int next = (state.get(property) + 1) % 16;
            return "Rotation -> " + next;
        }
        return null;
    }

    private static Collection<Direction> horizontalDirections() {
        List<Direction> directions = new ArrayList<Direction>();
        directions.add(Direction.NORTH);
        directions.add(Direction.EAST);
        directions.add(Direction.SOUTH);
        directions.add(Direction.WEST);
        return directions;
    }

    private static Collection<Direction> directionValues(DirectionProperty property) {
        List<Direction> directions = new ArrayList<Direction>(property.getValues());
        directions.sort(Comparator.comparingInt(Enum::ordinal));
        return directions;
    }

    private static <T> T nextFrom(T current, Collection<T> values) {
        List<T> list = new ArrayList<T>(values);
        int index = list.indexOf(current);
        if (index == -1) {
            return list.get(0);
        }
        return list.get((index + 1) % list.size());
    }

    private static BlockHalf nextHalf(BlockHalf current) {
        return current == BlockHalf.BOTTOM ? BlockHalf.TOP : BlockHalf.BOTTOM;
    }

    private static String formatDirection(Direction direction) {
        return direction == null ? "-" : direction.name();
    }

    private static HitResult getHitResult(MinecraftClient client, ClientPlayerEntity player) {
        if (client.crosshairTarget != null) {
            return client.crosshairTarget;
        }
        Vec3d eye = player.getCameraPosVec(1.0F);
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = eye.add(look.x * RAYCAST_DISTANCE, look.y * RAYCAST_DISTANCE, look.z * RAYCAST_DISTANCE);
        return player.world.rayTrace(new RayTraceContext(eye, end, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player));
    }
}