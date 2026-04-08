package net.azisaba.azisabamod.fabric.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.azisaba.azisabamod.fabric.chat.ChatImage;
import net.azisaba.azisabamod.fabric.util.UrlUtil;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ImageContents implements ComponentContents, AutoCloseable {
    public static final MapCodec<ImageContents> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.STRING.fieldOf("url").forGetter(ImageContents::getUrl)).apply(instance, ImageContents::new)
    );
    private final @NotNull ChatImage image;

    public ImageContents(@NotNull ChatImage image) {
        this.image = image;
        this.image.fetchAsyncAndRegister();
    }

    public ImageContents(@NotNull String url) {
        this(new ChatImage(url));
    }

    public @NotNull String getUrl() {
        return this.image.getUrl();
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Override
    public @NotNull MapCodec<? extends ComponentContents> codec() {
        return MAP_CODEC;
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(UrlUtil.isShouldBeImage(image.getUrl()) ? "[画像]" : image.getUrl());
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, @NotNull Style style) {
        return styledContentConsumer.accept(style, UrlUtil.isShouldBeImage(image.getUrl()) ? "[画像]" : image.getUrl());
    }

    @Override
    public String toString() {
        return "image{" + image.getUrl() + "}";
    }
}
