package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Manages all warps in memory with automatic database synchronization.
 * Handles sorting logic and player-based grouping for GUI filters.
 */
public class WarpManager {

    private final PW plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Warp> warps;

    public WarpManager(PW plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        // Load everything from DB into memory at startup
        this.warps = databaseManager.loadWarps();
    }

    public void createWarp(Warp warp) {
        warps.put(warp.getName().toLowerCase(), warp);
        databaseManager.saveWarp(warp);
    }

    public void saveWarp(Warp warp) {
        // Updates existing warp data in database
        databaseManager.saveWarp(warp);
    }

    public void deleteWarp(String name) {
        warps.remove(name.toLowerCase());
        databaseManager.deleteWarp(name);
    }

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public List<Warp> getPlayerWarps(UUID uuid) {
        return warps.values().stream()
                .filter(w -> w.getOwnerUUID().equals(uuid))
                .collect(Collectors.toList());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    /*
     * Main sorting logic: Most visited first, then oldest first.
     */
    public List<Warp> getAllWarpsSorted() {
        List<Warp> sortedList = new ArrayList<>(warps.values());
        sortedList.sort((w1, w2) -> {
            if (w2.getVisits() != w1.getVisits()) {
                return Integer.compare(w2.getVisits(), w1.getVisits());
            }
            return Long.compare(w1.getCreatedAt(), w2.getCreatedAt());
        });
        return sortedList;
    }

    /*
     * Groups all existing warps by their owners.
     * Returns a list of "Representative Warps" (one per player) to show unique players in the GUI.
     * Logic: For each player, we pick their most popular warp as the icon.
     */
    public List<Warp> getUniqueOwnersSorted() {
        Map<UUID, Warp> uniquePlayers = new HashMap<>();
        
        for (Warp warp : warps.values()) {
            UUID ownerId = warp.getOwnerUUID();
            
            // If we don't have this player yet, or if this current warp is more popular than the one saved
            if (!uniquePlayers.containsKey(ownerId) || warp.getVisits() > uniquePlayers.get(ownerId).getVisits()) {
                uniquePlayers.put(ownerId, warp);
            }
        }
        
        // Convert map back to list and sort by popularity (sum of visits of the player could be an alternative)
        List<Warp> sortedOwners = new ArrayList<>(uniquePlayers.values());
        sortedOwners.sort((w1, w2) -> Integer.compare(w2.getVisits(), w1.getVisits()));
        
        return sortedOwners;
    }
}
