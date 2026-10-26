package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.CoordinateSpawner;
import dev.lino.custombosses.model.NaturalSpawner;
import dev.lino.custombosses.model.RegionSpawner;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpawnerMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final List<RegionSpawner> regionList;
    private final List<CoordinateSpawner> coordList;
    private final List<NaturalSpawner> naturalList;

    public SpawnerMenu(CustomBossesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.regionList = new ArrayList<>(plugin.getSpawnerManager().getAllRegionSpawners());
        this.coordList = new ArrayList<>(plugin.getSpawnerManager().getAllCoordinateSpawners());
        this.naturalList = new ArrayList<>(plugin.getSpawnerManager().getAllNaturalSpawners());
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Automated Spawners"));
        build();
    }

    private void build() {
        inventory.clear();

        int slot = 0;
        for (RegionSpawner s : regionList) {
            if (slot >= 18) break;
            ItemStack item = new ItemBuilder(Material.COMPASS)
                    .name("&6[Region] &f" + s.getId())
                    .lore(
                            "&7Boss: &c" + s.getBossId(),
                            "&7World: &f" + s.getRegion().getWorldName(),
                            "&7Interval: &e" + s.getIntervalSeconds() + "s",
                            "&7Mob Cap: &b" + s.getMobCap(),
                            "&7Status: " + (s.isEnabled() ? "&aEnabled" : "&cDisabled"),
                            "",
                            "&eClick to toggle enable/disable"
                    )
                    .build();
            inventory.setItem(slot++, item);
        }

        slot = 18;
        for (CoordinateSpawner s : coordList) {
            if (slot >= 36) break;
            ItemStack item = new ItemBuilder(Material.RECOVERY_COMPASS)
                    .name("&d[Coord] &f" + s.getId())
                    .lore(
                            "&7Boss: &c" + s.getBossId(),
                            "&7Location: &f" + s.getWorldName() + " (" + (int) s.getX() + ", " + (int) s.getY() + ", " + (int) s.getZ() + ")",
                            "&7Interval: &e" + s.getIntervalSeconds() + "s",
                            "&7Status: " + (s.isEnabled() ? "&aEnabled" : "&cDisabled"),
                            "",
                            "&eClick to toggle enable/disable"
                    )
                    .build();
            inventory.setItem(slot++, item);
        }

        slot = 36;
        for (NaturalSpawner s : naturalList) {
            if (slot >= 45) break;
            ItemStack item = new ItemBuilder(Material.GRASS_BLOCK)
                    .name("&a[Natural] &f" + s.getId())
                    .lore(
                            "&7Boss: &c" + s.getBossId(),
                            "&7World: &f" + s.getWorldName(),
                            "&7Chance: &e" + s.getChance() + "%",
                            "&7Mob Cap: &b" + s.getMobCap(),
                            "&7Status: " + (s.isEnabled() ? "&aEnabled" : "&cDisabled"),
                            "",
                            "&eClick to toggle enable/disable"
                    )
                    .build();
            inventory.setItem(slot++, item);
        }

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack to Main Menu").build();
        inventory.setItem(49, back);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            new MainMenu(plugin, player).open();
            return;
        }

        if (slot >= 0 && slot < 18 && slot < regionList.size()) {
            RegionSpawner s = regionList.get(slot);
            s.setEnabled(!s.isEnabled());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        } else if (slot >= 18 && slot < 36 && (slot - 18) < coordList.size()) {
            CoordinateSpawner s = coordList.get(slot - 18);
            s.setEnabled(!s.isEnabled());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        } else if (slot >= 36 && slot < 45 && (slot - 36) < naturalList.size()) {
            NaturalSpawner s = naturalList.get(slot - 36);
            s.setEnabled(!s.isEnabled());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        }
    }
}
