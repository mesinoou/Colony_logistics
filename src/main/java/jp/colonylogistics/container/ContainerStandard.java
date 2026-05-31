package jp.colonylogistics.container;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Player-facing container specifications.
 *
 * <p>The older physical {@link ContainerSize} enum remains the save/NBT format.
 * This enum is the gameplay-facing standard layer used for generation, commands,
 * and display. Phase 17.8 unifies the placed footprint for all standards to
 * 3 deep x 7 wide x 3 high while preserving these serialized tier names for
 * progression, generation weighting, and saved manifests.</p>
 */
public enum ContainerStandard {
    STANDARD("standard", ContainerSize.SMALL, 2, 6, "std", "small", "medium"),
    LARGE("large", ContainerSize.LARGE, 2, 3),
    HEAVY("heavy", ContainerSize.HEAVY, 3, 1);

    private final String serializedName;
    private final ContainerSize physicalSize;
    private final int defaultContainerCount;
    private final int generationWeight;
    private final List<String> aliases;

    ContainerStandard(String serializedName, ContainerSize physicalSize, int defaultContainerCount, int generationWeight, String... aliases) {
        this.serializedName = serializedName;
        this.physicalSize = physicalSize;
        this.defaultContainerCount = defaultContainerCount;
        this.generationWeight = generationWeight;
        this.aliases = List.of(aliases);
    }

    public String serializedName() {
        return serializedName;
    }

    public ContainerSize physicalSize() {
        return physicalSize;
    }

    public int defaultContainerCount() {
        return defaultContainerCount;
    }

    public int generationWeight() {
        return generationWeight;
    }

    public static Optional<ContainerStandard> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String key = value.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(spec -> spec.serializedName.equals(key)
                        || spec.name().equalsIgnoreCase(value)
                        || spec.physicalSize.name().equalsIgnoreCase(value)
                        || spec.aliases.contains(key))
                .findFirst();
    }

    public static ContainerStandard fromSize(ContainerSize size) {
        return switch (size) {
            case LARGE -> LARGE;
            case HEAVY -> HEAVY;
            case SMALL, MEDIUM -> STANDARD;
            default -> STANDARD;
        };
    }

    public static List<ContainerStandard> availableFor(ContainerSize maxPhysicalSize) {
        return Arrays.stream(values())
                .filter(spec -> spec.physicalSize.ordinal() <= maxPhysicalSize.ordinal())
                .toList();
    }
}
