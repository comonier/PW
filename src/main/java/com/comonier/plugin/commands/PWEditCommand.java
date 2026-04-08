package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.MenuManager;
import com.comonier.plugin.managers.WarpManager;
import com.comonier.plugin.models.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/*
 * Executor for /pwedit and /pweditplayer commands.
 * Handles opening the edit GUI for own warps or other players' warps (admin).
 */
public class PWEditCommand implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final MenuManager menuManager;

    public PWEditCommand(PW plugin, WarpManager warpManager, MenuManager menuManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Command: /pwedit <warp_name>
        if (label.equalsIgnoreCase("pwedit")) {
            if (!player.hasPermission("pw.edit") && !player.hasPermission("pw.use")) {
                sendNoPerm(player, "pw.edit");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("§cUsage: /pwedit <warp_name>");
                return true;
            }

            Warp warp = warpManager.getWarp(args[0]);
            if (warp == null) {
                player.sendMessage("§cWarp not found.");
                return true;
            }

            // Only the owner can edit via /pwedit
            if (!warp.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("pw.admin")) {
                player.sendMessage("§cVocê só pode editar suas próprias warps.");
                return true;
            }

            menuManager.openEditMenu(player, warp);
            return true;
        }

        // Command: /pweditplayer <warp_name>
        if (label.equalsIgnoreCase("pweditplayer")) {
            if (!player.hasPermission("pw.admin")) {
                player.sendMessage("§cVocê não possui a permissão: pw.admin");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("§cUsage: /pweditplayer <warp_name>");
                return true;
            }

            Warp warp = warpManager.getWarp(args[0]);
            if (warp == null) {
                player.sendMessage("§cWarp not found.");
                return true;
            }

            menuManager.openEditMenu(player, warp);
            return true;
        }

        return false;
    }

    private void sendNoPerm(Player player, String perm) {
        String msg = plugin.getConfig().getString("messages.no-permission", "&cNo permission: {permission}")
                .replace("{permission}", perm).replace("&", "§");
        player.sendMessage(msg);
    }
}
