# Phase 16.29 - Compact GUI layout and Trade Terminal setup improvements

## Goals

This phase addresses playtest feedback that the Freight Board, Container Dock, and Trade Terminal screens were too tall and that contract rows were too narrow, causing text and buttons to overlap. It also changes the Trade Terminal setup flow so the request item is a one-item sample and the required count is specified separately.

## GUI layout changes

- Freight Board:
  - reduced visible rows from 9 to 6
  - reduced screen height from 330 to 236
  - kept filtering, scrolling, and selected-contract details
  - shortened several detail values before rendering

- Container Dock:
  - reduced screen height from 318 to 236
  - widened the screen to 480
  - capped synchronized rows to 5 contracts and 5 nearby containers
  - compressed diagnostic container rows to two lines

- Trade Terminal:
  - reduced screen height from 304 to 238
  - widened the screen to 480
  - moved player inventory to the lower compact area
  - limited visible history rows to 2
  - added explicit slot frames for request sample and escrow reward

## Trade Terminal request count

The request slot is now a sample slot. It accepts only one item. The requested delivery count is controlled by GUI buttons:

- -1 / +1
- -10 / +10

The current phase clamps requested count to 1-64 so the existing PlayerTradeContract ItemStack-based count representation can remain stable.

## Sample return behavior

The request sample item is returned to the player when the Trade Terminal GUI closes. The escrow reward slot remains terminal-backed until a trade is created or the player removes it manually.

## Compatibility

This phase assumes fresh test worlds, matching the current development workflow. No old-world migration is included.
