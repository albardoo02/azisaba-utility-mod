package net.azisaba.azisabamod.fabric.preview;

import net.minecraft.resources.Identifier;

public final class BuildToolPlusPreviewApi {
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath("buildtoolplus", "preview");

    public static final int PROTOCOL_V1 = 1;
    public static final int PROTOCOL_V2 = 2;

    public static final byte CLIENT_HELLO = 0;
    public static final byte SERVER_STATE = 1;

    private BuildToolPlusPreviewApi() {
    }
}

