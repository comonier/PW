package com.comonier.plugin;

import com.comonier.plugin.commands.*;
import com.comonier.plugin.listeners.MenuListener;
import com.comonier.plugin.managers.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PW extends JavaPlugin {

    private static PW instance;
    private static Permission perms = null;
    private static Chat chat = null;
    private static final Logger log = Logger.getLogger("Minecraft");

    private WarpManager warpManager;
    private MenuManager menuManager;
    private DatabaseManager databaseManager;
    private ProtectionManager protectionManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("PWMenu.yml", false);
        saveResource("messages_pt.yml", false);
        saveResource("messages_en.yml", false);
        saveResource("messages_es.yml", false);
        saveResource("messages_ru.yml", false);

        if (!setupPermissions()) {
            log.severe(String.format("[%s] - Vault dependency not found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupChat();

        this.databaseManager = new DatabaseManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.warpManager = new WarpManager(this, databaseManager);
        this.menuManager = new MenuManager(this);

        PWTabCompleter tabCompleter = new PWTabCompleter(this);
        getCommand("pw").setExecutor(new PWCommand(this, warpManager, menuManager, protectionManager));
        getCommand("pw").setTabCompleter(tabCompleter);
        getCommand("pwset").setExecutor(new PWSetCommand(this, warpManager, protectionManager));
        getCommand("pwset").setTabCompleter(tabCompleter);

        PWEditCommand editCmd = new PWEditCommand(this, warpManager, menuManager);
        getCommand("pwedit").setExecutor(editCmd);
        getCommand("pweditplayer").setExecutor(editCmd);

        PWAttributeCommands attrCmd = new PWAttributeCommands(this, warpManager, protectionManager);
        String[] attrCommands = {"pwsetname", "pwsetlore", "pwseticon", "pwdel", "pwreset"};
        for (String cmd : attrCommands) {
            getCommand(cmd).setExecutor(attrCmd);
            getCommand(cmd).setTabCompleter(tabCompleter);
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this, warpManager, menuManager), this);
    }

    public String getMessage(String path) {
        String lang = getConfig().getString("settings.language", "en");
        org.bukkit.configuration.file.FileConfiguration msgConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "messages_" + lang + ".yml"));
        String msg = msgConfig.getString("messages." + path);
        return (msg == null) ? "§cPath: " + path : msg.replace("&", "§");
    }

    public List<String> getMessageList(String path) {
        String lang = getConfig().getString("settings.language", "en");
        org.bukkit.configuration.file.FileConfiguration msgConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "messages_" + lang + ".yml"));
        List<String> list = msgConfig.getStringList("messages." + path);
        return list.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList());
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
    public WarpManager getWarpManager() { return warpManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public ProtectionManager getProtectionManager() { return protectionManager; }
}
