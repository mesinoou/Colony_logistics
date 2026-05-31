#!/usr/bin/env python3
"""Generate Phase 17.6 production-style Structurize/MineColonies blueprints.

The generated files intentionally keep the MineColonies hut anchor at local
(3, 1, 3) and preserve the blueprintDataProvider payload required by
MineColonies/Structurize Build Tool previews.
"""
from __future__ import annotations

import gzip
import math
import struct
from collections import OrderedDict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional

ROOT = Path(__file__).resolve().parents[1]
BLUEPRINT_DIR = ROOT / "src/main/resources/blueprints/colony_logistics_dev"
PACK_NAME = "Colony Logistics Dev"
ARCHITECT = "Colony Logistics Dev"
MCVERSION = 3955  # Minecraft 1.21.1 data version used by the existing generated files.
ANCHOR = (3, 1, 3)

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11


@dataclass(frozen=True)
class NbtList:
    elem_type: int
    items: list


@dataclass(frozen=True)
class NbtValue:
    tag_type: int
    value: object


def write_string(value: str) -> bytes:
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def payload(tag_type: int, value: object) -> bytes:
    if tag_type == TAG_BYTE:
        return struct.pack(">b", int(value))
    if tag_type == TAG_SHORT:
        return struct.pack(">h", int(value))
    if tag_type == TAG_INT:
        return struct.pack(">i", int(value))
    if tag_type == TAG_STRING:
        return write_string(str(value))
    if tag_type == TAG_INT_ARRAY:
        values = list(value)
        return struct.pack(">i", len(values)) + b"".join(struct.pack(">i", int(v)) for v in values)
    if tag_type == TAG_LIST:
        assert isinstance(value, NbtList)
        return bytes([value.elem_type]) + struct.pack(">i", len(value.items)) + b"".join(payload(value.elem_type, item) for item in value.items)
    if tag_type == TAG_COMPOUND:
        assert isinstance(value, OrderedDict)
        out = bytearray()
        for key, typed in value.items():
            assert isinstance(typed, NbtValue), key
            out.append(typed.tag_type)
            out += write_string(key)
            out += payload(typed.tag_type, typed.value)
        out.append(TAG_END)
        return bytes(out)
    raise ValueError(f"Unsupported tag type {tag_type}")


def compound(entries: Iterable[tuple[str, NbtValue]]) -> OrderedDict:
    return OrderedDict(entries)


def b(value: int) -> NbtValue:
    return NbtValue(TAG_BYTE, value)


def s(value: int) -> NbtValue:
    return NbtValue(TAG_SHORT, value)


def i(value: int) -> NbtValue:
    return NbtValue(TAG_INT, value)


def st(value: str) -> NbtValue:
    return NbtValue(TAG_STRING, value)


def lst(elem_type: int, items: list) -> NbtValue:
    return NbtValue(TAG_LIST, NbtList(elem_type, items))


def comp(value: OrderedDict) -> NbtValue:
    return NbtValue(TAG_COMPOUND, value)


def int_array(values: list[int]) -> NbtValue:
    return NbtValue(TAG_INT_ARRAY, values)


def pos_comp(x: int, y: int, z: int) -> OrderedDict:
    return compound([("x", i(x)), ("y", i(y)), ("z", i(z))])


def block_state(name: str, props: Optional[dict[str, str]] = None) -> OrderedDict:
    entries: list[tuple[str, NbtValue]] = [("Name", st(name))]
    if props:
        entries.append(("Properties", comp(compound((k, st(v)) for k, v in props.items()))))
    return compound(entries)


def pack_blocks(structure: list[list[list[int]]], sx: int, sy: int, sz: int) -> list[int]:
    # Structurize v1 flattens in y, z, x order and stores two short palette
    # entries in each int: high short first, low short second.
    one_dim: list[int] = []
    for y in range(sy):
        for z in range(sz):
            for x in range(sx):
                one_dim.append(structure[y][z][x])
    packed: list[int] = []
    for idx in range(0, len(one_dim), 2):
        first = one_dim[idx]
        second = one_dim[idx + 1] if idx + 1 < len(one_dim) else 0
        packed.append((first << 16) | second)
    return packed


def empty_structure(sx: int, sy: int, sz: int) -> list[list[list[int]]]:
    return [[[0 for _ in range(sx)] for _ in range(sz)] for _ in range(sy)]


def set_block(structure: list[list[list[int]]], x: int, y: int, z: int, state: int) -> None:
    sy = len(structure)
    sz = len(structure[0])
    sx = len(structure[0][0])
    if 0 <= x < sx and 0 <= y < sy and 0 <= z < sz:
        structure[y][z][x] = state


def box(structure: list[list[list[int]]], x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, state: int) -> None:
    for y in range(y1, y2 + 1):
        for z in range(z1, z2 + 1):
            for x in range(x1, x2 + 1):
                set_block(structure, x, y, z, state)


def line_x(structure: list[list[list[int]]], x1: int, x2: int, y: int, z: int, state: int) -> None:
    for x in range(x1, x2 + 1):
        set_block(structure, x, y, z, state)


def line_z(structure: list[list[list[int]]], x: int, y: int, z1: int, z2: int, state: int) -> None:
    for z in range(z1, z2 + 1):
        set_block(structure, x, y, z, state)


def make_blueprint(name: str, sx: int, sy: int, sz: int, palette: list[OrderedDict], structure: list[list[list[int]]]) -> bytes:
    ax, ay, az = ANCHOR
    hut_name = name.rstrip("12345")
    level = name[len(hut_name):]
    filename = f"{name}.blueprint"
    tile = compound([
        ("x", s(ax)),
        ("y", s(ay)),
        ("z", s(az)),
        ("blueprintDataProvider", comp(compound([
            ("schematicName", st(name)),
            ("corner1", comp(pos_comp(-ax, -ay, -az))),
            ("corner2", comp(pos_comp(sx - 1 - ax, sy - 1 - ay, sz - 1 - az))),
            ("posTagMap", lst(TAG_COMPOUND, [])),
            ("pack", st(PACK_NAME)),
            ("path", st(filename)),
        ]))),
        ("version", i(2)),
    ])
    root = compound([
        ("version", b(1)),
        ("size_x", s(sx)),
        ("size_y", s(sy)),
        ("size_z", s(sz)),
        ("palette", lst(TAG_COMPOUND, palette)),
        ("blocks", int_array(pack_blocks(structure, sx, sy, sz))),
        ("tile_entities", lst(TAG_COMPOUND, [tile])),
        ("entities", lst(TAG_COMPOUND, [])),
        ("required_mods", lst(TAG_STRING, ["colonylogistics"])),
        ("name", st(name)),
        ("architects", lst(TAG_STRING, [ARCHITECT])),
        ("mcversion", i(MCVERSION)),
        ("optional_data", comp(compound([
            ("structurize", comp(compound([
                ("primary_offset", comp(pos_comp(ax, ay, az))),
            ]))),
        ]))),
    ])
    raw = bytes([TAG_COMPOUND]) + write_string("") + payload(TAG_COMPOUND, root)
    return gzip.compress(raw, mtime=0)


def logistics_palette() -> list[OrderedDict]:
    return [
        block_state("minecraft:air"),                         # 0
        block_state("minecraft:cobblestone"),                 # 1 foundation
        block_state("minecraft:oak_planks"),                  # 2 floor/walls
        block_state("minecraft:glass"),                       # 3 windows
        block_state("minecraft:oak_slab", {"type": "bottom", "waterlogged": "false"}),  # 4 roof/counters
        block_state("colonylogistics:logistics_office"),      # 5 hut anchor
        block_state("minecraft:oak_fence", {"waterlogged": "false"}),                     # 6 rail/posts
        block_state("minecraft:lantern", {"hanging": "false", "waterlogged": "false"}),  # 7 lights
        block_state("minecraft:stone_bricks"),                # 8 corner/trim
        block_state("minecraft:bookshelf"),                   # 9 archive shelves
        block_state("minecraft:crafting_table"),              # 10 dispatch workbench
        block_state("minecraft:cartography_table"),           # 11 route planning table
        block_state("minecraft:polished_andesite"),           # 12 service counter
        block_state("minecraft:iron_bars", {"waterlogged": "false"}),                     # 13 queue rail/window mullions
        block_state("minecraft:spruce_planks"),               # 14 upgraded wall accent
    ]


def design_logistics(level: int) -> tuple[int, int, int, list[OrderedDict], list[list[list[int]]]]:
    # Phase 17.8.9: every Logistics Office level reserves the same final
    # production footprint as Level 5. Lower levels are intentionally simpler,
    # but the Build Tool/MineColonies construction volume is stable from Level 1
    # so roads, adjacent colony buildings, and player queue space do not need to
    # move when the office upgrades.
    sx, sy, sz = (15, 9, 13)
    S = empty_structure(sx, sy, sz)
    ax, ay, az = ANCHOR

    # Full reserved foundation and public approach. The whole final footprint is
    # occupied at y=0 from Level 1 to make the reserved area obvious in-game.
    for z in range(sz):
        for x in range(sx):
            border = x in (0, sx - 1) or z in (0, sz - 1)
            if border:
                state = 8
            elif z <= az + 1:
                state = 12 if (x == ax or x in (ax - 1, ax + 1)) else 2
            else:
                state = 2 if (x + z + level) % 6 else 12
            set_block(S, x, 0, z, state)

    # Entrance apron aligned to the hut anchor. This remains fixed across all
    # levels and is kept clear to support player traffic into the Freight Board
    # functionality now integrated into the Logistics Office.
    for x in range(max(0, ax - 2), min(sx, ax + 4)):
        set_block(S, x, 0, 0, 12)
    for z in range(1, az + 1):
        set_block(S, ax, 0, z, 12)

    # Progressive shell inside the fixed bounding box. Level 1 is an open office
    # kiosk, Level 2 closes more walls, Level 3 becomes a dispatch hall, and
    # Levels 4-5 add secure archive/coordination spaces without changing the
    # footprint.
    wall_height_by_level = {1: 2, 2: 3, 3: 4, 4: 5, 5: 6}
    wall_top = wall_height_by_level[level]
    for y in range(1, wall_top + 1):
        for z in range(sz):
            for x in range(sx):
                perimeter = x in (0, sx - 1) or z in (0, sz - 1)
                if not perimeter:
                    continue
                # Main entrance on the front/north side.
                if z == 0 and ax - 1 <= x <= ax + 1 and y <= 2:
                    continue
                corner = (x in (0, sx - 1) and z in (0, sz - 1))
                # Keep lower levels lighter and more open while reserving the same volume.
                if level == 1 and y == wall_top and not corner:
                    state = 6
                else:
                    window = level >= 2 and y == 2 and not corner and ((x + z + level) % 2 == 0)
                    state = 8 if corner else (3 if window else (14 if level >= 4 and y == wall_top else 2))
                set_block(S, x, y, z, state)

    # Fixed corner posts up to final roof height: these visually communicate the
    # final Level 5 bounds even at Level 1 while leaving most lower-level walls open.
    for x, z in [(0, 0), (sx - 1, 0), (0, sz - 1), (sx - 1, sz - 1)]:
        for y in range(wall_top + 1, sy - 1):
            set_block(S, x, y, z, 8 if level >= 3 else 6)

    # Roof/awning progression. Lower levels only have a dispatch awning and edge
    # beams; Level 5 has a full roof across the final office footprint.
    roof_y = sy - 1
    if level <= 2:
        awning_y = 4 if level == 1 else 5
        for x in range(max(0, ax - 3), min(sx, ax + 6)):
            for z in range(0, min(sz, az + 2)):
                if x in (max(0, ax - 3), min(sx, ax + 6) - 1) or z in (0, az + 1):
                    set_block(S, x, awning_y, z, 4)
        # Final footprint outline beams help operators see the future clearance.
        for x in range(sx):
            set_block(S, x, awning_y, 0, 8)
            set_block(S, x, awning_y, sz - 1, 8)
        for z in range(sz):
            set_block(S, 0, awning_y, z, 8)
            set_block(S, sx - 1, awning_y, z, 8)
    elif level <= 4:
        for z in range(sz):
            for x in range(sx):
                edge = x in (0, sx - 1) or z in (0, sz - 1)
                if edge or (level >= 4 and (x + z) % 3 != 0) or z <= az + 1:
                    set_block(S, x, roof_y - 1, z, 4 if not edge else 8)
    else:
        for z in range(sz):
            for x in range(sx):
                edge = x in (0, sx - 1) or z in (0, sz - 1)
                set_block(S, x, roof_y, z, 8 if edge else 4)

    # Keep an unobstructed walking lane from entrance to anchor.
    for z in range(0, az):
        for y in (1, 2):
            set_block(S, ax, y, z, 0)

    # Hut anchor and main dispatch desk. This stays at local (3,1,3) for every
    # level, matching existing MineColonies/Structurize expectations.
    set_block(S, ax, ay, az, 5)
    set_block(S, ax + 1, ay, az, 11)
    set_block(S, ax + 2, ay, az, 10)
    set_block(S, ax + 1, ay, az + 1, 12)
    set_block(S, ax + 2, ay, az + 1, 12)

    # Public queue rails and reception counter. The center path remains open.
    for z in range(1, az):
        if z % 2 == 1:
            set_block(S, ax - 2, 1, z, 13 if level >= 4 else 6)
            set_block(S, ax + 2, 1, z, 13 if level >= 4 else 6)
    for x in range(ax + 3, min(sx - 2, ax + 7)):
        set_block(S, x, 1, az, 12)
        if level >= 3:
            set_block(S, x, 2, az, 13)

    # Route planning desks and contract board area.
    set_block(S, ax + 4, 1, az + 2, 11)
    set_block(S, ax + 5, 1, az + 2, 10)
    if level >= 2:
        for x in range(ax + 3, min(sx - 2, ax + 8)):
            set_block(S, x, 1, az + 3, 12)
        for x in range(ax + 3, min(sx - 2, ax + 8), 2):
            set_block(S, x, 2, az + 3, 9 if level >= 4 else 6)

    # Archive shelving and carrier records. This grows toward the full Level 5
    # back wall while keeping front movement clear.
    if level >= 1:
        for x in range(2, min(sx - 2, 6 + level)):
            if x % 2 == 0 or level >= 3:
                set_block(S, x, 1, sz - 2, 9)
                if level >= 3:
                    set_block(S, x, 2, sz - 2, 9)
    if level >= 4:
        for x in range(2, sx - 2):
            if x not in (ax, ax + 1, ax + 2):
                set_block(S, x, 1, sz - 3, 9)
                if x % 2 == 0:
                    set_block(S, x, 2, sz - 3, 9)
    if level >= 5:
        for z in range(az + 3, sz - 2):
            set_block(S, sx - 2, 1, z, 12)
            if z % 2 == 0:
                set_block(S, sx - 2, 2, z, 9)

    # Side counters / staff workstations.
    for z in range(az + 2, sz - 2, 2):
        if level >= 2:
            set_block(S, 1, 1, z, 12)
        if level >= 3:
            set_block(S, sx - 2, 1, z, 12)

    # Lighting inside the reserved footprint.
    light_positions = [(1, 1), (sx - 2, 1), (1, sz - 2), (sx - 2, sz - 2)]
    if level >= 3:
        light_positions += [(sx // 2, 1), (sx // 2, sz - 2)]
    if level >= 5:
        light_positions += [(ax + 4, az + 2), (sx - 3, az + 4)]
    seen: set[tuple[int, int]] = set()
    for x, z in light_positions:
        if (x, z) in seen:
            continue
        seen.add((x, z))
        set_block(S, x, 1, z, 7)

    # Back mezzanine and notice rail for higher levels, leaving the main lane and
    # desk clear underneath.
    if level >= 4:
        mezz_y = 5
        for z in range(az + 4, sz - 1):
            for x in range(2, sx - 2):
                set_block(S, x, mezz_y, z, 2 if level == 4 else 14)
        for x in range(2, sx - 2):
            set_block(S, x, mezz_y + 1, az + 4, 13 if level >= 5 else 6)
        for z in range(az + 4, sz - 1):
            set_block(S, 2, mezz_y + 1, z, 13 if level >= 5 else 6)
            set_block(S, sx - 3, mezz_y + 1, z, 13 if level >= 5 else 6)

    return sx, sy, sz, logistics_palette(), S

def dock_palette() -> list[OrderedDict]:
    return [
        block_state("minecraft:air"),                         # 0
        block_state("minecraft:smooth_stone"),                # 1 concrete floor
        block_state("minecraft:polished_andesite"),           # 2 metal/stone trim
        block_state("minecraft:iron_block"),                  # 3 heavy frame
        block_state("minecraft:yellow_concrete"),             # 4 hazard stripe
        block_state("colonylogistics:container_dock"),        # 5 hut anchor
        block_state("minecraft:orange_concrete"),             # 6 cargo panel/marked pad
        block_state("minecraft:lantern", {"hanging": "false", "waterlogged": "false"}),  # 7 floor lamps
        block_state("minecraft:iron_bars", {"waterlogged": "false"}),                     # 8 guard rail
        block_state("minecraft:chain", {"axis": "y", "waterlogged": "false"}),           # 9 crane chain
        block_state("minecraft:gray_concrete"),               # 10 service bay
        block_state("minecraft:black_concrete"),              # 11 hazard stripe
        block_state("minecraft:oak_fence", {"waterlogged": "false"}),                     # 12 temporary bollards
        block_state("minecraft:glass"),                       # 13 control booth glass
        block_state("minecraft:stone_bricks"),                # 14 retaining wall
    ]


def design_dock(level: int) -> tuple[int, int, int, list[OrderedDict], list[list[list[int]]]]:
    # Phase 17.8.8: every Container Dock level reserves the same production
    # footprint as Level 5. Lower levels still look progressively simpler, but
    # upgrades no longer expand into new ground that may already be occupied by
    # containers, Create contraptions, paths, or adjacent colony buildings.
    sx, sy, sz = (19, 10, 19)
    S = empty_structure(sx, sy, sz)
    ax, ay, az = ANCHOR

    # Ground-level loading apron. This is intentionally mostly floor-only so
    # container spawn/recognition can be reconciled later via offsets without
    # fighting a dense blueprint volume.
    for z in range(sz):
        for x in range(sx):
            border = x in (0, sx - 1) or z in (0, sz - 1)
            state = 2 if border else 1
            # Cargo pad stripes centered beyond the hut; visual only at y=0.
            if 1 <= x <= sx - 2 and 2 <= z <= sz - 2:
                if (x + z) % 6 == 0:
                    state = 4
                elif (x + z) % 6 == 1:
                    state = 11
                elif (x - ax) >= 3 and z >= az:
                    state = 6 if (x + z + level) % 4 == 0 else 1
            set_block(S, x, 0, z, state)

    # Hut anchor/control terminal.
    set_block(S, ax, ay, az, 5)
    set_block(S, ax + 1, ay, az, 10)
    set_block(S, ax + 1, ay, az + 1, 10)
    set_block(S, ax - 1, ay, az, 8)

    # Front approach remains open.
    for z in range(0, az):
        for y in range(1, min(sy, 4)):
            for x in range(max(0, ax - 1), min(sx, ax + 2)):
                set_block(S, x, y, z, 0)

    # Perimeter frame columns and high crane rails.
    column_top = sy - 2
    column_positions: set[tuple[int, int]] = set()
    for x in range(0, sx, 4):
        column_positions.add((x, 0))
        column_positions.add((x, sz - 1))
    for z in range(0, sz, 4):
        column_positions.add((0, z))
        column_positions.add((sx - 1, z))
    column_positions.update({(sx - 1, sz - 1), (0, sz - 1), (sx - 1, 0), (0, 0)})
    for x, z in column_positions:
        for y in range(1, column_top + 1):
            set_block(S, x, y, z, 3 if y in (1, column_top) else 2)

    # High side rails only; central apron is kept open to sky.
    rail_y = column_top
    for x in range(0, sx):
        set_block(S, x, rail_y, 0, 3)
        set_block(S, x, rail_y, sz - 1, 3)
    for z in range(0, sz):
        set_block(S, 0, rail_y, z, 3)
        set_block(S, sx - 1, rail_y, z, 3)
    if level >= 2:
        line_z(S, 2, rail_y, 1, sz - 2, 2)
        line_z(S, sx - 3, rail_y, 1, sz - 2, 2)
    if level >= 4:
        line_x(S, 2, sx - 3, rail_y, sz // 2, 2)
        # A suspended chain marker on the side of the crane, not in the central spawn lane.
        chain_x = sx - 3
        for y in range(max(2, rail_y - 3), rail_y):
            set_block(S, chain_x, y, sz // 2, 9)

    # Low safety rails and bollards around edges; avoid the central pad.
    for x in range(1, sx - 1):
        if x not in range(ax - 2, ax + 3):
            set_block(S, x, 1, 1, 8 if level >= 3 else 12)
            set_block(S, x, 1, sz - 2, 8 if level >= 3 else 12)
    for z in range(2, sz - 2):
        if z % 3 == 0:
            set_block(S, 1, 1, z, 12)
            set_block(S, sx - 2, 1, z, 12)

    # Small control booth grows at high levels, kept beside the anchor and away
    # from the main cargo apron.
    if level >= 3:
        booth_x1, booth_x2 = 1, min(sx - 2, 5)
        booth_z1, booth_z2 = 1, min(sz - 2, 3)
        for x in range(booth_x1, booth_x2 + 1):
            set_block(S, x, 2, booth_z1, 13)
        for z in range(booth_z1, booth_z2 + 1):
            set_block(S, booth_x1, 2, z, 13)
        for x in range(booth_x1, booth_x2 + 1):
            for z in range(booth_z1, booth_z2 + 1):
                if not (x == ax and z == az):
                    set_block(S, x, 3, z, 2)

    # Lighting on the apron perimeter.
    light_positions = [(1, 1), (sx - 2, 1), (1, sz - 2), (sx - 2, sz - 2)]
    if level >= 2:
        light_positions += [(sx // 2, 1), (sx // 2, sz - 2)]
    if level >= 4:
        light_positions += [(1, sz // 2), (sx - 2, sz // 2)]
    for x, z in light_positions:
        set_block(S, x, 1, z, 7)

    # Retaining back wall for high levels; cargo side remains mostly open.
    if level >= 5:
        for x in range(1, sx - 1):
            set_block(S, x, 1, sz - 1, 14)
            set_block(S, x, 2, sz - 1, 14)
            if x % 2 == 0:
                set_block(S, x, 3, sz - 1, 13)

    return sx, sy, sz, dock_palette(), S



def trade_palette() -> list[OrderedDict]:
    return [
        block_state("minecraft:air"),                         # 0
        block_state("minecraft:smooth_stone"),                # 1 public floor
        block_state("minecraft:spruce_planks"),               # 2 warm wall/floor accent
        block_state("minecraft:stone_bricks"),                # 3 secure foundation/trim
        block_state("minecraft:glass"),                       # 4 windows
        block_state("colonylogistics:trade_terminal"),        # 5 hut anchor
        block_state("minecraft:dark_oak_planks"),             # 6 counters
        block_state("minecraft:bookshelf"),                   # 7 ledger shelves
        block_state("minecraft:iron_bars", {"waterlogged": "false"}), # 8 secure escrow bars
        block_state("minecraft:lantern", {"hanging": "false", "waterlogged": "false"}), # 9 lamps
        block_state("minecraft:green_concrete"),              # 10 open trade board
        block_state("minecraft:gold_block"),                  # 11 reward/vault marker
        block_state("minecraft:polished_andesite"),           # 12 desk/stone trim
        block_state("minecraft:oak_fence", {"waterlogged": "false"}), # 13 queue rails
        block_state("minecraft:cyan_stained_glass"),          # 14 teller glass
        block_state("minecraft:deepslate_tiles"),             # 15 level 4/5 secure vault
    ]


def design_trade_terminal(level: int) -> tuple[int, int, int, list[OrderedDict], list[list[list[int]]]]:
    # Fixed full production footprint for all levels, mirroring the Container
    # Dock rework. Lower levels reserve the whole marketplace footprint while
    # adding more secure trade/vault features over upgrades.
    sx, sy, sz = (15, 8, 13)
    S = empty_structure(sx, sy, sz)
    ax, ay, az = ANCHOR

    # Foundation and public trading floor. The whole footprint is reserved from
    # Level 1 so paths and neighboring buildings can be planned around the final
    # marketplace size.
    for z in range(sz):
        for x in range(sx):
            border = x in (0, sx - 1) or z in (0, sz - 1)
            if border:
                state = 3
            elif z <= az + 2:
                state = 1
            else:
                state = 2 if (x + z + level) % 5 == 0 else 1
            set_block(S, x, 0, z, state)

    # Front approach and queue strip aligned to the hut anchor.
    for x in range(max(0, ax - 2), min(sx, ax + 4)):
        set_block(S, x, 0, 0, 12)
    for z in range(1, min(sz - 1, az + 4)):
        if z != az:
            set_block(S, ax, 0, z, 12)

    wall_top = sy - 3
    # Progressive shell: all levels use the same footprint, but lower levels are
    # open market stalls while higher levels become a secure exchange hall.
    for y in range(1, wall_top + 1):
        for z in range(sz):
            for x in range(sx):
                perimeter = x in (0, sx - 1) or z in (0, sz - 1)
                if not perimeter:
                    continue
                # Entrance centered on the hut anchor side.
                if z == 0 and ax - 1 <= x <= ax + 1 and y <= 2:
                    continue
                corner = (x in (0, sx - 1) and z in (0, sz - 1))
                if level <= 1 and y > 2 and not corner:
                    continue
                if level == 2 and y > 3 and not corner:
                    continue
                window = y == 2 and not corner and ((x + z) % 2 == 0)
                state = 3 if corner else (4 if window else (15 if level >= 4 and y == wall_top else 2))
                set_block(S, x, y, z, state)

    # Roof/awning grows by level but reserves the same bounding box.
    roof_y = sy - 1
    awning_y = min(sy - 2, 4 + level // 2)
    if level <= 2:
        for x in range(1, sx - 1):
            set_block(S, x, awning_y, 0, 12)
            set_block(S, x, awning_y, 1, 12)
    else:
        for z in range(sz):
            for x in range(sx):
                if level >= 5 or x in (0, sx - 1) or z in (0, sz - 1) or (level >= 4 and (x + z) % 3 != 0):
                    set_block(S, x, roof_y, z, 12 if not (x in (0, sx - 1) or z in (0, sz - 1)) else 3)

    # Hut anchor and teller counter. Keep the anchor clear and visible from the
    # entrance; Trade Terminal remains its own building, not part of Logistics Office.
    set_block(S, ax, ay, az, 5)
    for x in range(ax + 1, min(sx - 1, ax + 5)):
        set_block(S, x, ay, az, 6)
    for x in range(ax + 1, min(sx - 1, ax + 5)):
        if level >= 3:
            set_block(S, x, ay + 1, az, 14)

    # Queue rails in the public lobby.
    for z in range(1, az):
        if z % 2 == 1:
            set_block(S, ax - 2, 1, z, 13)
            set_block(S, ax + 2, 1, z, 13)

    # Request/reward display desks and trade board.
    set_block(S, ax + 2, ay, az + 2, 10)
    set_block(S, ax + 3, ay, az + 2, 11)
    if level >= 2:
        for x in range(ax + 1, min(sx - 2, ax + 6)):
            set_block(S, x, ay, az + 3, 6)
        for x in range(ax + 1, min(sx - 2, ax + 6), 2):
            set_block(S, x, ay + 1, az + 3, 10)

    # Ledger shelves and history wall.
    for z in range(az + 2, sz - 2):
        if z % 2 == 0 or level >= 4:
            set_block(S, 1, 1, z, 7)
            if level >= 3:
                set_block(S, 1, 2, z, 7)
    if level >= 5:
        for x in range(3, sx - 3):
            set_block(S, x, 1, sz - 2, 7)
            if x % 2 == 0:
                set_block(S, x, 2, sz - 2, 7)

    # Secure escrow vault in the back-right area. It starts as marked storage and
    # becomes barred/stone-secured at higher levels without blocking the lobby.
    vault_x1, vault_x2 = sx - 5, sx - 2
    vault_z1, vault_z2 = sz - 5, sz - 2
    for x in range(vault_x1, vault_x2 + 1):
        for z in range(vault_z1, vault_z2 + 1):
            if x in (vault_x1, vault_x2) or z in (vault_z1, vault_z2):
                state = 15 if level >= 4 else 3
                set_block(S, x, 1, z, state)
                if level >= 4:
                    set_block(S, x, 2, z, 8 if (x + z) % 2 else 15)
            elif level >= 2:
                set_block(S, x, 1, z, 11 if (x + z) % 2 == 0 else 10)
    # Vault doorway.
    set_block(S, vault_x1, 1, vault_z1 + 1, 0)
    if level >= 4:
        set_block(S, vault_x1, 2, vault_z1 + 1, 8)

    # Lighting.
    light_positions = [(1, 1), (sx - 2, 1), (1, sz - 2), (sx - 2, sz - 2)]
    if level >= 3:
        light_positions += [(sx // 2, 1), (sx // 2, sz - 2)]
    if level >= 5:
        light_positions += [(sx // 2, 1, ), (ax + 3, az + 3)]
    seen = set()
    for x, z in light_positions:
        if (x, z) in seen:
            continue
        seen.add((x, z))
        set_block(S, x, 1, z, 9)

    # Level 5 adds a second-floor gallery over the back half, leaving public
    # access and the terminal desk clear below.
    if level >= 5:
        gallery_y = 4
        for z in range(az + 4, sz - 1):
            for x in range(2, sx - 2):
                set_block(S, x, gallery_y, z, 2)
        for x in range(2, sx - 2):
            set_block(S, x, gallery_y + 1, az + 4, 13)
        for z in range(az + 4, sz - 1):
            set_block(S, 2, gallery_y + 1, z, 13)
            set_block(S, sx - 3, gallery_y + 1, z, 13)

    return sx, sy, sz, trade_palette(), S

def write_all() -> None:
    BLUEPRINT_DIR.mkdir(parents=True, exist_ok=True)
    (BLUEPRINT_DIR / "huts").mkdir(parents=True, exist_ok=True)
    for base_name, designer in [("logistics_office", design_logistics), ("container_dock", design_dock), ("trade_terminal", design_trade_terminal)]:
        for level in range(1, 6):
            sx, sy, sz, palette, structure = designer(level)
            name = f"{base_name}{level}"
            data = make_blueprint(name, sx, sy, sz, palette, structure)
            for out_dir in (BLUEPRINT_DIR, BLUEPRINT_DIR / "huts"):
                (out_dir / f"{name}.blueprint").write_bytes(data)


if __name__ == "__main__":
    write_all()
