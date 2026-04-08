package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Manages all warps in memory with automatic database synchronization.
 * Fixed: Uses clean ID as the unique Map Key to prevent TabComplete kicks and search bugs.
 */
public class WarpManager {

    private final PW plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Warp> warps;

    public WarpManager(PW plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        // Memory map initialized using ID (clean text) as the primary key
        this.warps = databaseManager.loadWarps();
    }

    public void createWarp(Warp warp) {
        // Use ID for the key to ensure command compatibility
        warps.put(warp.getId().toLowerCase(), warp);
        databaseManager.saveWarp(warp);
    }

    public void saveWarp(Warp warp) {
        databaseManager.saveWarp(warp);
    }

    public void deleteWarp(String id) {
        warps.remove(id.toLowerCase());
        databaseManager.deleteWarp(id);
    }

    /*
     * Search method fixed to handle array arguments and clean text only.
     */
    public Warp getWarp(String[] args) {
        if (args == null || args.length == 0) return null;
        String id = args[0].replaceAll("(?i)&[0-9A-FK-OR]", "").toLowerCase();
        return warps.get(id);
    }

    public Warp getWarp(String id) {
        if (id == null) return null;
        String cleanId = id.replaceAll("(?i)&[0-9A-FK-OR]", "").toLowerCase();
        return warps.get(cleanId);
    }

    public List<Warp> getPlayerWarps(UUID uuid) {
        return warps.values().stream()
                .filter(w -> w.getOwnerUUID().equals(uuid))
                .collect(Collectors.toList());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

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

    public List<Warp> getUniqueOwnersSorted() {
        Map<UUID, Warp> uniquePlayers = new HashMap<>();
        for (Warp w : warps.values()) {
            if (!uniquePlayers.containsKey(w.getOwnerUUID()) || w.getVisits() > uniquePlayers.get(w.getOwnerUUID()).getVisits()) {
                uniquePlayers.put(w.getOwnerUUID(), w);
            }
        }
        List<Warp> sorted = new ArrayList<>(uniquePlayers.values());
        sorted.sort((w1, w2) -> Integer.compare(w2.getVisits(), w1.getVisits()));
        return sorted;
    }
}
