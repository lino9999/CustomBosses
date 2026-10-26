package dev.lino.custombosses.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDate;

public class CoordinateSpawner {

    private final String id;
    private String bossId;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private int intervalSeconds;
    private LocalDate specificDate;
    private double playerRadius;
    private boolean enabled;
    private transient long lastSpawnMillis;
    private transient boolean dateSpawnedToday;

    public CoordinateSpawner(String id, String bossId, String worldName, double x, double y, double z) {
        this.id = id;
        this.bossId = bossId;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.intervalSeconds = 600;
        this.specificDate = null;
        this.playerRadius = 50.0;
        this.enabled = true;
        this.lastSpawnMillis = 0L;
        this.dateSpawnedToday = false;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = Math.max(1, intervalSeconds);
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public double getPlayerRadius() {
        return playerRadius;
    }

    public void setPlayerRadius(double playerRadius) {
        this.playerRadius = Math.max(0.0, playerRadius);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public boolean canSpawnNow() {
        if (!enabled) {
            return false;
        }
        if (specificDate != null) {
            LocalDate today = LocalDate.now();
            if (today.isEqual(specificDate)) {
                return !dateSpawnedToday;
            }
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - lastSpawnMillis) >= (intervalSeconds * 1000L);
    }

    public void recordSpawn() {
        this.lastSpawnMillis = System.currentTimeMillis();
        if (specificDate != null && LocalDate.now().isEqual(specificDate)) {
            this.dateSpawnedToday = true;
        }
    }
}
