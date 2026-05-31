#!/usr/bin/env bash
set -euo pipefail

missing=0
check_file() {
  if [[ ! -f "$1" ]]; then
    echo "MISSING: $1"
    missing=1
  else
    echo "OK: $1"
  fi
}

check_file build.gradle
check_file gradle.properties
check_file src/main/resources/META-INF/neoforge.mods.toml
check_file src/main/resources/assets/colonylogistics/lang/en_us.json
check_file src/main/resources/assets/colonylogistics/lang/ja_jp.json
check_file src/main/resources/assets/colonylogistics/models/item/freight_parcel.json
check_file src/main/resources/assets/colonylogistics/textures/item/freight_parcel.png
check_file src/main/resources/blueprints/colony_logistics/pack.json
check_file src/main/resources/blueprints/colony_logistics/icon.png
check_file src/main/resources/data/colonylogistics/recipe/freight_parcel.json
check_file src/main/resources/data/colonylogistics/physics_block_properties/freight_container.json
check_file config/colonylogistics-common.toml.example
check_file run/mods/README.md

for id in logistics_office container_dock trade_terminal freight_container_core freight_container_part; do
  check_file "src/main/resources/assets/colonylogistics/blockstates/${id}.json"
  check_file "src/main/resources/assets/colonylogistics/models/block/${id}.json"
  check_file "src/main/resources/assets/colonylogistics/models/item/${id}.json"
done

if [[ $missing -ne 0 ]]; then
  echo "Project layout check failed."
  exit 1
fi

echo "Project layout check passed. This does not replace Gradle compilation."
