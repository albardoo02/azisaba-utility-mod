package net.azisaba.azisabautilitymod.mixin;

import net.azisaba.azisabautilitymod.Azisabautilitymod;
import net.azisaba.azisabautilitymod.connection.UpdateTimePacketHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

    @Inject(at = @At("RETURN"), method = "getPlayerName", cancellable = true)
    public void rewriteComponent(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        String s = UpdateTimePacketHandler.admin.get(entry.getProfile().getId());
        if (s == null || s.trim().isEmpty()) return;
        Text originalComponent = cir.getReturnValue();
        Text newComponent = originalComponent.copy().append(new LiteralText(" §8<" + s + "§8>"));
        cir.setReturnValue(newComponent);
    }


    @Shadow @Final private MinecraftClient client;
    @Shadow private Text header;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getNetworkHandler()Lnet/minecraft/client/network/ClientPlayNetworkHandler;", ordinal = 0))
    public void azisaba$addHeaderInfo(int width, Scoreboard scoreboard, ScoreboardObjective playerListScoreboardObjective, CallbackInfo ci) {
        if (this.client.player == null) {
            return;
        }

        String clientVersion = this.client.getGameVersion();
        ClientPlayerInteractionManager interactionManager = ((MixinMinecraftClientAccessor) this.client).getInteractionManager();

        GameMode gameMode = GameMode.SURVIVAL;
        if (interactionManager != null) {
            gameMode = interactionManager.getCurrentGameMode();
        }


        String gameModeName;
        if (gameMode == GameMode.SURVIVAL) {
            gameModeName = "survival";
        } else if (gameMode == GameMode.CREATIVE) {
            gameModeName = "creative";
        } else if (gameMode == GameMode.ADVENTURE) {
            gameModeName = "adventure";
        } else if (gameMode == GameMode.SPECTATOR) {
            gameModeName = "spectator";
        } else {
            gameModeName = "unknown";
        }

        Text versionText = new LiteralText("§7§oクライアント: " + clientVersion);
        Text gameModeText = new LiteralText("§7§oゲームモード: " + gameModeName);

        if (this.header != null) {
            this.header = this.header.copy()
                    .append(new LiteralText("\n"))
                    .append(versionText)
                    .append(new LiteralText("\n"))
                    .append(gameModeText)
                    .append(new LiteralText("\n"))
                    .append("§8------------------------------------------------");
        } else {
            this.header = versionText.copy()
                    .append(new LiteralText("\n"))
                    .append(gameModeText)
                    .append(new LiteralText("\n"))
                    .append("§8------------------------------------------------");
        }
    }
}