package com.comonier.plugin.commands;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Dynamic Tab Completer for PW.
 * Uses the internal Warp ID (clean text) to provide real-time suggestions without kicks.
 */
public class PWTabCompleter implements TabCompleter {

    private final PW plugin;

    public PWTabCompleter(PW plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args == null || args.length == 0) return Collections.emptyList();

        List<String> completions = new ArrayList<>();
        String cmdName = command.getName().toLowerCase();
        String currentArg = args[args.length - 1].toLowerCase();

        // Commands that require warp ID suggestions
        List<String> warpCommands = Arrays.asList("pw", "pwedit", "pweditplayer", "pwsetname", "pwsetlore", "pwseticon", "pwdel", "pwreset");

        if (warpCommands.contains(cmdName) && args.length == 1) {
            // Access the real-time memory map of WarpManager
            completions.addAll(plugin.getWarpManager().getWarps().stream()
                    .map(Warp::getId) // Always suggest the clean ID
                    .filter(id -> id.startsWith(currentArg))
                    .collect(Collectors.toList()));
        }

        // Confirmation argument for deletion
        if (cmdName.equals("pwdel") && args.length == 2) {
            if ("confirm".startsWith(currentArg)) completions.add("confirm");
        }

        Collections.sort(completions);
        return completions;
    }
}
