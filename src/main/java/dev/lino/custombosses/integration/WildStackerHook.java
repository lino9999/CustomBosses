package dev.lino.custombosses.integration;

import dev.lino.custombosses.CustomBossesPlugin;
import dev.lino.custombosses.util.Keys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;

public class WildStackerHook implements Listener {

    private final CustomBossesPlugin plugin;

    private static final String[] STACK_EVENT_CLASSES = {
            "com.bgsoftware.wildstacker.api.events.EntityStackEvent",
            "dev.rosewood.rosestacker.event.EntityStackEvent",
            "uk.antiperson.stackmob.api.events.EntityStackEvent",
            "com.kiilas.ultimatestacker.events.EntityStackEvent"
    };

    public WildStackerHook(CustomBossesPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        for (String className : STACK_EVENT_CLASSES) {
            try {
                Class<?> clazz = Class.forName(className);
                if (Event.class.isAssignableFrom(clazz)) {
                    Class<? extends Event> eventClass = clazz.asSubclass(Event.class);
                    Bukkit.getPluginManager().registerEvent(
                            eventClass,
                            this,
                            EventPriority.HIGHEST,
                            (listener, event) -> handleStackEvent(event),
                            plugin
                    );
                    plugin.getLogger().info("Hooked into entity stacker event: " + className);
                }
            } catch (ClassNotFoundException ignored) {}
        }
    }

    private void handleStackEvent(Event event) {
        if (!(event instanceof Cancellable cancellable)) {
            return;
        }

        try {
            Entity e1 = extractEntity(event, "getEntity");
            Entity e2 = extractEntity(event, "getTarget");

            if (isCustomBoss(e1) || isCustomBoss(e2)) {
                cancellable.setCancelled(true);
            }
        } catch (Throwable ignored) {}
    }

    private Entity extractEntity(Event event, String methodName) {
        try {
            Method m = event.getClass().getMethod(methodName);
            Object obj = m.invoke(event);
            if (obj instanceof Entity entity) {
                return entity;
            }
            if (obj != null) {
                try {
                    Method getLiv = obj.getClass().getMethod("getLivingEntity");
                    Object liv = getLiv.invoke(obj);
                    if (liv instanceof Entity entity) {
                        return entity;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public boolean isCustomBoss(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getPersistentDataContainer().has(Keys.BOSS_ID, PersistentDataType.STRING)) {
            return true;
        }
        if (entity.hasMetadata("custombosses_boss") || entity.hasMetadata("wildstacker_unstackable")) {
            return true;
        }
        return plugin.getActiveBossManager().getActiveBoss(entity.getUniqueId()) != null;
    }

    public void markUnstackable(LivingEntity entity) {
        if (entity == null) {
            return;
        }

        FixedMetadataValue meta = new FixedMetadataValue(plugin, true);
        entity.setMetadata("wildstacker_unstackable", meta);
        entity.setMetadata("unstackable", meta);
        entity.setMetadata("no-stack", meta);
        entity.setMetadata("nostack", meta);
        entity.setMetadata("custombosses_boss", meta);

        try {
            NamespacedKey wsKey = new NamespacedKey("wildstacker", "unstackable");
            entity.getPersistentDataContainer().set(wsKey, PersistentDataType.BYTE, (byte) 1);
        } catch (Throwable ignored) {}

        try {
            NamespacedKey smKey = new NamespacedKey("stackmob", "no-stack");
            entity.getPersistentDataContainer().set(smKey, PersistentDataType.BYTE, (byte) 1);
        } catch (Throwable ignored) {}

        try {
            NamespacedKey rsKey = new NamespacedKey("rosestacker", "unstackable");
            entity.getPersistentDataContainer().set(rsKey, PersistentDataType.BYTE, (byte) 1);
        } catch (Throwable ignored) {}

        try {
            Class<?> apiClass = Class.forName("com.bgsoftware.wildstacker.api.WildStackerAPI");
            Method getStackedEntityMethod = apiClass.getMethod("getStackedEntity", LivingEntity.class);
            Object stackedEntity = getStackedEntityMethod.invoke(null, entity);
            if (stackedEntity != null) {
                try {
                    Method setAmount = stackedEntity.getClass().getMethod("setStackAmount", int.class);
                    setAmount.invoke(stackedEntity, 1);
                } catch (NoSuchMethodException ignored) {}
                try {
                    Method setUnstackable = stackedEntity.getClass().getMethod("setUnstackable", boolean.class);
                    setUnstackable.invoke(stackedEntity, true);
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
