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

public class BossEditMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final BossProfile profile;

    public BossEditMenu(CustomBossesPlugin plugin, Player player, BossProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
        this.inventory = Bukkit.createInventory(this, 45, TextUtil.colorize("&8Editing: " + profile.getId()));
        build();
    }

    private void build() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack attributes = new ItemBuilder(Material.GOLDEN_APPLE)
                .name("&6&lAttributes")
                .lore(
                        "&7Health: &c" + profile.getMaxHealth(),
                        "&7Damage: &e" + profile.getAttackDamage(),
                        "&7Speed: &b" + profile.getMovementSpeed(),
                        "&7Scale: &a" + profile.getScale(),
                        "",
                        "&eClick to edit attributes."
                )
                .build();
        inventory.setItem(11, attributes);

        ItemStack abilities = new ItemBuilder(Material.BLAZE_POWDER)
                .name("&c&lAbilities (" + profile.getAbilities().size() + ")")
                .lore(
                        "&7Configure 38 unique boss abilities",
                        "&7including combat, periodic, and death skills.",
                        "",
                        "&eClick to manage abilities."
                )
                .build();
        inventory.setItem(13, abilities);

        ItemStack drops = new ItemBuilder(Material.CHEST)
                .name("&a&lDrops & Rewards (" + profile.getDrops().size() + ")")
                .lore(
                        "&7Configure ranked rewards,",
                        "&7killer drops, and reward commands.",
                        "",
                        "&eClick to manage drops."
                )
                .build();
        inventory.setItem(15, drops);

        ItemStack equipment = new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                .name("&b&lEquipment & Armor (" + profile.getEquipment().size() + ")")
                .lore(
                        "&7Configure boss armor, weapons,",
                        "&7off-hand items, and drop chances.",
                        "",
                        "&eClick to manage equipment."
                )
                .build();
        inventory.setItem(21, equipment);

        ItemStack spawnEgg = new ItemBuilder(Material.BLAZE_SPAWN_EGG)
                .name("&d&lGet Spawn Egg")
                .lore("&7Click to receive a spawn egg for &c" + profile.getId())
                .build();
        inventory.setItem(29, spawnEgg);

        ItemStack spawnHere = new ItemBuilder(Material.NETHER_STAR)
                .name("&e&lSpawn Boss Here")
                .lore("&7Click to spawn &c" + profile.getDisplayName() + " &7at your location.")
                .build();
        inventory.setItem(31, spawnHere);

        ItemStack save = new ItemBuilder(Material.EMERALD)
                .name("&a&lSave Changes to File")
                .lore("&7Click to persist changes to bosses.yml")
                .build();
        inventory.setItem(33, save);

        ItemStack back = new ItemBuilder(Material.ARROW).name("&eBack").build();
        inventory.setItem(36, back);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 36) {
            new BossListMenu(plugin, player).open();
            return;
        }

        if (slot == 11) {
            new AttributesMenu(plugin, player, profile).open();
        } else if (slot == 13) {
            new AbilitiesMenu(plugin, player, profile).open();
        } else if (slot == 15) {
            new DropsMenu(plugin, player, profile).open();
        } else if (slot == 21) {
            new EquipmentMenu(plugin, player, profile).open();
        } else if (slot == 29) {
            player.getInventory().addItem(plugin.getBossManager().createSpawnEgg(profile));
            player.sendMessage(TextUtil.colorize("&aReceived spawn egg for &c" + profile.getId()));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        } else if (slot == 31) {
            plugin.getBossManager().spawnBoss(profile, player.getLocation());
            player.sendMessage(TextUtil.colorize("&aSpawned &c" + profile.getDisplayName() + " &aat your location!"));
            player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
        } else if (slot == 33) {
            plugin.getConfigManager().saveBoss(profile);
            player.sendMessage(TextUtil.colorize("&aBoss profile &e" + profile.getId() + " &asaved to disk!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
    }
}
