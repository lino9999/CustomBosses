package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.AltarProfile;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AltarMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final List<AltarProfile> altars;

    public AltarMenu(CustomBossesPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.altars = new ArrayList<>(plugin.getAltarManager().getAllAltars());
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Sacrificial Altars"));
        build();
    }

    private void build() {
        inventory.clear();

        for (int i = 0; i < altars.size() && i < 45; i++) {
            AltarProfile altar = altars.get(i);
            ItemStack item = new ItemBuilder(altar.getBaseMaterial())
                    .name("&6&lAltar: &f" + altar.getId())
                    .lore(
                            "&7Boss: &c" + altar.getBossId(),
                            "&7Location: &f" + altar.getWorldName() + " (" + altar.getX() + ", " + altar.getY() + ", " + altar.getZ() + ")",
                            "&7Cooldown: &e" + altar.getCooldownSeconds() + "s",
                            "&7Sacrifices: &b" + altar.getRequiredItems().size() + " items",
                            "&7Status: " + (altar.isEnabled() ? "&aEnabled" : "&cDisabled"),
                            "",
                            "&eLeft-Click: Teleport to Altar",
                            "&bRight-Click: Toggle Enabled/Disabled"
                    )
                    .build();
            inventory.setItem(i, item);
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

        if (slot >= 0 && slot < altars.size()) {
            AltarProfile altar = altars.get(slot);
            if (event.isRightClick()) {
                altar.setEnabled(!altar.isEnabled());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                build();
            } else {
                Location loc = altar.getLocation();
                if (loc != null) {
                    player.teleport(loc.add(0.5, 1.0, 0.5));
                    player.sendMessage(TextUtil.colorize("&aTeleported to altar &e" + altar.getId()));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            }
        }
    }
}
