package com.comonier.plugin.models;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Represents a Player Warp in the system.
 * Stores location, owner, statistics and visual information.
 */
public class Warp {

    private final String id; // Unique ID for storage
    private String name;
    private final UUID ownerUUID;
    private final String ownerName;
    private Location location;
    private ItemStack icon;
    private List<String> lore;
    private int visits;
    private final long createdAt;
    private boolean locked;

    public Warp(String name, UUID ownerUUID, String ownerName, Location location, ItemStack icon) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.location = location;
        this.icon = icon;
        this.lore = new ArrayList<>();
        this.visits = 0;
        this.createdAt = System.currentTimeMillis();
        this.locked = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public int getVisits() {
        return visits;
    }

    public void addVisit() {
        this.visits = this.visits + 1;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
