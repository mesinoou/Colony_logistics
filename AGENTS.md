# AGENTS.md

このリポジトリは、Minecraft 1.21.1 / NeoForge 21.1.227 / MineColonies 1.1.1300 向けの MineColonies アドオン Mod「Colony Logistics」です。

## 開発目的

Colony Logistics は、MineColonies の複数コロニー間に物流契約を発生させ、プレイヤーが荷物や大型コンテナを輸送することで報酬を得られるようにする Mod です。

主な目的は、Create / Create Aeronautics などで作成した乗り物に、継続的な用途と移動目的を与えることです。

## 重要な前提

* Minecraft: 1.21.1
* NeoForge: 21.1.227
* MineColonies: 1.1.1300
* Java: 21
* Gradle wrapper: 使用する
* Sable は required
* Sable Companion への直接 import は避ける
* Trade Post for MineColonies とは通貨アイテムのみ連携する
* 報酬通貨は `mctradepost:mctp_coin`
* `minecraft:emerald` fallback は使用しない

## アーキテクチャ方針

MineColonies Hut ブロックには Colony Logistics 独自 BlockEntity を持たせない。

MineColonies 建築のアンカーは、MineColonies 標準の `minecolonies:colonybuilding` / `TileEntityColonyBuilding` に寄せる。

Colony Logistics 独自状態は BlockEntity ではなく `LogisticsMarketSavedData` に保存する。

複数建築物に対応するため、状態管理キーは原則として以下の組み合わせを使う。

```text
dimension + BlockPos
```

## 主要建築物

### Logistics Office

物流事務所。
Freight Board 機能はここに統合済み。
契約一覧、受注、キャンセル、納品などの入口になる。

### Container Dock

大型コンテナ生成・管理用建築物。
Trade Mode は削除済みで、常に Both / 両方扱い。
コンテナは全規格 3×7×3。
Create / Create Aeronautics で組み立てられる必要があるため、コンテナブロックを完全な破壊不能ブロックにしてはいけない。

### Trade Terminal

プレイヤー間取引用端末。
Logistics Office には統合しない。
作成された OPEN 状態のプレイヤー間取引は、全 Logistics Office から確認できる必要がある。

## 削除済み・復活させてはいけないもの

以下は過去の仕様であり、復活させない。

```text
- Standalone Freight Board block
- Freight Board item / recipe / blockstate / model / texture
- Container Dock の Trade Mode 切り替え
- minecraft:emerald fallback 報酬
- Small / Medium / Large / Heavy でサイズが異なるコンテナ
- Colony Logistics 独自 Hut BlockEntity
```

ただし、`FreightBoardScreen` という名前の画面クラスは、Logistics Office の契約一覧 UI として残っている場合がある。

## GUI レイアウト維持

以下の手動調整済み値は、理由なく変更しない。

```text
ContainerDockScreen.java:
- imageWidth = 470
- RIGHT_X = 235
- RIGHT_W = 240

FreightBoardScreen.java:
- imageWidth = 470
- STATUS_X = 250
- ASSIGNEE_X = 300
- REWARD_X = 340
- ACTION_W = 40

TradeTerminalScreen.java:
- imageWidth = 470
- LEFT_PANEL_W = 160
- OPEN_PANEL_X = 180
- OPEN_PANEL_W = 300
- HISTORY_PANEL_X = 180
- HISTORY_PANEL_W = 300
- INVENTORY_PANEL_X = 4
- INVENTORY_PANEL_Y = 176

TradeTerminalMenu.java:
- 実インベントリスロット startX = 10
- startY = 180
```

## Blueprint / 建築スタイル

現在使用する style pack は以下。

```text
src/main/resources/blueprints/colony_logistics/
```

旧 style pack は使用しない。

```text
src/main/resources/blueprints/colony_logistics_dev/
```

旧パックを復活させないこと。

Runtime installer は起動時に bundled blueprint pack を `<gameDir>/blueprints/colony_logistics/` へコピーする。

## Container Dock オフセット

協力者作成の建築データに合わせ、Container Dock のコンテナ生成候補は実測値を正とする。

コンテナ中央部との差分を、以下の形式で扱う。

```text
右側を正
/colonylogistics minecolonies resolve <dock> の結果方向を正面
(左右, 前後)
```

現在の実測候補は以下。

```text
(7,  2)
(11, 2)
(15, 2)
(3, -8)
(7, -8)
(11,-8)
(15,-8)
```

この7箇所に正しく 3×7×3 コンテナが生成できればよい。

## マルチプレイ上の注意

C2S パケットは、対応するメニューを実際に開いているプレイヤーだけが実行できるようにする。

遠隔操作対策として、建築からの距離チェックを維持する。

Trade Terminal の一時 request / reward スロットは、SavedData 共有バッファにしない。
メニュー単位の一時バッファとして扱い、閉じたときに返却する。

System chat 送信は `SafeSystemChat` を経由する。
`sendSystemMessage(Component.translatable(...))` を直接呼ばない。
Minecraft 1.21.1 環境で system_chat の encode / decode 失敗を起こす可能性があるため。

## ビルド

Windows:

```powershell
.\gradlew.bat clean build
```

Linux / macOS:

```bash
./gradlew clean build
```

compile のみ:

```powershell
.\gradlew.bat clean compileJava
```

## 作業時の確認

変更後は最低限以下を確認する。

```text
- compileJava が通るか
- Dedicated server で起動するか
- client-only class が server 側でロードされていないか
- Logistics Office / Container Dock / Trade Terminal の MineColonies UI タブが開くか
- Container Dock から正しい位置にコンテナが生成されるか
- Create Aeronautics でコンテナが組み立てられるか
- Trade Terminal の取引が他コロニー Logistics Office からも見えるか
```

## コミット方針

大きな変更は小さく分ける。

推奨ブランチ名:

```text
fix/container-offset
fix/system-chat
fix/multiplayer-sync
feature/logistics-contracts
feature/trade-terminal
docs/readme-update
```

生成物は原則 commit しない。

```text
build/
run/
.gradle/
*.jar
*.zip
logs/
crash-reports/
```

## 新しいチャットや Codex への指示

作業を再開する場合は、まず以下を読むこと。

```text
README.md
AGENTS.md
```

そのうえで、現在の最新仕様を古い README や Phase 16 系メモより優先すること。
