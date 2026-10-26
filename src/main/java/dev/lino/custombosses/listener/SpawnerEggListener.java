package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.Keys;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerEggListener implements Listener {

    private final CustomBossesPlugin plugin;

    public SpawnerEggListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(Keys.EGG_BOSS_ID, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!player.hasPermission("custombosses.egg.use") && !player.hasPermission("custombosses.admin")) {
            player.sendMessage(TextUtil.colorize("&cYou do not have permission to use CustomBosses spawn eggs."));
            return;
        }

        String bossId = pdc.get(Keys.EGG_BOSS_ID, PersistentDataType.STRING);
        BossProfile profile = plugin.getBossManager().getBoss(bossId);
        if (profile == null) {
            player.sendMessage(TextUtil.colorize("&cInvalid boss ID encoded in egg: " + bossId));
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        Location spawnLoc = clicked.getRelative(event.getBlockFace()).getLocation().add(0.5, 0.0, 0.5);

        if (!dev.lino.custombosses.integration.WorldGuardHook.isSpawningAllowed(spawnLoc)) {
            player.sendMessage(TextUtil.colorize("&cMob spawning is not allowed in this protected region."));
            return;
        }

        plugin.getBossManager().spawnBoss(profile, spawnLoc);

        spawnLoc.getWorld().spawnParticle(Particle.PORTAL, spawnLoc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        player.sendMessage(TextUtil.colorize("&aSpawned boss &c" + profile.getDisplayName() + "&a!"));
    }
}
