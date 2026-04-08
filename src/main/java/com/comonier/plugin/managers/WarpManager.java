package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Manages all warps in memory with automatic database synchronization.
 * Sorting logic: Visits first (descending), then Creation Date (ascending).
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
        // Just an alias to save existing warp data
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

    // Main sorting logic: Most visited first, then oldest first
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
}
