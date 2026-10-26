package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.AltarProfile;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.Keys;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ChunkListener implements Listener {

    private final CustomBossesPlugin plugin;

    public ChunkListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof ArmorStand stand && stand.getPersistentDataContainer().has(Keys.ALTAR_ID, PersistentDataType.STRING)) {
                String altarId = stand.getPersistentDataContainer().get(Keys.ALTAR_ID, PersistentDataType.STRING);
                AltarProfile altar = plugin.getAltarManager().getAltar(altarId);
                if (altar == null) {
                    stand.remove();
                } else {
                    UUID existing = plugin.getAltarManager().getHologramStand(altarId);
                    if (existing == null) {
                        plugin.getAltarManager().registerHologramStand(altarId, stand.getUniqueId());
                    } else if (!existing.equals(stand.getUniqueId())) {
                        stand.remove();
                    }
                }
                continue;
            }

            if (!(entity instanceof LivingEntity living) || !living.isValid()) {
                continue;
            }
            PersistentDataContainer pdc = living.getPersistentDataContainer();
            if (pdc.has(Keys.BOSS_ID, PersistentDataType.STRING)) {
                plugin.getWildStackerHook().markUnstackable(living);
                String bossId = pdc.get(Keys.BOSS_ID, PersistentDataType.STRING);
                if (bossId != null && plugin.getActiveBossManager().getActiveBoss(living.getUniqueId()) == null) {
                    BossProfile profile = plugin.getBossManager().getBoss(bossId);
                    if (profile != null) {
                        ActiveBoss activeBoss = new ActiveBoss(living, profile);
                        plugin.getActiveBossManager().registerBoss(activeBoss);
                        activeBoss.updateBossBar();
                    }
                }
            }
        }
    }
}
