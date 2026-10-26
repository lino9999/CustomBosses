package dev.lino.custombosses;

import dev.lino.custombosses.ability.AbilityManager;
import dev.lino.custombosses.command.CustomBossesCommand;
import dev.lino.custombosses.command.CustomBossesTabCompleter;
import dev.lino.custombosses.config.ConfigManager;
import dev.lino.custombosses.config.MessageManager;
import dev.lino.custombosses.gui.MenuListener;
import dev.lino.custombosses.integration.CustomBossesPAPI;
import dev.lino.custombosses.integration.WildStackerHook;
import dev.lino.custombosses.integration.WorldGuardHook;
import dev.lino.custombosses.listener.*;
import dev.lino.custombosses.manager.ActiveBossManager;
import dev.lino.custombosses.manager.AltarManager;
import dev.lino.custombosses.manager.BossManager;
import dev.lino.custombosses.manager.SpawnerManager;
import dev.lino.custombosses.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class CustomBossesPlugin extends JavaPlugin {

    private static CustomBossesPlugin instance;

    private MessageManager messageManager;
    private BossManager bossManager;
    private ActiveBossManager activeBossManager;
    private SpawnerManager spawnerManager;
    private AltarManager altarManager;
    private AbilityManager abilityManager;
    private ConfigManager configManager;
    private WildStackerHook wildStackerHook;

    private BukkitTask bossTickTask;
    private BukkitTask spawnerTickTask;

    public static CustomBossesPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Keys.init(this);
        WorldGuardHook.init();

        this.wildStackerHook = new WildStackerHook(this);
        this.wildStackerHook.register();

        this.messageManager = new MessageManager(this);
        this.bossManager = new BossManager(this);
        this.activeBossManager = new ActiveBossManager(this);
        this.spawnerManager = new SpawnerManager(this);
        this.altarManager = new AltarManager(this);
        this.abilityManager = new AbilityManager(this);
        this.configManager = new ConfigManager(this);

        this.configManager.loadAll();

        registerListeners();
        registerCommands();
        registerIntegrations();
        startTasks();

        restoreLoadedBosses();
        this.altarManager.refreshAllHolograms();

        getLogger().info("CustomBosses v" + getDescription().getVersion() + " by Lino successfully enabled.");
    }

    @Override
    public void onDisable() {
        if (bossTickTask != null) {
            bossTickTask.cancel();
        }
        if (spawnerTickTask != null) {
            spawnerTickTask.cancel();
        }

        if (altarManager != null) {
            altarManager.clearAllHolograms();
        }
        if (activeBossManager != null) {
            activeBossManager.removeAll(false);
        }

        getLogger().info("CustomBosses disabled cleanly.");
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new BossCombatListener(this), this);
        pm.registerEvents(new BossDeathListener(this), this);
        pm.registerEvents(new SpawnerEggListener(this), this);
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new AltarListener(this), this);
        pm.registerEvents(new ChunkListener(this), this);
        pm.registerEvents(new MenuListener(), this);
    }

    private void registerCommands() {
        PluginCommand cmd = getCommand("custombosses");
        if (cmd != null) {
            cmd.setExecutor(new CustomBossesCommand(this));
            cmd.setTabCompleter(new CustomBossesTabCompleter(this));
        }
    }

    private void registerIntegrations() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CustomBossesPAPI(this).register();
            getLogger().info("Hooked into PlaceholderAPI successfully.");
        }
    }

    private void startTasks() {
        this.bossTickTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (activeBossManager != null) {
                activeBossManager.tick();
            }
        }, 5L, 5L);

        this.spawnerTickTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (spawnerManager != null) {
                spawnerManager.tick();
            }
        }, 40L, 40L);
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public ActiveBossManager getActiveBossManager() {
        return activeBossManager;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public AltarManager getAltarManager() {
        return altarManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WildStackerHook getWildStackerHook() {
        return wildStackerHook;
    }

    private void restoreLoadedBosses() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.LivingEntity living : world.getLivingEntities()) {
                if (!living.isValid()) continue;
                org.bukkit.persistence.PersistentDataContainer pdc = living.getPersistentDataContainer();
                if (pdc.has(Keys.BOSS_ID, org.bukkit.persistence.PersistentDataType.STRING)) {
                    wildStackerHook.markUnstackable(living);
                    String bossId = pdc.get(Keys.BOSS_ID, org.bukkit.persistence.PersistentDataType.STRING);
                    if (bossId != null && activeBossManager.getActiveBoss(living.getUniqueId()) == null) {
                        dev.lino.custombosses.model.BossProfile profile = bossManager.getBoss(bossId);
                        if (profile != null) {
                            dev.lino.custombosses.model.ActiveBoss activeBoss = new dev.lino.custombosses.model.ActiveBoss(living, profile);
                            activeBossManager.registerBoss(activeBoss);
                            activeBoss.updateBossBar();
                        }
                    }
                }
            }
        }
    }
}
