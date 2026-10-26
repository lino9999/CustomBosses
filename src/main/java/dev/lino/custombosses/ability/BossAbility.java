package dev.lino.custombosses.ability;

public class BossAbility {

    private final AbilityType type;
    private AbilityTrigger trigger;
    private boolean enabled;
    private double chance;
    private int cooldownSeconds;
    private double range;
    private double value;
    private String stringValue;
    private int durationTicks;
    private String message;
    private transient long lastExecutedMillis;

    public BossAbility(AbilityType type) {
        this.type = type;
        this.trigger = type.getDefaultTrigger();
        this.enabled = true;
        this.chance = 100.0;
        this.cooldownSeconds = 10;
        this.range = 10.0;
        this.value = 5.0;
        this.stringValue = "";
        this.durationTicks = 100;
        this.message = "";
        this.lastExecutedMillis = 0L;
    }

    public AbilityType getType() {
        return type;
    }

    public AbilityTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(AbilityTrigger trigger) {
        this.trigger = trigger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean canExecuteNow() {
        if (!enabled) {
            return false;
        }
        if (cooldownSeconds <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        return (now - lastExecutedMillis) >= (cooldownSeconds * 1000L);
    }

    public void recordExecution() {
        this.lastExecutedMillis = System.currentTimeMillis();
    }
}
