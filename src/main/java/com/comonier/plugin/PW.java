package com.comonier.plugin;

import com.comonier.plugin.commands.PWAttributeCommands;
import com.comonier.plugin.commands.PWCommand;
import com.comonier.plugin.commands.PWEditCommand;
import com.comonier.plugin.commands.PWTabCompleter;
import com.comonier.plugin.listeners.MenuListener;
import com.comonier.plugin.managers.DatabaseManager;
import com.comonier.plugin.managers.MenuManager;
import com.comonier.plugin.managers.WarpManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/*
 * Main class for the PW (Player Warps) plugin.
 * Handles initialization of database, managers, commands, and tab completion.
 * Fully compatible with 1.19+ and Folia (26.1+).
 */
public class PW extends JavaPlugin {

    private static PW instance;
    private static Permission perms = null;
    private static Chat chat = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    private WarpManager warpManager;
    private MenuManager menuManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config and messages
        saveDefaultConfig();
        saveResource("PWMenu.yml", false);
        saveResource("messages_pt.yml", false);
        saveResource("messages_en.yml", false);
        saveResource("messages_es.yml", false);
        saveResource("messages_ru.yml", false);

        // Setup Vault
        if (!setupPermissions()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupChat();

        // Initialize Persistence and Managers
        this.databaseManager = new DatabaseManager(this);
        this.warpManager = new WarpManager(this, databaseManager);
        this.menuManager = new MenuManager(this);

        // Register Tab Completer
        PWTabCompleter tabCompleter = new PWTabCompleter(this);

        // Register Commands and Tab Completion
        getCommand("pw").setExecutor(new PWCommand(this, warpManager, menuManager));
        getCommand("pw").setTabCompleter(tabCompleter);
        
        PWEditCommand editCmd = new PWEditCommand(this, warpManager, menuManager);
        getCommand("pwedit").setExecutor(editCmd);
        getCommand("pwedit").setTabCompleter(tabCompleter);
        getCommand("pweditplayer").setExecutor(editCmd);
        getCommand("pweditplayer").setTabCompleter(tabCompleter);

        PWAttributeCommands attrCmd = new PWAttributeCommands(this, warpManager);
        String[] attrCommands = {"pwsetname", "pwsetlore", "pwseticon", "pwdel", "pwreset"};
        for (String cmd : attrCommands) {
            getCommand(cmd).setExecutor(attrCmd);
            getCommand(cmd).setTabCompleter(tabCompleter);
        }

        // Register Listeners
        getServer().getPluginManager().registerEvents(new MenuListener(this, warpManager, menuManager), this);

        log.info(String.format("[%s] Plugin enabled version %s (DB: %s)", 
            getDescription().getName(), 
            getDescription().getVersion(),
            getConfig().getString("database.type")));
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Plugin disabled.", getDescription().getName()));
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return chat != null;
    }

    public static PW getInstance() { return instance; }
    public static Permission getPermissions() { return perms; }
    public static Chat getChat() { return chat; }
    public WarpManager getWarpManager() { return warpManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
}
