package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.managers.WarpManager;
import com.comonier.plugin.models.Warp;
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
 * Handles attribute modification commands with real-time database saving.
 * Includes: /pwsetname, /pwsetlore, /pwseticon, /pwdel, /pwreset and /pwlock.
 */
public class PWAttributeCommands implements CommandExecutor {

    private final PW plugin;
    private final WarpManager warpManager;

    public PWAttributeCommands(PW plugin, WarpManager warpManager) {
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) return false;

        // The first argument is always the warp name
        Warp warp = warpManager.getWarp(args[0]);
        if (warp == null) {
            player.sendMessage("§cWarp não encontrada.");
            return true;
        }

        // Security: Check Ownership or Admin permission
        boolean isAdmin = player.hasPermission("pw.admin");
        boolean isOwner = warp.getOwnerUUID().equals(player.getUniqueId());

        if (!isOwner && !isAdmin) {
            player.sendMessage("§cVocê não tem permissão para alterar esta warp.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "pwsetname":
                if (!checkPerm(player, "pw.setname")) return true;
                if (args.length > 1) {
                    String newName = args[1];
                    warpManager.deleteWarp(warp.getName()); // Remove old entry from DB/Memory
                    warp.setName(newName);
                    warpManager.createWarp(warp); // Re-insert as new entry
                    player.sendMessage("§aNome da warp alterado com sucesso.");
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
                    warpManager.saveWarp(warp); // Save to DB
                    player.sendMessage("§aLore alterada com sucesso.");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
                break;

            case "pwseticon":
                if (!checkPerm(player, "pw.seticon")) return true;
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType() == Material.AIR) {
                    player.sendMessage("§cSegure um item na mão para definir como ícone.");
                    return true;
                }
                warp.setIcon(handItem.clone());
                warpManager.saveWarp(warp); // Save to DB
                player.sendMessage("§aÍcone alterado com sucesso.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;

            case "pwreset":
                if (!checkPerm(player, "pw.reset")) return true;
                warp.setLocation(player.getLocation());
                warpManager.saveWarp(warp); // Save to DB
                player.sendMessage("§aLocalização da warp redefinida com sucesso.");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                break;

            case "pwdel":
                if (!checkPerm(player, "pw.del")) return true;
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    warpManager.deleteWarp(warp.getName());
                    player.sendMessage("§aWarp removida com sucesso.");
                    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
                } else {
                    player.sendMessage("§cDigite /pwdel " + warp.getName() + " confirm para confirmar.");
                }
                break;
        }
        return true;
    }

    private boolean checkPerm(Player p, String perm) {
        if (p.hasPermission(perm) || p.hasPermission("pw.use") || p.hasPermission("pw.admin")) {
            return true;
        }
        p.sendMessage("§cVocê não possui a permissão: " + perm);
        return false;
    }
}
