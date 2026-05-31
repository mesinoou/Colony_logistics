# Phase 17.8.9 - Logistics Office blueprint rebuild

This phase rebuilds the Logistics Office Level 1-5 blueprints so they follow the same production-footprint policy already applied to the Container Dock and Trade Terminal.

## Goals

- Reserve the final Logistics Office footprint from Level 1.
- Keep the MineColonies/Structurize Hut anchor at local `(3, 1, 3)`.
- Preserve `blueprintDataProvider` and the duplicated `huts/` copies.
- Keep the Logistics Office as the in-colony entry point for Freight Board style contracts.
- Avoid touching the manually tuned GUI files.

## Blueprint changes

All Logistics Office levels now use the Level 5 production footprint:

- `size_x = 15`
- `size_y = 9`
- `size_z = 13`
- `primary_offset = (3, 1, 3)`
- `corner1 = (-3, -1, -3)`
- `corner2 = (11, 7, 9)`

Lower levels are no longer smaller buildings. Instead, they reserve the complete footprint and gradually add more structure:

- Level 1: open logistics kiosk with final footprint foundation and outline beams.
- Level 2: more perimeter closure, queue rails, route planning desks, and early archive fixtures.
- Level 3: dispatch hall with fuller shell, counters, lighting, and expanded records area.
- Level 4: secured archive/coordination office with mezzanine base and improved rails.
- Level 5: full roof, full archive wall, mezzanine/notice rail, and complete dispatch hall treatment.

## Files updated

- `src/main/resources/blueprints/colony_logistics_dev/logistics_office1.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/logistics_office2.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/logistics_office3.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/logistics_office4.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/logistics_office5.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/logistics_office1.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/logistics_office2.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/logistics_office3.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/logistics_office4.blueprint`
- `src/main/resources/blueprints/colony_logistics_dev/huts/logistics_office5.blueprint`
- `scripts/generate_phase17_6_blueprints.py`

## Not changed

- Container Dock spawn offsets and rotation handling are unchanged from Phase 17.8.8.
- Container Dock blueprints are unchanged from Phase 17.8.8.
- Trade Terminal blueprints are unchanged from Phase 17.8.8.
- The manually tuned GUI files are unchanged.
