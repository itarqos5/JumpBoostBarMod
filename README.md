# JumpBoostBar

Multi-loader development environment for a server-side project targeting:
- Fabric
- NeoForge
- Paper

No Stonecutter is used.
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
"# JumpBoostBarMod" 
