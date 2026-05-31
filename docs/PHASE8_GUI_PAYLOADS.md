# Phase 8: GUI C2S payloads

This phase removes the temporary command-driven buttons from the first GUI pass.

Implemented payloads:

- `AcceptFreightPayload`
  - Sent from the Freight Board Accept button.
  - Server decides whether the contract is inventory freight or container freight.
  - Inventory freight grants a `Freight Parcel` item after validation.
  - Container freight changes the contract to accepted after validation.

- `SpawnContainerPayload`
  - Sent from the Container Dock Spawn button.
  - Server validates player proximity, dock block entity, colony linkage, contract assignment, dock origin, container unlocks, active job limits, and target space before placing the multiblock.

- `DeliverContainerPayload`
  - Sent from the Container Dock Deliver button.
  - Server validates player proximity, dock block entity, container core, manifest, contract state, player assignment, destination colony, destination dock, sealed state, size, weight class, and Sable-aware range before paying rewards.

The menu snapshots remain read-only display data. They are not trusted for gameplay state; the server re-checks the current world and `LogisticsMarketSavedData` before applying any action.

Debug commands remain available for development, but normal survival gameplay should now use the GUI buttons without requiring operator permissions.
