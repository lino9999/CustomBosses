package dev.lino.custombosses.model;

import dev.lino.custombosses.ability.BossAbility;
import dev.lino.custombosses.ability.AbilityType;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class BossProfile {

    private final String id;
    private String displayName;
    private EntityType entityType;
    private double maxHealth;
    private double attackDamage;
    private double movementSpeed;
    private double followRange;
    private double knockbackResistance;
    private double scale;

    private EntityType passengerType;
    private Particle ambientParticle;
    private int ambientParticleCount;
    private final List<PotionEffect> potionEffects;

    private final Map<EquipmentSlot, ItemStack> equipment;
    private final Map<EquipmentSlot, Float> dropChances;

    private boolean bossBarEnabled;
    private String bossBarTitle;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;
    private double bossBarRange;

    private boolean glowing;
    private boolean silent;
    private boolean ai;
    private Integer customModelData;

    private boolean immuneFall;
    private boolean immuneFire;
    private boolean immuneDrown;
    private boolean immuneExplosion;
    private boolean immuneKnockback;
    private boolean immuneProjectile;
    private boolean immuneSuffocation;
    private boolean preventDespawn;

    private final Map<AbilityType, BossAbility> abilities;
    private final List<BossDrop> drops;
    private int xpDrop;
    private boolean clearVanillaDrops;

    public BossProfile(String id, EntityType entityType) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.displayName = "&c" + id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
        this.entityType = entityType != null ? entityType : EntityType.ZOMBIE;
        this.maxHealth = 100.0;
        this.attackDamage = 10.0;
        this.movementSpeed = 0.25;
        this.followRange = 32.0;
        this.knockbackResistance = 0.5;
        this.scale = 1.0;

        this.passengerType = null;
        this.ambientParticle = null;
        this.ambientParticleCount = 0;
        this.potionEffects = new ArrayList<>();

        this.equipment = new EnumMap<>(EquipmentSlot.class);
        this.dropChances = new EnumMap<>(EquipmentSlot.class);

        this.bossBarEnabled = true;
        this.bossBarTitle = this.displayName + " &7[&c%health%&7/&c%max_health%&7]";
        this.bossBarColor = BarColor.RED;
        this.bossBarStyle = BarStyle.SOLID;
        this.bossBarRange = 30.0;

        this.glowing = false;
        this.silent = false;
        this.ai = true;
        this.customModelData = null;

        this.immuneFall = true;
        this.immuneFire = false;
        this.immuneDrown = true;
        this.immuneExplosion = false;
        this.immuneKnockback = false;
        this.immuneProjectile = false;
        this.immuneSuffocation = true;
        this.preventDespawn = true;

        this.abilities = new EnumMap<>(AbilityType.class);
        this.drops = new ArrayList<>();
        this.xpDrop = 50;
        this.clearVanillaDrops = true;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = Math.max(1.0, maxHealth);
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = Math.max(0.0, attackDamage);
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = Math.max(0.01, movementSpeed);
    }

    public double getFollowRange() {
        return followRange;
    }

    public void setFollowRange(double followRange) {
        this.followRange = Math.max(1.0, followRange);
    }

    public double getKnockbackResistance() {
        return knockbackResistance;
    }

    public void setKnockbackResistance(double knockbackResistance) {
        this.knockbackResistance = Math.max(0.0, Math.min(1.0, knockbackResistance));
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.1, Math.min(16.0, scale));
    }

    public EntityType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(EntityType passengerType) {
        this.passengerType = passengerType;
    }

    public Particle getAmbientParticle() {
        return ambientParticle;
    }

    public void setAmbientParticle(Particle ambientParticle) {
        this.ambientParticle = ambientParticle;
    }

    public int getAmbientParticleCount() {
        return ambientParticleCount;
    }

    public void setAmbientParticleCount(int ambientParticleCount) {
        this.ambientParticleCount = Math.max(0, ambientParticleCount);
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void addPotionEffect(PotionEffect effect) {
        if (effect != null) {
            this.potionEffects.add(effect);
        }
    }

    public Map<EquipmentSlot, ItemStack> getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentSlot slot, ItemStack itemStack) {
        if (itemStack == null) {
            this.equipment.remove(slot);
        } else {
            this.equipment.put(slot, itemStack);
        }
    }

    public Map<EquipmentSlot, Float> getDropChances() {
        return dropChances;
    }

    public void setDropChance(EquipmentSlot slot, float chance) {
        this.dropChances.put(slot, chance);
    }

    public boolean isBossBarEnabled() {
        return bossBarEnabled;
    }

    public void setBossBarEnabled(boolean bossBarEnabled) {
        this.bossBarEnabled = bossBarEnabled;
    }

    public String getBossBarTitle() {
        return bossBarTitle;
    }

    public void setBossBarTitle(String bossBarTitle) {
        this.bossBarTitle = bossBarTitle;
    }

    public BarColor getBossBarColor() {
        return bossBarColor;
    }

    public void setBossBarColor(BarColor bossBarColor) {
        this.bossBarColor = bossBarColor;
    }

    public BarStyle getBossBarStyle() {
        return bossBarStyle;
    }

    public void setBossBarStyle(BarStyle bossBarStyle) {
        this.bossBarStyle = bossBarStyle;
    }

    public double getBossBarRange() {
        return bossBarRange;
    }

    public void setBossBarRange(double bossBarRange) {
        this.bossBarRange = bossBarRange;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }

    public boolean isImmuneFall() {
        return immuneFall;
    }

    public void setImmuneFall(boolean immuneFall) {
        this.immuneFall = immuneFall;
    }

    public boolean isImmuneFire() {
        return immuneFire;
    }

    public void setImmuneFire(boolean immuneFire) {
        this.immuneFire = immuneFire;
    }

    public boolean isImmuneDrown() {
        return immuneDrown;
    }

    public void setImmuneDrown(boolean immuneDrown) {
        this.immuneDrown = immuneDrown;
    }

    public boolean isImmuneExplosion() {
        return immuneExplosion;
    }

    public void setImmuneExplosion(boolean immuneExplosion) {
        this.immuneExplosion = immuneExplosion;
    }

    public boolean isImmuneKnockback() {
        return immuneKnockback;
    }

    public void setImmuneKnockback(boolean immuneKnockback) {
        this.immuneKnockback = immuneKnockback;
    }

    public boolean isImmuneProjectile() {
        return immuneProjectile;
    }

    public void setImmuneProjectile(boolean immuneProjectile) {
        this.immuneProjectile = immuneProjectile;
    }

    public boolean isImmuneSuffocation() {
        return immuneSuffocation;
    }

    public void setImmuneSuffocation(boolean immuneSuffocation) {
        this.immuneSuffocation = immuneSuffocation;
    }

    public boolean isPreventDespawn() {
        return preventDespawn;
    }

    public void setPreventDespawn(boolean preventDespawn) {
        this.preventDespawn = preventDespawn;
    }

    public Map<AbilityType, BossAbility> getAbilities() {
        return abilities;
    }

    public void addAbility(BossAbility ability) {
        if (ability != null) {
            this.abilities.put(ability.getType(), ability);
        }
    }

    public void removeAbility(AbilityType type) {
        this.abilities.remove(type);
    }

    public List<BossDrop> getDrops() {
        return drops;
    }

    public int getXpDrop() {
        return xpDrop;
    }

    public void setXpDrop(int xpDrop) {
        this.xpDrop = Math.max(0, xpDrop);
    }

    public boolean isClearVanillaDrops() {
        return clearVanillaDrops;
    }

    public void setClearVanillaDrops(boolean clearVanillaDrops) {
        this.clearVanillaDrops = clearVanillaDrops;
    }
}
