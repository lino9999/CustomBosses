package dev.lino.custombosses.ability;

public enum AbilityType {
    TRANSFORM("Transform", AbilityTrigger.LOW_HEALTH),
    PROJECTILE_THROW("Projectile Throw", AbilityTrigger.PERIODIC),
    KILL_AURA("Kill Aura", AbilityTrigger.PERIODIC),
    TELEPORT("Teleport", AbilityTrigger.PERIODIC),
    VACUUM("Vacuum Items", AbilityTrigger.PERIODIC),
    DRAIN("Hunger Drain", AbilityTrigger.PERIODIC),
    COMMAND_DISPATCH("Command Dispatch", AbilityTrigger.PERIODIC),
    GETSUGA("Getsuga Tenshou", AbilityTrigger.PERIODIC),
    LEAP("Leap Attack", AbilityTrigger.PERIODIC),
    WEB_TRAP("Web Trap", AbilityTrigger.PERIODIC),
    PULL("Vortex Pull", AbilityTrigger.PERIODIC),
    VELOCITY_REVERSAL("Velocity Reversal", AbilityTrigger.PERIODIC),
    SCATTER_BLAST("Scatter Blast", AbilityTrigger.PERIODIC),
    REGENERATION("Regeneration", AbilityTrigger.PERIODIC),
    FREEZE_ZONE("Freeze Zone", AbilityTrigger.PERIODIC),
    TOXIC_FOG("Toxic Fog", AbilityTrigger.PERIODIC),
    HEAT_WAVE("Heat Wave", AbilityTrigger.PERIODIC),
    ZEUS("Zeus Strike", AbilityTrigger.PERIODIC),
    TELEPORT_SWAP("Teleport Swap", AbilityTrigger.PERIODIC),
    POWER_WAVE("Power Wave", AbilityTrigger.PERIODIC),
    SOUL_MARK("Soul Mark", AbilityTrigger.PERIODIC),
    EARTHQUAKE("Earthquake", AbilityTrigger.PERIODIC),
    FREEZE_ATTACK("Freeze Attack", AbilityTrigger.ON_ATTACK),
    TOSS_ATTACK("Toss Attack", AbilityTrigger.ON_ATTACK),
    LIFESTEAL_ATTACK("Life Steal Attack", AbilityTrigger.ON_ATTACK),
    WITCHCRAFT_ATTACK("Witchcraft Attack", AbilityTrigger.ON_ATTACK),
    EXECUTE_THRESHOLD("Execute Threshold", AbilityTrigger.ON_ATTACK),
    TARGET_CLEANSE("Target Cleanse", AbilityTrigger.ON_ATTACK),
    THORNS("Thorns Armor", AbilityTrigger.ON_DAMAGED),
    QUICK_BUFF("Quick Buff", AbilityTrigger.ON_DAMAGED),
    COUNTER_HIT("Counter Hit", AbilityTrigger.ON_DAMAGED),
    CLEANSE("Self Cleanse", AbilityTrigger.ON_DAMAGED),
    PHOENIX_IMMORTAL("Phoenix Immortal", AbilityTrigger.ON_DEATH),
    SHADOW_CLONE("Shadow Clones", AbilityTrigger.ON_DEATH),
    MINIONS("Minion Spawner", AbilityTrigger.ON_DEATH),
    LAST_EFFECT("Last Effect", AbilityTrigger.ON_DEATH),
    LAST_DEATH_EXPLOSION("Death Explosion", AbilityTrigger.ON_DEATH),
    FATAL_CURSE("Fatal Curse", AbilityTrigger.ON_DEATH);

    private final String displayName;
    private final AbilityTrigger defaultTrigger;

    AbilityType(String displayName, AbilityTrigger defaultTrigger) {
        this.displayName = displayName;
        this.defaultTrigger = defaultTrigger;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AbilityTrigger getDefaultTrigger() {
        return defaultTrigger;
    }
}
