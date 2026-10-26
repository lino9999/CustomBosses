package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BossPickerMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final Consumer<BossProfile> callback;
    private final List<BossProfile> bossList;

    public BossPickerMenu(CustomBossesPlugin plugin, Player player, Consumer<BossProfile> callback) {
        this.plugin = plugin;
        this.player = player;
        this.callback = callback;
        this.bossList = new ArrayList<>(plugin.getBossManager().getAllBosses());
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Select a Boss"));
        build();
    }

    private void build() {
        for (int i = 0; i < bossList.size() && i < 45; i++) {
            BossProfile profile = bossList.get(i);
            ItemStack item = new ItemBuilder(Material.CREEPER_HEAD)
                    .name(profile.getDisplayName())
                    .lore(
                            "&7ID: &f" + profile.getId(),
                            "&7Health: &c" + profile.getMaxHealth(),
                            "&7Damage: &e" + profile.getAttackDamage(),
                            "",
                            "&aClick to select."
                    )
                    .build();
            inventory.setItem(i, item);
        }

        ItemStack close = new ItemBuilder(Material.BARRIER)
                .name("&cClose")
                .build();
        inventory.setItem(49, close);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        if (slot >= 0 && slot < bossList.size()) {
            BossProfile selected = bossList.get(slot);
            player.closeInventory();
            if (callback != null) {
                callback.accept(selected);
            }
        }
    }
}
