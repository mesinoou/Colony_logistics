package jp.colonylogistics.container;

public record ContainerRequirement(
        ContainerSize size,
        int requiredVolume,
        int cargoGameplayWeight,
        ContainerWeightClass weightClass
) {
    public boolean requiresContainer() {
        return size != ContainerSize.NONE;
    }
}
