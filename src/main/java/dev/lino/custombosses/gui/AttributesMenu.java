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
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class AttributesMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final BossProfile profile;

    public AttributesMenu(CustomBossesPlugin plugin, Player player, BossProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
        this.inventory = Bukkit.createInventory(this, 36, TextUtil.colorize("&8Attributes: " + profile.getId()));
        build();
    }

    private void build() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack health = new ItemBuilder(Material.RED_DYE)
                .name("&c&lMax Health: &f" + profile.getMaxHealth())
                .lore("&7Left-Click: +50 HP", "&7Right-Click: -50 HP")
                .build();
        inventory.setItem(10, health);

        ItemStack damage = new ItemBuilder(Material.IRON_SWORD)
                .name("&e&lAttack Damage: &f" + profile.getAttackDamage())
                .lore("&7Left-Click: +5 DMG", "&7Right-Click: -5 DMG")
                .build();
        inventory.setItem(12, damage);

        ItemStack speed = new ItemBuilder(Material.SUGAR)
                .name("&b&lSpeed: &f" + String.format(Locale.ROOT, "%.2f", profile.getMovementSpeed()))
                .lore("&7Left-Click: +0.05", "&7Right-Click: -0.05")
                .build();
        inventory.setItem(14, speed);

        ItemStack scale = new ItemBuilder(Material.SLIME_BALL)
                .name("&a&lScale: &f" + String.format(Locale.ROOT, "%.1f", profile.getScale()))
                .lore("&7Left-Click: +0.5", "&7Right-Click: -0.5")
                .build();
        inventory.setItem(16, scale);

        ItemStack glowing = new ItemBuilder(profile.isGlowing() ? Material.GLOWSTONE : Material.COBBLESTONE)
                .name("&e&lGlowing: &f" + profile.isGlowing())
                .lore("&7Click to toggle")
                .build();
        inventory.setItem(19, glowing);

        ItemStack silent = new ItemBuilder(profile.isSilent() ? Material.NOTE_BLOCK : Material.COBBLESTONE)
                .name("&7&lSilent: &f" + profile.isSilent())
                .lore("&7Click to toggle")
                .build();
        inventory.setItem(21, silent);

        ItemStack ai = new ItemBuilder(profile.isAi() ? Material.ZOMBIE_HEAD : Material.COBBLESTONE)
                .name("&6&lAI Enabled: &f" + profile.isAi())
                .lore("&7Click to toggle")
                .build();
        inventory.setItem(23, ai);

        ItemStack despawn = new ItemBuilder(profile.isPreventDespawn() ? Material.NAME_TAG : Material.COBBLESTONE)
                .name("&d&lPrevent Despawn: &f" + profile.isPreventDespawn())
                .lore("&7Click to toggle")
                .build();
        inventory.setItem(25, despawn);

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack").build();
        inventory.setItem(31, back);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 31) {
            new BossEditMenu(plugin, player, profile).open();
            return;
        }

        boolean rightClick = event.isRightClick();
        if (slot == 10) {
            profile.setMaxHealth(profile.getMaxHealth() + (rightClick ? -50 : 50));
        } else if (slot == 12) {
            profile.setAttackDamage(profile.getAttackDamage() + (rightClick ? -5 : 5));
        } else if (slot == 14) {
            profile.setMovementSpeed(profile.getMovementSpeed() + (rightClick ? -0.05 : 0.05));
        } else if (slot == 16) {
            profile.setScale(profile.getScale() + (rightClick ? -0.5 : 0.5));
        } else if (slot == 19) {
            profile.setGlowing(!profile.isGlowing());
        } else if (slot == 21) {
            profile.setSilent(!profile.isSilent());
        } else if (slot == 23) {
            profile.setAi(!profile.isAi());
        } else if (slot == 25) {
            profile.setPreventDespawn(!profile.isPreventDespawn());
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        build();
    }
}
