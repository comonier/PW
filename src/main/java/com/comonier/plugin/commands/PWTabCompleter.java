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
 * Fixed Tab Completer.
 * Strips color codes from suggestions to prevent "Illegal Characters" disconnect.
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

        List<String> warpCommands = Arrays.asList("pw", "pwedit", "pweditplayer", "pwsetname", "pwsetlore", "pwseticon", "pwdel", "pwreset");

        if (warpCommands.contains(cmdName) && args.length == 1) {
            completions.addAll(plugin.getWarpManager().getWarps().stream()
                    // NEW: Strip color codes from the name before suggesting it in chat
                    .map(w -> w.getName().replaceAll("(?i)&[0-9A-FK-OR]", ""))
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList()));
        }

        if (cmdName.equals("pwdel") && args.length == 2) {
            if ("confirm".startsWith(currentArg)) completions.add("confirm");
        }

        Collections.sort(completions);
        return completions;
    }
}
