package com.comonier.plugin.managers;

import com.comonier.plugin.PW;
import com.comonier.plugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;

/*
 * Handles SQLite and MySQL persistence.
 * Fixed: Visit loading logic and connection stability.
 */
public class DatabaseManager {

    private final PW plugin;
    private Connection connection;
    private final String type;

    public DatabaseManager(PW plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        initDatabase();
        createTable();
    }

    private void initDatabase() {
        try {
            if (type.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.host");
                String port = plugin.getConfig().getString("database.port");
                String db = plugin.getConfig().getString("database.name");
                String user = plugin.getConfig().getString("database.user");
                String pass = plugin.getConfig().getString("database.password");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            } else {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/warps.db");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS pw_warps (" +
                "name_id VARCHAR(64) PRIMARY KEY, " +
                "display_name TEXT, " +
                "owner_uuid VARCHAR(36), " +
                "owner_name TEXT, " +
                "world TEXT, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, " +
                "icon TEXT, " +
                "lore TEXT, " +
                "visits INT, " +
                "created_at LONG, " +
                "locked BOOLEAN)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveWarp(Warp warp) {
        String sql = "REPLACE INTO pw_warps (name_id, display_name, owner_uuid, owner_name, world, x, y, z, yaw, pitch, icon, lore, visits, created_at, locked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warp.getName().toLowerCase());
            pstmt.setString(2, warp.getName());
            pstmt.setString(3, warp.getOwnerUUID().toString());
            pstmt.setString(4, warp.getOwnerName());
            pstmt.setString(5, warp.getLocation().getWorld().getName());
            pstmt.setDouble(6, warp.getLocation().getX());
            pstmt.setDouble(7, warp.getLocation().getY());
            pstmt.setDouble(8, warp.getLocation().getZ());
            pstmt.setFloat(9, warp.getLocation().getYaw());
            pstmt.setFloat(10, warp.getLocation().getPitch());
            pstmt.setString(11, serializeItem(warp.getIcon()));
            pstmt.setString(12, String.join(";", warp.getLore()));
            pstmt.setInt(13, warp.getVisits());
            pstmt.setLong(14, warp.getCreatedAt());
            pstmt.setBoolean(15, warp.isLocked());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteWarp(String name) {
        String sql = "DELETE FROM pw_warps WHERE name_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Warp> loadWarps() {
        Map<String, Warp> map = new HashMap<>();
        String sql = "SELECT * FROM pw_warps";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Location loc = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch")
                );
                Warp warp = new Warp(rs.getString("display_name"), 
                                     UUID.fromString(rs.getString("owner_uuid")),
                                     rs.getString("owner_name"), loc, 
                                     deserializeItem(rs.getString("icon")));
                
                warp.setLocked(rs.getBoolean("locked"));
                String loreRaw = rs.getString("lore");
                if (loreRaw != null && loreRaw.length() > 0) {
                    warp.setLore(new ArrayList<>(Arrays.asList(loreRaw.split(";"))));
                }

                // Visit fix using reverse logic to avoid < symbol
                int targetVisits = rs.getInt("visits");
                while (targetVisits > 0) {
                    warp.addVisit();
                    targetVisits--;
                }

                map.put(rs.getString("name_id"), warp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private String serializeItem(ItemStack item) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private ItemStack deserializeItem(String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            return new ItemStack(org.bukkit.Material.PLAYER_HEAD);
        }
    }
}
