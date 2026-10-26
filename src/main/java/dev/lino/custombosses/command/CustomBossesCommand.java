package dev.lino.custombosses.command;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.gui.MainMenu;
import dev.lino.custombosses.listener.WandListener;
import dev.lino.custombosses.model.AltarProfile;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.model.CuboidRegion;
import dev.lino.custombosses.model.RegionSpawner;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class CustomBossesCommand implements CommandExecutor {

    private final CustomBossesPlugin plugin;

    public CustomBossesCommand(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("menu") || sub.equals("gui")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.colorize("&cThis command can only be executed by players."));
                return true;
            }
            if (!player.hasPermission("custombosses.admin") && !player.hasPermission("custombosses.menu")) {
                plugin.getMessageManager().send(player, "no_permission");
                return true;
            }
            new MainMenu(plugin, player).open();
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.reload")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            plugin.getConfigManager().loadAll();
            sender.sendMessage(TextUtil.colorize("&a[CustomBosses] All configurations, bosses, altars and spawners reloaded!"));
            return true;
        }

        if (sub.equals("wand")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.colorize("&cThis command can only be executed by players."));
                return true;
            }
            if (!player.hasPermission("custombosses.admin") && !player.hasPermission("custombosses.wand")) {
                plugin.getMessageManager().send(player, "no_permission");
                return true;
            }
            player.getInventory().addItem(plugin.getBossManager().createAdminWand());
            player.sendMessage(TextUtil.colorize("&a[CustomBosses] Admin wand added to your inventory."));
            return true;
        }

        if (sub.equals("list")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.list")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            sender.sendMessage(TextUtil.colorize("&6&l--- Loaded Bosses (" + plugin.getBossManager().getAllBosses().size() + ") ---"));
            for (BossProfile profile : plugin.getBossManager().getAllBosses()) {
                sender.sendMessage(TextUtil.colorize("&7- &f" + profile.getId() + " &7(&c" + profile.getDisplayName() + "&7) HP: &a" + profile.getMaxHealth()));
            }
            return true;
        }

        if (sub.equals("killall")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.kill")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            int total = plugin.getActiveBossManager().getAllActiveBosses().size();
            plugin.getActiveBossManager().removeAll(true);
            sender.sendMessage(TextUtil.colorize("&a[CustomBosses] Successfully eliminated all &c" + total + " &aactive bosses."));
            return true;
        }

        if (sub.equals("spawn")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.spawn")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUsage: /" + label + " spawn <boss_id> [world x y z]"));
                return true;
            }
            BossProfile profile = plugin.getBossManager().getBoss(args[1]);
            if (profile == null) {
                sender.sendMessage(TextUtil.colorize("&cBoss profile not found: " + args[1]));
                return true;
            }

            Location spawnLoc;
            if (args.length >= 6) {
                World world = Bukkit.getWorld(args[2]);
                if (world == null) {
                    sender.sendMessage(TextUtil.colorize("&cWorld not found: " + args[2]));
                    return true;
                }
                try {
                    double x = Double.parseDouble(args[3]);
                    double y = Double.parseDouble(args[4]);
                    double z = Double.parseDouble(args[5]);
                    spawnLoc = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtil.colorize("&cInvalid coordinates specified."));
                    return true;
                }
            } else if (sender instanceof Player p) {
                spawnLoc = p.getLocation();
            } else {
                sender.sendMessage(TextUtil.colorize("&cConsole must specify world and coordinates!"));
                return true;
            }

            plugin.getBossManager().spawnBoss(profile, spawnLoc);
            sender.sendMessage(TextUtil.colorize("&aSpawned boss &c" + profile.getDisplayName() + " &aat &e" + spawnLoc.getWorld().getName() + " " + spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ()));
            return true;
        }

        if (sub.equals("spawnegg") || sub.equals("giveegg") || sub.equals("saveegg")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.egg")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUsage: /" + label + " " + sub + " <boss_id> [player] [amount]"));
                return true;
            }
            BossProfile profile = plugin.getBossManager().getBoss(args[1]);
            if (profile == null) {
                sender.sendMessage(TextUtil.colorize("&cBoss profile not found: " + args[1]));
                return true;
            }

            Player target = null;
            if (args.length >= 3) {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(TextUtil.colorize("&cPlayer not found: " + args[2]));
                    return true;
                }
            } else if (sender instanceof Player p) {
                target = p;
            } else {
                sender.sendMessage(TextUtil.colorize("&cConsole must specify a target player."));
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Math.max(1, Integer.parseInt(args[3]));
                } catch (NumberFormatException ignored) {}
            }

            ItemStack egg = plugin.getBossManager().createSpawnEgg(profile);
            egg.setAmount(amount);
            target.getInventory().addItem(egg);
            sender.sendMessage(TextUtil.colorize("&aGave &e" + amount + "x &aSpawn Egg for &c" + profile.getId() + " &ato &b" + target.getName()));
            return true;
        }

        if (sub.equals("altar")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.altar")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("create")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.colorize("&cOnly players can create altars at their target block."));
                    return true;
                }
                Block targetBlock = player.getTargetBlockExact(5);
                if (targetBlock == null) {
                    player.sendMessage(TextUtil.colorize("&cLook at a block within 5 blocks to create an altar."));
                    return true;
                }
                String altarId = args[2].toLowerCase(Locale.ROOT);
                String bossId = args[3].toLowerCase(Locale.ROOT);
                if (!plugin.getBossManager().hasBoss(bossId)) {
                    player.sendMessage(TextUtil.colorize("&cUnknown boss ID: " + bossId));
                    return true;
                }
                AltarProfile altar = new AltarProfile(altarId, bossId, targetBlock.getLocation());
                plugin.getAltarManager().addAltar(altar);
                plugin.getConfigManager().saveAltars();
                player.sendMessage(TextUtil.colorize("&aAltar &e" + altarId + " &acreated linking to boss &c" + bossId + "!"));
                return true;
            } else if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
                String altarId = args[2].toLowerCase(Locale.ROOT);
                plugin.getAltarManager().removeAltar(altarId);
                plugin.getConfigManager().saveAltars();
                sender.sendMessage(TextUtil.colorize("&aRemoved altar &e" + altarId));
                return true;
            } else {
                sender.sendMessage(TextUtil.colorize("&cUsage: /" + label + " altar <create|remove|list> ..."));
                return true;
            }
        }

        if (sub.equals("spawner")) {
            if (!sender.hasPermission("custombosses.admin") && !sender.hasPermission("custombosses.spawner")) {
                plugin.getMessageManager().send(sender, "no_permission");
                return true;
            }
            if (args.length >= 5 && args[1].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("region")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(TextUtil.colorize("&cOnly players can create region spawners with wand selection."));
                    return true;
                }
                Location p1 = WandListener.getPos1(player.getUniqueId());
                Location p2 = WandListener.getPos2(player.getUniqueId());
                if (p1 == null || p2 == null) {
                    player.sendMessage(TextUtil.colorize("&cUse the admin wand to set Position 1 and Position 2 first!"));
                    return true;
                }
                String spawnerId = args[3].toLowerCase(Locale.ROOT);
                String bossId = args[4].toLowerCase(Locale.ROOT);
                if (!plugin.getBossManager().hasBoss(bossId)) {
                    player.sendMessage(TextUtil.colorize("&cUnknown boss ID: " + bossId));
                    return true;
                }
                CuboidRegion region = new CuboidRegion(p1, p2);
                RegionSpawner spawner = new RegionSpawner(spawnerId, bossId, region);
                plugin.getSpawnerManager().addRegionSpawner(spawner);
                plugin.getConfigManager().saveSpawners();
                player.sendMessage(TextUtil.colorize("&aRegion spawner &e" + spawnerId + " &acreated for boss &c" + bossId + "!"));
                return true;
            } else if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
                String spawnerId = args[2].toLowerCase(Locale.ROOT);
                plugin.getSpawnerManager().removeRegionSpawner(spawnerId);
                plugin.getSpawnerManager().removeCoordinateSpawner(spawnerId);
                plugin.getSpawnerManager().removeNaturalSpawner(spawnerId);
                plugin.getConfigManager().saveSpawners();
                sender.sendMessage(TextUtil.colorize("&aRemoved spawner &e" + spawnerId));
                return true;
            } else {
                sender.sendMessage(TextUtil.colorize("&cUsage: /" + label + " spawner create region <id> <boss_id>"));
                return true;
            }
        }

        sendHelp(sender, label);
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(TextUtil.colorize("&6&l=== CustomBosses Commands ==="));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " menu &7- Open the interactive GUI control panel"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " spawn <boss_id> [world x y z] &7- Spawn a boss"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " spawnegg <boss_id> [player] [amount] &7- Give boss spawn egg"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " wand &7- Get the admin selection wand"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " altar create <id> <boss_id> &7- Create altar at target block"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " spawner create region <id> <boss_id> &7- Create region spawner"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " killall &7- Remove all currently active bosses"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " list &7- List all registered bosses"));
        sender.sendMessage(TextUtil.colorize("&e/" + label + " reload &7- Reload all configuration files"));
    }
}
