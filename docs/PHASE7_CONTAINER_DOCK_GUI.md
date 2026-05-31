# Phase 7: Container Dock GUI

This phase adds a first-pass Container Dock screen.

## What works

Right-clicking a Container Dock now opens a menu that shows two snapshots:

- accepted container freight contracts assigned to the player and originating at this Dock
- sealed freight containers detected within the Dock delivery radius

The screen has two actions:

- **Spawn**: calls the existing container spawn command with the Dock position and a suggested core position
- **Deliver**: calls the existing container delivery command for a detected container core

## Current technical limitation

Buttons still route through existing commands:

```mcfunction
/colonylogistics container spawn <contract> <dock> <core>
/colonylogistics container deliver <dock> <core>
```

Those commands currently require permission level 2 because the original development loop was command-driven. The next networking phase should replace button actions with dedicated C2S payloads and server-side validation methods so normal survival players can use the Dock GUI.

## Suggested next step

Implement C2S payloads:

- `C2S_SpawnContainerFromDock`
- `C2S_DeliverContainerAtDock`
- optionally `C2S_SetDockMode`

Both payloads should call `ContainerDockService` directly and should not require operator permissions.

## Superseded by Phase 8

The command-backed button behavior described above has been replaced in Phase 8 by dedicated C2S payloads:

- `SpawnContainerPayload`
- `DeliverContainerPayload`

The old commands remain for debugging, but the GUI no longer depends on command permissions.
