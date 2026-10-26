package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.gui.BossPickerMenu;
import dev.lino.custombosses.util.Keys;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Location;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WandListener implements Listener {

    private final CustomBossesPlugin plugin;
    private static final Map<UUID, Location> POS1_MAP = new HashMap<>();
    private static final Map<UUID, Location> POS2_MAP = new HashMap<>();

    public WandListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    public static Location getPos1(UUID uuid) {
        return POS1_MAP.get(uuid);
    }

    public static Location getPos2(UUID uuid) {
        return POS2_MAP.get(uuid);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(Keys.WAND_MARKER, PersistentDataType.STRING)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("custombosses.wand") && !player.hasPermission("custombosses.admin")) {
            return;
        }

        event.setCancelled(true);

        if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            new BossPickerMenu(plugin, player, (chosenProfile) -> {
                Location target = event.getClickedBlock() != null
                        ? event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0.0, 0.5)
                        : player.getLocation();
                plugin.getBossManager().spawnBoss(chosenProfile, target);
                player.sendMessage(TextUtil.colorize("&aSpawned &c" + chosenProfile.getDisplayName() + " &aat your target location!"));
            }).open();
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            POS1_MAP.put(player.getUniqueId(), clicked.getLocation());
            player.sendMessage(TextUtil.colorize("&6&l[ WAND ] &aPosition 1 set to: &e" + clicked.getX() + ", " + clicked.getY() + ", " + clicked.getZ()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            POS2_MAP.put(player.getUniqueId(), clicked.getLocation());
            player.sendMessage(TextUtil.colorize("&6&l[ WAND ] &aPosition 2 set to: &e" + clicked.getX() + ", " + clicked.getY() + ", " + clicked.getZ()));
        }
    }
}
