package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.*;
import com.comonier.plugin.models.Warp;
import com.comonier.plugin.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

/*
 * Handles /pwset.
 * Fixed in v1.3: Clean ID used for all announcements to prevent formatting leaks.
 */
public class PWSetCommand implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final ProtectionManager protectionManager;

    public PWSetCommand(PW plugin, WarpManager warpManager, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.protectionManager = protectionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("pw.set")) {
            player.sendMessage(plugin.getMessage("no-permission").replace("{permission}", "pw.set"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUse: /pwset <name>");
            return true;
        }

        String warpId = args[0].toLowerCase();
        int limit = getWarpLimit(player);
        if (warpManager.getPlayerWarps(player.getUniqueId()).size() >= limit && !player.hasPermission("pw.admin")) {
            player.sendMessage(plugin.getMessage("limit-reached").replace("{limit}", String.valueOf(limit)));
            return true;
        }

        if (!protectionManager.isSafeLocation(player.getLocation())) {
            player.sendMessage(plugin.getMessage("unsafe-location"));
            return true;
        }

        if (!protectionManager.canBuild(player, player.getLocation())) {
            player.sendMessage(plugin.getMessage("no-build-permission"));
            return true;
        }

        if (warpManager.getWarp(warpId) != null) {
            player.sendMessage(plugin.getMessage("warp-exists"));
            return true;
        }

        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) icon.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            icon.setItemMeta(meta);
        }

        Warp newWarp = new Warp(warpId, player.getUniqueId(), player.getName(), player.getLocation(), icon);
        warpManager.createWarp(newWarp);

        player.sendMessage(plugin.getMessage("warp-created").replace("{warp}", warpId));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        if (plugin.getConfig().getBoolean("announcements.global-chat")) {
            Bukkit.broadcastMessage(plugin.getMessage("broadcast-create")
                    .replace("{player}", player.getName())
                    .replace("{warp}", warpId));
        }

        if (plugin.getConfig().getBoolean("announcements.discord.enabled")) {
            String title = plugin.getMessage("discord-announcement-title");
            // Fixed: Always use warpId (clean) for Discord
            String desc = plugin.getMessage("discord-announcement-desc")
                    .replace("{player}", player.getName())
                    .replace("{warp}", warpId);
            sendDiscordNotice(title, desc, 65280);
        }

        return true;
    }

    private void sendDiscordNotice(String title, String desc, int color) {
        String url = plugin.getConfig().getString("announcements.discord.webhook-url");
        if (url == null || url.isEmpty() || url.length() < 10) return;
        String json = "{\"username\":\"" + plugin.getConfig().getString("announcements.discord.username") + "\",\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + desc + "\",\"color\":" + color + "}]}";
        DiscordWebhook.send(url, json);
    }

    private int getWarpLimit(Player player) {
        if (player.hasPermission("pw.limit.*") || player.isOp()) return 999;
        int max = plugin.getConfig().getInt("settings.default-warp-limit", 5);
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (pai.getPermission().startsWith("pw.limit.")) {
                try {
                    int val = Integer.parseInt(pai.getPermission().replace("pw.limit.", ""));
                    if (val > max) max = val;
                } catch (Exception ignored) {}
            }
        }
        return max;
    }
}
