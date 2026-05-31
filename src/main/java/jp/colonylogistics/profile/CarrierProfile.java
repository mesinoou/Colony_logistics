package jp.colonylogistics.profile;

import jp.colonylogistics.config.ColonyLogisticsConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class CarrierProfile {
    private final UUID playerId;
    private int completedJobs;
    private int failedJobs;
    private int reputation;
    private int carrierLevel;
    private long totalDistanceDelivered;
    private int totalCargoDelivered;

    public CarrierProfile(UUID playerId) {
        this.playerId = playerId;
        this.reputation = 0;
        this.carrierLevel = 1;
    }

    public UUID playerId() { return playerId; }
    public int completedJobs() { return completedJobs; }
    public int failedJobs() { return failedJobs; }
    public int reputation() { return reputation; }
    public int carrierLevel() { return carrierLevel; }
    public long totalDistanceDelivered() { return totalDistanceDelivered; }
    public int totalCargoDelivered() { return totalCargoDelivered; }

    public void recordCompletedJob(int rewardAmount, int cargoAmount, long estimatedDistance) {
        completedJobs++;
        reputation += ColonyLogisticsConfig.reputationGainForReward(rewardAmount);
        totalCargoDelivered += Math.max(0, cargoAmount);
        totalDistanceDelivered += Math.max(0, estimatedDistance);
        recalculateLevel();
    }

    public void recordFailedJob() {
        failedJobs++;
        reputation = Math.max(0, reputation - ColonyLogisticsConfig.failedJobReputationPenalty());
        recalculateLevel();
    }


    public void setLevelForTesting(int requestedLevel) {
        int level = Math.max(1, Math.min(5, requestedLevel));
        switch (level) {
            case 5, 4, 3, 2 -> {
                completedJobs = Math.max(completedJobs, ColonyLogisticsConfig.carrierCompletedJobsForLevel(level));
                reputation = Math.max(reputation, ColonyLogisticsConfig.carrierReputationForLevel(level));
            }
            default -> {
                completedJobs = 0;
                failedJobs = 0;
                reputation = 0;
                totalDistanceDelivered = 0;
                totalCargoDelivered = 0;
            }
        }
        recalculateLevel();
    }

    public void addCompletedJobsForTesting(int count) {
        int safeCount = Math.max(0, count);
        completedJobs += safeCount;
        reputation += safeCount * 5;
        totalCargoDelivered += safeCount;
        totalDistanceDelivered += safeCount * 10L;
        recalculateLevel();
    }

    public void resetForTesting() {
        completedJobs = 0;
        failedJobs = 0;
        reputation = 0;
        carrierLevel = 1;
        totalDistanceDelivered = 0;
        totalCargoDelivered = 0;
    }

    private void recalculateLevel() {
        carrierLevel = ColonyLogisticsConfig.carrierLevelFor(completedJobs, reputation);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", playerId);
        tag.putInt("CompletedJobs", completedJobs);
        tag.putInt("FailedJobs", failedJobs);
        tag.putInt("Reputation", reputation);
        tag.putInt("CarrierLevel", carrierLevel);
        tag.putLong("TotalDistanceDelivered", totalDistanceDelivered);
        tag.putInt("TotalCargoDelivered", totalCargoDelivered);
        return tag;
    }

    public static CarrierProfile load(CompoundTag tag) {
        CarrierProfile profile = new CarrierProfile(tag.hasUUID("PlayerId") ? tag.getUUID("PlayerId") : new UUID(0L, 0L));
        profile.completedJobs = tag.getInt("CompletedJobs");
        profile.failedJobs = tag.getInt("FailedJobs");
        profile.reputation = tag.getInt("Reputation");
        profile.carrierLevel = Math.max(1, tag.getInt("CarrierLevel"));
        profile.totalDistanceDelivered = tag.getLong("TotalDistanceDelivered");
        profile.totalCargoDelivered = tag.getInt("TotalCargoDelivered");
        profile.recalculateLevel();
        return profile;
    }
}
