package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.comonier.plugin.menus.EditMenu;
import com.comonier.plugin.menus.MainMenu;
import com.comonier.plugin.menus.WarpListMenu;
import com.comonier.plugin.models.Warp;
import org.bukkit.entity.Player;
import java.util.List;

/*
 * Updated MenuManager.
 * Now acts as a central hub to call specific menu classes.
 */
public class MenuManager {

    private final PW plugin;
    private final MainMenu mainMenu;
    private final EditMenu editMenu;
    private final WarpListMenu warpListMenu;

    public MenuManager(PW plugin) {
        this.plugin = plugin;
        this.mainMenu = new MainMenu(plugin);
        this.editMenu = new EditMenu(plugin);
        this.warpListMenu = new WarpListMenu(plugin);
    }

    // Opens the main starting GUI
    public void openMainMenu(Player player) {
        mainMenu.open(player);
    }

    // Opens the editor GUI for a specific warp
    public void openEditMenu(Player player, Warp warp) {
        editMenu.open(player, warp);
    }

    // Opens the paginated list of warps
    public void openWarpList(Player player, List<Warp> warps, String title, int page) {
        warpListMenu.open(player, warps, title, page);
    }
}
