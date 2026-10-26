package dev.lino.custombosses.gui;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.ability.AbilityType;
import dev.lino.custombosses.ability.BossAbility;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.util.ItemBuilder;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilitiesMenu extends MenuHolder {

    private final CustomBossesPlugin plugin;
    private final Player player;
    private final BossProfile profile;
    private final List<AbilityType> allTypes;

    public AbilitiesMenu(CustomBossesPlugin plugin, Player player, BossProfile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
        this.allTypes = Arrays.asList(AbilityType.values());
        this.inventory = Bukkit.createInventory(this, 54, TextUtil.colorize("&8Abilities: " + profile.getId()));
        build();
    }

    private void build() {
        for (int i = 0; i < allTypes.size() && i < 45; i++) {
            AbilityType type = allTypes.get(i);
            BossAbility existing = profile.getAbilities().get(type);
            boolean active = existing != null && existing.isEnabled();

            Material mat = active ? Material.LIME_DYE : Material.GRAY_DYE;
            ItemBuilder builder = new ItemBuilder(mat)
                    .name((active ? "&a✔ " : "&7✖ ") + type.getDisplayName())
                    .lore(
                            "&7Trigger: &e" + type.getDefaultTrigger().name(),
                            "&7Status: " + (active ? "&aActive" : "&cDisabled"),
                            active ? "&7Chance: &f" + existing.getChance() + "%" : "",
                            active ? "&7Cooldown: &f" + existing.getCooldownSeconds() + "s" : "",
                            active ? "&7Power/Value: &f" + existing.getValue() : "",
                            "",
                            "&eLeft-Click to toggle on/off",
                            "&bRight-Click to cycle cooldown (-5s / +5s)"
                    );

            inventory.setItem(i, builder.build());
        }

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

        if (slot >= 0 && slot < allTypes.size()) {
            AbilityType type = allTypes.get(slot);
            BossAbility ability = profile.getAbilities().get(type);

            if (event.isRightClick()) {
                if (ability != null && ability.isEnabled()) {
                    int newCd = ability.getCooldownSeconds() >= 30 ? 5 : ability.getCooldownSeconds() + 5;
                    ability.setCooldownSeconds(newCd);
                }
            } else {
                if (ability != null && ability.isEnabled()) {
                    profile.removeAbility(type);
                } else {
                    BossAbility newAbility = new BossAbility(type);
                    newAbility.setEnabled(true);
                    profile.addAbility(newAbility);
                }
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        }
    }
}
