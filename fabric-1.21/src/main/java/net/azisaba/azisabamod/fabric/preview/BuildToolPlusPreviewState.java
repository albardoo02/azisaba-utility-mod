package net.azisaba.azisabamod.fabric.preview;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public record BuildToolPlusPreviewState(
        int protocol,
        Mode mode,
        int buildAmount,
        boolean surfacePlacement,
        boolean replaceMode,
        boolean offHandEnabled,
        boolean undoEnabled,
        int nextAlignmentOrdinal,
        @Nullable BlockPos pos1,
        @Nullable BlockPos pos2,
        boolean templatePlacementPending,
        @Nullable TemplatePreview templatePreview
) {
    public static final BuildToolPlusPreviewState EMPTY = new BuildToolPlusPreviewState(
            0,
            Mode.UNKNOWN,
            1,
            true,
            false,
            true,
            true,
            -1,
            null,
            null,
            false,
            null
    );

    public boolean isValid() {
        return protocol == BuildToolPlusPreviewApi.PROTOCOL_V1 || protocol == BuildToolPlusPreviewApi.PROTOCOL_V2;
    }

    public enum Mode {
        LINE,
        SQUARE,
        DIAGONAL,
        AREA_PLACEMENT,
        DEBUG_STICK,
        AREA_SELECT,
        AREA_DELETE,
        UNKNOWN;

        public static Mode fromOrdinal(int ordinal) {
            Mode[] values = values();
            if (ordinal < 0 || ordinal >= UNKNOWN.ordinal()) {
                return UNKNOWN;
            }
            return values[ordinal];
        }
    }

    public record TemplatePreview(String name, int sizeX, int sizeY, int sizeZ, int blockCount) {
    }
}

