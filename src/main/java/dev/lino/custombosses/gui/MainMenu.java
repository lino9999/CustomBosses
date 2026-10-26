package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MainMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;

    public MainMenu(CustomBossesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 36, TextUtil.colorize("&8CustomBosses Control Panel"));
        build();
    }

    private void build() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack bossesItem = new ItemBuilder(Material.NETHERITE_SWORD)
                .name("&c&lBoss Profiles")
                .lore(
                        "&7Manage all custom bosses,",
                        "&7attributes, drops and abilities.",
                        "",
                        "&eClick to open Boss Manager."
                )
                .build();
        inventory.setItem(11, bossesItem);

        ItemStack spawnersItem = new ItemBuilder(Material.SPAWNER)
                .name("&6&lAutomated Spawners")
                .lore(
                        "&7Configure natural, region,",
                        "&7and coordinate-based spawners.",
                        "",
                        "&eClick to open Spawner Manager."
                )
                .build();
        inventory.setItem(13, spawnersItem);

        ItemStack altarsItem = new ItemBuilder(Material.BEACON)
                .name("&d&lSacrificial Altars")
                .lore(
                        "&7View and configure world",
                        "&7altars and required sacrifices.",
                        "",
                        "&eClick to open Altar Manager."
                )
                .build();
        inventory.setItem(15, altarsItem);

        ItemStack wandItem = new ItemBuilder(Material.BLAZE_ROD)
                .name("&e&lGet Admin Wand")
                .lore(
                        "&7Receive wand for selecting",
                        "&7regions and quick boss spawning.",
                        "",
                        "&eClick to receive wand."
                )
                .build();
        inventory.setItem(29, wandItem);

        ItemStack monitorItem = new ItemBuilder(Material.ENDER_EYE)
                .name("&b&lActive Boss Monitor")
                .lore(
                        "&7Currently Active: &a" + plugin.getActiveBossManager().getAllActiveBosses().size(),
                        "",
                        "&cClick to kill all active bosses."
                )
                .build();
        inventory.setItem(31, monitorItem);

        ItemStack reloadItem = new ItemBuilder(Material.REDSTONE)
                .name("&a&lReload Configurations")
                .lore(
                        "&7Reloads all YML configs",
                        "&7and refreshes altars.",
                        "",
                        "&eClick to reload."
                )
                .build();
        inventory.setItem(33, reloadItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 11) {
            new BossListMenu(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else if (slot == 13) {
            new SpawnerMenu(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else if (slot == 15) {
            new AltarMenu(plugin, player).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } else if (slot == 29) {
            player.getInventory().addItem(plugin.getBossManager().createAdminWand());
            player.sendMessage(TextUtil.colorize("&aAdmin wand added to your inventory."));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        } else if (slot == 31) {
            int killed = plugin.getActiveBossManager().getAllActiveBosses().size();
            plugin.getActiveBossManager().removeAll(true);
            player.sendMessage(TextUtil.colorize("&aRemoved all &c" + killed + " &aactive bosses."));
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
            build();
        } else if (slot == 33) {
            plugin.getConfigManager().loadAll();
            player.sendMessage(TextUtil.colorize("&aCustomBosses configurations reloaded successfully!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            build();
        }
    }
}
