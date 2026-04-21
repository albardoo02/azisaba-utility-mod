package net.azisaba.azisabautilitymod;

import net.minecraft.block.Material;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

public final class BuildToolPlusUtil {
    private static final String BUILDING_WAND_KEY = "buildtoolplus:building_wand";
    private static final String TOOL_LEVEL_KEY = "buildtoolplus:tool_level";

    private BuildToolPlusUtil() {
    }

    public static boolean isBuildTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!stack.hasTag()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("PublicBukkitValues", 10)) {
            return false;
        }
        CompoundTag publicBukkitValues = tag.getCompound("PublicBukkitValues");
        return publicBukkitValues.contains(BUILDING_WAND_KEY) || publicBukkitValues.contains(TOOL_LEVEL_KEY);
    }

    public static Hand getActiveToolHand(ClientPlayerEntity player, boolean offHandEnabled) {
        if (isBuildTool(player.getMainHandStack())) {
            return Hand.MAIN_HAND;
        }
        if (offHandEnabled && isBuildTool(player.getOffHandStack())) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    public static ItemStack getAreaPlacementSourceStack(ClientPlayerEntity player, Hand activeToolHand) {
        ItemStack source;
        if (activeToolHand == Hand.MAIN_HAND) {
            source = player.getOffHandStack();
        } else {
            source = player.getMainHandStack();
        }
        if (source == null || source.isEmpty() || !(source.getItem() instanceof BlockItem)) {
            return null;
        }
        if (source.getItem() instanceof BlockItem) {
            return source;
        }
        return null;
    }

    public static boolean isAirOrLiquid(net.minecraft.block.BlockState state) {
        if (state == null) {
            return false;
        }
        return state.isAir() || state.getMaterial().isLiquid();
    }
}