# Colony Logistics structure template notes

MineColonies building registration is implemented in Java, but the actual buildable structures still need to be authored in Structurize/Blueprint format.

Required building families:

- logistics_office
- container_dock

Recommended levels:

- level 1..5 for logistics_office
- level 1..5 for container_dock, even if the model is mostly cosmetic

The Java side expects:

- Logistics Office level controls freight features and Dock count.
- Container Dock must contain the `colonylogistics:container_dock` hut block.
- Logistics Office must contain the `colonylogistics:logistics_office` hut block.

Keep these templates out of Sable/Create mobile contraptions. MineColonies buildings remain static; only Freight Containers move.
