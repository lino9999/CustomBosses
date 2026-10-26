package dev.lino.custombosses.model;

import java.util.concurrent.ThreadLocalRandom;

public class NaturalSpawner {

    private final String id;
    private String bossId;
    private String worldName;
    private double chance;
    private double minPlayerDistance;
    private double maxPlayerDistance;
    private int mobCap;
    private int minLightLevel;
    private int maxLightLevel;
    private int intervalSeconds;
    private boolean dayOnly;
    private boolean nightOnly;
    private boolean enabled;
    private transient long lastCheckMillis;

    public NaturalSpawner(String id, String bossId, String worldName) {
        this.id = id;
        this.bossId = bossId;
        this.worldName = worldName;
        this.chance = 15.0;
        this.minPlayerDistance = 16.0;
        this.maxPlayerDistance = 48.0;
        this.mobCap = 2;
        this.minLightLevel = 0;
        this.maxLightLevel = 15;
        this.intervalSeconds = 60;
        this.dayOnly = false;
        this.nightOnly = false;
        this.enabled = true;
        this.lastCheckMillis = 0L;
    }

    public String getId() {
        return id;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = Math.max(0.0, Math.min(100.0, chance));
    }

    public double getMinPlayerDistance() {
        return minPlayerDistance;
    }

    public void setMinPlayerDistance(double minPlayerDistance) {
        this.minPlayerDistance = Math.max(1.0, minPlayerDistance);
    }

    public double getMaxPlayerDistance() {
        return maxPlayerDistance;
    }

    public void setMaxPlayerDistance(double maxPlayerDistance) {
        this.maxPlayerDistance = Math.max(minPlayerDistance + 1.0, maxPlayerDistance);
    }

    public int getMobCap() {
        return mobCap;
    }

    public void setMobCap(int mobCap) {
        this.mobCap = Math.max(1, mobCap);
    }

    public int getMinLightLevel() {
        return minLightLevel;
    }

    public void setMinLightLevel(int minLightLevel) {
        this.minLightLevel = Math.max(0, Math.min(15, minLightLevel));
    }

    public int getMaxLightLevel() {
        return maxLightLevel;
    }

    public void setMaxLightLevel(int maxLightLevel) {
        this.maxLightLevel = Math.max(0, Math.min(15, maxLightLevel));
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = Math.max(1, intervalSeconds);
    }

    public boolean isDayOnly() {
        return dayOnly;
    }

    public void setDayOnly(boolean dayOnly) {
        this.dayOnly = dayOnly;
    }

    public boolean isNightOnly() {
        return nightOnly;
    }

    public void setNightOnly(boolean nightOnly) {
        this.nightOnly = nightOnly;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean canCheckNow() {
        if (!enabled) {
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - lastCheckMillis) >= (intervalSeconds * 1000L);
    }

    public void recordCheck() {
        this.lastCheckMillis = System.currentTimeMillis();
    }

    public boolean rollChance() {
        return ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= chance;
    }
}
