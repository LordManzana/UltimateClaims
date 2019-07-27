package com.songoda.ultimateclaims.claim;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.utils.settings.Setting;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PowerCell {

    private Location location;

    private List<ItemStack> items = new ArrayList<>();

    private int currentPower = 10;

    private double economyBalance = 0;

    public int tick(Claim claim) {
        UltimateClaims plugin = UltimateClaims.getInstance();
        if (this.currentPower <= 0) {
            List<String> materials = Setting.ITEM_VALUES.getStringList();
            for (String value : materials) {
                Material material = Material.valueOf(value.split(":")[0]);
                if (getItems().stream().anyMatch(item -> item.getType() == material)) {
                    this.removeOneMaterial(material);
                    this.currentPower += Integer.parseInt(value.split(":")[1]);
                    if (plugin.getHologram() != null)
                        plugin.getHologram().update(this);
                    return this.currentPower;
                }
            }
            double economyValue = Setting.ECONOMY_VALUE.getDouble();
            if (economyBalance >= economyValue) {
                this.economyBalance -= economyValue;
                this.currentPower += 1;
            }

        }
        if (location != null && plugin.getHologram() != null)
            plugin.getHologram().update(this);
        return this.currentPower -= claim == null ? 0 : claim.getClaimedChunks().size();
    }

    private int getMaterialAmount(Material material) {
        int amount = 0;
        for (ItemStack item : items) {
            if (item.getType() != material) continue;
            amount = item.getAmount();
        }
        return amount;
    }

    private void removeOneMaterial(Material material) {
        for (ItemStack item : this.items) {
            if (item.getType() != material) continue;

            item.setAmount(item.getAmount() - 1);

            if (item.getAmount() <= 0)
                this.items.remove(item);
            return;
        }
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public int getTotalPower() {
        return getItemPower() + getEconomyPower();
    }

    public int getItemPower() {
        int total = currentPower;
        List<String> materials = Setting.ITEM_VALUES.getStringList();
        for (String value : materials) {
            Material material = Material.valueOf(value.split(":")[0]);
            if (this.items.stream().anyMatch(item -> item.getType() == material)) {
                total += getMaterialAmount(material) + Integer.parseInt(value.split(":")[1]);
            }
        }
        return total;
    }

    public int getEconomyPower() {
        return (int) Math.floor(economyBalance / Setting.ECONOMY_VALUE.getDouble());
    }

    public List<ItemStack> getItems() {
        return new ArrayList<>(this.items);
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean hasLocation() {
        return location != null;
    }

    public void setLocation(Location location) {
        this.location = location;
        tick(null);
    }

    public void destroy() {
        if (location != null) {
            for (ItemStack item : this.items) {
                if (item == null) continue;
                location.getWorld().dropItemNaturally(location, item);
            }
            if (UltimateClaims.getInstance().getHologram() != null)
                UltimateClaims.getInstance().getHologram().remove(this);
        }
        this.items.clear();
        this.location = null;
    }
}
