package dev.lino.custombosses.model;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BossDrop {

    private ItemStack item;
    private double chance;
    private int minAmount;
    private int maxAmount;
    private List<String> commands;
    private RewardType rewardType;

    public BossDrop(ItemStack item, double chance) {
        this.item = item;
        this.chance = chance;
        this.minAmount = 1;
        this.maxAmount = 1;
        this.commands = new ArrayList<>();
        this.rewardType = RewardType.CHANCE_DROP;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands != null ? commands : new ArrayList<>();
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType != null ? rewardType : RewardType.CHANCE_DROP;
    }
}
