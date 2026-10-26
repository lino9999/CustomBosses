package dev.lino.custombosses.ability;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AbilityManager {

    private final CustomBossesPlugin plugin;

    public AbilityManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    public void tickPeriodicAbilities(ActiveBoss boss) {
        if (boss == null || boss.isDeadOrRemoved()) {
            return;
        }
        LivingEntity entity = boss.getEntity();
        if (entity == null || !entity.isValid()) {
            return;
        }

        for (BossAbility ability : boss.getAbilities().values()) {
            if (ability.getTrigger() != AbilityTrigger.PERIODIC || !ability.canExecuteNow()) {
                continue;
            }
            ability.recordExecution();
            if (rollChance(ability.getChance())) {
                executePeriodic(boss, ability);
            }
        }
    }

    private void executePeriodic(ActiveBoss boss, BossAbility ability) {
        LivingEntity entity = boss.getEntity();
        Location loc = entity.getLocation();
        double range = ability.getRange();

        switch (ability.getType()) {
            case PROJECTILE_THROW -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Vector direction = target.getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector()).normalize();
                String typeStr = ability.getStringValue().toUpperCase(Locale.ROOT);
                Class<? extends Projectile> projClass = Arrow.class;
                if (typeStr.contains("WITHER")) projClass = WitherSkull.class;
                else if (typeStr.contains("FIREBALL")) projClass = Fireball.class;
                else if (typeStr.contains("SNOW")) projClass = Snowball.class;
                else if (typeStr.contains("EGG")) projClass = Egg.class;
                Projectile proj = entity.launchProjectile(projClass, direction.multiply(1.5));
                proj.setShooter(entity);
                loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            }
            case KILL_AURA -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Location behind = target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(1.2));
                behind.setDirection(target.getLocation().subtract(behind).toVector());
                entity.teleport(behind);
                target.damage(ability.getValue(), entity);
                damageArmor(target, 5);
                loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 3);
                loc.getWorld().playSound(behind, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.8f);
            }
            case TELEPORT -> {
                Player target = getNearestPlayer(loc, range);
                if (target != null) {
                    Location dest = target.getLocation().add(ThreadLocalRandom.current().nextDouble(-3, 3), 0, ThreadLocalRandom.current().nextDouble(-3, 3));
                    entity.teleport(dest);
                    loc.getWorld().spawnParticle(Particle.PORTAL, dest.add(0, 1, 0), 25);
                    loc.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            }
            case VACUUM -> {
                List<Entity> nearby = entity.getNearbyEntities(range, range, range);
                int absorbed = 0;
                for (Entity e : nearby) {
                    if (e instanceof Item item) {
                        item.teleport(loc);
                        item.remove();
                        absorbed++;
                    }
                }
                if (absorbed > 0) {
                    healEntity(entity, absorbed * ability.getValue());
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 15);
                    loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                }
            }
            case DRAIN -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        p.setFoodLevel(Math.max(0, p.getFoodLevel() - (int) ability.getValue()));
                        p.sendMessage(TextUtil.colorize("&c" + boss.getProfile().getDisplayName() + " &cdrained your energy!"));
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 20);
                }
            }
            case COMMAND_DISPATCH -> {
                String cmd = ability.getStringValue();
                if (!cmd.isEmpty()) {
                    Player target = getNearestPlayer(loc, range);
                    String playerPlaceholder = target != null ? target.getName() : "";
                    String processed = cmd.replace("%player%", playerPlaceholder)
                            .replace("%boss%", boss.getProfile().getDisplayName())
                            .replace("%world%", loc.getWorld().getName())
                            .replace("%x%", String.valueOf(loc.getBlockX()))
                            .replace("%y%", String.valueOf(loc.getBlockY()))
                            .replace("%z%", String.valueOf(loc.getBlockZ()));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
                }
            }
            case GETSUGA -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Vector dir = target.getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector()).normalize();
                WitherSkull skull = entity.launchProjectile(WitherSkull.class, dir.multiply(1.8));
                skull.setCharged(true);
                skull.setShooter(entity);
                loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc.add(0, 1, 0), 15);
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.7f);
            }
            case LEAP -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Vector dir = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                dir.setY(0.45);
                entity.setVelocity(dir.multiply(1.3));
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
            }
            case WEB_TRAP -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Location pLoc = target.getLocation().getBlock().getLocation();
                List<Block> changed = new ArrayList<>();
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block b = pLoc.clone().add(x, 0, z).getBlock();
                        if (b.getType().isAir()) {
                            b.setType(Material.COBWEB);
                            changed.add(b);
                        }
                    }
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Block b : changed) {
                        if (b.getType() == Material.COBWEB) {
                            b.setType(Material.AIR);
                        }
                    }
                }, Math.max(20, ability.getDurationTicks()));
                loc.getWorld().playSound(pLoc, Sound.BLOCK_COBWEB_PLACE, 1.0f, 0.8f);
            }
            case PULL -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        Vector pullVec = loc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2);
                        pullVec.setY(0.3);
                        p.setVelocity(pullVec);
                        p.damage(ability.getValue(), entity);
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 30);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                }
            }
            case VELOCITY_REVERSAL -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        p.setVelocity(p.getVelocity().multiply(-1.5));
                    }
                }
            }
            case SCATTER_BLAST -> {
                for (int i = 0; i < 4; i++) {
                    double ox = ThreadLocalRandom.current().nextDouble(-range, range);
                    double oz = ThreadLocalRandom.current().nextDouble(-range, range);
                    Location blastLoc = loc.clone().add(ox, 0, oz);
                    loc.getWorld().createExplosion(blastLoc, (float) ability.getValue(), false, false);
                }
            }
            case REGENERATION -> {
                healEntity(entity, ability.getValue());
                loc.getWorld().spawnParticle(Particle.HEART, loc.add(0, 1.5, 0), 5);
                loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
            }
            case FREEZE_ZONE -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ability.getDurationTicks(), 2));
                        p.setFreezeTicks(Math.max(p.getFreezeTicks(), ability.getDurationTicks()));
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc.clone().add(0, 1, 0), 40, range / 2, 0.5, range / 2, 0.05);
                }
            }
            case TOXIC_FOG -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, ability.getDurationTicks(), 1));
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0, 1, 0), 35, range / 2, 0.5, range / 2, 0.05);
                }
            }
            case HEAT_WAVE -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        p.setFireTicks(ability.getDurationTicks());
                        p.damage(ability.getValue(), entity);
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 45, range / 2, 0.5, range / 2, 0.05);
                    loc.getWorld().playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);
                }
            }
            case ZEUS -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        if (loc.getWorld() != null) {
                            loc.getWorld().strikeLightning(p.getLocation());
                        }
                        p.damage(ability.getValue(), entity);
                    }
                }
            }
            case TELEPORT_SWAP -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                Location pLoc = target.getLocation();
                target.teleport(loc);
                entity.teleport(pLoc);
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 20);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                }
                if (pLoc.getWorld() != null) {
                    pLoc.getWorld().spawnParticle(Particle.PORTAL, pLoc.clone().add(0, 1, 0), 20);
                }
            }
            case POWER_WAVE -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        Vector push = p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.5);
                        push.setY(0.4);
                        p.setVelocity(push);
                        p.damage(ability.getValue(), entity);
                        damageArmor(p, 4);
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 0.5, 0), 10, range / 2, 0.2, range / 2, 0.05);
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                }
            }
            case SOUL_MARK -> {
                Player target = getNearestPlayer(loc, range);
                if (target == null) return;
                target.sendMessage(TextUtil.colorize("&c&lSOUL MARK! &7Move away from &c" + boss.getProfile().getDisplayName() + " &7or take massive damage!"));
                if (target.getWorld() != null) {
                    target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getLocation().add(0, 1, 0), 10);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (target.isOnline() && entity.isValid() && target.getLocation().distanceSquared(entity.getLocation()) <= range * range) {
                        target.damage(ability.getValue(), entity);
                        if (target.getWorld() != null) {
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.8f);
                        }
                    }
                }, 60L);
            }
            case EARTHQUAKE -> {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player p && isValidTarget(p)) {
                        Vector up = new Vector(0, 0.8, 0);
                        p.setVelocity(up);
                        p.damage(ability.getValue(), entity);
                    }
                }
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.BLOCK, loc.clone().add(0, 0.2, 0), 60, range / 2, 0.1, range / 2, Material.DIRT.createBlockData());
                    loc.getWorld().playSound(loc, Sound.BLOCK_STONE_BREAK, 1.5f, 0.6f);
                }
            }
            default -> {}
        }

        if (ability.getMessage() != null && !ability.getMessage().isEmpty()) {
            broadcastAbilityMessage(entity, range, ability.getMessage());
        }
    }

    public void handleAttack(ActiveBoss boss, LivingEntity victim) {
        if (boss == null || victim == null) return;
        LivingEntity entity = boss.getEntity();

        for (BossAbility ability : boss.getAbilities().values()) {
            if (ability.getTrigger() != AbilityTrigger.ON_ATTACK || !ability.canExecuteNow()) continue;
            ability.recordExecution();
            if (!rollChance(ability.getChance())) continue;

            switch (ability.getType()) {
                case FREEZE_ATTACK -> {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ability.getDurationTicks(), 2));
                    victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), ability.getDurationTicks()));
                }
                case TOSS_ATTACK -> {
                    victim.setVelocity(new Vector(0, 1.1, 0));
                }
                case LIFESTEAL_ATTACK -> {
                    healEntity(entity, ability.getValue());
                    entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0, 1.5, 0), 3);
                }
                case WITCHCRAFT_ATTACK -> {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ability.getDurationTicks(), 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ability.getDurationTicks(), 1));
                }
                case EXECUTE_THRESHOLD -> {
                    Attribute maxAttr = ActiveBoss.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
                    AttributeInstance max = maxAttr != null ? victim.getAttribute(maxAttr) : null;
                    double maxHp = max != null ? max.getValue() : 20.0;
                    double currentPercent = (victim.getHealth() / maxHp) * 100.0;
                    if (currentPercent <= ability.getValue()) {
                        victim.setHealth(0.0);
                    }
                }
                case TARGET_CLEANSE -> {
                    for (PotionEffect effect : victim.getActivePotionEffects()) {
                        victim.removePotionEffect(effect.getType());
                    }
                }
                default -> {}
            }
        }
    }

    public void handleDamaged(ActiveBoss boss, Entity damager, double damage) {
        if (boss == null || damager == null) return;
        LivingEntity entity = boss.getEntity();

        for (BossAbility ability : boss.getAbilities().values()) {
            if (ability.getTrigger() != AbilityTrigger.ON_DAMAGED || !ability.canExecuteNow()) continue;
            ability.recordExecution();
            if (!rollChance(ability.getChance())) continue;

            switch (ability.getType()) {
                case THORNS -> {
                    if (damager instanceof LivingEntity liv && liv.getLocation().distanceSquared(entity.getLocation()) <= 16.0) {
                        liv.damage(ability.getValue(), entity);
                    }
                }
                case QUICK_BUFF -> {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ability.getDurationTicks(), 1));
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ability.getDurationTicks(), 1));
                }
                case COUNTER_HIT -> {
                    if (damager instanceof LivingEntity liv) {
                        liv.damage(damage * (ability.getValue() / 100.0), entity);
                    }
                }
                case CLEANSE -> {
                    for (PotionEffect effect : entity.getActivePotionEffects()) {
                        entity.removePotionEffect(effect.getType());
                    }
                }
                default -> {}
            }
        }

        checkLowHealthThreshold(boss, entity.getHealth());
    }

    public boolean handlePhoenixResurrection(ActiveBoss boss) {
        if (boss == null || boss.getEntity() == null || !boss.getEntity().isValid()) {
            return false;
        }
        BossAbility ability = boss.getAbilities().get(AbilityType.PHOENIX_IMMORTAL);
        if (ability == null || !ability.isEnabled() || !ability.canExecuteNow()) {
            return false;
        }
        if (!rollChance(ability.getChance())) {
            return false;
        }
        ability.recordExecution();
        LivingEntity entity = boss.getEntity();
        double restoreHp = boss.getProfile().getMaxHealth();
        entity.setHealth(restoreHp);
        Location loc = entity.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2);
            loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 0.8f);
        }
        if (ability.getMessage() != null && !ability.getMessage().isEmpty()) {
            broadcastAbilityMessage(entity, ability.getRange(), ability.getMessage());
        }
        boss.updateBossBar();
        return true;
    }

    public void checkLowHealthThreshold(ActiveBoss boss, double remainingHp) {
        if (boss == null || boss.isTransformed()) return;
        LivingEntity entity = boss.getEntity();
        if (entity == null || !entity.isValid()) return;
        Attribute maxAttr = ActiveBoss.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        AttributeInstance max = maxAttr != null ? entity.getAttribute(maxAttr) : null;
        double maxHp = max != null ? max.getValue() : boss.getProfile().getMaxHealth();
        double percent = (remainingHp / maxHp) * 100.0;

        BossAbility transformAbility = boss.getAbilities().get(AbilityType.TRANSFORM);
        if (transformAbility != null && transformAbility.isEnabled() && percent <= transformAbility.getValue()) {
            boss.setTransformed(true);
            String nextBossId = transformAbility.getStringValue();
            Location loc = entity.getLocation();
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, 1, 0), 3);
                loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.2f, 0.7f);
            }
            plugin.getActiveBossManager().removeBoss(boss, false);
            entity.remove();

            BossProfile nextProfile = plugin.getBossManager().getBoss(nextBossId);
            if (nextProfile != null) {
                plugin.getBossManager().spawnBoss(nextProfile, loc);
            }
        }
    }

    public boolean handleDeath(ActiveBoss boss, Player killer) {
        if (boss == null) return false;
        LivingEntity entity = boss.getEntity();
        Location loc = entity.getLocation();

        for (BossAbility ability : boss.getAbilities().values()) {
            if (ability.getTrigger() != AbilityTrigger.ON_DEATH || !ability.canExecuteNow()) continue;
            ability.recordExecution();
            if (!rollChance(ability.getChance())) continue;

            switch (ability.getType()) {
                case PHOENIX_IMMORTAL -> {
                    entity.setHealth(boss.getProfile().getMaxHealth());
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
                    boss.setDeadOrRemoved(false);
                    return true;
                }
                case SHADOW_CLONE -> {
                    for (int i = 0; i < 2; i++) {
                        Location cloneLoc = loc.clone().add(ThreadLocalRandom.current().nextDouble(-2, 2), 0, ThreadLocalRandom.current().nextDouble(-2, 2));
                        Entity clone = loc.getWorld().spawnEntity(cloneLoc, boss.getProfile().getEntityType());
                        if (clone instanceof LivingEntity liv) {
                            liv.setCustomName(TextUtil.colorize(boss.getProfile().getDisplayName() + " &7(Clone)"));
                            liv.setCustomNameVisible(true);
                        }
                    }
                }
                case MINIONS -> {
                    String minionBossId = ability.getStringValue();
                    BossProfile minionProfile = plugin.getBossManager().getBoss(minionBossId);
                    int count = Math.max(1, (int) ability.getValue());
                    for (int i = 0; i < count; i++) {
                        Location mLoc = loc.clone().add(ThreadLocalRandom.current().nextDouble(-2, 2), 0, ThreadLocalRandom.current().nextDouble(-2, 2));
                        if (minionProfile != null) {
                            plugin.getBossManager().spawnBoss(minionProfile, mLoc);
                        }
                    }
                }
                case LAST_EFFECT -> {
                    if (killer != null) {
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ability.getDurationTicks(), 1));
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ability.getDurationTicks(), 1));
                    }
                }
                case LAST_DEATH_EXPLOSION -> {
                    loc.getWorld().spawn(loc, TNTPrimed.class);
                }
                case FATAL_CURSE -> {
                    if (killer != null) {
                        killer.setHealth(0.0);
                        killer.sendMessage(TextUtil.colorize("&4&lFATAL CURSE! &c" + boss.getProfile().getDisplayName() + " dragged you to the grave!"));
                    }
                }
                default -> {}
            }
        }
        return false;
    }

    private Player getNearestPlayer(Location loc, double range) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        Player nearest = null;
        double bestDistSq = range * range;
        for (Player p : loc.getWorld().getPlayers()) {
            if (!isValidTarget(p)) {
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

    private boolean isValidTarget(Player p) {
        return p != null && p.isValid() && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR;
    }

    private void healEntity(LivingEntity entity, double amount) {
        Attribute maxAttr = ActiveBoss.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        AttributeInstance max = maxAttr != null ? entity.getAttribute(maxAttr) : null;
        double maxHp = max != null ? max.getValue() : 100.0;
        entity.setHealth(Math.min(maxHp, entity.getHealth() + amount));
    }

    private void damageArmor(Player player, int durabilityDamage) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType().getMaxDurability() > 0) {
                armor.setDurability((short) Math.min(armor.getType().getMaxDurability(), armor.getDurability() + durabilityDamage));
            }
        }
    }

    private void broadcastAbilityMessage(LivingEntity entity, double range, String message) {
        String colored = TextUtil.colorize(message);
        for (Entity e : entity.getNearbyEntities(range, range, range)) {
            if (e instanceof Player p && isValidTarget(p)) {
                p.sendMessage(colored);
            }
        }
    }

    private boolean rollChance(double chance) {
        if (chance >= 100.0) return true;
        if (chance <= 0.0) return false;
        return ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= chance;
    }
}
