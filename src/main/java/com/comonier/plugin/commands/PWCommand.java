package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.*;
import com.comonier.plugin.menus.WarpListMenu;
import com.comonier.plugin.models.Warp;
import com.comonier.plugin.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/*
 * Handles /pw command.
 * Fixed in v1.3: Discord Webhook now uses the clean ID to avoid any color formatting.
 */
public class PWCommand implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final WarpListMenu warpListMenu;
    private final ProtectionManager protectionManager;

    public PWCommand(PW plugin, WarpManager warpManager, MenuManager menuManager, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.protectionManager = protectionManager;
        this.warpListMenu = new WarpListMenu(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("pw.use")) {
            player.sendMessage(plugin.getMessage("no-permission").replace("{permission}", "pw.use"));
            return true;
        }

        if (args.length == 0) {
            warpListMenu.open(player, warpManager.getAllWarpsSorted(), "§8Player Warps", 0);
            return true;
        }

        Warp warp = warpManager.getWarp(args[0]);
        if (warp == null) {
            player.sendMessage(plugin.getMessage("warp-not-found"));
            return true;
        }

        if (!protectionManager.isLocationStillSafe(warp.getOwnerName(), warp.getLocation())) {
            if (!warp.isLocked()) {
                warp.setLocked(true);
                warpManager.saveWarp(warp);
            }
            player.sendMessage(plugin.getMessage("warp-auto-locked"));
            return true;
        }

        if (warp.isLocked() && !warp.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("pw.admin")) {
            player.sendMessage(plugin.getMessage("warp-locked-error"));
            return true;
        }

        player.teleport(warp.getLocation());
        warp.addVisit();
        warpManager.saveWarp(warp);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.sendMessage(plugin.getMessage("warp-teleport").replace("{warp}", warp.getName()));

        if (plugin.getConfig().getBoolean("announcements.global-chat")) {
            Bukkit.broadcastMessage(plugin.getMessage("broadcast-teleport")
                    .replace("{player}", player.getName())
                    .replace("{warp}", warp.getName()));
        }

        if (plugin.getConfig().getBoolean("announcements.discord.enabled")) {
            String title = plugin.getMessage("discord-teleport-title");
            // Fixed: Use warp.getId() to send ONLY the clean name to Discord
            String desc = plugin.getMessage("discord-teleport-desc")
                    .replace("{player}", player.getName())
                    .replace("{warp}", warp.getId());
            
            sendDiscordNotice(title, desc, 3447003);
        }

        return true;
    }

    private void sendDiscordNotice(String title, String desc, int color) {
        String url = plugin.getConfig().getString("announcements.discord.webhook-url");
        if (url == null || url.isEmpty() || url.length() < 10) return;
        
        String username = plugin.getConfig().getString("announcements.discord.username", "Player Warps");
        String json = "{"
                + "\"username\": \"" + username + "\","
                + "\"embeds\": [{"
                + "\"title\": \"" + title + "\","
                + "\"description\": \"" + desc + "\","
                + "\"color\": " + color
                + "}]"
                + "}";
        
        DiscordWebhook.send(url, json);
    }
}
