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
import java.util.stream.Collectors;

/*
 * Listens to inventory clicks to handle menu navigation and actions.
 * Updated to handle pagination, sorting filters and edit commands.
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
            // Extracts warp name from title "Editando Warp: Name"
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
            String[] parts = title.split("Pg ");
            if (parts.length > 1) currentPage = Integer.parseInt(parts[1].trim()) - 1;
        } catch (Exception e) { currentPage = 0; }

        if (slot == 0) { // Own Warps
            List<Warp> ownWarps = warpManager.getPlayerWarps(player.getUniqueId());
            warpListMenu.open(player, ownWarps, "§8Player Warps - Suas Warps", 0);
        } else if (slot == 7) { // Filter by Name (Visits/Date)
            List<Warp> allWarps = warpManager.getAllWarpsSorted();
            warpListMenu.open(player, allWarps, "§8Player Warps - Nome", 0);
        } else if (slot == 8) { // Filter by Players (Total visits sum)
            // Sorting by player logic is handled in the warp manager sorted list
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
                String warpName = item.getItemMeta().getDisplayName().substring(2); // Remove color code
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
                sendTutorial(player, "edit-name-tutorial");
                break;
            case 34: // Change Lore Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-lore-tutorial");
                break;
            case 37: // Back to main
                menuManager.openMainMenu(player);
                break;
            case 39: // Change Icon Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-icon-tutorial");
                break;
            case 41: // Lock/Unlock Toggle
                if (!player.hasPermission("pw.lock") && !player.hasPermission("pw.use")) {
                    player.sendActionBar("§cVocê não possui a permissão: pw.lock");
                    return;
                }
                boolean newState = !warp.isLocked();
                warp.setLocked(newState);
                warpManager.saveWarps();
                player.playSound(player.getLocation(), newState ? Sound.BLOCK_CHEST_CLOSE : Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                player.sendActionBar(newState ? "§cWarp trancada com sucesso." : "§aWarp destrancada com sucesso.");
                menuManager.openEditMenu(player, warp);
                break;
            case 43: // Remove Tutorial
                player.closeInventory();
                sendTutorial(player, "edit-remove-tutorial");
                break;
        }
    }

    private void sendTutorial(Player player, String key) {
        List<String> lines = plugin.getConfig().getStringList("messages." + key);
        if (lines.isEmpty()) {
            // Fallback if config is missing
            player.sendMessage("§eSiga as instruções do comando para editar sua warp.");
        } else {
            for (String line : lines) {
                player.sendMessage(line.replace("&", "§"));
            }
        }
        player.sendMessage("§8Digite: /pwedit " + " <warp> para voltar ao menu.");
    }
}
