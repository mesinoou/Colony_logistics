package jp.colonylogistics.physics;

import jp.colonylogistics.container.ContainerWeightClass;

public record SableMassProfile(
        ContainerWeightClass weightClass,
        double massPerBlock,
        double volumePerBlock,
        double friction
) {
    public static SableMassProfile of(ContainerWeightClass weightClass) {
        return new SableMassProfile(weightClass, weightClass.massPerBlock(), 1.0D, 1.0D);
    }
}
