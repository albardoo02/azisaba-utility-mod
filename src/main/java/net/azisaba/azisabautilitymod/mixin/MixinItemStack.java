package net.azisaba.azisabautilitymod.mixin;

import net.azisaba.azisabautilitymod.connection.UpdateTimePacketHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow public abstract boolean hasTag();
    @Shadow public abstract CompoundTag getTag();

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void addCustomTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (!this.hasTag()) {
            return;
        }

        CompoundTag tag = this.getTag();
        if (tag == null) {
            return;
        }

        List<Text> lines = cir.getReturnValue();

        if (tag.contains("MYTHIC_TYPE", 8)) {
            String mmid = tag.getString("MYTHIC_TYPE");
            lines.add(new LiteralText("MMID: " + mmid).formatted(Formatting.DARK_GRAY));
        }

        if (tag.contains("PublicBukkitValues", 10)) {
            CompoundTag publicBukkitValues = tag.getCompound("PublicBukkitValues");
            if (publicBukkitValues.contains("mythicmobs:type", 8)) {
                String mmId = publicBukkitValues.getString("mythicmobs:type");
                lines.add(new LiteralText("MMID: " + mmId).formatted(Formatting.DARK_GRAY));
            }
        }

        if (tag.contains("CustomModelData", 3)) {
            int customModelData = tag.getInt("CustomModelData");
            lines.add(new LiteralText("CustomModelData: " + customModelData).formatted(Formatting.DARK_GRAY));
        }

        if (tag.contains("soulbound", 8)) {
            String uuid = tag.getString("soulbound");
            try {
                String name = UpdateTimePacketHandler.uuidToNameMap.get(UUID.fromString(uuid));
                if (name != null) {
                    lines.add(new LiteralText("Soulbound: " + uuid + " (" + name + ")").formatted(Formatting.DARK_GRAY));
                } else {
                    lines.add(new LiteralText("Soulbound: " + uuid).formatted(Formatting.DARK_GRAY));
                }
            } catch (Exception e) {
                lines.add(new LiteralText("Soulbound: " + uuid).formatted(Formatting.DARK_GRAY));
            }
        }

        if (tag.contains("RepairCost", 3)) {
            int repairCost = tag.getInt("RepairCost");
            if (repairCost > 0) {
                lines.add(new LiteralText("RepairCost: " + repairCost).formatted(Formatting.DARK_GRAY));
            }
        }
    }
}