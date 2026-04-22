package net.azisaba.azisabamod.fabric;

import com.google.gson.Gson;
import net.azisaba.azisabamod.fabric.debug.AzisabaDebugScreenEntries;
import net.azisaba.azisabamod.fabric.preview.BuildToolPlusPreviewRenderer;
import net.azisaba.azisabamod.fabric.preview.BuildToolPlusPreviewSync;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Mod implements ModInitializer {
    private static final Gson GSON = new Gson();
    public static final String API_BASE = "https://api-ktor.azisaba.net";
    public static final Logger LOGGER = LoggerFactory.getLogger("AzisabaMod");
    public static final ModConfig CONFIG = new ModConfig();

    @Override
    public void onInitialize() {
        CONFIG.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(Commands.builder());
        });
        new AzisabaDebugScreenEntries();
        new PlacementPreviewRenderer();
        new BuildToolPlusPreviewSync();
        new BuildToolPlusPreviewRenderer();
    }

    public static String makeRequest(String path) throws IOException, URISyntaxException {
        String url = API_BASE + "/" + path;
        HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
        connection.addRequestProperty("Authorization", "Bearer " + CONFIG.apiKey);
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static <T> T requestGson(@NotNull String url, @NotNull Class<T> clazz) {
        try {
            URLConnection connection = new URI(url).toURL().openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + CONFIG.apiKey);
            connection.connect();
            String s = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return GSON.fromJson(s, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
