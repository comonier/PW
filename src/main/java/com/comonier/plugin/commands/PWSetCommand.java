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
 * Handles /pwset with limits, safety and land protection.
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

        String warpName = args[0];
        if (warpManager.getWarp(warpName) != null) {
            player.sendMessage(plugin.getMessage("warp-exists"));
            return true;
        }

        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) icon.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            icon.setItemMeta(meta);
        }

        Warp newWarp = new Warp(warpName, player.getUniqueId(), player.getName(), player.getLocation(), icon);
        warpManager.createWarp(newWarp);

        player.sendMessage(plugin.getMessage("warp-created").replace("{warp}", warpName));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        if (plugin.getConfig().getBoolean("announcements.global-chat")) {
            Bukkit.broadcastMessage(plugin.getMessage("broadcast-create").replace("{player}", player.getName()).replace("{warp}", warpName));
        }

        return true;
    }

    private int getWarpLimit(Player player) {
        if (player.hasPermission("pw.limit.*") || player.isOp()) return 999;
        int max = plugin.getConfig().getInt("settings.default-warp-limit", 5);
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith("pw.limit.")) {
                try {
                    int val = Integer.parseInt(perm.replace("pw.limit.", ""));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return max;
    }
}
