package dev.lino.custombosses.command;

import dev.lino.custombosses.CustomBossesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CustomBossesTabCompleter implements TabCompleter {

    private final CustomBossesPlugin plugin;
    private static final List<String> ROOT_SUBS = Arrays.asList(
            "menu", "gui", "spawn", "spawnegg", "giveegg", "saveegg",
            "wand", "altar", "spawner", "killall", "list", "reload", "help"
    );

    public CustomBossesTabCompleter(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], ROOT_SUBS, completions);
            Collections.sort(completions);
            return completions;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("spawn") || sub.equals("spawnegg") || sub.equals("giveegg") || sub.equals("saveegg")) {
            if (args.length == 2) {
                StringUtil.copyPartialMatches(args[1], plugin.getBossManager().getBossIds(), completions);
                Collections.sort(completions);
                return completions;
            }
            if (args.length == 3 && (sub.equals("spawnegg") || sub.equals("giveegg"))) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                StringUtil.copyPartialMatches(args[2], playerNames, completions);
                Collections.sort(completions);
                return completions;
            }
        }

        if (sub.equals("altar")) {
            if (args.length == 2) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("create", "remove", "list"), completions);
                Collections.sort(completions);
                return completions;
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("create")) {
                StringUtil.copyPartialMatches(args[3], plugin.getBossManager().getBossIds(), completions);
                Collections.sort(completions);
                return completions;
            }
        }

        if (sub.equals("spawner")) {
            if (args.length == 2) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("create", "remove", "list"), completions);
                Collections.sort(completions);
                return completions;
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("create")) {
                StringUtil.copyPartialMatches(args[2], Arrays.asList("region", "coordinate", "natural"), completions);
                Collections.sort(completions);
                return completions;
            }
            if (args.length == 5 && args[1].equalsIgnoreCase("create")) {
                StringUtil.copyPartialMatches(args[4], plugin.getBossManager().getBossIds(), completions);
                Collections.sort(completions);
                return completions;
            }
        }

        return completions;
    }
}
