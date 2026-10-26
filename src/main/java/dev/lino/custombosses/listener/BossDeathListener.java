package dev.lino.custombosses.listener;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.model.ActiveBoss;
import dev.lino.custombosses.model.BossDrop;
import dev.lino.custombosses.model.BossProfile;
import dev.lino.custombosses.model.RewardType;
import dev.lino.custombosses.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BossDeathListener implements Listener {

    private final CustomBossesPlugin plugin;

    public BossDeathListener(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        ActiveBoss boss = plugin.getActiveBossManager().getActiveBoss(event.getEntity());
        if (boss == null) {
            return;
        }

        BossProfile profile = boss.getProfile();
        Player killer = event.getEntity().getKiller();

        boolean resurrected = plugin.getAbilityManager().handleDeath(boss, killer);
        if (resurrected) {
            return;
        }

        boss.setDeadOrRemoved(true);

        if (profile.isClearVanillaDrops()) {
            event.getDrops().clear();
        }
        event.setDroppedExp(profile.getXpDrop());

        List<Map.Entry<UUID, Double>> ranking = new ArrayList<>(boss.getDamageDealers().entrySet());
        ranking.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        if (killer == null && !ranking.isEmpty()) {
            killer = Bukkit.getPlayer(ranking.get(0).getKey());
        }

        Location dropLoc = event.getEntity().getLocation();
        distributeRewards(profile, killer, ranking, dropLoc);

        String broadcastMsg = plugin.getMessageManager().getRaw("boss_defeated")
                .replace("%boss%", profile.getDisplayName())
                .replace("%killer%", killer != null ? killer.getName() : "Unknown");
        Bukkit.broadcastMessage(TextUtil.colorize(plugin.getMessageManager().getPrefix() + broadcastMsg));

        plugin.getActiveBossManager().removeBoss(boss, false);
    }

    private void distributeRewards(BossProfile profile, Player killer, List<Map.Entry<UUID, Double>> ranking, Location dropLoc) {
        Player top1 = ranking.size() > 0 ? Bukkit.getPlayer(ranking.get(0).getKey()) : null;
        Player top2 = ranking.size() > 1 ? Bukkit.getPlayer(ranking.get(1).getKey()) : null;
        Player top3 = ranking.size() > 2 ? Bukkit.getPlayer(ranking.get(2).getKey()) : null;

        for (Map.Entry<EquipmentSlot, ItemStack> entry : profile.getEquipment().entrySet()) {
            if (entry.getValue() == null || entry.getValue().getType() == Material.AIR) {
                continue;
            }
            float chance = profile.getDropChances().getOrDefault(entry.getKey(), 0.0f);
            if (chance > 0.0f && ThreadLocalRandom.current().nextFloat() <= chance) {
                if (dropLoc != null && dropLoc.getWorld() != null) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, entry.getValue().clone());
                }
            }
        }

        for (BossDrop drop : profile.getDrops()) {
            if (drop == null) continue;
            RewardType type = drop.getRewardType();
            double roll = ThreadLocalRandom.current().nextDouble(0.0, 100.0);
            if (roll > drop.getChance()) {
                continue;
            }

            ItemStack item = drop.getItem();
            ItemStack rewardItem = null;
            if (item != null && item.getType() != Material.AIR) {
                int min = Math.max(1, Math.min(drop.getMinAmount(), drop.getMaxAmount()));
                int max = Math.max(min, Math.max(drop.getMinAmount(), drop.getMaxAmount()));
                int amount = min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
                rewardItem = item.clone();
                rewardItem.setAmount(amount);
            }

            switch (type) {
                case KILLER -> {
                    giveReward(killer, rewardItem, drop.getCommands(), profile, dropLoc);
                }
                case TOP_1 -> {
                    giveReward(top1, rewardItem, drop.getCommands(), profile, dropLoc);
                }
                case TOP_2 -> {
                    giveReward(top2, rewardItem, drop.getCommands(), profile, dropLoc);
                }
                case TOP_3 -> {
                    giveReward(top3, rewardItem, drop.getCommands(), profile, dropLoc);
                }
                case ALL_CONTRIBUTORS -> {
                    for (Map.Entry<UUID, Double> entry : ranking) {
                        Player p = Bukkit.getPlayer(entry.getKey());
                        if (p != null && p.isOnline()) {
                            giveReward(p, rewardItem != null ? rewardItem.clone() : null, drop.getCommands(), profile, dropLoc);
                        }
                    }
                }
                case CHANCE_DROP -> {
                    if (rewardItem != null && dropLoc.getWorld() != null) {
                        dropLoc.getWorld().dropItemNaturally(dropLoc, rewardItem);
                    }
                    dispatchCommands(killer, drop.getCommands(), profile, dropLoc);
                }
            }
        }
    }

    private void giveReward(Player player, ItemStack item, List<String> commands, BossProfile profile, Location dropLoc) {
        if (player != null && player.isOnline()) {
            if (item != null) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack left : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
            dispatchCommands(player, commands, profile, dropLoc);
        } else {
            if (item != null && dropLoc.getWorld() != null) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            }
            dispatchCommands(null, commands, profile, dropLoc);
        }
    }

    private void dispatchCommands(Player player, List<String> commands, BossProfile profile, Location dropLoc) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        String pName = player != null ? player.getName() : "Unknown";
        String worldName = (dropLoc != null && dropLoc.getWorld() != null) ? dropLoc.getWorld().getName() : "";
        int x = dropLoc != null ? dropLoc.getBlockX() : 0;
        int y = dropLoc != null ? dropLoc.getBlockY() : 0;
        int z = dropLoc != null ? dropLoc.getBlockZ() : 0;
        for (String cmd : commands) {
            String processed = cmd
                    .replace("%player%", pName)
                    .replace("%killer%", pName)
                    .replace("%boss%", profile.getDisplayName())
                    .replace("%world%", worldName)
                    .replace("%x%", String.valueOf(x))
                    .replace("%y%", String.valueOf(y))
                    .replace("%z%", String.valueOf(z));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
        }
    }
}
