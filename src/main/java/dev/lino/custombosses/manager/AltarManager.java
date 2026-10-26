package dev.lino.custombosses.manager;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.AltarProfile;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.Keys;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AltarManager {

    private final CustomBossesPlugin plugin;
    private final Map<String, AltarProfile> altars;
    private final Map<String, UUID> hologramStands;

    public AltarManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
        this.altars = new ConcurrentHashMap<>();
        this.hologramStands = new ConcurrentHashMap<>();
    }

    public void addAltar(AltarProfile altar) {
        if (altar != null) {
            altars.put(altar.getId().toLowerCase(Locale.ROOT), altar);
            spawnHologram(altar);
        }
    }

    public AltarProfile getAltar(String id) {
        if (id == null) return null;
        return altars.get(id.toLowerCase(Locale.ROOT));
    }

    public AltarProfile getAltarAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        for (AltarProfile altar : altars.values()) {
            if (altar.getWorldName().equals(loc.getWorld().getName())
                    && altar.getX() == loc.getBlockX()
                    && altar.getY() == loc.getBlockY()
                    && altar.getZ() == loc.getBlockZ()) {
                return altar;
            }
        }
        return null;
    }

    public Collection<AltarProfile> getAllAltars() {
        return Collections.unmodifiableCollection(altars.values());
    }

    public void removeAltar(String id) {
        if (id != null) {
            removeHologram(id.toLowerCase(Locale.ROOT));
            altars.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public UUID getHologramStand(String altarId) {
        if (altarId == null) return null;
        return hologramStands.get(altarId.toLowerCase(Locale.ROOT));
    }

    public void registerHologramStand(String altarId, UUID uuid) {
        if (altarId != null && uuid != null) {
            hologramStands.put(altarId.toLowerCase(Locale.ROOT), uuid);
        }
    }

    public boolean interactAltar(Player player, Block block) {
        AltarProfile altar = getAltarAt(block.getLocation());
        if (altar == null) {
            return false;
        }

        if (!altar.canSummonNow()) {
            player.sendMessage(TextUtil.colorize("&cThis altar is recharging! Please wait."));
            return true;
        }

        for (ItemStack required : altar.getRequiredItems()) {
            if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                player.sendMessage(TextUtil.colorize("&cYou lack the required sacrifice: &e" + required.getAmount() + "x " + required.getType().name()));
                return true;
            }
        }

        for (ItemStack required : altar.getRequiredItems()) {
            player.getInventory().removeItem(required);
        }

        BossProfile profile = plugin.getBossManager().getBoss(altar.getBossId());
        if (profile == null) {
            player.sendMessage(TextUtil.colorize("&cConfigured boss not found: " + altar.getBossId()));
            return true;
        }

        Location spawnLoc = block.getLocation().add(0.5, 1.5, 0.5);
        plugin.getBossManager().spawnBoss(profile, spawnLoc);
        altar.recordSummon();

        spawnLoc.getWorld().strikeLightningEffect(spawnLoc);
        spawnLoc.getWorld().spawnParticle(Particle.PORTAL, spawnLoc, 50, 0.5, 0.5, 0.5, 0.1);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.2f, 0.8f);

        player.sendMessage(TextUtil.colorize("&6&l[ ALTAR ] &aThe sacrifice has been accepted! &c" + profile.getDisplayName() + " &aawakes!"));

        if (altar.isRemoveBlockOnSummon()) {
            block.setType(Material.AIR);
            removeHologram(altar.getId());
        }
        return true;
    }

    public void spawnHologram(AltarProfile altar) {
        Location loc = altar.getLocation();
        if (loc == null || loc.getWorld() == null) return;
        removeHologram(altar.getId());

        Location holoLoc = loc.clone().add(0.5, 1.2, 0.5);
        if (holoLoc.getChunk().isLoaded()) {
            for (Entity nearby : loc.getWorld().getNearbyEntities(holoLoc, 1.5, 1.5, 1.5)) {
                if (nearby instanceof ArmorStand as && as.getPersistentDataContainer().has(Keys.ALTAR_ID, PersistentDataType.STRING)) {
                    as.remove();
                }
            }
        }

        ArmorStand stand = loc.getWorld().spawn(holoLoc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setMarker(true);
            as.setCustomName(TextUtil.colorize(altar.getHologramTitle()));
            as.setCustomNameVisible(true);
            as.getPersistentDataContainer().set(Keys.ALTAR_ID, PersistentDataType.STRING, altar.getId());
        });
        hologramStands.put(altar.getId().toLowerCase(Locale.ROOT), stand.getUniqueId());
    }

    public void removeHologram(String altarId) {
        UUID standUuid = hologramStands.remove(altarId.toLowerCase(Locale.ROOT));
        if (standUuid != null) {
            Entity entity = Bukkit.getEntity(standUuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public void refreshAllHolograms() {
        for (AltarProfile altar : altars.values()) {
            Location loc = altar.getLocation();
            if (loc != null && loc.getWorld() != null && loc.getChunk().isLoaded()) {
                UUID current = hologramStands.get(altar.getId().toLowerCase(Locale.ROOT));
                Entity entity = current != null ? Bukkit.getEntity(current) : null;
                if (entity == null || !entity.isValid()) {
                    spawnHologram(altar);
                }
            }
        }
    }

    public void clearAllHolograms() {
        for (String id : new ArrayList<>(hologramStands.keySet())) {
            removeHologram(id);
        }
    }

    public void clearAll() {
        clearAllHolograms();
        altars.clear();
        hologramStands.clear();
    }
}
