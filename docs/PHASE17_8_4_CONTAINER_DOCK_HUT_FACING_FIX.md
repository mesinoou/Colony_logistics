# Phase 17.8.4 - Container Dock hut-facing based lane transform

Phase 17.8.3 rotated the indoor spawn lane through MineColonies' cached building rotation. In game testing showed that the resulting direction and position could still be wrong; rotating the building produced the same mismatch. The likely cause is that `getRotation()` can be unresolved (`-1`) while the placed hut block already has the correct Build Tool-rotated `AbstractBlockHut.FACING` block state.

## Fix

- The Container Dock cargo direction is now resolved from the actual hut block state first.
- The blueprint front/control side is local north, and the cargo apron is local south, so cargo forward is `hutFacing.getOpposite()`.
- MineColonies/Structurize cached rotation is kept only as a fallback.
- A `getRotation() == -1` fallback no longer maps to an arbitrary horizontal direction.
- Reflection now walks declared superclass methods as well as public methods, so runtime API visibility differences are less likely to break the fallback path.

## Expected result

The existing indoor lane offsets remain:

- `(5, 2, 6)`
- `(5, 2, 10)`
- `(5, 2, 14)`

They are transformed according to the actual placed Container Dock hut facing, so unrotated and Build Tool-rotated docks should place 3x7x3 containers inside the building, nearest first, with one block between rows.

## Test command

Use:

```text
/colonylogistics container candidates <dock> standard
```

Confirm that `dockForward` equals the cargo-apron/back side of the hut block and that the first three candidates are inside the visible Dock building.
