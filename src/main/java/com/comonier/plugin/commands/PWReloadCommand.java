package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/*
 * Dedicated command executor for /pwreload.
 * Reinitializes the plugin configuration and managers.
 */
public class PWReloadCommand implements CommandExecutor {

    private final PW plugin;

    public PWReloadCommand(PW plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Permission check: only for admins or console
        if (!sender.hasPermission("pw.admin")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage(plugin.getMessage("no-permission").replace("{permission}", "pw.admin"));
            } else {
                sender.sendMessage("§c[PW] Only console or admins can use this.");
            }
            return true;
        }

        // Execute reload logic
        plugin.reloadPlugin();
        
        String msg = "§a[PW] Configurações e menus recarregados com sucesso!";
        if (sender instanceof Player) {
            sender.sendMessage(msg);
        } else {
            plugin.getLogger().info("Plugin reloaded successfully!");
        }

        return true;
    }
}
