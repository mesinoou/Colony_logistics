# Phase 6 - Freight Board GUI

This phase adds the first real GUI path for freight contracts.

## Implemented

- `ModMenus` registry
- `FreightBoardMenu`
- `FreightBoardRow`
- `FreightBoardScreen`
- Client-side screen registration
- `FreightBoardBlock` now opens the menu instead of printing chat-only listings
- The menu synchronizes a snapshot of up to 12 open contracts through the vanilla menu extra-data buffer
- Accept buttons call the existing `/colonylogistics freight accept <uuid>` command as a temporary C2S path

## Why this design

A custom live-refresh packet is intentionally deferred. The board only needs a small read-only snapshot for the first playable loop, and the existing command handler already contains the server-side validation for accepting jobs.

## Next steps

- Replace command-based accept buttons with a dedicated C2S packet
- Add paging and filters
- Add a Dock GUI for container spawn/deliver actions
- Add a Trade Terminal GUI for player-created item contracts
