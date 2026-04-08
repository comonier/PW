# PW - Player Warps System

**Full Support for 1.19+, 1.20+ and 1.21.x (Tested on 1.21.1)**
**Native Hooks:** GriefPrevention, RedProtect, and WorldGuard
**Platform:** Optimized for Folia, Paper, and Spigot

The ultimate high-performance Player Warp system for modern Minecraft servers. Designed with a focus on simplicity for both Java and Bedrock players, featuring a robust GUI and a separate **ID/DisplayName** engine to prevent chat kicks and command bugs.

## 🚀 Features

*   **Dynamic Paged GUI:** Organized 54-slot menu system with smart paging and player filtering.
*   **Player Filtering Logic:** A unique mode to browse warps by owner, displaying skin heads and grouping warps intuitively.
*   **Protection Integration:** Native hooks for **WorldGuard**, **GriefPrevention**, and **RedProtect**. Players can only create warps where they have Trust/Build permissions.
*   **Safety Teleport:** Automatic block validation (blocks lava, water, air, crops, fences, etc.) with an auto-lock system for unsafe locations.
*   **Dual-Storage Support:** High-performance **MySQL** for networks or local **SQLite** for standalone servers.
*   **Multi-Language Core:** Full translation support via files (**PT, EN, ES, RU**).
*   **Discord Webhooks:** Real-time notifications for warp creation, deletion, and teleport events.
*   **Permission-Based Limits:** Define warp limits dynamically using permissions (e.g., `pw.limit.10`).

---

## 🛠 Commands & Permissions

### Player Commands


| Command | Description | Permission |
| :--- | :--- | :--- |
| `/pw` | Opens the main menu or teleports to a warp | `pw.use` |
| `/pw <warp>` | Teleports directly to a specific warp | `pw.tp` |
| `/pwset <warp>` | Creates a new warp at your current location | `pw.set` |
| `/pwedit <warp>` | Opens the visual editor panel for your warp | `pw.edit` |
| `/pwsetname <warp> <name>`| Changes the visual name (Supports Colors) | `pw.setname` |
| `/pwsetlore <warp> <lore>`| Changes the warp description (Supports Colors) | `pw.setlore` |
| `/pwseticon <warp>` | Changes the icon using the item in your hand | `pw.seticon` |
| `/pwdel <warp> confirm` | Permanently removes your warp | `pw.del` |
| `/pwreset <warp>` | Resets the warp location to your current position | `pw.reset` |

### Admin Commands


| Command | Description | Permission |
| :--- | :--- | :--- |
| `/pweditplayer <warp>` | Opens the editor for any player's warp | `pw.admin` |
| `/pwreload` | Reloads all configurations, menus, and languages | `pw.admin` |

---

## 📦 Installation

1.  Download the latest `PW.jar` and place it in your `/plugins/` folder.
2.  Ensure **Vault** is installed (Mandatory Dependency).
3.  Restart your server to generate the configuration files.

---

## ⚠️ Important Notice
*   **Java 17+** is required.
*   Fully compatible with **Folia** and **Geyser**.
*   **Dual-Name System:** Internal IDs are always clean text (no colors) to ensure command stability, while Display Names support full color formatting for the GUI.

Developed with ❤️ by **Comonier**