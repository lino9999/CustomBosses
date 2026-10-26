package dev.lino.custombosses.manager;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.util.Keys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveBossManager {

    private final CustomBossesPlugin plugin;
    private final Map<UUID, ActiveBoss> activeBosses;
    private long tickCounter;

    public ActiveBossManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
        this.activeBosses = new ConcurrentHashMap<>();
        this.tickCounter = 0L;
    }

    public void registerBoss(ActiveBoss activeBoss) {
        if (activeBoss == null) {
            return;
        }
        activeBosses.put(activeBoss.getEntityUniqueId(), activeBoss);
    }

    public ActiveBoss getActiveBoss(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return activeBosses.get(uuid);
    }

    public ActiveBoss getActiveBoss(Entity entity) {
        if (entity == null) {
            return null;
        }
        ActiveBoss direct = activeBosses.get(entity.getUniqueId());
        if (direct != null) {
            return direct;
        }
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (pdc.has(Keys.BOSS_ID, PersistentDataType.STRING)) {
            String bossId = pdc.get(Keys.BOSS_ID, PersistentDataType.STRING);
            if (entity instanceof LivingEntity living && bossId != null) {
                var profile = plugin.getBossManager().getBoss(bossId);
                if (profile != null) {
                    ActiveBoss reconstructed = new ActiveBoss(living, profile);
                    activeBosses.put(entity.getUniqueId(), reconstructed);
                    return reconstructed;
                }
            }
        }
        return null;
    }

    public Collection<ActiveBoss> getAllActiveBosses() {
        return Collections.unmodifiableCollection(activeBosses.values());
    }

    public int getActiveCountByProfile(String bossId) {
        int count = 0;
        for (ActiveBoss boss : activeBosses.values()) {
            if (boss.getProfile().getId().equalsIgnoreCase(bossId) && !boss.isDeadOrRemoved()) {
                count++;
            }
        }
        return count;
    }

    public void removeBoss(ActiveBoss activeBoss, boolean killEntity) {
        if (activeBoss == null) {
            return;
        }
        activeBoss.remove();
        activeBosses.remove(activeBoss.getEntityUniqueId());
        if (killEntity && activeBoss.getEntity() != null && activeBoss.getEntity().isValid()) {
            activeBoss.getEntity().remove();
        }
    }

    public void removeAll(boolean killEntities) {
        for (ActiveBoss boss : new ArrayList<>(activeBosses.values())) {
            removeBoss(boss, killEntities);
        }
        activeBosses.clear();
    }

    public void tick() {
        tickCounter++;
        boolean fullSecond = (tickCounter % 4 == 0);

        Iterator<Map.Entry<UUID, ActiveBoss>> iterator = activeBosses.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveBoss boss = iterator.next().getValue();
            LivingEntity entity = boss.getEntity();
            if (entity == null || !entity.isValid() || entity.isDead()) {
                boss.remove();
                iterator.remove();
                continue;
            }

            boss.tickAI();
            boss.tickAmbientEffects();

            if (fullSecond) {
                boss.updateBossBar();
                plugin.getAbilityManager().tickPeriodicAbilities(boss);
            }
        }
    }
}
