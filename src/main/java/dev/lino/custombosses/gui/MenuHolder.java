package dev.lino.custombosses.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class MenuHolder implements InventoryHolder {

    protected Inventory inventory;

    public abstract void handleClick(InventoryClickEvent event);

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
