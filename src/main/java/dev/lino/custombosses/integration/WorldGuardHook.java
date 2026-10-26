package dev.lino.custombosses.integration;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Method;

public class WorldGuardHook {

    private static boolean worldGuardPresent = false;

    public static void init() {
        worldGuardPresent = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }

    public static boolean isSpawningAllowed(Location loc) {
        if (!worldGuardPresent || loc == null) {
            return true;
        }

        try {
            Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstance = wgClass.getMethod("getInstance");
            Object wgInstance = getInstance.invoke(null);

            Method getPlatform = wgClass.getMethod("getPlatform");
            Object platform = getPlatform.invoke(wgInstance);

            Method getRegionContainer = platform.getClass().getMethod("getRegionContainer");
            Object container = getRegionContainer.invoke(platform);

            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Method adaptLoc = bukkitAdapter.getMethod("adapt", Location.class);
            Object weLoc = adaptLoc.invoke(null, loc);

            Method createQuery = container.getClass().getMethod("createQuery");
            Object query = createQuery.invoke(container);

            Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
            Object mobSpawnFlag = flagsClass.getField("MOB_SPAWNING").get(null);

            Method testState = query.getClass().getMethod("testState", weLoc.getClass(), Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable"), Class.forName("com.sk89q.worldguard.protection.flags.StateFlag[]"));
            Object array = java.lang.reflect.Array.newInstance(Class.forName("com.sk89q.worldguard.protection.flags.StateFlag"), 1);
            java.lang.reflect.Array.set(array, 0, mobSpawnFlag);

            Object result = testState.invoke(query, weLoc, null, array);
            if (result instanceof Boolean bool) {
                return bool;
            }
        } catch (Throwable ignored) {}

        return true;
    }
}
