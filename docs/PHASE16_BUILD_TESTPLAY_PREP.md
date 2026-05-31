# Phase 16 - Build and Test-Play Preparation

This phase switches from feature growth to making the project easier to build and test in a local modded client.

## Changes

- Removed compile-time import of Sable Companion from `SableCompanionDeliveryRangeResolver`.
- Kept Sable physics integration data-driven through `physics_block_properties`.
- Added reflection-based Sable Companion lookup with vanilla distance fallback.
- Added server tick market top-up loop.
- Added market pacing options to common config.
- Added example `config/colonylogistics-common.toml.example`.
- Added `run/mods/README.md` for local runtime jars.
- Added `.gitignore`.
- Added `scripts/build_testplay_checklist.sh` for resource/layout checks.

## Local build target

Use a local Gradle installation or generate a wrapper in the project directory:

```bash
gradle wrapper --gradle-version 8.10.2
./gradlew build
```

If your NeoForge ModDev plugin requires a different Gradle version, use the version requested by the plugin output.

## Runtime notes

The initial test-play build does not compile against Sable Companion directly.  This is intentional.  Container physical mass is still supplied to Sable through datapack JSON.  If Sable Companion is present at runtime and exposes a compatible `distanceSquaredWithSubLevels(Level, Vec3, Vec3)` method, the resolver uses it. Otherwise delivery checks use vanilla distance.

## First test-play checklist

1. Put required dependency jars in `run/mods/`.
2. Build with `./gradlew build`.
3. Launch `./gradlew runClient`.
4. Create or load a test world.
5. Use temporary commands if MineColonies blueprints are not yet present:
   - `/colonylogistics colony setoffice 1 2`
   - `/colonylogistics colony setoffice 2 2`
   - `/colonylogistics dock bind <pos> 1 2`
   - `/colonylogistics dock bind <pos> 2 2`
6. Wait one Minecraft day or run generation commands manually.
7. Open Freight Board and accept an inventory or container job.
8. Use Container Dock GUI to spawn/deliver container jobs.
9. Open Trade Terminal and test escrow creation, delivery, cancel/refund, and history display.
