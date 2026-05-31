# Phase 16.15 - Freight Board visibility fix

Phase 16.14 added contract status to the Freight Board snapshot, but the board still sorted OPEN jobs first. In worlds with enough generated OPEN jobs, accepted or picked-up contracts were pushed below `MAX_ROWS` and looked as if they were not reflected.

This phase makes the board useful as a contract-status dashboard:

- The snapshot is generated for the current viewer.
- Contracts assigned to the viewer are promoted above newly generated OPEN jobs.
- Active viewer contracts are shown first, followed by viewer history, other active jobs, and OPEN jobs.
- Each row now includes a compact assignee marker: none / self / other.
- The client screen adds a `Carrier` / `担当` column.

This is still a snapshot taken when the board is opened. Live refresh while the screen stays open remains future work.
