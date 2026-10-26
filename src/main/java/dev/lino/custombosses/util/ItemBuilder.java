package dev.lino.custombosses.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material != null ? material : Material.STONE, Math.max(1, amount));
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack != null ? itemStack.clone() : new ItemStack(Material.STONE);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (this.meta != null && name != null) {
            this.meta.setDisplayName(TextUtil.colorize(name));
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (this.meta != null && lore != null) {
            this.meta.setLore(TextUtil.colorize(lore));
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (this.meta != null && line != null) {
            List<String> current = this.meta.getLore();
            if (current == null) {
                current = new ArrayList<>();
            }
            current.add(TextUtil.colorize(line));
            this.meta.setLore(current);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.meta != null && enchantment != null) {
            this.meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags) {
        if (this.meta != null && flags != null) {
            this.meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder customModelData(Integer data) {
        if (this.meta != null && data != null) {
            this.meta.setCustomModelData(data);
        }
        return this;
    }

    public ItemBuilder stringData(NamespacedKey key, String value) {
        if (this.meta != null && key != null && value != null) {
            this.meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }
}
