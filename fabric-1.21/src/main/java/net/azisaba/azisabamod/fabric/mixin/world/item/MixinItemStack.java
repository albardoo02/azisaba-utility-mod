package net.azisaba.azisabamod.fabric.mixin.world.item;

import net.azisaba.azisabamod.fabric.connection.UpdateTimePacketHandler;
import net.azisaba.azisabamod.fabric.debug.AzisabaDebugScreenEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract DataComponentMap getComponents();

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    public void addTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> list = cir.getReturnValue();
        if (!(list instanceof ArrayList<Component>)) {
            list = new ArrayList<>(list);
        }
        CompoundTag tag = getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_MYTHIC_TYPE) && tag.get("MYTHIC_TYPE") instanceof StringTag(String value)) {
            list.add(Component.literal("MMID: " + value).withStyle(ChatFormatting.DARK_GRAY));
        }
        var publicBukkitValues = tag.getCompound("PublicBukkitValues").orElseGet(CompoundTag::new);
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_MYTHIC_TYPE) && publicBukkitValues.get("mythicmobs:type") instanceof StringTag(String value)) {
            list.add(Component.literal("MMID: " + value).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_ITEM_MODEL) && getComponents().has(DataComponents.ITEM_MODEL)) {
            list.add(Component.literal("ItemModel: " + getComponents().get(DataComponents.ITEM_MODEL)).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_CUSTOM_MODEL_DATA) && getComponents().has(DataComponents.CUSTOM_MODEL_DATA)) {
            var cmd = Objects.requireNonNull(getComponents().get(DataComponents.CUSTOM_MODEL_DATA));
            if (!cmd.floats().isEmpty()) {
                list.add(Component.literal("CustomModelData (floats): " + cmd.floats()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (!cmd.flags().isEmpty()) {
                list.add(Component.literal("CustomModelData (flags): " + cmd.flags()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (!cmd.colors().isEmpty()) {
                list.add(Component.literal("CustomModelData (colors): " + cmd.colors()).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (!cmd.strings().isEmpty()) {
                list.add(Component.literal("CustomModelData (strings): " + cmd.strings()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_SOULBOUND) && tag.get("soulbound") instanceof StringTag(String uuid)) {
            try {
                String name = UpdateTimePacketHandler.uuidToNameMap.get(UUID.fromString(uuid));
                list.add(Component.literal("Soulbound: " + uuid + " (" + name + ")").withStyle(ChatFormatting.DARK_GRAY));
            } catch (Exception e) {
                list.add(Component.literal("Soulbound: " + uuid).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_REPAIR_COST) && getComponents().has(DataComponents.REPAIR_COST)) {
            int repairCost = Objects.requireNonNull(getComponents().get(DataComponents.REPAIR_COST));
            if (repairCost > 0) {
                list.add(Component.literal("RepairCost: " + repairCost).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (AzisabaDebugScreenEntries.isEnabled(AzisabaDebugScreenEntries.ITEM_TOOLTIP_SHULKER_ID) && tag.get("ShulkerId") instanceof StringTag(String value)) {
            list.add(Component.literal("ShulkerId: " + value).withStyle(ChatFormatting.DARK_GRAY));
        }
        cir.setReturnValue(list);
    }
}
