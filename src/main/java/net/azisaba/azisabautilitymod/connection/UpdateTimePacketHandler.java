package net.azisaba.azisabautilitymod.connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.azisabautilitymod.Azisabautilitymod;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.LongStream;

public class UpdateTimePacketHandler extends ChannelInboundHandlerAdapter {
    public static final Map<UUID, String> admin = new ConcurrentHashMap<>();
    public static final Map<UUID, String> uuidToNameMap = new ConcurrentHashMap<>();
    public final List<Long> times = new CopyOnWriteArrayList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WorldTimeUpdateS2CPacket) {
            times.add(System.currentTimeMillis());
        }
        if (msg instanceof PlayerListS2CPacket) {
            PlayerListS2CPacket packet = (PlayerListS2CPacket) msg;
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                GameProfile profile = entry.getProfile();
                if (profile == null) continue;
                if (profile.getName() != null) {
                    uuidToNameMap.put(profile.getId(), profile.getName());
                }
                if (admin.containsKey(profile.getId())) continue;
                new Thread(() -> {
                    StringBuilder sb = new StringBuilder(3 * 10);
                    JsonObject obj = Azisabautilitymod.requestGson("https://api-ktor.azisaba.net/players/" + profile.getId(), JsonObject.class);
                    if (obj.get("groups").getAsJsonArray().contains(new JsonPrimitive("developer"))) {
                        sb.append("§b").append("d+");
                    }
                    if (obj.get("groups").getAsJsonArray().contains(new JsonPrimitive("alladmin"))) {
                        sb.append("§c").append("a+");
                    }
                    JsonObject servers = obj.get("servers").getAsJsonObject();
                    for (Admins value : Admins.values()) {
                        if (!servers.has(value.name().toLowerCase())) continue;
                        JsonObject server = servers.get(value.name().toLowerCase()).getAsJsonObject();
                        if (server.get("admin").getAsBoolean()) {
                            sb.append("§c").append(value.chr);
                        } else if (server.get("moderator").getAsBoolean()) {
                            sb.append("§6").append(value.chr);
                        } else if (server.get("builder").getAsBoolean()) {
                            sb.append("§e").append(value.chr);
                        }
                    }
                    admin.put(profile.getId(), sb.toString());
                    Azisabautilitymod.LOGGER.info("Added admin info for {}: {}", profile.getName(), sb.toString());
                }).start();
            }
        }
        super.channelRead(ctx, msg);
    }

    public LongStream timesStream() {
        return times.stream().mapToLong(l -> l);
    }

    public double getAverageMsPerSecond(long period) {
        List<Long> times = new ArrayList<>();
        long lastTime = -1;
        for (long l : timesStream().filter(t -> System.currentTimeMillis() - t <= period).toArray()) {
            if (lastTime != -1) {
                times.add(l - lastTime);
            }
            lastTime = l;
        }
        return times.stream().mapToLong(l -> l).average().orElse(0.0);
    }

    public enum Admins {
        Life("L"),
        LGW("G"),
        LGW2("G2"),
        Sclat("S"),
        Despawn("E"),
        Diverse("D"),
        TSL("T"),
        Vanilife("V"),
        Lobby("O"),
        Afnw2("W"),
        JG("J"),
        AFK("A");

        public final String chr;

        Admins(String chr) {
            this.chr = chr;
        }
    }
}