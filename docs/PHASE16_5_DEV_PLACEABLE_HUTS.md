# Phase 16.5: Development-placeable hut items

MineColonies hut items normally use `ItemBlockHut`, which routes placement through the Build Tool / blueprint system.
Because this project does not yet ship real Structurize/MineColonies `.blueprint` files, the test build could show the blocks in the creative tab but reject placement with a missing building data / blueprint data message.

For test-play, the Logistics Office, Container Dock, and Trade Terminal item registrations now use vanilla `BlockItem` instead of `ItemBlockHut`.

This allows direct creative placement for feature testing:

- Freight Board GUI
- Container Dock GUI
- Trade Terminal GUI
- manual `/colonylogistics ... bind` flows
- container spawning and delivery loops

Important: direct placement is a development fallback. It does not create a fully builder-constructed MineColonies building from a schematic.

Before a proper release, add real Structurize blueprints for each hut and switch these item registrations back to MineColonies `ItemBlockHut` or provide a separate production item path.

Expected future blueprint names, following MineColonies naming convention:

- `colony_logistics/logistics_office1.blueprint` ... `logistics_office5.blueprint`
- `colony_logistics/container_dock1.blueprint` ... `container_dock5.blueprint`
- `colony_logistics/trade_terminal1.blueprint` ... `trade_terminal5.blueprint`

