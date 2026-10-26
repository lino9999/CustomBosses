package dev.lino.custombosses.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class LocationUtil {

    private LocationUtil() {}

    public static Location findSafeGround(World world, int x, int startY, int minY, int maxY, int z) {
        if (world == null) {
            return null;
        }

        int clampedStartY = Math.max(minY, Math.min(maxY, startY));

        for (int y = clampedStartY; y >= minY; y--) {
            if (isSafeStandBlock(world, x, y, z)) {
                return new Location(world, x + 0.5, y + 1.0, z + 0.5);
            }
        }

        for (int y = clampedStartY + 1; y <= maxY; y++) {
            if (isSafeStandBlock(world, x, y, z)) {
                return new Location(world, x + 0.5, y + 1.0, z + 0.5);
            }
        }

        return null;
    }

    private static boolean isSafeStandBlock(World world, int x, int y, int z) {
        Block floor = world.getBlockAt(x, y, z);
        Material floorMat = floor.getType();

        if (!floorMat.isSolid() || floorMat == Material.LAVA || floorMat == Material.FIRE || floorMat == Material.CACTUS || floorMat == Material.POWDER_SNOW || floorMat == Material.BARRIER) {
            return false;
        }

        Block feet = world.getBlockAt(x, y + 1, z);
        Material feetMat = feet.getType();
        if (feetMat.isSolid() || feetMat == Material.LAVA || feetMat == Material.FIRE) {
            return false;
        }

        Block head = world.getBlockAt(x, y + 2, z);
        Material headMat = head.getType();
        return !headMat.isSolid() && headMat != Material.LAVA && headMat != Material.FIRE;
    }
}
