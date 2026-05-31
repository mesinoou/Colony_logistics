# Phase 17.2.1 - Compile fix

- Added missing `LogisticsContract` import to `ContainerDockBlock`.
- This fixes the `LogisticsContract::canSpawnMoreContainers` compile failure and the follow-up Object type inference error.
