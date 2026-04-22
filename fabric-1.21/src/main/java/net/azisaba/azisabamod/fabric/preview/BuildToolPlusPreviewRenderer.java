package net.azisaba.azisabamod.fabric.preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.azisaba.azisabamod.fabric.Mod;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuildToolPlusPreviewRenderer {
    private static final int OUTLINE_COLOR = 0x404040;
    private static final int OUTLINE_RED = 0x55;
    private static final int OUTLINE_GREEN = 0x55;
    private static final int OUTLINE_BLUE = 0xFF;
    private static final int OUTLINE_ALPHA = 0xFF;
    private static final int FILL_ALPHA = 0x38;
    private static final float OUTLINE_WIDTH = 8.0f;
    private static final double RAYCAST_DISTANCE = 5.0;
    private static final int MAX_TEMPLATE_PREVIEW_BLOCKS = 4096;

    public BuildToolPlusPreviewRenderer() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(this::render);
    }

    private void render(WorldRenderContext context) {
        if (!Mod.CONFIG.blockPlacementPreview) {
            return;
        }

        BuildToolPlusPreviewState state = BuildToolPlusPreviewSync.getState();
        if (!state.isValid()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || player.isSpectator()) {
            return;
        }

        renderSelection(context, state.pos1(), state.pos2());

        HitResult hitResult = getHitResult(minecraft, player);
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos base = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        Direction direction = state.surfacePlacement() ? blockHitResult.getDirection() : mapDirection(state.nextAlignmentOrdinal(), blockHitResult.getDirection());

        ItemStack stack = player.getMainHandItem();
        if (state.mode() == BuildToolPlusPreviewState.Mode.LINE && stack.getItem() instanceof BlockItem) {
            renderLine(context, player, base, state.buildAmount(), direction);
        } else if (state.mode() == BuildToolPlusPreviewState.Mode.SQUARE && stack.getItem() instanceof BlockItem) {
            renderSquare(context, player, base, state.buildAmount(), direction);
        } else if (state.mode() == BuildToolPlusPreviewState.Mode.DIAGONAL && stack.getItem() instanceof BlockItem) {
            renderDiagonal(context, player, base, state.buildAmount(), direction);
        }

        if (state.templatePlacementPending() && state.templatePreview() != null) {
            renderTemplate(context, player, base, state.templatePreview());
        }
    }

    private void renderSelection(WorldRenderContext context, @Nullable BlockPos pos1, @Nullable BlockPos pos2) {
        if (pos1 == null || pos2 == null) {
            return;
        }
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                renderSingle(context, new BlockPos(x, y, minZ));
                if (minZ != maxZ) {
                    renderSingle(context, new BlockPos(x, y, maxZ));
                }
            }
        }
        for (int z = minZ + 1; z < maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                renderSingle(context, new BlockPos(minX, y, z));
                if (minX != maxX) {
                    renderSingle(context, new BlockPos(maxX, y, z));
                }
            }
        }
    }

    private void renderTemplate(WorldRenderContext context, LocalPlayer player, BlockPos base, BuildToolPlusPreviewState.TemplatePreview templatePreview) {
        int sizeX = templatePreview.sizeX();
        int sizeY = templatePreview.sizeY();
        int sizeZ = templatePreview.sizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return;
        }

        int max = Math.min(MAX_TEMPLATE_PREVIEW_BLOCKS, sizeX * sizeY * sizeZ);
        int rendered = 0;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (rendered++ >= max) {
                        return;
                    }
                    if (x != 0 && y != 0 && z != 0 && x != sizeX - 1 && y != sizeY - 1 && z != sizeZ - 1) {
                        continue;
                    }
                    BlockPos pos = base.offset(x, y, z);
                    if (player.level().getBlockState(pos).isAir()) {
                        renderSingle(context, pos);
                    }
                }
            }
        }
    }

    private Direction mapDirection(int nextAlignmentOrdinal, Direction fallback) {
        return switch (nextAlignmentOrdinal) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> fallback;
        };
    }

    private void renderLine(WorldRenderContext context, LocalPlayer player, BlockPos base, int size, Direction direction) {
        for (int i = 0; i < size; i++) {
            BlockPos pos = base.relative(direction, i);
            if (!player.level().getBlockState(pos).isAir()) {
                break;
            }
            renderSingle(context, pos);
        }
    }

    private void renderSquare(WorldRenderContext context, LocalPlayer player, BlockPos base, int size, Direction direction) {
        renderSingle(context, base);
        for (int i = 2; i <= size; i++) {
            List<Direction> directions = getDirections(direction);
            Set<BlockPos> rendered = new HashSet<>();
            renderStack(context, player, base, direction, directions, i, rendered);
        }
    }

    private void renderDiagonal(WorldRenderContext context, LocalPlayer player, BlockPos base, int size, Direction direction) {
        Direction side = getDirections(direction).getFirst();
        for (int i = 0; i < size; i++) {
            BlockPos pos = base.relative(direction, i).relative(side, i);
            if (!player.level().getBlockState(pos).isAir()) {
                break;
            }
            renderSingle(context, pos);
        }
    }

    private List<Direction> getDirections(Direction direction) {
        List<Direction> directions = new ArrayList<>();
        for (Direction value : Direction.values()) {
            if (value == direction || value == direction.getOpposite()) {
                continue;
            }
            directions.add(value);
        }
        return directions;
    }

    private Set<Direction> getLeftRightDirections(Direction hitResult, Direction direction) {
        Set<Direction> set = new HashSet<>();
        if (hitResult == Direction.NORTH || hitResult == Direction.SOUTH) {
            if (direction == Direction.EAST || direction == Direction.WEST) {
                set.add(Direction.UP);
                set.add(Direction.DOWN);
            } else {
                set.add(Direction.EAST);
                set.add(Direction.WEST);
            }
            return set;
        }
        boolean northOrSouth = direction == Direction.NORTH || direction == Direction.SOUTH;
        if (hitResult == Direction.EAST || hitResult == Direction.WEST) {
            if (northOrSouth) {
                set.add(Direction.UP);
                set.add(Direction.DOWN);
            } else {
                set.add(Direction.NORTH);
                set.add(Direction.SOUTH);
            }
        } else if (northOrSouth) {
            set.add(Direction.EAST);
            set.add(Direction.WEST);
        } else {
            set.add(Direction.NORTH);
            set.add(Direction.SOUTH);
        }
        return set;
    }

    private void renderStack(WorldRenderContext context, LocalPlayer player, BlockPos pos, Direction hitResult, List<Direction> directions, int size, Set<BlockPos> rendered) {
        for (Direction direction : directions) {
            BlockPos loopPos = getBlockLoop(pos, direction, size);
            if (player.level().getBlockState(loopPos).isAir() && !rendered.contains(loopPos)) {
                renderSingle(context, loopPos);
                rendered.add(loopPos);
            }
            for (Direction value : getLeftRightDirections(hitResult, direction)) {
                renderLoop(context, player, loopPos, value, size, rendered);
            }
        }
    }

    private void renderLoop(WorldRenderContext context, LocalPlayer player, BlockPos pos, Direction direction, int size, Set<BlockPos> rendered) {
        for (int i = 1; i < size; i++) {
            BlockPos loopPos = pos.relative(direction);
            if (player.level().getBlockState(loopPos).isAir() && !rendered.contains(loopPos)) {
                renderSingle(context, loopPos);
                rendered.add(loopPos);
            }
            pos = loopPos;
        }
    }

    private BlockPos getBlockLoop(BlockPos pos, Direction direction, int count) {
        while (count > 1) {
            pos = pos.relative(direction);
            count--;
        }
        return pos;
    }

    private void renderSingle(WorldRenderContext context, BlockPos pos) {
        PoseStack matrices = context.matrices();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        VertexConsumer fillConsumer = context.consumers().getBuffer(RenderTypes.debugFilledBox());
        renderFilledShape(matrices, fillConsumer, pos, cameraPos);

        VertexConsumer outlineConsumer = context.consumers().getBuffer(RenderTypes.lines());
        ShapeRenderer.renderShape(
                matrices,
                outlineConsumer.setLineWidth(OUTLINE_WIDTH),
                Shapes.block(),
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z,
                OUTLINE_COLOR,
                OUTLINE_ALPHA / 255.0f
        );
    }

    private void renderFilledShape(PoseStack matrices, VertexConsumer consumer, BlockPos pos, Vec3 cameraPos) {
        Pose pose = matrices.last();
        double baseX = pos.getX() - cameraPos.x;
        double baseY = pos.getY() - cameraPos.y;
        double baseZ = pos.getZ() - cameraPos.z;
        Shapes.block().forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> addBoxFaces(
                pose,
                consumer,
                (float) (baseX + minX),
                (float) (baseY + minY),
                (float) (baseZ + minZ),
                (float) (baseX + maxX),
                (float) (baseY + maxY),
                (float) (baseZ + maxZ)
        ));
    }

    private void addBoxFaces(Pose pose, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        addQuad(pose, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, 0.0f, 0.0f, -1.0f);
        addQuad(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, 0.0f, 0.0f, 1.0f);
        addQuad(pose, consumer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, -1.0f, 0.0f, 0.0f);
        addQuad(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 1.0f, 0.0f, 0.0f);
        addQuad(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0.0f, 1.0f, 0.0f);
        addQuad(pose, consumer, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, 0.0f, -1.0f, 0.0f);
    }

    private void addQuad(Pose pose, VertexConsumer consumer,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float nx, float ny, float nz) {
        addVertex(pose, consumer, x1, y1, z1, nx, ny, nz);
        addVertex(pose, consumer, x2, y2, z2, nx, ny, nz);
        addVertex(pose, consumer, x3, y3, z3, nx, ny, nz);
        addVertex(pose, consumer, x4, y4, z4, nx, ny, nz);
    }

    private void addVertex(Pose pose, VertexConsumer consumer, float x, float y, float z, float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE, FILL_ALPHA)
                .setNormal(pose, nx, ny, nz);
    }

    private HitResult getHitResult(Minecraft minecraft, LocalPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookAngle = player.getViewVector(1.0f);
        Vec3 reachEnd = eyePosition.add(lookAngle.scale(RAYCAST_DISTANCE));
        return minecraft.level.clip(new ClipContext(eyePosition, reachEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}

