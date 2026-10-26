# CustomBosses

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11--26.3-green?style=for-the-badge&logo=minecraft&logoColor=white)](https://spigotmc.org/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20Folia-blue?style=for-the-badge)](https://papermc.io/)
[![Author](https://img.shields.io/badge/Author-Lino-orange?style=for-the-badge)](https://github.com/lino9999)
[![License](https://img.shields.io/badge/License-MIT-red?style=for-the-badge)](LICENSE)

> **Next-Generation Custom Boss Creation, Automated World Spawners & Sacrificial Altars for Minecraft Servers.**

---

## 📌 About CustomBosses

**CustomBosses** is an advanced, high-performance Spigot/Paper/Folia plugin designed for server owners seeking rich PvE encounters without lag, memory leaks, or brittle internal NMS code. 

Built with clean, modern object-oriented architecture and pure Bukkit/Paper APIs, CustomBosses provides an interactive in-game GUI editor, 38 unique boss abilities, ranked damage contribution rewards, custom boss spawn eggs, sacrificial world altars, and three independent spawner systems.

### 🏷️ GitHub Repository Metadata (Recommended)
- **Description / About**: `Next-Generation Custom Bosses, 38 Unique Abilities, Automated Spawners & Sacrificial Altars for Minecraft 1.21.11 to 26.3.`
- **Website**: `https://github.com/lino9999/CustomBosses`
- **Topics / SEO Tags**:
  `minecraft`, `minecraft-plugin`, `custom-bosses`, `boss-plugin`, `paper`, `spigot`, `folia`, `spigotmc`, `pve`, `mmorpg`, `minecraft-spawner`, `altars`, `java-21`, `gameplay`, `bossbar`

---

## ⚡ Core Highlights

- 🖥️ **Interactive In-Game GUI Editor**: Configure boss health, damage, speed, scale, custom model data, equipment, immunities, drops, and abilities visually.
- 🛡️ **Stacker Plugins Compatibility**: Fully compatible with WildStacker, RoseStacker, StackMob, and UltimateStacker. Bosses and their mount passengers are automatically protected from being merged into regular mob stacks.
- ⚔️ **38 Dynamic Boss Abilities**: Complete ability portfolio spanning periodic AOE, targeted combat, defense retaliation, phase transformations, and on-death revenge strikes.
- 🗺️ **3 Automated Spawner Systems**:
  - **Region Spawners**: Define 3D cuboid zones using the admin wand, complete with mob caps, time-of-day constraints, and respawn intervals.
  - **Coordinate Spawners**: Fixed position or real-world calendar date triggers (`YYYY-MM-DD`).
  - **Natural Spawners**: Player proximity wilderness spawns with customizable light level ranges and distance radiuses.
- 🔮 **Sacrificial Altars**: World blocks featuring floating holograms that require item tributes to summon bosses.
- 🏆 **Ranked Damage Contribution Rewards**: Grant items and run console commands based on combat rankings (Top 1, Top 2, Top 3 damage dealers, killer, or all contributors).
- 📊 **Boss Bars & Health HUD**: Customizable colors (Red, Purple, Blue, Green, etc.), styles (Solid, Notched), and live health percentage formatting.
- 🥚 **Boss Spawn Eggs & Admin Wand**: Portable spawn eggs with custom PDC tags and an admin wand with shift-right-click quick boss spawning.
- 🧩 **PlaceholderAPI Expansion**: Expose live boss counts and boss health across scoreboards, tablists, and menus.

---

## 🎮 38 Boss Abilities Overview

| Ability | Trigger | Description |
| :--- | :--- | :--- |
| **Transform** | Low Health | At `<X%` HP, triggers an invulnerability spiral animation and transforms into a next-phase boss profile. |
| **Projectile Throw** | Periodic | Fires targeted Fireballs, Snowballs, Arrows, or Wither Skulls towards nearby players. |
| **Kill Aura** | Periodic | Teleports behind the target's blind spot, strikes with sweep effects, and damages armor durability. |
| **Teleport** | Periodic | Proximity teleport around players with portal particle effects. |
| **Vacuum Items** | Periodic | Sucks up all dropped items on the ground within range and heals the boss per item absorbed. |
| **Hunger Drain** | Periodic | Drains the food meter of surrounding players. |
| **Command Dispatch** | Periodic | Executes console commands with dynamic `%player%`, `%boss%`, and coordinate placeholders. |
| **Getsuga Tenshou** | Periodic | Launches a charged, high-velocity dark skull with trailing particle emissions. |
| **Leap Attack** | Periodic | Performs a forward leaping lunge towards distant targets. |
| **Web Trap** | Periodic | Traps target locations in cobwebs that automatically dissolve after 4 seconds (80 ticks). |
| **Vortex Pull** | Periodic | Pulls all nearby players towards the boss with reverse velocity and applies damage. |
| **Velocity Reversal** | Periodic | Reverses player movement trajectories unexpectedly. |
| **Scatter Blast** | Periodic | Detonates multiple cluster explosions in radius without terrain destruction. |
| **Regeneration** | Periodic | Restores health to the boss accompanied by heart particles. |
| **Freeze Zone** | Periodic | Creates an AOE slowness aura freezing nearby entities. |
| **Toxic Fog** | Periodic | Spawns a noxious cloud applying poison over time. |
| **Heat Wave** | Periodic | Ignites surrounding players for a set duration. |
| **Zeus Strike** | Periodic | Summons targeted lightning strikes upon players. |
| **Teleport Swap** | Periodic | Swaps physical positions between the boss and a target player. |
| **Power Wave** | Periodic | Radial shockwave dealing knockback and equipment durability damage. |
| **Soul Mark** | Periodic | Marks a player with red particles; detonates for heavy true damage if they fail to escape in time. |
| **Earthquake** | Periodic | Shakes the ground, launching nearby players upward with block particles. |
| **Freeze Attack** | On Attack | Melee hits inflict slowness and freezing ticks. |
| **Toss Attack** | On Attack | Melee strikes toss players into the air. |
| **Life Steal** | On Attack | Siphons health from victims to the boss. |
| **Witchcraft Attack** | On Attack | Inflicts blindness and weakness curses. |
| **Execute Threshold** | On Attack | Instantly slays targets whose health drops below the configured threshold %. |
| **Target Cleanse** | On Attack | Strips all positive potion effects from the victim. |
| **Thorns Armor** | On Damaged | Reflects damage back to close-range attackers within 4 blocks. |
| **Quick Buff** | On Damaged | Applies Speed and Resistance buffs to the boss upon taking damage. |
| **Counter Hit** | On Damaged | Reflects a percentage of incoming damage directly back to the attacker. |
| **Self Cleanse** | On Damaged | Purges all negative potion effects from the boss. |
| **Phoenix Immortal** | On Death | Chance to resurrect with full health and a fiery explosion. |
| **Shadow Clones** | On Death | Duplicates into clone minions upon death. |
| **Minion Spawner** | On Death | Summons a horde of minion bosses to avenge their master. |
| **Last Effect** | On Death | Afflicts the killer with a fatal wither/slowness curse. |
| **Death Explosion** | On Death | Spawns primed TNT at the death site. |
| **Fatal Curse** | On Death | Striking death blow: instantly eliminates the killer! |

---

## 📜 Commands & Permissions

| Command | Aliases | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/custombosses menu` | `/cb menu`, `/cb gui` | `custombosses.menu` | Opens the full in-game GUI Control Panel. |
| `/custombosses spawn <id> [world x y z]` | `/cb spawn` | `custombosses.spawn` | Spawns a custom boss at your location or target coordinates. |
| `/custombosses spawnegg <id> [player] [qty]` | `/cb giveegg` | `custombosses.egg` | Gives a boss spawn egg. |
| `/custombosses wand` | `/cb wand` | `custombosses.wand` | Receives the admin selection wand. |
| `/custombosses altar create <id> <boss_id>` | `/cb altar` | `custombosses.altar` | Creates an altar at the block you are looking at. |
| `/custombosses altar remove <id>` | `/cb altar` | `custombosses.altar` | Removes an altar and its hologram. |
| `/custombosses spawner create region <id> <boss_id>` | `/cb spawner` | `custombosses.spawner` | Creates a region spawner using current wand selection. |
| `/custombosses spawner remove <id>` | `/cb spawner` | `custombosses.spawner` | Deletes a spawner. |
| `/custombosses killall [boss_id]` | `/cb killall` | `custombosses.kill` | Removes all active bosses across all worlds. |
| `/custombosses list` | `/cb list` | `custombosses.list` | Lists all registered boss templates. |
| `/custombosses reload` | `/cb reload` | `custombosses.reload` | Reloads all configuration files and altars. |

---

## 🧩 PlaceholderAPI Placeholders

| Placeholder | Output |
| :--- | :--- |
| `%custombosses_active%` | Total number of currently active bosses across all worlds |
| `%custombosses_total%` | Total number of registered boss profiles loaded |
| `%custombosses_boss_active_<boss_id>%` | Number of living instances of `<boss_id>` |
| `%custombosses_boss_health_<boss_id>%` | Current health of the active instance of `<boss_id>` |

---

## 📦 Installation & Quick Start

1. Download `CustomBosses-1.0.0.jar` from [Releases](https://github.com/lino9999/CustomBosses/releases).
2. Place the JAR into your server's `plugins/` directory.
3. Start the server (Requires Java 21+ and Paper/Spigot 1.21.11 to 26.3).
4. Run `/cb menu` in-game to start customizing bosses immediately!

---

## 📄 License & Credits

- Developed and rewritten by **Lino** (`https://github.com/lino9999`).
- Licensed under the open-source **MIT License**.
