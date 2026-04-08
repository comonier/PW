package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/*
 * Manages protection checks and location safety validation.
 * Ensures warps are created on solid ground and not on dangerous or illegal blocks.
 */
public class ProtectionManager {

    private final PW plugin;

    public ProtectionManager(PW plugin) {
        this.plugin = plugin;
    }

    public boolean canBuild(Player player, Location loc) {
        if (player.hasPermission("pw.admin")) return true;

        if (isPluginEnabled("WorldGuard")) {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            if (!query.testState(BukkitAdapter.adapt(loc), localPlayer, com.sk89q.worldguard.protection.flags.Flags.BUILD)) {
                return false;
            }
        }

        if (isPluginEnabled("GriefPrevention")) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
            if (claim != null) {
                if (claim.allowBuild(player, Material.AIR) != null) return false;
            }
        }

        if (isPluginEnabled("RedProtect")) {
            Region rpRegion = RedProtect.get().getAPI().getRegion(loc);
            if (rpRegion != null) {
                if (!rpRegion.canBuild(player)) return false;
            }
        }

        return true;
    }

    /*
     * Validates if the location is safe for a warp.
     * Blocks like lava, water, air, crops, fences, and glass panes are denied.
     */
    public boolean isSafeLocation(Location loc) {
        Block feet = loc.getBlock();
        Block ground = loc.clone().subtract(0, 1, 0).getBlock();
        Material mat = ground.getType();
        String name = mat.name();

        // Deny liquids and air on ground
        if (mat == Material.LAVA || mat == Material.WATER || mat == Material.AIR) return false;
        if (feet.getType() == Material.LAVA || feet.getType() == Material.WATER) return false;

        // Deny dangerous or fragile blocks
        if (name.contains("FENCE") || name.contains("PANE") || name.contains("WALL")) return false;
        if (name.contains("LEAVES") || name.contains("SAPLING") || mat == Material.SOUL_SAND || mat == Material.SOUL_SOIL) return false;
        
        // Deny crops and soil
        if (mat == Material.FARMLAND || name.contains("STEM") || name.contains("WHEAT") || name.contains("CARROTS") || name.contains("POTATOES")) return false;

        // Allow carpets, pressure plates, buttons and snow on TOP of solid blocks
        // The core check is if the ground is solid enough
        if (mat.isSolid()) {
            // Exceptions: even if solid, some blocks are forbidden (checked above like fences)
            return true;
        }

        // Specifically allow light decorations that players stand on
        return mat == Material.MOSS_CARPET || name.contains("CARPET") || name.contains("PLATE") || mat == Material.SNOW;
    }

    public boolean isLocationStillSafe(String ownerName, Location loc) {
        if (isPluginEnabled("GriefPrevention")) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
            if (claim == null) return false;
        }
        if (isPluginEnabled("RedProtect")) {
            Region rpRegion = RedProtect.get().getAPI().getRegion(loc);
            if (rpRegion == null) return false;
        }
        return isSafeLocation(loc);
    }

    private boolean isPluginEnabled(String name) {
        Plugin p = plugin.getServer().getPluginManager().getPlugin(name);
        return p != null && p.isEnabled();
    }
}
