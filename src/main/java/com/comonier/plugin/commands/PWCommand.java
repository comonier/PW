package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.*;
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
 * Main command /pw.
 * Now using externalized messages for all outputs.
 */
public class PWCommand implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final MenuManager menuManager;
    private final ProtectionManager protectionManager;

    public PWCommand(PW plugin, WarpManager warpManager, MenuManager menuManager, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.menuManager = menuManager;
        this.protectionManager = protectionManager;
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
            menuManager.openMainMenu(player);
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
            sendDiscordNotice("Teleport Notice", "Player **" + player.getName() + "** traveled to warp: **" + warp.getName() + "**", 3447003);
        }
        return true;
    }

    private void sendDiscordNotice(String title, String desc, int color) {
        String url = plugin.getConfig().getString("announcements.discord.webhook-url");
        if (url == null || url.isEmpty() || url.contains("your-link-here")) return;
        String json = "{\"username\":\"" + plugin.getConfig().getString("announcements.discord.username") + "\",\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + desc + "\",\"color\":" + color + "}]}";
        DiscordWebhook.send(url, json);
    }
}
