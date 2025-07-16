package net.azisaba.azisabautilitymod;

import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
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

public class Azisabautilitymod implements ModInitializer {

	public static final String MOD_ID = "azisabautilitymod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final ModConfig CONFIG = ModConfig.createAndLoad();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Azisaba Utility Mod for 1.15.2...");
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