# AI Agent Developer Guide - CustomBosses

Welcome to **CustomBosses**, a high-performance Spigot/Paper boss creation plugin developed by **Lino**.
This guide provides AI coding agents and human contributors with essential architecture knowledge, extension patterns, and token-saving rules to develop and maintain the codebase efficiently without wasting context tokens.

---

## 1. Token Economy & Exploration Rules

To minimize LLM token consumption when working with this repository, always follow these rules:

1. **Never read generated directories**: Do not read `target/`, `.work/`, `Plugin originale/`, or any `.jar` / `.zip` files. Refer to `.ignore`, `.gitignore`, and `.aiignore`.
2. **Targeted reading**: Read only the specific class file relevant to the task at hand rather than loading multiple directories into context.
3. **No Code Comments**: Strictly adhere to the project constraint: **never write code comments** (`//`, `/* */`, `/** */`) inside `.java` files. Use self-explanatory method names and clean OOP design.
4. **English Only**: All identifiers, log entries, default config entries, and user messages must be in English.

---

## 2. Architecture & Package Layout

The project follows a decoupled, modular OOP design located under `src/main/java/dev/lino/custombosses/`:

```
dev.lino.custombosses/
├── CustomBossesPlugin.java    # Main JavaPlugin entry point and lifecycle manager
├── ability/                   # Ability execution engine
│   ├── AbilityTrigger.java    # Triggers: PERIODIC, ON_ATTACK, ON_DAMAGED, LOW_HEALTH, ON_DEATH
│   ├── AbilityType.java       # Enum of all 38 supported boss abilities
│   ├── BossAbility.java       # Configuration holder for an ability instance
│   └── AbilityManager.java    # Core execution logic for combat and periodic effects
├── command/                   # Commands and tab completers
│   ├── CustomBossesCommand.java      # Command dispatcher for /custombosses (/cb, /bosses)
│   └── CustomBossesTabCompleter.java  # Auto-completion logic
├── config/                    # YAML configuration and localization
│   ├── ConfigManager.java     # Loads & serializes bosses.yml, spawners.yml, altars.yml, config.yml
│   └── MessageManager.java    # English message formatting with prefix & placeholders
├── gui/                       # Interactive Inventory GUI system
│   ├── MenuHolder.java        # Base InventoryHolder identifying menus
│   ├── MenuListener.java      # Dispatches click events safely
│   ├── MainMenu.java          # Root control panel
│   ├── BossListMenu.java      # Paginated boss template browser
│   ├── BossEditMenu.java      # Main boss modification menu
│   ├── AttributesMenu.java    # Visual attribute modifier (+/- health, damage, speed, scale)
│   ├── AbilitiesMenu.java     # Ability toggle & parameter adjustments
│   ├── EquipmentMenu.java     # Visual boss armor, weapons & drop chance editor
│   ├── DropsMenu.java         # Ranked drop & command reward editor
│   ├── SpawnerMenu.java       # Region, coordinate, and natural spawner toggles
│   ├── AltarMenu.java         # Sacrificial altar browser
│   └── BossPickerMenu.java    # Quick boss selection dialog
├── integration/               # Soft hooks
│   ├── CustomBossesPAPI.java  # PlaceholderAPI expansion (%custombosses_...%)
│   ├── WildStackerHook.java   # Multi-stacker compatibility (WildStacker, RoseStacker, StackMob)
│   └── WorldGuardHook.java    # Safe reflection hook for WorldGuard mob-spawning flag
├── listener/                  # Bukkit event listeners
│   ├── BossCombatListener.java   # Combat damage handling, slime split & creeper protection
│   ├── BossDeathListener.java    # Rewards, ranking distribution, and death abilities
│   ├── ChunkListener.java        # Chunk load boss recovery and bar restoration
│   ├── SpawnerEggListener.java   # Right-click spawn egg handler
│   ├── WandListener.java         # Admin wand region selection and quick spawning
│   └── AltarListener.java        # Altar block click & sacrifice checking
├── manager/                   # Runtime state managers
│   ├── BossManager.java          # Boss templates, spawning & equipment logic
│   ├── ActiveBossManager.java    # Live ActiveBoss registry, boss bars & tracking
│   ├── SpawnerManager.java       # Automated spawner ticking (region, coord, natural)
│   └── AltarManager.java         # Altar registration & hologram armor stand tracking
├── model/                     # Data models
│   ├── ActiveBoss.java           # Live boss entity wrapper (BossBar, passive AI, ambient particles)
│   ├── BossProfile.java          # Persistent boss configuration model (particles, passengers, potions)
│   ├── BossDrop.java             # Drop entry with chances, amounts & reward types
│   ├── RewardType.java           # KILLER, TOP_1, TOP_2, TOP_3, ALL_CONTRIBUTORS, CHANCE_DROP
│   ├── CuboidRegion.java         # 3D bounding box for region spawners
│   ├── RegionSpawner.java        # Periodic region spawner configuration
│   ├── CoordinateSpawner.java    # Fixed coordinate / date-based spawner
│   ├── NaturalSpawner.java       # Proximity player natural spawner
│   └── AltarProfile.java         # World block altar with sacrifice requirements
└── util/                      # Utilities
    ├── Keys.java                 # PersistentDataContainer NamespacedKeys
    ├── ItemBuilder.java          # Fluent ItemStack construction utility
    └── TextUtil.java             # Hex (&#rrggbb) and legacy color code formatting
```

---

## 3. Persistent Data (PDC) Contract

PersistentDataContainer keys registered in `dev.lino.custombosses.util.Keys`:

| Key | Type | Attached To | Purpose |
| :--- | :--- | :--- | :--- |
| `custombosses:boss_id` | STRING | LivingEntity | Identifies the BossProfile ID of an active entity |
| `custombosses:boss_uuid` | STRING | LivingEntity | Unique UUID string for entity reconciliation |
| `custombosses:egg_boss_id` | STRING | ItemStack (Spawn Egg) | Encodes boss ID to spawn upon right-click |
| `custombosses:wand_marker` | STRING | ItemStack (Blaze Rod) | Flags the item as the Admin Wand |
| `custombosses:altar_id` | STRING | ArmorStand (Hologram) | Identifies hologram marker linked to an altar |

---

## 4. Architectural Core Mechanics

The following systems define CustomBosses core PvE architecture:

1. **Passive Mob Aggro AI**: Entities normally passive in vanilla Minecraft (e.g. `CHICKEN`, `COW`, `PIG`, `SHEEP`, `VILLAGER`, `HORSE`, `IRON_GOLEM`, etc.) gain custom AI pursuit within 16 blocks and deal physical attack damage and knockback upon contact.
2. **Ambient Particle System**: Configurable particle aura (`FLAME`, `SOUL_FIRE_FLAME`, `PORTAL`, `DRAGON_BREATH`, etc.) rendered continuously around living bosses.
3. **Permanent Potion Effects**: Configurable default potion buffs applied to bosses upon spawn (`SPEED`, `STRENGTH`, `RESISTANCE`, `FIRE_RESISTANCE`, `INVISIBILITY`, `GLOWING`, `REGENERATION`).
4. **Mounted Passengers**: Bosses can have custom passenger entities mounted (`passenger_type`).
5. **Slime & Magma Cube Split Prevention**: Bosses using Slime or MagmaCube entity types do not leave unwanted tiny vanilla split entities upon death.
6. **Creeper Explosion Block Protection**: Creeper bosses clear block damage upon detonation to protect server builds.
7. **Chunk Restoration**: `ChunkListener` detects and registers existing bosses when chunks load so boss bars and abilities resume automatically.
8. **WorldGuard Hook**: Verifies `Flags.MOB_SPAWNING` before executing natural or regional spawns in protected territories.
9. **WildStacker & Stacker Hook**: Dynamically detects stacking events (`WildStacker`, `RoseStacker`, `StackMob`, `UltimateStacker`), cancels boss merging, marks PDC keys, and sets entity metadata so bosses and their mounts never get stacked into generic mob stacks.
10. **Robust Drop Distribution & Equipment Drops**: Safe drop distribution with bounds protection, killer fallback when killed by environment, commands execution, and guaranteed equipment drop chances.

---

## 5. How to Add a New Boss Ability

To implement a new ability without breaking existing workflows:

1. **Register Enum Entry**: Add the ability constant to `AbilityType.java` with its display name and `AbilityTrigger` (`PERIODIC`, `ON_ATTACK`, `ON_DAMAGED`, `LOW_HEALTH`, or `ON_DEATH`).
2. **Implement Logic in `AbilityManager.java`**:
   - For `PERIODIC`: Add a `case YOUR_ABILITY -> { ... }` in `executePeriodic()`.
   - For `ON_ATTACK`: Add a `case YOUR_ABILITY -> { ... }` in `handleAttack()`.
   - For `ON_DAMAGED`: Add a `case YOUR_ABILITY -> { ... }` in `handleDamaged()`.
   - For `ON_DEATH`: Add a `case YOUR_ABILITY -> { ... }` in `handleDeath()`.
3. **YAML Serialization**: `ConfigManager.java` and `AbilitiesMenu.java` automatically iterate over `AbilityType.values()`, so no additional UI glue is needed!

---

## 6. Compatibility (Minecraft 1.21.11 to 26.3)

- **Pure Spigot/Paper API**: The codebase strictly avoids internal NMS (`net.minecraft.server.*`) reflection or fragile field offsets.
- **Dynamic Attribute Resolution**: Uses `ActiveBoss.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH")` and `Attribute.SCALE` to support both modern Mojang-mapped enum constants and future attribute revisions.
- **Folia Compatibility**: Marked as `folia-supported: true`. Schedulers operate per-world or through standard runTaskTimer methods.
