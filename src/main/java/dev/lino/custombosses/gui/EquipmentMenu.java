package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EquipmentMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final BossProfile profile;
    private final Map<Integer, EquipmentSlot> slotMap;

    public EquipmentMenu(CustomBossesPlugin plugin, Player player, BossProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
        this.slotMap = new HashMap<>();
        this.slotMap.put(10, EquipmentSlot.HEAD);
        this.slotMap.put(19, EquipmentSlot.CHEST);
        this.slotMap.put(28, EquipmentSlot.LEGS);
        this.slotMap.put(37, EquipmentSlot.FEET);
        this.slotMap.put(23, EquipmentSlot.HAND);
        this.slotMap.put(25, EquipmentSlot.OFF_HAND);
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Equipment: " + profile.getId()));
        build();
    }

    private void build() {
        inventory.clear();
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        for (Map.Entry<Integer, EquipmentSlot> entry : slotMap.entrySet()) {
            int invSlot = entry.getKey();
            EquipmentSlot eqSlot = entry.getValue();
            ItemStack equipped = profile.getEquipment().get(eqSlot);
            float chance = profile.getDropChances().getOrDefault(eqSlot, 0.0f);
            String chanceStr = String.format(Locale.ROOT, "%.0f%%", chance * 100.0f);

            if (equipped != null && equipped.getType() != Material.AIR) {
                ItemStack display = new ItemBuilder(equipped.clone())
                        .addLore("&8-----------------------")
                        .addLore("&7Slot: &e" + formatSlot(eqSlot))
                        .addLore("&7Drop Chance: &a" + chanceStr)
                        .addLore("")
                        .addLore("&eClick with item on cursor to replace")
                        .addLore("&bRight-Click: Change drop chance (+10%)")
                        .addLore("&cShift-Right-Click: Remove equipment")
                        .build();
                inventory.setItem(invSlot, display);
            } else {
                Material icon = getPlaceholder(eqSlot);
                ItemStack display = new ItemBuilder(icon)
                        .name("&eEmpty " + formatSlot(eqSlot) + " Slot")
                        .lore(
                                "&7Drop Chance: &a" + chanceStr,
                                "",
                                "&eClick with item on cursor to equip",
                                "&bRight-Click: Change drop chance (+10%)"
                        )
                        .build();
                inventory.setItem(invSlot, display);
            }
        }

        ItemStack guide = new ItemBuilder(Material.ARMOR_STAND)
                .name("&6&lEquipment & Armor Guide")
                .lore(
                        "&7Equip weapons and armor onto the boss.",
                        "",
                        "&fLeft: &7Helmet, Chest, Legs, Boots",
                        "&fRight: &7Main Hand & Off Hand",
                        "",
                        "&eClick with an item on cursor to equip.",
                        "&bRight-Click slot: Change drop chance.",
                        "&cShift-Right-Click: Remove item."
                )
                .build();
        inventory.setItem(14, guide);

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack").build();
        inventory.setItem(49, back);
    }

    private Material getPlaceholder(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Material.CHAINMAIL_HELMET;
            case CHEST -> Material.CHAINMAIL_CHESTPLATE;
            case LEGS -> Material.CHAINMAIL_LEGGINGS;
            case FEET -> Material.CHAINMAIL_BOOTS;
            case HAND -> Material.IRON_SWORD;
            case OFF_HAND -> Material.SHIELD;
            default -> Material.BARRIER;
        };
    }

    private String formatSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "Helmet";
            case CHEST -> "Chestplate";
            case LEGS -> "Leggings";
            case FEET -> "Boots";
            case HAND -> "Main Hand";
            case OFF_HAND -> "Off Hand";
            default -> slot.name();
        };
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

        if (!slotMap.containsKey(slot)) {
            return;
        }

        EquipmentSlot eqSlot = slotMap.get(slot);
        ItemStack cursor = event.getCursor();

        if (cursor != null && cursor.getType() != Material.AIR) {
            profile.setEquipment(eqSlot, cursor.clone());
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
            player.sendMessage(TextUtil.colorize("&aEquipped &e" + cursor.getType().name() + " &ain &e" + formatSlot(eqSlot) + "&a."));
            build();
            return;
        }

        if (event.isShiftClick() && event.isRightClick()) {
            profile.setEquipment(eqSlot, null);
            profile.setDropChance(eqSlot, 0.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage(TextUtil.colorize("&cCleared equipment in &e" + formatSlot(eqSlot) + "&c."));
            build();
            return;
        }

        if (event.isRightClick()) {
            float current = profile.getDropChances().getOrDefault(eqSlot, 0.0f);
            current += 0.10f;
            if (current > 1.01f) {
                current = 0.0f;
            }
            float rounded = Math.round(current * 100.0f) / 100.0f;
            profile.setDropChance(eqSlot, rounded);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        }
    }
}
