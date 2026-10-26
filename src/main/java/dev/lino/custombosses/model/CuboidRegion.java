package dev.lino.custombosses.model;

import dev.lino.custombosses.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public class CuboidRegion {

    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public CuboidRegion(Location loc1, Location loc2) {
        this.worldName = loc1.getWorld() != null ? loc1.getWorld().getName() : "world";
        this.minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        this.maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        this.maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
    }

    public CuboidRegion(String worldName, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.worldName = worldName;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null || !loc.getWorld().getName().equals(worldName)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Location getRandomLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        int rx = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int rz = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
        int highestY = world.getHighestBlockYAt(rx, rz);
        int targetY = Math.max(minY, Math.min(highestY, maxY));
        Location safe = LocationUtil.findSafeGround(world, rx, targetY, minY, maxY, rz);
        if (safe != null) {
            return safe;
        }
        return new Location(world, rx + 0.5, Math.max(minY, Math.min(highestY + 1, maxY)), rz + 0.5);
    }
}
