package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.MenuManager;
import com.comonier.plugin.managers.WarpManager;
import com.comonier.plugin.models.Warp;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/*
 * Main command executor for /pw.
 * Handles opening the main menu and basic warp teleportation via command.
 */
public class PWCommand implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final MenuManager menuManager;

    public PWCommand(PW plugin, WarpManager warpManager, MenuManager menuManager) {
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

        // Check base permission
        if (!player.hasPermission("pw.use")) {
            String msg = plugin.getConfig().getString("messages.no-permission", "&cNo permission: {permission}")
                    .replace("{permission}", "pw.use").replace("&", "§");
            player.sendMessage(msg);
            return true;
        }

        // /pw - Opens Main GUI
        if (args.length == 0) {
            menuManager.openMainMenu(player);
            return true;
        }

        // /pw <warp_name> - Quick teleport
        if (args.length == 1) {
            String warpName = args[0];
            Warp warp = warpManager.getWarp(warpName);

            if (warp == null) {
                String msg = plugin.getConfig().getString("messages.warp-not-found", "&cWarp not found.")
                        .replace("&", "§");
                player.sendMessage(msg);
                return true;
            }

            // Check if warp is locked and player is not the owner/admin
            if (warp.isLocked() && !warp.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("pw.admin")) {
                player.sendMessage("§cEsta warp está trancada pelo dono.");
                return true;
            }

            // Check teleport permission
            if (!player.hasPermission("pw.tp") && !player.hasPermission("pw.use")) {
                player.sendMessage("§cVocê não possui a permissão: pw.tp");
                return true;
            }

            // Teleport logic
            player.teleport(warp.getLocation());
            warp.addVisit();
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.sendMessage("§aTeleportado para a warp: §f" + warp.getName());
            return true;
        }

        return false;
    }
}
