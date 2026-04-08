package com.comonier.plugin.menus;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Specific class for handling the Warp Editor GUI layout.
 */
public class EditMenu {

    private final PW plugin;

    public EditMenu(PW plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Warp warp) {
        String title = "§8Editando Warp: " + warp.getName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        applyDesign(inv);

        // Action Slots
        inv.setItem(13, warp.getIcon()); // Preview
        inv.setItem(28, createItem(Material.ENDER_PEARL, "§aTeleportar"));
        inv.setItem(30, createItem(Material.RED_BED, "§eResetar Localização"));
        inv.setItem(32, createItem(Material.NAME_TAG, "§bAlterar Nome"));
        inv.setItem(34, createItem(Material.ENCHANTED_BOOK, "§dAlterar Lore"));
        inv.setItem(37, createItem(Material.PLAYER_HEAD, "§6Voltar ao Menu"));
        inv.setItem(39, createItem(warp.getIcon().getType(), "§9Alterar Ícone"));
        
        String lockText = warp.isLocked() ? "§c[Trancado]" : "§a[Destrancado]";
        inv.setItem(41, createItem(Material.IRON_DOOR, "§fTrancar/Destrancar", List.of("§7Status: " + lockText)));
        inv.setItem(43, createItem(Material.BARRIER, "§cRemover Warp"));

        player.openInventory(inv);
    }

    private void applyDesign(Inventory inv) {
        ItemStack black = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack dark = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack light = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ");

        int[] blackSlots = {0, 8, 45, 53};
        for (int s : blackSlots) inv.setItem(s, black);

        int[] darkSlots = {1,2,3,4,5,6,7,9,17,18,19,20,21,22,23,24,25,26,27,35,36,44,46,47,48,49,50,51,52};
        for (int s : darkSlots) inv.setItem(s, dark);

        int[] lightSlots = {10,11,12,14,15,16,29,31,33,38,40,42};
        for (int s : lightSlots) inv.setItem(s, light);
    }

    private ItemStack createItem(Material mat, String name) {
        return createItem(mat, name, null);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
