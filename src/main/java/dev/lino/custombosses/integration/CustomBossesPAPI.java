package dev.lino.custombosses.integration;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.BossProfile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Locale;

public class CustomBossesPAPI extends PlaceholderExpansion {

    private final CustomBossesPlugin plugin;

    public CustomBossesPAPI(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "custombosses";
    }

    @Override
    public String getAuthor() {
        return "Lino";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null) {
            return "";
        }
        String p = params.toLowerCase(Locale.ROOT);

        if (p.equals("active")) {
            return String.valueOf(plugin.getActiveBossManager().getAllActiveBosses().size());
        }
        if (p.equals("total")) {
            return String.valueOf(plugin.getBossManager().getAllBosses().size());
        }
        if (p.startsWith("boss_active_")) {
            String bossId = p.substring("boss_active_".length());
            return String.valueOf(plugin.getActiveBossManager().getActiveCountByProfile(bossId));
        }
        if (p.startsWith("boss_health_")) {
            String bossId = p.substring("boss_health_".length());
            for (ActiveBoss boss : plugin.getActiveBossManager().getAllActiveBosses()) {
                if (boss.getProfile().getId().equalsIgnoreCase(bossId) && boss.getEntity().isValid()) {
                    return String.format(Locale.ROOT, "%.1f", boss.getEntity().getHealth());
                }
            }
            return "0.0";
        }

        return null;
    }
}
