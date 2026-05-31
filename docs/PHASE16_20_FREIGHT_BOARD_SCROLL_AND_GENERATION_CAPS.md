# Phase 16.20 - Freight Board scrolling and early test generation caps

This phase improves early one-player delivery testing by separating the Freight Board snapshot size from the number of rows visible on screen.

## Changes

- Freight Board menu snapshot cap raised from 12 rows to 200 rows.
- Freight Board screen now shows a 9-row viewport with mouse-wheel scrolling.
- The screen displays the current visible range, for example `1-9 / 27`.
- Accept/Terminal buttons are rebuilt when the list is scrolled, so the button always targets the visible row.
- Early test-play generation caps were added to keep the first generated contract pool small:
  - `market.testInventoryJobCapPerColony = 4`
  - `market.testContainerJobCapPerColony = 2`

## Notes

These caps affect future automatic/top-up generation only. Existing OPEN contracts already stored in the world are not deleted. Use a fresh test world, complete/cancel contracts, or scroll the Freight Board to find newly created contracts in an existing test world.
