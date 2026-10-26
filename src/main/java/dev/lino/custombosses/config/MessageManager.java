package dev.lino.custombosses.config;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final CustomBossesPlugin plugin;
    private final Map<String, String> messages;
    private String prefix;

    public MessageManager(CustomBossesPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.prefix = "&8[&cCustomBosses&8] &r";
    }

    public void load(FileConfiguration config) {
        messages.clear();
        this.prefix = TextUtil.colorize(config.getString("prefix", "&8[&cCustomBosses&8] &r"));
        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key));
            }
        }
    }

    public String getRaw(String key) {
        return messages.getOrDefault(key, key);
    }

    public String get(String key) {
        return prefix + TextUtil.colorize(getRaw(key));
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String msg = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace(entry.getKey(), entry.getValue());
            }
        }
        sender.sendMessage(msg);
    }

    public String getPrefix() {
        return prefix;
    }
}
