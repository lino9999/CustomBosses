package dev.lino.custombosses.config;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.ability.AbilityTrigger;
import dev.lino.custombosses.ability.AbilityType;
import dev.lino.custombosses.ability.BossAbility;
import dev.lino.custombosses.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class ConfigManager {

    private final CustomBossesPlugin plugin;

    private File bossesFile;
    private FileConfiguration bossesConfig;

    private File spawnersFile;
    private FileConfiguration spawnersConfig;

    private File altarsFile;
    private FileConfiguration altarsConfig;

    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ConfigManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        plugin.getBossManager().clearAll();
        plugin.getSpawnerManager().clearAll();
        plugin.getAltarManager().clearAll();

        loadMessages();
        loadBosses();
        loadSpawners();
        loadAltars();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getMessageManager().load(messagesConfig);
    }

    private void loadBosses() {
        bossesFile = new File(plugin.getDataFolder(), "bosses.yml");
        if (!bossesFile.exists()) {
            plugin.saveResource("bosses.yml", false);
        }
        bossesConfig = YamlConfiguration.loadConfiguration(bossesFile);

        ConfigurationSection section = bossesConfig.getConfigurationSection("bosses");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection bs = section.getConfigurationSection(id);
            if (bs == null) continue;

            String typeStr = bs.getString("entity_type", "ZOMBIE");
            EntityType type = EntityType.ZOMBIE;
            try {
                type = EntityType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {}

            BossProfile profile = new BossProfile(id, type);
            profile.setDisplayName(bs.getString("display_name", "&c" + id));
            profile.setMaxHealth(bs.getDouble("max_health", 100.0));
            profile.setAttackDamage(bs.getDouble("attack_damage", 10.0));
            profile.setMovementSpeed(bs.getDouble("movement_speed", 0.25));
            profile.setFollowRange(bs.getDouble("follow_range", 32.0));
            profile.setKnockbackResistance(bs.getDouble("knockback_resistance", 0.5));
            profile.setScale(bs.getDouble("scale", 1.0));

            if (bs.contains("passenger_type")) {
                try {
                    profile.setPassengerType(EntityType.valueOf(bs.getString("passenger_type").toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {}
            }

            if (bs.contains("ambient_particle.type")) {
                try {
                    profile.setAmbientParticle(Particle.valueOf(bs.getString("ambient_particle.type").toUpperCase(Locale.ROOT)));
                    profile.setAmbientParticleCount(bs.getInt("ambient_particle.count", 3));
                } catch (IllegalArgumentException ignored) {}
            }

            if (bs.isList("potion_effects")) {
                for (Map<?, ?> map : bs.getMapList("potion_effects")) {
                    String potName = (String) map.get("type");
                    int duration = map.get("duration") instanceof Number n ? n.intValue() : 99999;
                    int amp = map.get("amplifier") instanceof Number n ? n.intValue() : 0;
                    if (potName != null) {
                        PotionEffectType pet = PotionEffectType.getByName(potName.toUpperCase(Locale.ROOT));
                        if (pet != null) {
                            profile.addPotionEffect(new PotionEffect(pet, duration, amp));
                        }
                    }
                }
            }

            ConfigurationSection equipSec = bs.getConfigurationSection("equipment");
            if (equipSec != null) {
                for (String slotName : equipSec.getKeys(false)) {
                    try {
                        EquipmentSlot slot = EquipmentSlot.valueOf(slotName.toUpperCase(Locale.ROOT));
                        ItemStack item = equipSec.getItemStack(slotName);
                        if (item != null) {
                            profile.setEquipment(slot, item);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            ConfigurationSection dropChanceSec = bs.getConfigurationSection("drop_chances");
            if (dropChanceSec != null) {
                for (String slotName : dropChanceSec.getKeys(false)) {
                    try {
                        EquipmentSlot slot = EquipmentSlot.valueOf(slotName.toUpperCase(Locale.ROOT));
                        profile.setDropChance(slot, (float) dropChanceSec.getDouble(slotName, 0.0));
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            profile.setBossBarEnabled(bs.getBoolean("boss_bar.enabled", true));
            profile.setBossBarTitle(bs.getString("boss_bar.title", profile.getDisplayName() + " &7[&c%health%&7/&c%max_health%&7]"));
            try {
                profile.setBossBarColor(BarColor.valueOf(bs.getString("boss_bar.color", "RED").toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {}
            try {
                profile.setBossBarStyle(BarStyle.valueOf(bs.getString("boss_bar.style", "SOLID").toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {}
            profile.setBossBarRange(bs.getDouble("boss_bar.range", 30.0));

            profile.setGlowing(bs.getBoolean("properties.glowing", false));
            profile.setSilent(bs.getBoolean("properties.silent", false));
            profile.setAi(bs.getBoolean("properties.ai", true));
            if (bs.contains("properties.custom_model_data")) {
                profile.setCustomModelData(bs.getInt("properties.custom_model_data"));
            }

            profile.setImmuneFall(bs.getBoolean("immunities.fall", true));
            profile.setImmuneFire(bs.getBoolean("immunities.fire", false));
            profile.setImmuneDrown(bs.getBoolean("immunities.drown", true));
            profile.setImmuneExplosion(bs.getBoolean("immunities.explosion", false));
            profile.setImmuneKnockback(bs.getBoolean("immunities.knockback", false));
            profile.setImmuneProjectile(bs.getBoolean("immunities.projectile", false));
            profile.setImmuneSuffocation(bs.getBoolean("immunities.suffocation", true));
            profile.setPreventDespawn(bs.getBoolean("immunities.prevent_despawn", true));

            ConfigurationSection abilitiesSec = bs.getConfigurationSection("abilities");
            if (abilitiesSec != null) {
                for (String abKey : abilitiesSec.getKeys(false)) {
                    try {
                        AbilityType abType = AbilityType.valueOf(abKey.toUpperCase(Locale.ROOT));
                        ConfigurationSection as = abilitiesSec.getConfigurationSection(abKey);
                        if (as != null && as.getBoolean("enabled", true)) {
                            BossAbility ability = new BossAbility(abType);
                            ability.setEnabled(true);
                            if (as.contains("trigger")) {
                                try {
                                    ability.setTrigger(AbilityTrigger.valueOf(as.getString("trigger").toUpperCase(Locale.ROOT)));
                                } catch (IllegalArgumentException ignored) {}
                            }
                            ability.setChance(as.getDouble("chance", 100.0));
                            ability.setCooldownSeconds(as.getInt("cooldown", 10));
                            ability.setRange(as.getDouble("range", 10.0));
                            ability.setValue(as.getDouble("value", 5.0));
                            ability.setStringValue(as.getString("string_value", ""));
                            ability.setDurationTicks(as.getInt("duration_ticks", 100));
                            ability.setMessage(as.getString("message", ""));
                            profile.addAbility(ability);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            ConfigurationSection dropsSec = bs.getConfigurationSection("drops");
            if (dropsSec != null) {
                for (String dropKey : dropsSec.getKeys(false)) {
                    ConfigurationSection ds = dropsSec.getConfigurationSection(dropKey);
                    if (ds != null) {
                        ItemStack item = ds.getItemStack("item");
                        if (item == null && ds.contains("material")) {
                            Material mat = Material.matchMaterial(ds.getString("material", "DIAMOND"));
                            if (mat != null) {
                                item = new ItemStack(mat);
                            }
                        }
                        if (item != null) {
                            BossDrop drop = new BossDrop(item, ds.getDouble("chance", 50.0));
                            drop.setMinAmount(ds.getInt("min_amount", 1));
                            drop.setMaxAmount(ds.getInt("max_amount", 1));
                            drop.setCommands(ds.getStringList("commands"));
                            try {
                                drop.setRewardType(RewardType.valueOf(ds.getString("reward_type", "CHANCE_DROP").toUpperCase(Locale.ROOT)));
                            } catch (IllegalArgumentException ignored) {}
                            profile.getDrops().add(drop);
                        }
                    }
                }
            }

            profile.setXpDrop(bs.getInt("rewards.xp", 50));
            profile.setClearVanillaDrops(bs.getBoolean("rewards.clear_vanilla_drops", true));

            plugin.getBossManager().registerBoss(profile);
        }
    }

    public void saveBoss(BossProfile profile) {
        if (bossesConfig == null || profile == null) return;
        String path = "bosses." + profile.getId();

        bossesConfig.set(path + ".display_name", profile.getDisplayName());
        bossesConfig.set(path + ".entity_type", profile.getEntityType().name());
        bossesConfig.set(path + ".max_health", profile.getMaxHealth());
        bossesConfig.set(path + ".attack_damage", profile.getAttackDamage());
        bossesConfig.set(path + ".movement_speed", profile.getMovementSpeed());
        bossesConfig.set(path + ".follow_range", profile.getFollowRange());
        bossesConfig.set(path + ".knockback_resistance", profile.getKnockbackResistance());
        bossesConfig.set(path + ".scale", profile.getScale());

        if (profile.getPassengerType() != null) {
            bossesConfig.set(path + ".passenger_type", profile.getPassengerType().name());
        }
        if (profile.getAmbientParticle() != null) {
            bossesConfig.set(path + ".ambient_particle.type", profile.getAmbientParticle().name());
            bossesConfig.set(path + ".ambient_particle.count", profile.getAmbientParticleCount());
        }

        bossesConfig.set(path + ".boss_bar.enabled", profile.isBossBarEnabled());
        bossesConfig.set(path + ".boss_bar.title", profile.getBossBarTitle());
        bossesConfig.set(path + ".boss_bar.color", profile.getBossBarColor().name());
        bossesConfig.set(path + ".boss_bar.style", profile.getBossBarStyle().name());
        bossesConfig.set(path + ".boss_bar.range", profile.getBossBarRange());

        bossesConfig.set(path + ".properties.glowing", profile.isGlowing());
        bossesConfig.set(path + ".properties.silent", profile.isSilent());
        bossesConfig.set(path + ".properties.ai", profile.isAi());
        bossesConfig.set(path + ".properties.custom_model_data", profile.getCustomModelData());

        bossesConfig.set(path + ".immunities.fall", profile.isImmuneFall());
        bossesConfig.set(path + ".immunities.fire", profile.isImmuneFire());
        bossesConfig.set(path + ".immunities.drown", profile.isImmuneDrown());
        bossesConfig.set(path + ".immunities.explosion", profile.isImmuneExplosion());
        bossesConfig.set(path + ".immunities.knockback", profile.isImmuneKnockback());
        bossesConfig.set(path + ".immunities.projectile", profile.isImmuneProjectile());
        bossesConfig.set(path + ".immunities.suffocation", profile.isImmuneSuffocation());
        bossesConfig.set(path + ".immunities.prevent_despawn", profile.isPreventDespawn());

        bossesConfig.set(path + ".equipment", null);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : profile.getEquipment().entrySet()) {
            bossesConfig.set(path + ".equipment." + entry.getKey().name(), entry.getValue());
        }
        bossesConfig.set(path + ".drop_chances", null);
        for (Map.Entry<EquipmentSlot, Float> entry : profile.getDropChances().entrySet()) {
            bossesConfig.set(path + ".drop_chances." + entry.getKey().name(), entry.getValue());
        }

        bossesConfig.set(path + ".abilities", null);
        for (BossAbility ab : profile.getAbilities().values()) {
            String abPath = path + ".abilities." + ab.getType().name();
            bossesConfig.set(abPath + ".enabled", ab.isEnabled());
            bossesConfig.set(abPath + ".trigger", ab.getTrigger().name());
            bossesConfig.set(abPath + ".chance", ab.getChance());
            bossesConfig.set(abPath + ".cooldown", ab.getCooldownSeconds());
            bossesConfig.set(abPath + ".range", ab.getRange());
            bossesConfig.set(abPath + ".value", ab.getValue());
            bossesConfig.set(abPath + ".string_value", ab.getStringValue());
            bossesConfig.set(abPath + ".duration_ticks", ab.getDurationTicks());
            bossesConfig.set(abPath + ".message", ab.getMessage());
        }

        bossesConfig.set(path + ".drops", null);
        int dropIdx = 1;
        for (BossDrop drop : profile.getDrops()) {
            if (drop == null) continue;
            String dPath = path + ".drops.drop_" + dropIdx++;
            if (drop.getItem() != null) {
                bossesConfig.set(dPath + ".item", drop.getItem());
                bossesConfig.set(dPath + ".material", drop.getItem().getType().name());
            }
            bossesConfig.set(dPath + ".chance", drop.getChance());
            bossesConfig.set(dPath + ".min_amount", drop.getMinAmount());
            bossesConfig.set(dPath + ".max_amount", drop.getMaxAmount());
            bossesConfig.set(dPath + ".reward_type", drop.getRewardType().name());
            bossesConfig.set(dPath + ".commands", drop.getCommands());
        }

        bossesConfig.set(path + ".rewards.xp", profile.getXpDrop());
        bossesConfig.set(path + ".rewards.clear_vanilla_drops", profile.isClearVanillaDrops());

        try {
            bossesConfig.save(bossesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save bosses.yml: " + e.getMessage());
        }
    }

    private void loadSpawners() {
        spawnersFile = new File(plugin.getDataFolder(), "spawners.yml");
        if (!spawnersFile.exists()) {
            plugin.saveResource("spawners.yml", false);
        }
        spawnersConfig = YamlConfiguration.loadConfiguration(spawnersFile);

        ConfigurationSection regSec = spawnersConfig.getConfigurationSection("regions");
        if (regSec != null) {
            for (String key : regSec.getKeys(false)) {
                ConfigurationSection s = regSec.getConfigurationSection(key);
                if (s == null) continue;
                String bossId = s.getString("boss_id", "titan");
                String world = s.getString("world", "world");
                int x1 = s.getInt("min_x", 0);
                int y1 = s.getInt("min_y", 0);
                int z1 = s.getInt("min_z", 0);
                int x2 = s.getInt("max_x", 0);
                int y2 = s.getInt("max_y", 0);
                int z2 = s.getInt("max_z", 0);
                CuboidRegion region = new CuboidRegion(world, x1, y1, z1, x2, y2, z2);
                RegionSpawner spawner = new RegionSpawner(key, bossId, region);
                spawner.setIntervalSeconds(s.getInt("interval", 300));
                spawner.setMobCap(s.getInt("mob_cap", 1));
                spawner.setDayOnly(s.getBoolean("day_only", false));
                spawner.setNightOnly(s.getBoolean("night_only", false));
                spawner.setEnabled(s.getBoolean("enabled", true));
                plugin.getSpawnerManager().addRegionSpawner(spawner);
            }
        }

        ConfigurationSection coordSec = spawnersConfig.getConfigurationSection("coordinates");
        if (coordSec != null) {
            for (String key : coordSec.getKeys(false)) {
                ConfigurationSection s = coordSec.getConfigurationSection(key);
                if (s == null) continue;
                String bossId = s.getString("boss_id", "titan");
                String world = s.getString("world", "world");
                double x = s.getDouble("x", 0.0);
                double y = s.getDouble("y", 64.0);
                double z = s.getDouble("z", 0.0);
                CoordinateSpawner spawner = new CoordinateSpawner(key, bossId, world, x, y, z);
                spawner.setIntervalSeconds(s.getInt("interval", 600));
                spawner.setPlayerRadius(s.getDouble("player_radius", 50.0));
                spawner.setEnabled(s.getBoolean("enabled", true));
                if (s.contains("specific_date")) {
                    try {
                        spawner.setSpecificDate(LocalDate.parse(s.getString("specific_date")));
                    } catch (Exception ignored) {}
                }
                plugin.getSpawnerManager().addCoordinateSpawner(spawner);
            }
        }

        ConfigurationSection natSec = spawnersConfig.getConfigurationSection("natural");
        if (natSec != null) {
            for (String key : natSec.getKeys(false)) {
                ConfigurationSection s = natSec.getConfigurationSection(key);
                if (s == null) continue;
                String bossId = s.getString("boss_id", "titan");
                String world = s.getString("world", "world");
                NaturalSpawner spawner = new NaturalSpawner(key, bossId, world);
                spawner.setChance(s.getDouble("chance", 15.0));
                spawner.setMinPlayerDistance(s.getDouble("min_distance", 16.0));
                spawner.setMaxPlayerDistance(s.getDouble("max_distance", 48.0));
                spawner.setMobCap(s.getInt("mob_cap", 2));
                spawner.setMinLightLevel(s.getInt("min_light", 0));
                spawner.setMaxLightLevel(s.getInt("max_light", 15));
                spawner.setIntervalSeconds(s.getInt("interval", 60));
                spawner.setDayOnly(s.getBoolean("day_only", false));
                spawner.setNightOnly(s.getBoolean("night_only", false));
                spawner.setEnabled(s.getBoolean("enabled", true));
                plugin.getSpawnerManager().addNaturalSpawner(spawner);
            }
        }
    }

    public void saveSpawners() {
        if (spawnersConfig == null) return;
        spawnersConfig.set("regions", null);
        for (RegionSpawner s : plugin.getSpawnerManager().getAllRegionSpawners()) {
            String p = "regions." + s.getId();
            spawnersConfig.set(p + ".boss_id", s.getBossId());
            spawnersConfig.set(p + ".world", s.getRegion().getWorldName());
            spawnersConfig.set(p + ".min_x", s.getRegion().getMinX());
            spawnersConfig.set(p + ".min_y", s.getRegion().getMinY());
            spawnersConfig.set(p + ".min_z", s.getRegion().getMinZ());
            spawnersConfig.set(p + ".max_x", s.getRegion().getMaxX());
            spawnersConfig.set(p + ".max_y", s.getRegion().getMaxY());
            spawnersConfig.set(p + ".max_z", s.getRegion().getMaxZ());
            spawnersConfig.set(p + ".interval", s.getIntervalSeconds());
            spawnersConfig.set(p + ".mob_cap", s.getMobCap());
            spawnersConfig.set(p + ".day_only", s.isDayOnly());
            spawnersConfig.set(p + ".night_only", s.isNightOnly());
            spawnersConfig.set(p + ".enabled", s.isEnabled());
        }

        spawnersConfig.set("coordinates", null);
        for (CoordinateSpawner s : plugin.getSpawnerManager().getAllCoordinateSpawners()) {
            String p = "coordinates." + s.getId();
            spawnersConfig.set(p + ".boss_id", s.getBossId());
            spawnersConfig.set(p + ".world", s.getWorldName());
            spawnersConfig.set(p + ".x", s.getX());
            spawnersConfig.set(p + ".y", s.getY());
            spawnersConfig.set(p + ".z", s.getZ());
            spawnersConfig.set(p + ".interval", s.getIntervalSeconds());
            spawnersConfig.set(p + ".player_radius", s.getPlayerRadius());
            spawnersConfig.set(p + ".enabled", s.isEnabled());
            if (s.getSpecificDate() != null) {
                spawnersConfig.set(p + ".specific_date", s.getSpecificDate().toString());
            }
        }

        spawnersConfig.set("natural", null);
        for (NaturalSpawner s : plugin.getSpawnerManager().getAllNaturalSpawners()) {
            String p = "natural." + s.getId();
            spawnersConfig.set(p + ".boss_id", s.getBossId());
            spawnersConfig.set(p + ".world", s.getWorldName());
            spawnersConfig.set(p + ".chance", s.getChance());
            spawnersConfig.set(p + ".min_distance", s.getMinPlayerDistance());
            spawnersConfig.set(p + ".max_distance", s.getMaxPlayerDistance());
            spawnersConfig.set(p + ".mob_cap", s.getMobCap());
            spawnersConfig.set(p + ".min_light", s.getMinLightLevel());
            spawnersConfig.set(p + ".max_light", s.getMaxLightLevel());
            spawnersConfig.set(p + ".interval", s.getIntervalSeconds());
            spawnersConfig.set(p + ".day_only", s.isDayOnly());
            spawnersConfig.set(p + ".night_only", s.isNightOnly());
            spawnersConfig.set(p + ".enabled", s.isEnabled());
        }

        try {
            spawnersConfig.save(spawnersFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save spawners.yml: " + e.getMessage());
        }
    }

    private void loadAltars() {
        altarsFile = new File(plugin.getDataFolder(), "altars.yml");
        if (!altarsFile.exists()) {
            plugin.saveResource("altars.yml", false);
        }
        altarsConfig = YamlConfiguration.loadConfiguration(altarsFile);

        ConfigurationSection sec = altarsConfig.getConfigurationSection("altars");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection(key);
            if (s == null) continue;
            String bossId = s.getString("boss_id", "titan");
            String world = s.getString("world", "world");
            int x = s.getInt("x", 0);
            int y = s.getInt("y", 64);
            int z = s.getInt("z", 0);
            Material mat = Material.matchMaterial(s.getString("base_material", "OBSIDIAN"));
            AltarProfile altar = new AltarProfile(key, bossId, world, x, y, z, mat);
            altar.setHologramTitle(s.getString("hologram_title", "&6&l[ ALTAR ] &eSacrifice to summon &c" + bossId));
            altar.setCooldownSeconds(s.getInt("cooldown", 120));
            altar.setRemoveBlockOnSummon(s.getBoolean("remove_block_on_summon", false));
            altar.setEnabled(s.getBoolean("enabled", true));

            List<?> reqList = s.getList("required_items");
            if (reqList != null) {
                for (Object obj : reqList) {
                    if (obj instanceof ItemStack is) {
                        altar.addRequiredItem(is);
                    }
                }
            }
            plugin.getAltarManager().addAltar(altar);
        }
    }

    public void saveAltars() {
        if (altarsConfig == null) return;
        altarsConfig.set("altars", null);
        for (AltarProfile a : plugin.getAltarManager().getAllAltars()) {
            String p = "altars." + a.getId();
            altarsConfig.set(p + ".boss_id", a.getBossId());
            altarsConfig.set(p + ".world", a.getWorldName());
            altarsConfig.set(p + ".x", a.getX());
            altarsConfig.set(p + ".y", a.getY());
            altarsConfig.set(p + ".z", a.getZ());
            altarsConfig.set(p + ".base_material", a.getBaseMaterial().name());
            altarsConfig.set(p + ".hologram_title", a.getHologramTitle());
            altarsConfig.set(p + ".cooldown", a.getCooldownSeconds());
            altarsConfig.set(p + ".remove_block_on_summon", a.isRemoveBlockOnSummon());
            altarsConfig.set(p + ".enabled", a.isEnabled());
            altarsConfig.set(p + ".required_items", a.getRequiredItems());
        }

        try {
            altarsConfig.save(altarsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save altars.yml: " + e.getMessage());
        }
    }
}
