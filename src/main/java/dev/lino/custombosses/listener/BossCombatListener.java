package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.BossProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.util.Vector;

public class BossCombatListener implements Listener {

    private final CustomBossesPlugin plugin;

    public BossCombatListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss != null) {
            LivingEntity target = event.getTarget();
            if (target != null && (!(target instanceof Player player) || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
                event.setCancelled(true);
                event.setTarget(null);
                if (boss.getEntity() instanceof Mob mob) {
                    Player nearest = findNearestSurvivalPlayer(mob.getLocation(), boss.getProfile().getFollowRange());
                    if (nearest != null) {
                        mob.setTarget(nearest);
                    }
                }
            }
            return;
        }

        ActiveBoss targetBoss = plugin.getActiveBossManager().getActiveBoss(event.getTarget());
        if (targetBoss != null && !(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            event.setTarget(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss == null) {
            return;
        }

        BossProfile profile = boss.getProfile();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (profile.isImmuneFall() && (cause == EntityDamageEvent.DamageCause.FALL)) {
            event.setCancelled(true);
            return;
        }
        if (profile.isImmuneFire() && (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR)) {
            event.setCancelled(true);
            return;
        }
        if (profile.isImmuneDrown() && (cause == EntityDamageEvent.DamageCause.DROWNING)) {
            event.setCancelled(true);
            return;
        }
        if (profile.isImmuneExplosion() && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            event.setCancelled(true);
            return;
        }
        if (profile.isImmuneSuffocation() && (cause == EntityDamageEvent.DamageCause.SUFFOCATION
                || cause == EntityDamageEvent.DamageCause.CRAMMING)) {
            event.setCancelled(true);
            return;
        }

        double remainingHp = boss.getEntity().getHealth() - event.getFinalDamage();
        if (remainingHp <= 0.0) {
            if (plugin.getAbilityManager().handlePhoenixResurrection(boss)) {
                event.setCancelled(true);
                return;
            }
        } else {
            plugin.getAbilityManager().checkLowHealthThreshold(boss, remainingHp);
        }

        boss.updateBossBar();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        Entity realDamager = damager;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
            realDamager = shooter;
        }

        ActiveBoss attackerBoss = plugin.getActiveBossManager().getActiveBoss(realDamager);
        if (attackerBoss != null && victim instanceof LivingEntity livingVictim) {
            plugin.getAbilityManager().handleAttack(attackerBoss, livingVictim);
        }

        ActiveBoss victimBoss = plugin.getActiveBossManager().getActiveBoss(victim);
        if (victimBoss != null) {
            if (victimBoss.getProfile().isImmuneProjectile() && damager instanceof Projectile) {
                event.setCancelled(true);
                return;
            }

            if (victimBoss.getProfile().isImmuneKnockback()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (victimBoss.getEntity() != null && victimBoss.getEntity().isValid()) {
                        victimBoss.getEntity().setVelocity(new Vector(0, 0, 0));
                    }
                });
            }

            if (realDamager instanceof Player player) {
                victimBoss.registerDamage(player.getUniqueId(), event.getFinalDamage());
            } else if (victimBoss.getEntity() instanceof Mob mob) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (mob.isValid()) {
                        Player nearest = findNearestSurvivalPlayer(mob.getLocation(), victimBoss.getProfile().getFollowRange());
                        mob.setTarget(nearest);
                    }
                });
            }

            plugin.getAbilityManager().handleDamaged(victimBoss, realDamager, event.getDamage());
            victimBoss.updateBossBar();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss != null) {
            boss.updateBossBar();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSlimeSplit(SlimeSplitEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss != null) {
            event.blockList().clear();
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
}
