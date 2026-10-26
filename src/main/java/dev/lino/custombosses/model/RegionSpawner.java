package dev.lino.custombosses.model;

public class RegionSpawner {

    private final String id;
    private String bossId;
    private CuboidRegion region;
    private int intervalSeconds;
    private int mobCap;
    private boolean dayOnly;
    private boolean nightOnly;
    private boolean enabled;
    private transient long lastSpawnMillis;

    public RegionSpawner(String id, String bossId, CuboidRegion region) {
        this.id = id;
        this.bossId = bossId;
        this.region = region;
        this.intervalSeconds = 300;
        this.mobCap = 1;
        this.dayOnly = false;
        this.nightOnly = false;
        this.enabled = true;
        this.lastSpawnMillis = 0L;
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

    public CuboidRegion getRegion() {
        return region;
    }

    public void setRegion(CuboidRegion region) {
        this.region = region;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = Math.max(1, intervalSeconds);
    }

    public int getMobCap() {
        return mobCap;
    }

    public void setMobCap(int mobCap) {
        this.mobCap = Math.max(1, mobCap);
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

    public boolean canSpawnNow() {
        if (!enabled) {
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - lastSpawnMillis) >= (intervalSeconds * 1000L);
    }

    public void recordSpawn() {
        this.lastSpawnMillis = System.currentTimeMillis();
    }
}
