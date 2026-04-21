package net.azisaba.azisabautilitymod;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class BuildToolPreviewState {
    private static volatile Snapshot current = Snapshot.empty();

    private BuildToolPreviewState() {
    }

    public static Snapshot get() {
        return current;
    }

    public static void set(Snapshot snapshot) {
        current = snapshot;
    }

    public static void clear() {
        current = Snapshot.empty();
    }

    public static final class Snapshot {
        private final Mode mode;
        private final int amount;
        private final boolean surfacePlacement;
        private final boolean replaceMode;
        private final boolean offHandEnabled;
        private final boolean undoEnabled;
        private final Direction nextAlignment;
        private final BlockPos pos1;
        private final BlockPos pos2;

        public Snapshot(Mode mode, int amount, boolean surfacePlacement, boolean replaceMode, boolean offHandEnabled, boolean undoEnabled, Direction nextAlignment, BlockPos pos1, BlockPos pos2) {
            this.mode = mode;
            this.amount = amount;
            this.surfacePlacement = surfacePlacement;
            this.replaceMode = replaceMode;
            this.offHandEnabled = offHandEnabled;
            this.undoEnabled = undoEnabled;
            this.nextAlignment = nextAlignment;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public static Snapshot empty() {
            return new Snapshot(null, 0, true, false, true, true, null, null, null);
        }

        public boolean isEmpty() {
            return mode == null;
        }

        public Mode mode() {
            return mode;
        }

        public int amount() {
            return amount;
        }

        public boolean surfacePlacement() {
            return surfacePlacement;
        }

        public boolean replaceMode() {
            return replaceMode;
        }

        public boolean offHandEnabled() {
            return offHandEnabled;
        }

        public boolean undoEnabled() {
            return undoEnabled;
        }

        public Direction nextAlignment() {
            return nextAlignment;
        }

        public BlockPos pos1() {
            return pos1;
        }

        public BlockPos pos2() {
            return pos2;
        }
    }

    public enum Mode {
        LINE("Line"),
        SQUARE("Square"),
        DIAGONAL("Diagonal"),
        AREA_PLACEMENT("Area Placement"),
        DEBUG_STICK("Debug Stick"),
        AREA_SELECT("Area Select"),
        AREA_DELETE("Area Delete");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public static Mode fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                return null;
            }
            return values()[ordinal];
        }
    }
}