package net.azisaba.azisabautilitymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModConfig {

    public static final String MOD_ID = "azisabautilitymod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Path CONFIG_FILE = Paths.get("config", "azisabautilitymod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String apiKey = "";

    private static ModConfig instance;

    public static ModConfig createAndLoad() {
        if (instance == null) {
            instance = new ModConfig();
            instance.loadConfig();
        }
        return instance;
    }

    public void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) {
            LOGGER.info("Config file not found, creating default.");
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            ModConfig loadedConfig = GSON.fromJson(reader, ModConfig.class);
            if (loadedConfig != null) {
                this.apiKey = loadedConfig.apiKey;
                LOGGER.info("Config loaded successfully.");
            } else {
                LOGGER.warn("Config file is empty or invalid, creating default.");
                saveConfig();
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to load config file, creating default. Error: " + e.getMessage());
            saveConfig();
        }
    }

    public void saveConfig() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(this, writer);
                LOGGER.info("Config saved successfully.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config file. Error: " + e.getMessage());
        }
    }

    public String apiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}