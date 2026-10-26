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

public class BossListMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final List<BossProfile> bosses;

    public BossListMenu(CustomBossesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.bosses = new ArrayList<>(plugin.getBossManager().getAllBosses());
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Boss Profiles"));
        build();
    }

    private void build() {
        for (int i = 0; i < bosses.size() && i < 45; i++) {
            BossProfile profile = bosses.get(i);
            ItemStack item = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                    .name(profile.getDisplayName())
                    .lore(
                            "&7ID: &f" + profile.getId(),
                            "&7Entity: &f" + profile.getEntityType().name(),
                            "&7Health: &c" + profile.getMaxHealth(),
                            "&7Damage: &e" + profile.getAttackDamage(),
                            "&7Abilities: &b" + profile.getAbilities().size(),
                            "&7Drops: &a" + profile.getDrops().size(),
                            "",
                            "&eClick to edit this boss."
                    )
                    .build();
            inventory.setItem(i, item);
        }

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack to Main Menu").build();
        inventory.setItem(45, back);

        ItemStack close = new ItemBuilder(Material.BARRIER).name("&cClose").build();
        inventory.setItem(49, close);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 45) {
            new MainMenu(plugin, player).open();
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot >= 0 && slot < bosses.size()) {
            BossProfile profile = bosses.get(slot);
            new BossEditMenu(plugin, player, profile).open();
        }
    }
}
