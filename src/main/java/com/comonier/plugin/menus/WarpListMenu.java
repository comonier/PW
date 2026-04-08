package com.comonier.plugin.menus;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Handles the Paginated GUI for Warp Listings (Slots 10 to 43).
 * Dynamically calculates locked slots based on player's pw.limit.<number> permission.
 */
public class WarpListMenu {

    private final PW plugin;

    public WarpListMenu(PW plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, List<Warp> warps, String title, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, title + " - Pg " + (page + 1));
        
        applyBorders(inv);

        // Get the available slots in the grid (10 to 43, skipping borders)
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 10; i < 44; i++) {
            if (i % 9 == 0 || (i + 1) % 9 == 0) continue;
            availableSlots.add(i);
        }

        int itemsPerPage = availableSlots.size();
        int startIndex = page * itemsPerPage;
        int playerLimit = getWarpLimit(player);

        for (int i = 0; i < itemsPerPage; i++) {
            int warpIndex = startIndex + i;
            int slot = availableSlots.get(i);

            // 1. If there is a warp at this index, show it
            if (warpIndex < warps.size()) {
                inv.setItem(slot, formatWarpIcon(warps.get(warpIndex)));
            } 
            // 2. If no warp, check if the slot is within player's creation limit
            else {
                if (warpIndex < playerLimit) {
                    // Space available for creation
                    inv.setItem(slot, createItem(Material.WHITE_STAINED_GLASS_PANE, "&fWarp " + (warpIndex + 1), List.of("&eEspaço Livre")));
                } else {
                    // Slot locked by permission
                    inv.setItem(slot, createItem(Material.RED_STAINED_GLASS_PANE, "&cSlot Trancado", List.of("&7Você não possui permissão", "&7para este limite de warps.")));
                }
            }
        }

        // Navigation
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "&aPágina Anterior"));
        }
        if (warps.size() > (startIndex + itemsPerPage) || (startIndex + itemsPerPage) < playerLimit) {
            // Show next page if there are more warps OR if there are still free slots to show
            if (playerLimit > (startIndex + itemsPerPage) || warps.size() > (startIndex + itemsPerPage)) {
                inv.setItem(53, createItem(Material.ARROW, "&aPróxima Página"));
            }
        }

        setupStaticButtons(inv, player);
        player.openInventory(inv);
    }

    private int getWarpLimit(Player player) {
        if (player.hasPermission("pw.limit.*") || player.hasPermission("pw.admin") || player.isOp()) return 999;
        
        int max = plugin.getConfig().getInt("settings.default-warp-limit", 5);
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith("pw.limit.")) {
                try {
                    int val = Integer.parseInt(perm.replace("pw.limit.", ""));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return max;
    }

    private void applyBorders(Inventory inv) {
        ItemStack black = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        int[] fillers = {1, 2, 3, 4, 5, 6, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52};
        for (int s : fillers) inv.setItem(s, black);
    }

    private void setupStaticButtons(Inventory inv, Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§aSuas Warps");
            head.setItemMeta(meta);
        }
        inv.setItem(0, head);
        inv.setItem(7, createItem(Material.COMPASS, "&bBusca por WarpName"));
        inv.setItem(8, createItem(Material.PLAYER_HEAD, "&eBusca por Jogadores"));
    }

    private ItemStack formatWarpIcon(Warp warp) {
        ItemStack item = warp.getIcon().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b" + warp.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Dono: " + warp.getOwnerName());
            if (warp.getLore() != null) lore.addAll(warp.getLore());
            lore.add("");
            lore.add(warp.isLocked() ? "§c[Trancada]" : "§a[Aberta]");
            lore.add("§7Visitas: §f" + warp.getVisits());
            lore.add("");
            lore.add("§eClique para teleportar.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            if (lore != null) {
                meta.setLore(lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name) {
        return createItem(mat, name, null);
    }
}
