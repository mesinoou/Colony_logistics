# Phase 17.9.9.1 - Sable dependency revert

## Change

- Reverted `META-INF/neoforge.mods.toml` Sable dependency metadata to the Phase 17.9.8 style.
- `sable` is again declared as `type="required"` with `versionRange="[1.1.3,)"`.
- Removed the optional `sablecompanion` dependency metadata entry that was added in Phase 17.9.9.

## Kept from Phase 17.9.9

- The Logistics Office/Freight Board tooltip crash fix is unchanged.
- UUID arguments passed to translated tooltip components remain converted to strings.

## Reason

The Sable mod loading failure was separate from the Logistics Office screen-render crash. The direct crash cause was the UUID argument passed to `Component.translatable`, so only that screen-render fix remains in this patch.
