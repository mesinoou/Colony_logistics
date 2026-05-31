# Phase 17.3 - Container standards

This phase introduces player-facing container standards while keeping the existing physical ContainerSize enum for NBT and multiblock construction.

## Standards

- Standard Container: physical SMALL container, default 2 containers per contract.
- Large Container: physical LARGE container, default 2 containers per contract; fragile cargo may require 3.
- Heavy Container: physical HEAVY container, default 3 containers per contract.

The old MEDIUM physical size is no longer generated automatically. It remains supported as a legacy/debug alias and is displayed as Standard.

## Commands

- `/colonylogistics freight generatecontainers standard`
- `/colonylogistics freight generatecontainers large`
- `/colonylogistics freight generatecontainers heavy`
- `/colonylogistics container localtest <dock> standard`
- `/colonylogistics container localtest <dock> large`
- `/colonylogistics container localtest <dock> heavy`

Aliases: `small`, `medium`, and `std` map to `standard`.

## Display

GUI labels now show translated cargo names and player-facing standards instead of raw enum names.
