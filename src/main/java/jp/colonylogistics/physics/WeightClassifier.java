package jp.colonylogistics.physics;

import jp.colonylogistics.container.ContainerSize;
import jp.colonylogistics.container.ContainerWeightClass;

public final class WeightClassifier {
    public ContainerWeightClass classify(int cargoGameplayWeight, ContainerSize size) {
        int normalized = cargoGameplayWeight + size.baseGameplayWeight();
        if (normalized <= 0) return ContainerWeightClass.EMPTY;
        if (normalized < 100) return ContainerWeightClass.LIGHT;
        if (normalized < 250) return ContainerWeightClass.MEDIUM;
        if (normalized < 500) return ContainerWeightClass.HEAVY;
        if (normalized < 1000) return ContainerWeightClass.SUPER_HEAVY;
        return ContainerWeightClass.EXTREME;
    }
}
