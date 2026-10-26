package dev.lino.custombosses.manager;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.Keys;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossManager {

    private final CustomBossesPlugin plugin;
    private final Map<String, BossProfile> bosses;

    public BossManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
        this.bosses = new ConcurrentHashMap<>();
    }

    public void registerBoss(BossProfile profile) {
        if (profile != null) {
            bosses.put(profile.getId().toLowerCase(Locale.ROOT), profile);
        }
    }

    public BossProfile getBoss(String id) {
        if (id == null) {
            return null;
        }
        return bosses.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean hasBoss(String id) {
        if (id == null) {
            return false;
        }
        return bosses.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public void removeBoss(String id) {
        if (id != null) {
            bosses.remove(id.toLowerCase(Locale.ROOT));
        }
    }

    public void clearAll() {
        bosses.clear();
    }

    public Collection<BossProfile> getAllBosses() {
        return Collections.unmodifiableCollection(bosses.values());
    }

    public Set<String> getBossIds() {
        return Collections.unmodifiableSet(bosses.keySet());
    }

    public ActiveBoss spawnBoss(BossProfile profile, Location location) {
        if (profile == null || location == null || location.getWorld() == null) {
            return null;
        }

        Entity rawEntity = location.getWorld().spawnEntity(location, profile.getEntityType());
        if (!(rawEntity instanceof LivingEntity living)) {
            rawEntity.remove();
            return null;
        }

        PersistentDataContainer pdc = living.getPersistentDataContainer();
        pdc.set(Keys.BOSS_ID, PersistentDataType.STRING, profile.getId());
        pdc.set(Keys.BOSS_UUID, PersistentDataType.STRING, living.getUniqueId().toString());

        living.setCustomName(TextUtil.colorize(profile.getDisplayName()));
        living.setCustomNameVisible(true);
        living.setGlowing(profile.isGlowing());
        living.setSilent(profile.isSilent());
        living.setAI(profile.isAi());
        living.setRemoveWhenFarAway(!profile.isPreventDespawn());

        applyAttribute(living, ActiveBoss.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH"), profile.getMaxHealth());
        living.setHealth(profile.getMaxHealth());
        applyAttribute(living, ActiveBoss.resolveAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE"), profile.getAttackDamage());
        applyAttribute(living, ActiveBoss.resolveAttribute("MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED"), profile.getMovementSpeed());
        applyAttribute(living, ActiveBoss.resolveAttribute("FOLLOW_RANGE", "GENERIC_FOLLOW_RANGE"), profile.getFollowRange());
        applyAttribute(living, ActiveBoss.resolveAttribute("KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE"), profile.getKnockbackResistance());
        applyAttribute(living, ActiveBoss.resolveAttribute("SCALE", "GENERIC_SCALE"), profile.getScale());

        for (PotionEffect effect : profile.getPotionEffects()) {
            living.addPotionEffect(effect);
        }

        EntityEquipment equip = living.getEquipment();
        if (equip != null) {
            for (Map.Entry<EquipmentSlot, ItemStack> entry : profile.getEquipment().entrySet()) {
                equip.setItem(entry.getKey(), entry.getValue().clone());
            }
            for (Map.Entry<EquipmentSlot, Float> entry : profile.getDropChances().entrySet()) {
                applyDropChance(equip, entry.getKey(), entry.getValue());
            }
        }

        if (profile.getPassengerType() != null) {
            Entity passenger = location.getWorld().spawnEntity(location, profile.getPassengerType());
            if (passenger instanceof LivingEntity livingPassenger) {
                plugin.getWildStackerHook().markUnstackable(livingPassenger);
            }
            living.addPassenger(passenger);
        }

        plugin.getWildStackerHook().markUnstackable(living);

        ActiveBoss activeBoss = new ActiveBoss(living, profile);
        plugin.getActiveBossManager().registerBoss(activeBoss);
        return activeBoss;
    }

    private void applyAttribute(LivingEntity living, Attribute attribute, double value) {
        if (attribute == null) {
            return;
        }
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private void applyDropChance(EntityEquipment equip, EquipmentSlot slot, float chance) {
        if (equip == null || slot == null) return;
        switch (slot) {
            case HEAD -> equip.setHelmetDropChance(chance);
            case CHEST -> equip.setChestplateDropChance(chance);
            case LEGS -> equip.setLeggingsDropChance(chance);
            case FEET -> equip.setBootsDropChance(chance);
            case HAND -> equip.setItemInMainHandDropChance(chance);
            case OFF_HAND -> equip.setItemInOffHandDropChance(chance);
            default -> {}
        }
    }

    public ItemStack createSpawnEgg(BossProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ItemBuilder(Material.BLAZE_SPAWN_EGG)
                .name(profile.getDisplayName() + " &7[Spawn Egg]")
                .lore(
                        "&8CustomBosses Spawn Egg",
                        "&7Boss: &f" + profile.getId(),
                        "&7Health: &c" + profile.getMaxHealth(),
                        "&7Damage: &e" + profile.getAttackDamage(),
                        "",
                        "&eRight-click on a block to spawn."
                )
                .stringData(Keys.EGG_BOSS_ID, profile.getId())
                .build();
    }

    public ItemStack createAdminWand() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .name("&6&lCustomBosses Admin Wand")
                .lore(
                        "&7Left-Click: Select Position 1",
                        "&7Right-Click: Select Position 2",
                        "&7Shift-Right-Click: Cycle Boss Picker",
                        "",
                        "&eUse for defining regions and altars."
                )
                .stringData(Keys.WAND_MARKER, "true")
                .build();
    }
}
