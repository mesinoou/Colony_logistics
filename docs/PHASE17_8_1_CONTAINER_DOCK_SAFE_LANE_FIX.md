# Phase 17.8.1 - Container Dock safe lane fix

## Problem observed in-game

After Phase 17.8, the Dock could report `コンテナを生成しました。` while the player could not find the container from the Container Dock.

The likely cause was that the server still accepted older GUI/config spawn suggestions first. Those suggestions could pass the contract state checks and find an air fallback, but the chosen position was no longer a clear, predictable staging lane after all containers were unified to a 3 deep x 7 wide x 3 high footprint.

## Fix

Container spawning now tries a deterministic exterior lane before any client-provided or TOML apron candidates.

Default core offsets from the Hut anchor are:

- First container: `(19, 2, 4)`
- Second container: `(19, 2, 8)`
- Third container: `(19, 2, 12)`
- Further containers continue every 4 blocks on Z.

With the unified footprint:

- width: 7 blocks
- depth: 3 blocks
- height: 3 blocks
- Z pitch: 4 blocks

This leaves exactly one empty block between adjacent container footprints.

## Old config safety

Existing test worlds may still have:

```toml
deliveryRadius = 18.0
containerRecognitionRadius = 18.0
```

The service now clamps both runtime values to at least `28.0`, so old TOML files cannot make the safe exterior lane spawn but then fail recognition/delivery range checks.

## False success prevention

`ContainerMultiblockBuilder.place` now returns `boolean` and verifies:

- all target blocks accepted `level.setBlock`
- the core position actually contains `freight_container_core`
- the core block entity exists so the manifest can be stored

If this verification fails, the contract is not advanced and the player receives `PLACEMENT_FAILED` instead of a false success.

## Debug messages

Successful spawn now also prints the actual core coordinate:

```text
コンテナCoreを <x, y, z> に配置しました。
```

Use this with:

```text
/colonylogistics container candidates <dock> standard
/colonylogistics container diagnose <dock>
```

## Preserved constraints

- Container physical size remains 3 deep x 7 wide x 3 high.
- Standard / Large / Heavy gameplay tiers remain unchanged.
- Container Dock blueprint visuals are unchanged.
- Manually tuned GUI files are unchanged.
