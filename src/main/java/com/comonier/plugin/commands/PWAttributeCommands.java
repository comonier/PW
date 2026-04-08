package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.WarpManager;
import com.comonier.plugin.managers.ProtectionManager;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * Handles attribute modification commands with full message migration.
 * Commands: /pwsetname, /pwsetlore, /pwseticon, /pwdel, /pwreset.
 */
public class PWAttributeCommands implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;
    private final ProtectionManager protectionManager;

    public PWAttributeCommands(PW plugin, WarpManager warpManager, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
        this.protectionManager = protectionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) return false;

        Warp warp = warpManager.getWarp(args[0]);
        if (warp == null) {
            player.sendMessage(plugin.getMessage("warp-not-found"));
            return true;
        }

        boolean isAdmin = player.hasPermission("pw.admin");
        boolean isOwner = warp.getOwnerUUID().equals(player.getUniqueId());

        if (!isOwner && !isAdmin) {
            player.sendMessage(plugin.getMessage("no-permission").replace("{permission}", "pw.edit"));
            return true;
        }

        switch (label.toLowerCase()) {
            case "pwsetname":
                if (!checkPerm(player, "pw.setname")) return true;
                if (args.length > 1) {
                    String oldName = warp.getName();
                    String newName = args[1];
                    warpManager.deleteWarp(oldName);
                    warp.setName(newName);
                    warpManager.createWarp(warp);
                    player.sendMessage(plugin.getMessage("success.name-changed"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
                break;

            case "pwsetlore":
                if (!checkPerm(player, "pw.setlore")) return true;
                if (args.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String rawLore = sb.toString().trim();
                    String[] lines = rawLore.split("[,;]");
                    List<String> formattedLore = new ArrayList<>();
                    for (String line : lines) {
                        formattedLore.add(line.trim().replace("&", "§"));
                    }
                    warp.setLore(formattedLore);
                    warpManager.saveWarp(warp);
                    player.sendMessage(plugin.getMessage("success.lore-changed"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
                break;

            case "pwseticon":
                if (!checkPerm(player, "pw.seticon")) return true;
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType() == Material.AIR) {
                    player.sendMessage("§cSegure um item na mão!");
                    return true;
                }
                warp.setIcon(handItem.clone());
                warpManager.saveWarp(warp);
                player.sendMessage(plugin.getMessage("success.icon-changed"));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;

            case "pwreset":
                if (!checkPerm(player, "pw.reset")) return true;
                if (!protectionManager.isSafeLocation(player.getLocation())) {
                    player.sendMessage(plugin.getMessage("unsafe-location"));
                    return true;
                }
                if (!protectionManager.canBuild(player, player.getLocation())) {
                    player.sendMessage(plugin.getMessage("no-build-permission"));
                    return true;
                }
                warp.setLocation(player.getLocation());
                warp.setLocked(false);
                warpManager.saveWarp(warp);
                player.sendMessage(plugin.getMessage("success.location-reset"));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;

            case "pwdel":
                if (!checkPerm(player, "pw.del")) return true;
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    String warpName = warp.getName();
                    warpManager.deleteWarp(warpName);
                    player.sendMessage(plugin.getMessage("warp-deleted"));
                    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);

                    if (plugin.getConfig().getBoolean("announcements.global-chat")) {
                        Bukkit.broadcastMessage(plugin.getMessage("broadcast-delete")
                                .replace("{player}", player.getName())
                                .replace("{warp}", warpName));
                    }
                    if (plugin.getConfig().getBoolean("announcements.discord.enabled")) {
                        sendDiscordNotice("Warp Deletada", "O jogador **" + player.getName() + "** deletou a warp: **" + warpName + "**", 16711680);
                    }
                } else {
                    player.sendMessage("§cUse: /pwdel " + warp.getName() + " confirm");
                }
                break;
        }
        return true;
    }

    private void sendDiscordNotice(String title, String desc, int color) {
        String url = plugin.getConfig().getString("announcements.discord.webhook-url");
        if (url == null || url.isEmpty() || url.contains("your-link-here")) return;
        String json = "{\"username\":\"" + plugin.getConfig().getString("announcements.discord.username") + "\",\"embeds\":[{\"title\":\"" + title + "\",\"description\":\"" + desc + "\",\"color\":" + color + "}]}";
        DiscordWebhook.send(url, json);
    }

    private boolean checkPerm(Player p, String perm) {
        if (p.hasPermission(perm) || p.hasPermission("pw.use") || p.hasPermission("pw.admin")) return true;
        p.sendMessage(plugin.getMessage("no-permission").replace("{permission}", perm));
        return false;
    }
}
