package com.comonier.plugin.models;

import com.comonier.plugin.utils.PWUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Represents a Player Warp in the system.
 * This class separates the internal ID (used for commands and database) 
 * from the Display Name (which supports color codes for the GUI).
 */
public class Warp {

    private final String id; // Clean name used as key in database and maps
    private String displayName; // Name with & color codes for visual display
    private final UUID ownerUUID;
    private final String ownerName;
    private Location location;
    private ItemStack icon;
    private List<String> lore;
    private int visits;
    private final long createdAt;
    private boolean locked;

    public Warp(String id, UUID ownerUUID, String ownerName, Location location, ItemStack icon) {
        this.id = id.toLowerCase();
        this.displayName = id; // Default display name is the ID itself
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

    /*
     * Returns the name with processed color codes for GUI display.
     */
    public String getName() {
        return PWUtils.color(displayName);
    }

    /*
     * Returns the raw display name with '&' codes for database saving.
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
