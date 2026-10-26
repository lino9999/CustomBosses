package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.BossDrop;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.model.RewardType;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DropsMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final BossProfile profile;

    public DropsMenu(CustomBossesPlugin plugin, Player player, BossProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Drops: " + profile.getId()));
        build();
    }

    private void build() {
        inventory.clear();
        List<BossDrop> drops = profile.getDrops();

        for (int i = 0; i < drops.size() && i < 45; i++) {
            BossDrop drop = drops.get(i);
            ItemBuilder builder = new ItemBuilder(drop.getItem().clone())
                    .addLore("&8-----------------------")
                    .addLore("&7Reward Type: &e" + drop.getRewardType().name())
                    .addLore("&7Drop Chance: &a" + drop.getChance() + "%")
                    .addLore("&7Amount: &f" + drop.getMinAmount() + " - " + drop.getMaxAmount())
                    .addLore("&7Commands: &d" + drop.getCommands().size())
                    .addLore("")
                    .addLore("&eLeft-Click: Cycle Reward Type")
                    .addLore("&bRight-Click: Change Chance (+10% / -10%)")
                    .addLore("&cShift-Right-Click: Remove Drop");

            inventory.setItem(i, builder.build());
        }

        ItemStack addDrop = new ItemBuilder(Material.EMERALD)
                .name("&a&lAdd Held Item as Drop")
                .lore("&7Click with an item in your cursor/hand to add it.")
                .build();
        inventory.setItem(48, addDrop);

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack").build();
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
            new BossEditMenu(plugin, player, profile).open();
            return;
        }

        if (slot == 48) {
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand != null && inHand.getType() != Material.AIR) {
                BossDrop newDrop = new BossDrop(inHand.clone(), 50.0);
                profile.getDrops().add(newDrop);
                player.sendMessage(TextUtil.colorize("&aAdded &e" + inHand.getType().name() + " &aas boss drop!"));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                build();
            } else {
                player.sendMessage(TextUtil.colorize("&cHold an item in your main hand to add as drop."));
            }
            return;
        }

        List<BossDrop> drops = profile.getDrops();
        if (slot >= 0 && slot < drops.size()) {
            BossDrop drop = drops.get(slot);

            if (event.isShiftClick() && event.isRightClick()) {
                drops.remove(slot);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else if (event.isRightClick()) {
                double newChance = drop.getChance() >= 100.0 ? 10.0 : drop.getChance() + 10.0;
                drop.setChance(newChance);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            } else {
                RewardType[] types = RewardType.values();
                int nextIndex = (drop.getRewardType().ordinal() + 1) % types.length;
                drop.setRewardType(types[nextIndex]);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            build();
        }
    }
}
