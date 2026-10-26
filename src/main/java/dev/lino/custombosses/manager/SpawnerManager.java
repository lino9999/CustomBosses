package dev.lino.custombosses.manager;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.integration.WorldGuardHook;
import dev.lino.custombosses.model.*;
import dev.lino.custombosses.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnerManager {

    private final CustomBossesPlugin plugin;
    private final Map<String, RegionSpawner> regionSpawners;
    private final Map<String, CoordinateSpawner> coordinateSpawners;
    private final Map<String, NaturalSpawner> naturalSpawners;

    public SpawnerManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
        this.regionSpawners = new ConcurrentHashMap<>();
        this.coordinateSpawners = new ConcurrentHashMap<>();
        this.naturalSpawners = new ConcurrentHashMap<>();
    }

    public void addRegionSpawner(RegionSpawner spawner) {
        if (spawner != null) {
            regionSpawners.put(spawner.getId().toLowerCase(Locale.ROOT), spawner);
        }
    }

    public RegionSpawner getRegionSpawner(String id) {
        if (id == null) return null;
        return regionSpawners.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<RegionSpawner> getAllRegionSpawners() {
        return Collections.unmodifiableCollection(regionSpawners.values());
    }

    public void removeRegionSpawner(String id) {
        if (id != null) {
            regionSpawners.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public void addCoordinateSpawner(CoordinateSpawner spawner) {
        if (spawner != null) {
            coordinateSpawners.put(spawner.getId().toLowerCase(Locale.ROOT), spawner);
        }
    }

    public CoordinateSpawner getCoordinateSpawner(String id) {
        if (id == null) return null;
        return coordinateSpawners.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<CoordinateSpawner> getAllCoordinateSpawners() {
        return Collections.unmodifiableCollection(coordinateSpawners.values());
    }

    public void removeCoordinateSpawner(String id) {
        if (id != null) {
            coordinateSpawners.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public void addNaturalSpawner(NaturalSpawner spawner) {
        if (spawner != null) {
            naturalSpawners.put(spawner.getId().toLowerCase(Locale.ROOT), spawner);
        }
    }

    public NaturalSpawner getNaturalSpawner(String id) {
        if (id == null) return null;
        return naturalSpawners.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<NaturalSpawner> getAllNaturalSpawners() {
        return Collections.unmodifiableCollection(naturalSpawners.values());
    }

    public void removeNaturalSpawner(String id) {
        if (id != null) {
            naturalSpawners.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public void clearAll() {
        regionSpawners.clear();
        coordinateSpawners.clear();
        naturalSpawners.clear();
    }

    public void tick() {
        tickRegionSpawners();
        tickCoordinateSpawners();
        tickNaturalSpawners();
    }

    private void tickRegionSpawners() {
        for (RegionSpawner spawner : regionSpawners.values()) {
            if (!spawner.canSpawnNow()) {
                continue;
            }
            BossProfile profile = plugin.getBossManager().getBoss(spawner.getBossId());
            if (profile == null) {
                continue;
            }
            World world = Bukkit.getWorld(spawner.getRegion().getWorldName());
            if (world == null) {
                continue;
            }

            long time = world.getTime();
            boolean isDay = time < 12300 || time > 23850;
            if (spawner.isDayOnly() && !isDay) continue;
            if (spawner.isNightOnly() && isDay) continue;

            int activeInRegion = countBossesInRegion(spawner.getRegion(), spawner.getBossId());
            if (activeInRegion >= spawner.getMobCap()) {
                continue;
            }

            Location spawnLoc = spawner.getRegion().getRandomLocation();
            if (spawnLoc != null && WorldGuardHook.isSpawningAllowed(spawnLoc)) {
                plugin.getBossManager().spawnBoss(profile, spawnLoc);
                spawner.recordSpawn();
            }
        }
    }

    private void tickCoordinateSpawners() {
        for (CoordinateSpawner spawner : coordinateSpawners.values()) {
            if (!spawner.canSpawnNow()) {
                continue;
            }
            BossProfile profile = plugin.getBossManager().getBoss(spawner.getBossId());
            if (profile == null) {
                continue;
            }
            Location loc = spawner.getLocation();
            if (loc == null || loc.getWorld() == null || !WorldGuardHook.isSpawningAllowed(loc)) {
                continue;
            }

            if (spawner.getPlayerRadius() > 0) {
                boolean playerNear = false;
                double radSq = spawner.getPlayerRadius() * spawner.getPlayerRadius();
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= radSq) {
                        playerNear = true;
                        break;
                    }
                }
                if (!playerNear) {
                    continue;
                }
            }

            int currentActive = plugin.getActiveBossManager().getActiveCountByProfile(spawner.getBossId());
            if (currentActive > 0) {
                continue;
            }

            plugin.getBossManager().spawnBoss(profile, loc);
            spawner.recordSpawn();
        }
    }

    private void tickNaturalSpawners() {
        for (NaturalSpawner spawner : naturalSpawners.values()) {
            if (!spawner.canCheckNow()) {
                continue;
            }
            spawner.recordCheck();
            if (!spawner.rollChance()) {
                continue;
            }

            BossProfile profile = plugin.getBossManager().getBoss(spawner.getBossId());
            if (profile == null) {
                continue;
            }
            World world = Bukkit.getWorld(spawner.getWorldName());
            if (world == null || world.getPlayers().isEmpty()) {
                continue;
            }

            long time = world.getTime();
            boolean isDay = time < 12300 || time > 23850;
            if (spawner.isDayOnly() && !isDay) continue;
            if (spawner.isNightOnly() && isDay) continue;

            int currentActive = plugin.getActiveBossManager().getActiveCountByProfile(spawner.getBossId());
            if (currentActive >= spawner.getMobCap()) {
                continue;
            }

            List<Player> players = world.getPlayers();
            Player randomPlayer = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            Location pLoc = randomPlayer.getLocation();

            double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
            double dist = ThreadLocalRandom.current().nextDouble(spawner.getMinPlayerDistance(), spawner.getMaxPlayerDistance());
            double ox = Math.cos(angle) * dist;
            double oz = Math.sin(angle) * dist;

            int targetX = pLoc.getBlockX() + (int) ox;
            int targetZ = pLoc.getBlockZ() + (int) oz;
            int startY = pLoc.getBlockY();
            int minY = world.getMinHeight() + 1;
            int maxY = world.getEnvironment() == World.Environment.NETHER ? 120 : world.getMaxHeight() - 2;

            Location spawnLoc = LocationUtil.findSafeGround(world, targetX, startY, minY, maxY, targetZ);
            if (spawnLoc == null) {
                continue;
            }

            Block block = spawnLoc.getBlock();
            int light = block.getLightLevel();
            if (light < spawner.getMinLightLevel() || light > spawner.getMaxLightLevel()) {
                continue;
            }

            if (!WorldGuardHook.isSpawningAllowed(spawnLoc)) {
                continue;
            }

            plugin.getBossManager().spawnBoss(profile, spawnLoc);
        }
    }

    private int countBossesInRegion(CuboidRegion region, String bossId) {
        int count = 0;
        for (ActiveBoss boss : plugin.getActiveBossManager().getAllActiveBosses()) {
            if (boss.getProfile().getId().equalsIgnoreCase(bossId)) {
                if (region.contains(boss.getEntity().getLocation())) {
                    count++;
                }
            }
        }
        return count;
    }
}
