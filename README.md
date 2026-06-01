# JumpBoostBar

Because who needs to build leg muscles when you can just... charge up and fly? It's not lazy, it's efficient.

This mod/plugin allows you to charge a jump by sneaking, and then release to perform a super jump! The height of the jump is determined by how long you charge it and your current Jump Boost potion effect.

## Features

- **Chargeable Jumps:** Hold sneak to charge, release to jump.
- **Jump Boost Integration:** The higher your Jump Boost level, the higher you can jump.
- **Visual Feedback:** The charge is displayed on your experience bar.
- **Cross-Platform:** Works on Fabric (client-side), NeoForge (client-side), and Paper servers.

## Installation

### Fabric & NeoForge (Client-Side)

1. Download the appropriate JAR file for your mod loader (Fabric or NeoForge).
2. Place the JAR file in your `mods` folder.
3. Run the game.

### Paper (Server-Side)

1. Download the Paper JAR file.
2. Place the JAR file in your `plugins` folder.
3. Restart your server.

## Configuration (Paper)

The Paper plugin can be configured in the `plugins/JumpBoostBar/config.yml` file.

- `enabled`: Enable or disable the plugin.
- `bar`: The type of bar to use for the jump boost bar. Valid options are `xp` and `bossbar`.
- `actionbar-message`: The message to display in the action bar when the jump boost bar is active. You can use `{blocks}` to display the number of blocks the player has jumped.

## For Developers

This project is open source! Feel free to contribute on [GitHub](https://github.com/itarqos5/JumpBoostBarMod).

## What This Setup Gives You

- One shared codebase in `common/src/main/java`
- Loader-specific bootstrap code in:
	- `fabric/src/main/java`
	- `neoforge/src/main/java`
	- `paper/src/main/java`
- Independent build targets per loader from the same shared logic
- Version knobs in `gradle.properties` so you can move across 1.21.x without rewriting code

## Project Layout

```
.
|-- common/
|   `-- src/main/java/gg/literal/jumpboostbar/common/
|-- fabric/
|   |-- src/main/java/gg/literal/jumpboostbar/fabric/
|   `-- src/main/resources/fabric.mod.json
|-- neoforge/
|   |-- src/main/java/gg/literal/jumpboostbar/neoforge/
|   `-- src/main/resources/META-INF/neoforge.mods.toml
|-- paper/
|   |-- src/main/java/gg/literal/jumpboostbar/paper/
|   `-- src/main/resources/paper-plugin.yml
|-- build.gradle
|-- gradle.properties
`-- settings.gradle
```

## Group And Coordinates

- Group ID: `gg.literal`
- Base mod id: `jumpboostbar`

Configured in `gradle.properties`.

## Versioning Strategy (1.21.x Range)

Edit `gradle.properties` to shift target patch versions:

- `mc_version`
- `fabric_api_version`
- `neoforge_version`
- `paper_api_version`

Then build each loader module.

Example (override from command line without editing files):

```powershell
./gradlew :fabric:build -Pmc_version=1.21.6
```

Note: each loader ecosystem has its own compatible artifact versions. Keep the 4 properties aligned per target patch.

## Build Commands

```powershell
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew :paper:build
```

## Run Dev Servers

Fabric dedicated server run config:

```powershell
./gradlew :fabric:runDedicatedServer
```

NeoForge dedicated server run config:

```powershell
./gradlew :neoforge:runServer
```

Paper is a plugin target; drop the built jar into your Paper server `plugins/` folder.

## Where To Put Shared Logic

Put gameplay/business logic in `common/src/main/java`. Keep only loader adapters and event wiring inside each loader module.

This is the key part that prevents recoding for every loader/version.
