# NeoForge 1.21.1 port

This branch contains the native NeoForge port of Tale of Kingdoms.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.226
- Java 21
- Mod id: `taleofkingdoms`

## Porting stages

1. Keep the original Fabric build green as a reference.
2. Replace the loader and Gradle configuration.
3. Port registries, entity attributes, lifecycle events and commands.
4. Port networking, client rendering, screens and configuration.
5. Validate structures, quests, kingdom upgrades and saved data.

The `master` branch remains an untouched copy of the upstream project.
