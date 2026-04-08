package net.azisaba.azisabamod.fabric.util;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {
    public static final Pattern URL_PATTERN = Pattern.compile("(https?://[\\w/:%#$&?()~=.+\\-]+)");

    public static @NotNull List<@NotNull MatchResult> matchUrls(@NotNull String text) {
        Matcher matcher = URL_PATTERN.matcher(text);
        List<MatchResult> list = new ArrayList<>();
        int lastIndex = 0;
        while (matcher.find()) {
            list.add(new MatchResult(false, text.substring(lastIndex, matcher.start())));
            list.add(new MatchResult(true, matcher.group()));
            lastIndex = matcher.end();
        }
        list.add(new MatchResult(false, text.substring(lastIndex)));
        return list;
    }

    public static boolean isShouldBeImage(String url) {
        URI uri = URI.create(url);
        return uri.getPath().endsWith(".png") || uri.getPath().endsWith(".jpg") || uri.getPath().endsWith(".jpeg");
    }

    public record MatchResult(boolean isUrl, @NotNull String text) {
    }
}
