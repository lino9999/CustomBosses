package dev.lino.custombosses.model;

import dev.lino.custombosses.ability.AbilityType;
import dev.lino.custombosses.ability.BossAbility;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class ActiveBoss {

    private static final Set<EntityType> PASSIVE_TYPES = EnumSet.of(
            EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.PIG,
            EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.VILLAGER,
            EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.RABBIT, EntityType.LLAMA,
            EntityType.FOX, EntityType.PANDA, EntityType.TURTLE, EntityType.CAT, EntityType.OCELOT
    );

    private final UUID entityUniqueId;
    private final LivingEntity entity;
    private final BossProfile profile;
    private final BossBar bossBar;
    private final Map<UUID, Double> damageDealers;
    private final Map<AbilityType, BossAbility> abilities;
    private final long spawnedAtMillis;
    private boolean deadOrRemoved;
    private boolean transformed;
    private long lastPassiveAttackMillis;

    public ActiveBoss(LivingEntity entity, BossProfile profile) {
        this.entityUniqueId = entity.getUniqueId();
        this.entity = entity;
        this.profile = profile;
        this.damageDealers = new HashMap<>();
        this.spawnedAtMillis = System.currentTimeMillis();
        this.deadOrRemoved = false;
        this.transformed = false;
        this.lastPassiveAttackMillis = 0L;

        this.abilities = new EnumMap<>(AbilityType.class);
        for (Map.Entry<AbilityType, BossAbility> entry : profile.getAbilities().entrySet()) {
            BossAbility copy = new BossAbility(entry.getKey());
            copy.setEnabled(entry.getValue().isEnabled());
            copy.setTrigger(entry.getValue().getTrigger());
            copy.setChance(entry.getValue().getChance());
            copy.setCooldownSeconds(entry.getValue().getCooldownSeconds());
            copy.setRange(entry.getValue().getRange());
            copy.setValue(entry.getValue().getValue());
            copy.setStringValue(entry.getValue().getStringValue());
            copy.setDurationTicks(entry.getValue().getDurationTicks());
            copy.setMessage(entry.getValue().getMessage());
            this.abilities.put(entry.getKey(), copy);
        }

        if (profile.isBossBarEnabled()) {
            this.bossBar = Bukkit.createBossBar(
                    formatBossBarTitle(),
                    profile.getBossBarColor(),
                    profile.getBossBarStyle()
            );
            this.bossBar.setProgress(1.0);
            this.bossBar.setVisible(true);
        } else {
            this.bossBar = null;
        }
    }

    public UUID getEntityUniqueId() {
        return entityUniqueId;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public BossProfile getProfile() {
        return profile;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public Map<UUID, Double> getDamageDealers() {
        return damageDealers;
    }

    public Map<AbilityType, BossAbility> getAbilities() {
        return abilities;
    }

    public long getSpawnedAtMillis() {
        return spawnedAtMillis;
    }

    public boolean isDeadOrRemoved() {
        return deadOrRemoved;
    }

    public void setDeadOrRemoved(boolean deadOrRemoved) {
        this.deadOrRemoved = deadOrRemoved;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public void setTransformed(boolean transformed) {
        this.transformed = transformed;
    }

    public void registerDamage(UUID playerUniqueId, double damage) {
        if (playerUniqueId == null || damage <= 0.0) {
            return;
        }
        damageDealers.merge(playerUniqueId, damage, Double::sum);
    }

    public void tickAmbientEffects() {
        if (entity == null || !entity.isValid()) return;
        Particle p = profile.getAmbientParticle();
        if (p != null) {
            int count = Math.max(1, profile.getAmbientParticleCount());
            Location loc = entity.getLocation().add(0, 1.0, 0);
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(p, loc, count, 0.4, 0.6, 0.4, 0.02);
            }
        }
    }

    public void tickAI() {
        if (entity == null || !entity.isValid() || !profile.isAi()) {
            return;
        }

        if (entity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();
            if (target != null && (!(target instanceof Player player) || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || !player.isValid())) {
                mob.setTarget(null);
                target = null;
            }

            if (target == null) {
                Player nearest = findNearestSurvivalPlayer(mob.getLocation(), profile.getFollowRange());
                if (nearest != null) {
                    mob.setTarget(nearest);
                    target = nearest;
                }
            }

            if (PASSIVE_TYPES.contains(profile.getEntityType()) && profile.getEntityType() != EntityType.IRON_GOLEM) {
                tickPassiveAggro(mob, target);
            }
        }
    }

    private void tickPassiveAggro(Mob mob, LivingEntity target) {
        if (!(target instanceof Player nearest)) {
            return;
        }

        Location loc = mob.getLocation();
        Vector diff = nearest.getLocation().toVector().subtract(loc.toVector());
        if (diff.lengthSquared() > 0.0001) {
            Vector direction = diff.normalize();
            direction.setY(0.1);
            mob.setVelocity(direction.multiply(profile.getMovementSpeed() * 1.8));

            double distSq = nearest.getLocation().distanceSquared(loc);
            if (distSq <= 3.24) {
                long now = System.currentTimeMillis();
                if ((now - lastPassiveAttackMillis) >= 1000L) {
                    lastPassiveAttackMillis = now;
                    nearest.damage(profile.getAttackDamage(), mob);
                    Vector push = direction.clone().multiply(0.4);
                    push.setY(0.3);
                    nearest.setVelocity(push);
                    if (loc.getWorld() != null) {
                        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                    }
                }
            }
        }
    }

    private Player findNearestSurvivalPlayer(Location loc, double range) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        Player nearest = null;
        double bestDistSq = range * range;
        for (Player p : loc.getWorld().getPlayers()) {
            if (!p.isValid() || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            double distSq = p.getLocation().distanceSquared(loc);
            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }

    public void updateBossBar() {
        if (bossBar == null || entity == null || !entity.isValid()) {
            return;
        }
        Attribute maxAttr = resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        AttributeInstance maxHealthAttr = maxAttr != null ? entity.getAttribute(maxAttr) : null;
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : profile.getMaxHealth();
        double currentHealth = Math.max(0.0, Math.min(entity.getHealth(), maxHealth));
        double progress = Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));

        bossBar.setTitle(formatBossBarTitle());
        bossBar.setProgress(progress);

        Location loc = entity.getLocation();
        double rangeSq = profile.getBossBarRange() * profile.getBossBarRange();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getWorld().equals(loc.getWorld()) && online.getLocation().distanceSquared(loc) <= rangeSq) {
                if (!bossBar.getPlayers().contains(online)) {
                    bossBar.addPlayer(online);
                }
            } else {
                bossBar.removePlayer(online);
            }
        }
    }

    private String formatBossBarTitle() {
        double currentHealth = entity != null ? entity.getHealth() : profile.getMaxHealth();
        Attribute maxAttr = resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        AttributeInstance maxHealthAttr = (entity != null && maxAttr != null) ? entity.getAttribute(maxAttr) : null;
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : profile.getMaxHealth();

        String title = profile.getBossBarTitle()
                .replace("%boss%", profile.getDisplayName())
                .replace("%health%", String.format(Locale.ROOT, "%.1f", currentHealth))
                .replace("%max_health%", String.format(Locale.ROOT, "%.1f", maxHealth));
        return TextUtil.colorize(title);
    }

    public void remove() {
        this.deadOrRemoved = true;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
    }

    public static Attribute resolveAttribute(String... names) {
        for (String name : names) {
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }
}
