package dev.lino.custombosses.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    private static Plugin plugin;

    public static NamespacedKey BOSS_ID;
    public static NamespacedKey BOSS_UUID;
    public static NamespacedKey BOSS_OWNER;
    public static NamespacedKey SPAWNER_ID;
    public static NamespacedKey EGG_BOSS_ID;
    public static NamespacedKey WAND_MARKER;
    public static NamespacedKey ALTAR_ID;

    private Keys() {}

    public static void init(Plugin instance) {
        plugin = instance;
        BOSS_ID = new NamespacedKey(plugin, "boss_id");
        BOSS_UUID = new NamespacedKey(plugin, "boss_uuid");
        BOSS_OWNER = new NamespacedKey(plugin, "boss_owner");
        SPAWNER_ID = new NamespacedKey(plugin, "spawner_id");
        EGG_BOSS_ID = new NamespacedKey(plugin, "egg_boss_id");
        WAND_MARKER = new NamespacedKey(plugin, "wand_marker");
        ALTAR_ID = new NamespacedKey(plugin, "altar_id");
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
