package jp.colonylogistics.container;

import java.util.Arrays;
import java.util.List;

public enum ContainerSize {
    NONE(0, 0, 0, 0, 0, 0),

    /*
     * Phase 17.8: all player-facing container standards share one physical
     * multiblock footprint before multiplayer tests. The gameplay tier still
     * comes from ContainerStandard / weight class / logistics level, but the
     * placed blocks are always 7 wide x 3 high x 3 deep. Phase 17.8.6 keeps
     * this D/W/H meaning and rotates the placement axes so the 7-wide side
     * matches the in-game manual Dock mock-up.
     */
    SMALL(7, 3, 3, 63, 100, 2),
    MEDIUM(7, 3, 3, 63, 180, 3),
    LARGE(7, 3, 3, 63, 400, 4),
    HEAVY(7, 3, 3, 63, 800, 5);

    private final int width;
    private final int height;
    private final int depth;
    private final int volume;
    private final int baseGameplayWeight;
    private final int requiredLogisticsLevel;

    ContainerSize(int width, int height, int depth, int volume, int baseGameplayWeight, int requiredLogisticsLevel) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.volume = volume;
        this.baseGameplayWeight = baseGameplayWeight;
        this.requiredLogisticsLevel = requiredLogisticsLevel;
    }

    public int width() { return width; }
    public int height() { return height; }
    public int depth() { return depth; }
    public int volume() { return volume; }
    public int baseGameplayWeight() { return baseGameplayWeight; }
    public int requiredLogisticsLevel() { return requiredLogisticsLevel; }

    public int halfWidth() { return width / 2; }
    public int halfHeight() { return height / 2; }
    public int halfDepth() { return depth / 2; }

    public boolean isContainer() {
        return this != NONE;
    }

    public static List<ContainerSize> realSizes() {
        return Arrays.stream(values()).filter(ContainerSize::isContainer).toList();
    }

    public static List<ContainerSize> realSizesUpTo(ContainerSize maxSize) {
        return realSizes().stream()
                .filter(size -> size.ordinal() <= maxSize.ordinal())
                .toList();
    }
}
