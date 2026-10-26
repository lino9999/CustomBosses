package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.AltarProfile;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AltarListener implements Listener {

    private final CustomBossesPlugin plugin;

    public AltarListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();
        if (plugin.getAltarManager().getAltarAt(clicked.getLocation()) != null) {
            event.setCancelled(true);
            plugin.getAltarManager().interactAltar(player, clicked);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        AltarProfile altar = plugin.getAltarManager().getAltarAt(block.getLocation());
        if (altar == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("custombosses.admin")) {
            plugin.getAltarManager().removeAltar(altar.getId());
            plugin.getConfigManager().saveAltars();
            player.sendMessage(TextUtil.colorize("&6&l[ ALTAR ] &eBoss altar '&6" + altar.getId() + "&e' was removed."));
        } else {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cThis boss altar is protected!"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getAltarManager().getAltarAt(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getAltarManager().getAltarAt(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getAltarManager().getAltarAt(b.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getAltarManager().getAltarAt(b.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
