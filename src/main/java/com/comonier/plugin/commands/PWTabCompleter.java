package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Provides tab-completion for all PW commands.
 * Lists available warps based on context.
 */
public class PWTabCompleter implements TabCompleter {

    private final PW plugin;

    public PWTabCompleter(PW plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> warpNames = plugin.getWarpManager().getWarps().stream()
                    .map(Warp::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            completions.addAll(warpNames);
        }

        return completions;
    }
}
