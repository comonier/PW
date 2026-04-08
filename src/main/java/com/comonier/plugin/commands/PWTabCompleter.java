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
 * Dynamic Tab Completer with null-safety and syntax guarding.
 */
public class PWTabCompleter implements TabCompleter {

    private final PW plugin;

    public PWTabCompleter(PW plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Guard against empty args or nulls to prevent console errors
        if (args == null || args.length == 0) return Collections.emptyList();

        List<String> completions = new ArrayList<>();
        String cmdName = command.getName().toLowerCase();
        String currentArg = args[args.length - 1].toLowerCase();

        // List of commands that suggest warp names as the first argument
        List<String> warpCommands = Arrays.asList("pw", "pwedit", "pweditplayer", "pwsetname", "pwsetlore", "pwseticon", "pwdel", "pwreset");

        if (warpCommands.contains(cmdName) && args.length == 1) {
            completions.addAll(plugin.getWarpManager().getWarps().stream()
                    .map(Warp::getName)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList()));
        }

        // Specific guard for /pwdel confirmation
        if (cmdName.equals("pwdel") && args.length == 2) {
            if ("confirm".startsWith(currentArg)) completions.add("confirm");
        }

        Collections.sort(completions);
        return completions;
    }
}
