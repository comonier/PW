package com.comonier.plugin.listeners;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.MenuManager;
import com.comonier.plugin.managers.WarpManager;
import com.comonier.plugin.menus.WarpListMenu;
import com.comonier.plugin.models.Warp;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/*
 * Listens to inventory clicks to handle menu navigation and actions.
 * Fixed: Now correctly loads tutorial message lists from translated files.
 */
public class MenuListener implements Listener {

    private final PW plugin;
    private final WarpManager warpManager;
    private final MenuManager menuManager;
    private final WarpListMenu warpListMenu;

    public MenuListener(PW plugin, WarpManager warpManager, MenuManager menuManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.menuManager = menuManager;
        this.warpListMenu = new WarpListMenu(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        // Check if the inventory is part of our plugin
        if (!title.contains("Player Warps") && !title.contains("Editando Warp")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType().isAir()) return;

        // Logic for Main/List Menus
        if (title.contains("Player Warps")) {
            handleListMenuClick(player, slot, title);
        } 
        // Logic for Edit Panel
        else if (title.contains("Editando Warp")) {
            String[] parts = title.split(":");
            if (parts.length > 1) {
                String warpName = parts[1].trim();
                Warp warp = warpManager.getWarp(warpName);
                if (warp != null) {
                    handleEditMenuClick(player, slot, warp);
                }
            }
        }
    }

    private void handleListMenuClick(Player player, int slot, String title) {
        int currentPage = 0;
        try {
            if (title.contains("Pg ")) {
                String[] parts = title.split("Pg ");
                currentPage = Integer.parseInt(parts[1]) - 1;
            }
        } catch (Exception e) { currentPage = 0; }

        if (slot == 0) { // Own Warps
            List<Warp> ownWarps = warpManager.getPlayerWarps(player.getUniqueId());
            warpListMenu.open(player, ownWarps, "§8Player Warps - Suas Warps", 0);
        } else if (slot == 7) { // Filter by Name
            warpListMenu.open(player, warpManager.getAllWarpsSorted(), "§8Player Warps - Nome", 0);
        } else if (slot == 8) { // Filter by Players
            warpListMenu.open(player, warpManager.getAllWarpsSorted(), "§8Player Warps - Jogadores", 0);
        } else if (slot == 45) { // Prev Page
            if (currentPage > 0) {
                warpListMenu.open(player, warpManager.getAllWarpsSorted(), "§8Player Warps - Nome", currentPage - 1);
            }
        } else if (slot == 53) { // Next Page
            warpListMenu.open(player, warpManager.getAllWarpsSorted(), "§8Player Warps - Nome", currentPage + 1);
        } else if (slot > 9 && slot < 44) { // Click on a Warp icon
            ItemStack item = player.getOpenInventory().getItem(slot);
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String warpName = item.getItemMeta().getDisplayName().substring(2); 
                player.performCommand("pw " + warpName);
            }
        }
    }

    private void handleEditMenuClick(Player player, int slot, Warp warp) {
        switch (slot) {
            case 28: // Teleport
                player.closeInventory();
                player.performCommand("pw " + warp.getName());
                break;
            case 30: // Reset Location
                player.closeInventory();
                player.performCommand("pwreset " + warp.getName());
                break;
            case 32: // Change Name Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-name-tutorial", warp.getName());
                break;
            case 34: // Change Lore Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-lore-tutorial", warp.getName());
                break;
            case 37: // Back to main
                menuManager.openMainMenu(player);
                break;
            case 39: // Change Icon Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-icon-tutorial", warp.getName());
                break;
            case 41: // Lock/Unlock
                if (!player.hasPermission("pw.lock") && !player.hasPermission("pw.use")) {
                    player.sendActionBar(plugin.getMessage("no-permission").replace("{permission}", "pw.lock"));
                    return;
                }
                boolean newState = !warp.isLocked();
                warp.setLocked(newState);
                warpManager.saveWarp(warp);
                player.playSound(player.getLocation(), newState ? Sound.BLOCK_CHEST_CLOSE : Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                player.sendActionBar(newState ? plugin.getMessage("success.warp-locked") : plugin.getMessage("success.warp-unlocked"));
                menuManager.openEditMenu(player, warp);
                break;
            case 43: // Remove Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-remove-tutorial", warp.getName());
                break;
        }
    }

    /*
     * Sends the tutorial message list to the player.
     * Fixed to use plugin.getMessageList() for translated files.
     */
    private void sendTutorial(Player player, String key, String warpName) {
        List<String> lines = plugin.getMessageList(key);
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines) {
                player.sendMessage(line);
            }
        }
        player.sendMessage("§8Digite: §7/pwedit " + warpName + " §8para voltar ao menu.");
    }
}
