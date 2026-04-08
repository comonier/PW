package com.comonier.plugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Utility class for common tasks like color formatting and sound playing.
 * Ensures compatibility across different Minecraft versions.
 */
public class PWUtils {

    /*
     * Translates '&' color codes into Minecraft color format.
     */
    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /*
     * Translates a list of strings with color codes.
     */
    public static List<String> color(List<String> messages) {
        return messages.stream().map(PWUtils::color).collect(Collectors.toList());
    }

    /*
     * Plays a sound for a specific player at their location.
     */
    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    /*
     * Plays a sound at a specific location for everyone nearby.
     */
    public static void playSoundAt(Location loc, Sound sound) {
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, sound, 1.0f, 1.0f);
        }
    }
}
