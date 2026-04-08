package net.azisaba.azisabamod.fabric.chat;

import com.mojang.blaze3d.platform.NativeImage;
import net.azisaba.azisabamod.fabric.Mod;
import net.azisaba.azisabamod.fabric.util.Optionals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ChatImage implements AutoCloseable {
    private static final ExecutorService FETCHER = Executors.newCachedThreadPool();
    private static final AtomicLong ID_GENERATOR = new AtomicLong();
    private final @NotNull String url;
    private final @NotNull Identifier identifier = Identifier.fromNamespaceAndPath("azisaba", "image/" + ID_GENERATOR.incrementAndGet());
    private @Nullable DynamicTexture texture;

    public ChatImage(@NotNull String url) {
        this.url = url;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @Nullable DynamicTexture getTexture() {
        return texture;
    }

    public @NotNull Identifier getIdentifier() {
        return identifier;
    }

    public @NotNull Optional<NativeImage> getPixels() {
        return texture == null ? Optional.empty() : Optional.ofNullable(texture.getPixels());
    }

    public @NotNull OptionalInt getWidth() {
        return Optionals.mapToInt(getPixels(), NativeImage::getWidth);
    }

    public @NotNull OptionalInt getHeight() {
        return Optionals.mapToInt(getPixels(), NativeImage::getHeight);
    }

    public @Nullable NativeImage fetch() {
        if (this.texture != null) return null;
        try {
            URL url = new URI(this.url).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("User-Agent", "AzisabaMod (https://github.com/AzisabaNetwork/AzisabaMod) Minecraft");
            try (InputStream in = connection.getInputStream()) {
                return NativeImage.read(in);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            if (!e.getMessage().equals("Bad PNG Signature") && !e.getMessage().equals("PNG header missing")) {
                Mod.LOGGER.warn("Failed to fetch image {}", this.url, e);
            }
        }
        return null;
    }

    public void fetchAsyncAndRegister() {
        FETCHER.submit(() -> {
            NativeImage image = fetch();
            if (image == null) return;
            Minecraft.getInstance().execute(() -> {
                this.texture = new DynamicTexture(identifier::toString, image);
                Minecraft.getInstance().getTextureManager().register(identifier, this.texture);
            });
        });
    }

    @Override
    public void close() {
        if (this.texture != null) {
            this.texture.close();
        }
    }
}
