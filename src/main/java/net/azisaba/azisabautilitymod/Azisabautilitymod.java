package net.azisaba.azisabautilitymod;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;

public class Azisabautilitymod implements ModInitializer {

	public static final String MOD_ID = "azisabautilitymod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ModConfig CONFIG = ModConfig.createAndLoad();


	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Azisaba Utility Mod for 1.15.2...");
		BuildToolPreviewNetworking.init();

		HudRenderCallback.EVENT.register((tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.debugEnabled) {
				return;
			}

			double x = client.player.getX();
			double y = client.player.getY();
			double z = client.player.getZ();

			DecimalFormat coordFormat = new DecimalFormat("0.00");
			String coords = String.format("%s, %s, %s",
					coordFormat.format(x),
					coordFormat.format(y),
					coordFormat.format(z));

			String displayString = String.format(Locale.ROOT, "座標: %s",  coords);
			String previewString = PlacementPreviewRenderer.getHudText(client);

			TextRenderer textRenderer = client.textRenderer;
			int textWidth = textRenderer.getStringWidth(displayString);
			if (previewString != null) {
				textWidth = Math.max(textWidth, textRenderer.getStringWidth(previewString));
			}
			int textHeight = textRenderer.fontHeight;

			int paddingLeft = 2;
			int paddingRight = 2;
			int paddingTop = 0;
			int paddingBottom = 0;

			int bgColor = 0xA0505050;
			int textColor = 0xFFFFFFFF;

			int rectX1 = 0;
			int rectY1 = 0;
			int rectX2 = rectX1 + textWidth + paddingLeft + paddingRight;
			int lineCount = previewString == null ? 1 : 2;
			int rectY2 = rectY1 + (textHeight * lineCount) + paddingTop + paddingBottom;

			DrawableHelper.fill(rectX1, rectY1, rectX2, rectY2, bgColor);
			RenderSystem.enableTexture();

			float textDrawX = (float)(rectX1 + paddingLeft);
			float textDrawY = (float)(0);

			client.textRenderer.drawWithShadow(displayString, textDrawX, textDrawY, textColor);
			if (previewString != null) {
				client.textRenderer.drawWithShadow(previewString, textDrawX, textDrawY + textHeight, textColor);
			}
		});
	}

	private static byte[] readAllBytes(InputStream inputStream) throws IOException {
		final int BUFFER_SIZE = 4096;
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		return outputStream.toByteArray();
	}

	public static String makeRequest(String path) throws IOException {
		String url = "https://api-ktor.azisaba.net/" + path;
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.addRequestProperty("Authorization", "Bearer " + CONFIG.apiKey());
		return new String(readAllBytes(connection.getInputStream()), StandardCharsets.UTF_8);
	}

	public static <T> T requestGson(String url, Class<T> clazz) {
		try {
			URLConnection connection = new URI(url).toURL().openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + CONFIG.apiKey());
			connection.connect();
			String s = new String(readAllBytes(connection.getInputStream()), StandardCharsets.UTF_8);
			return new Gson().fromJson(s, clazz);
		} catch (Exception e) {
			throw new RuntimeException("Failed to request Gson from " + url, e);
		}
	}
}