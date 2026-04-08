package com.comonier.plugin.menus;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Paginated Menu for listing Warps or Players.
 * Handles different view modes based on the inventory title.
 */
public class WarpListMenu {

    private final PW plugin;

    public WarpListMenu(PW plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, List<Warp> warps, String title, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, title + " - Pg " + (page + 1));
        
        applyBorders(inv);

        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 10; i < 44; i++) {
            if (i % 9 == 0 || (i + 1) % 9 == 0) continue;
            availableSlots.add(i);
        }

        int itemsPerPage = availableSlots.size();
        int startIndex = page * itemsPerPage;
        
        boolean isPersonalMenu = title.contains("Suas Warps");
        boolean isPlayerFilter = title.contains("Jogadores");

        for (int i = 0; i < itemsPerPage; i++) {
            int warpIndex = startIndex + i;
            int slot = availableSlots.get(i);

            if (warpIndex < warps.size()) {
                Warp w = warps.get(warpIndex);
                if (isPlayerFilter) {
                    inv.setItem(slot, formatPlayerHead(w));
                } else {
                    inv.setItem(slot, formatWarpIcon(w));
                }
            } 
            else if (isPersonalMenu) {
                int playerLimit = getWarpLimit(player);
                if (warpIndex < playerLimit) {
                    inv.setItem(slot, createItem(Material.WHITE_STAINED_GLASS_PANE, "&fWarp Disponível", List.of("&eSlot Livre")));
                } else {
                    inv.setItem(slot, createItem(Material.RED_STAINED_GLASS_PANE, "&cSlot Trancado", List.of("&7Limite atingido.")));
                }
            }
        }

        // Navigation logic
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, "§aPágina Anterior"));
        }
        
        if (warps.size() > (startIndex + itemsPerPage)) {
            inv.setItem(53, createItem(Material.ARROW, "§aPróxima Página"));
        }

        setupStaticButtons(inv, player);
        player.openInventory(inv);
    }

    private ItemStack formatPlayerHead(Warp warp) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(warp.getOwnerUUID()));
            meta.setDisplayName("§eDono: §f" + warp.getOwnerName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Clique para ver todas as");
            lore.add("§7warps deste jogador.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
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
        inv.setItem(7, createItem(Material.COMPASS, "§bBusca por Nome"));
        inv.setItem(8, createItem(Material.PLAYER_HEAD, "§eBusca por Jogadores"));
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            if (lore != null) meta.setLore(lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name) { return createItem(mat, name, null); }

    private int getWarpLimit(Player player) {
        if (player.hasPermission("pw.limit.*") || player.isOp()) return 999;
        int max = plugin.getConfig().getInt("settings.default-warp-limit", 5);
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith("pw.limit.")) {
                try {
                    int val = Integer.parseInt(perm.replace("pw.limit.", ""));
                    if (val > max) max = val;
                } catch (Exception ignored) {}
            }
        }
        return max;
    }
}
