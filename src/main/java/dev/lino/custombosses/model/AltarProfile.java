package dev.lino.custombosses.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AltarProfile {

    private final String id;
    private String bossId;
    private String worldName;
    private int x;
    private int y;
    private int z;
    private Material baseMaterial;
    private String hologramTitle;
    private final List<ItemStack> requiredItems;
    private boolean removeBlockOnSummon;
    private int cooldownSeconds;
    private boolean enabled;
    private transient long lastSummonMillis;

    public AltarProfile(String id, String bossId, Location loc) {
        this.id = id;
        this.bossId = bossId;
        this.worldName = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.baseMaterial = loc.getBlock().getType();
        this.hologramTitle = "&6&l[ ALTAR ] &eRight-click with sacrifice to summon &c" + bossId;
        this.requiredItems = new ArrayList<>();
        this.removeBlockOnSummon = false;
        this.cooldownSeconds = 120;
        this.enabled = true;
        this.lastSummonMillis = 0L;
    }

    public AltarProfile(String id, String bossId, String worldName, int x, int y, int z, Material baseMaterial) {
        this.id = id;
        this.bossId = bossId;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.baseMaterial = baseMaterial != null ? baseMaterial : Material.OBSIDIAN;
        this.hologramTitle = "&6&l[ ALTAR ] &eRight-click with sacrifice to summon &c" + bossId;
        this.requiredItems = new ArrayList<>();
        this.removeBlockOnSummon = false;
        this.cooldownSeconds = 120;
        this.enabled = true;
        this.lastSummonMillis = 0L;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public void setBaseMaterial(Material baseMaterial) {
        this.baseMaterial = baseMaterial;
    }

    public String getHologramTitle() {
        return hologramTitle;
    }

    public void setHologramTitle(String hologramTitle) {
        this.hologramTitle = hologramTitle;
    }

    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    public void addRequiredItem(ItemStack item) {
        if (item != null) {
            this.requiredItems.add(item.clone());
        }
    }

    public boolean isRemoveBlockOnSummon() {
        return removeBlockOnSummon;
    }

    public void setRemoveBlockOnSummon(boolean removeBlockOnSummon) {
        this.removeBlockOnSummon = removeBlockOnSummon;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
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

    public boolean canSummonNow() {
        if (!enabled) {
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - lastSummonMillis) >= (cooldownSeconds * 1000L);
    }

    public void recordSummon() {
        this.lastSummonMillis = System.currentTimeMillis();
    }
}
