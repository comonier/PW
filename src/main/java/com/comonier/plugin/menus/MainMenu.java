package com.comonier.plugin.menus;

import com.comonier.plugin.PW;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Specific class for handling the Main GUI (/pw) layout.
 */
public class MainMenu {

    private final PW plugin;

    public MainMenu(PW plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = "§8Player Warps - Principal";
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill borders with black glass
        ItemStack blackGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        int[] fillers = {1, 2, 3, 4, 5, 6, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52};
        for (int slot : fillers) {
            inv.setItem(slot, blackGlass);
        }

        // Slot 0: Player Profile
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§aSuas Warps");
            meta.setLore(List.of("§7Clique para gerenciar suas warps."));
            head.setItemMeta(meta);
        }
        inv.setItem(0, head);

        // Slot 7 and 8: Filters
        inv.setItem(7, createItem(Material.COMPASS, "§bBuscar por Nome", List.of("§7Filtro: Visitas e Data")));
        inv.setItem(8, createItem(Material.PLAYER_HEAD, "§eBuscar por Jogadores", List.of("§7Filtro: Popularidade")));

        player.openInventory(inv);
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
