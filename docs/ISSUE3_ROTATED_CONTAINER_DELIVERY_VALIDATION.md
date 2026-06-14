# Issue 3 - 回転コンテナ納品のゲーム内確認

## 目的

Container Dock から生成した 3x7x3 コンテナを Create / Create Aeronautics で移動・90度回転した後でも、納品時に core / part を含む全 63 ブロックが撤去されることを確認する。

## 事前準備

1. サーバーとクライアントに同じ Colony Logistics jar と依存 Mod を入れる。
2. `config/colonylogistics-common.toml` で以下が有効になっていることを確認する。

```toml
[testing]
debugMultiplayerNetworkLogging = true
debugContractLifecycleLogging = true
```

3. テスト用に同一 Dock で完結させる場合だけ、ワールド内で一時的に有効化する。

```mcfunction
/colonylogistics testing loopbackcontainer true
```

確認後は必ず戻す。

```mcfunction
/colonylogistics testing loopbackcontainer false
```

## 基本フロー

1. MineColonies の Logistics Office と Container Dock を有効な建築として配置する。
2. Container Dock の位置を控える。以降は `<dock>` と表記する。
3. テスト用コンテナ契約を作る。

```mcfunction
/colonylogistics container localtest <dock> standard
```

4. Logistics Office の MineColonies 建築 UI を開き、Colony Logistics タブから対象のコンテナ契約を受注する。
5. Container Dock の MineColonies 建築 UI を開き、Colony Logistics タブからコンテナを生成する。
6. 生成された core 座標を控える。必要なら以下で周辺コンテナを確認する。

```mcfunction
/colonylogistics container diagnose <dock>
/colonylogistics container inspect <core>
```

## ケース A: 生成時と同じ向きで納品

1. 生成されたコンテナを動かさず、または向きを変えずに Dock の認識範囲内へ置く。
2. Container Dock UI の「近くのコンテナ」に対象行が表示され、状態が納品可能であることを確認する。
3. UI の「納品」を押す。
4. 期待結果:
   - チャットにコンテナ納品成功が表示される。
   - 3x7x3 の 63 ブロックが全て消える。
   - サーバーログに `result=SUCCESS` と `removal=removed 63/63` が出る。
   - 通常は `strategy=connected` が出る。
   - `remaining=0` である。

## ケース B: 90度回転して納品

1. 新しい契約とコンテナを生成する。
2. Super Glue などで 3x7x3 全体が Create / Create Aeronautics の contraption に含まれるようにする。
3. contraption / vessel として組み立て、コンテナの長辺方向が生成時から90度回転するように移動・配置する。
4. Dock の認識範囲内で contraption を分解し、実ブロックとして再配置する。
5. Container Dock UI の「近くのコンテナ」に対象行が表示されることを確認する。
6. UI の「納品」を押す。
7. 期待結果:
   - 中央部だけでなく、両端の part block も残らない。
   - サーバーログに `result=SUCCESS` が出る。
   - 通常は `strategy=connected` と `connected=63` が出る。
   - 隣接コンテナと接続している場合は `strategy=oriented` にフォールバックすることがある。
   - `remaining=0` である。

ログ例:

```text
[CL-MP][container][deliver] result=SUCCESS ... removal=removed 63/63 strategy=connected selectedBefore=63 connected=63 remaining=0 selectedFacing=EAST manifestFacing=SOUTH manifestFacingCount=27 rotatedFacingCount=63 leftovers=[]
```

## ケース C: part 座標を基準に納品

UI は通常 core 座標を送るが、修正後は part 座標からも同一コンテナ core を解決できる。

1. 新しい契約とコンテナを生成する。
2. core ではなく、端の part block 座標を控える。
3. 以下を実行する。

```mcfunction
/colonylogistics container deliver <dock> <part>
```

4. 期待結果:
   - コマンドは core を解決して納品成功する。
   - 3x7x3 の 63 ブロックが全て消える。
   - 出力末尾に解決された core 座標が表示される。

## ケース D: 隣接コンテナを巻き込まない

1. 同じ Dock でコンテナを2個以上生成し、隣接候補パッドに置く。
2. 片方だけを 90度回転して納品する。
3. 期待結果:
   - 納品対象コンテナだけが消える。
   - 隣のコンテナは core と part を含めて残る。
   - 残ったコンテナは Container Dock UI から引き続き認識できる。

## 失敗時に見る場所

- `remaining` が 0 でない場合、同じログ行の `leftovers=[...]` に残った座標が出る。
- `connected` が 63 未満の場合、納品前にコンテナの一部が欠けているか、contraption 分解後に別ブロックへ置換されている可能性がある。
- `connected` が 64 以上で `strategy=oriented` の場合、隣接コンテナとブロック面で接続している可能性がある。
- `selectedBefore` が 63 未満かつ `strategy=oriented` の場合、向き候補でも全体を特定できていない可能性がある。
- `manifestFacingCount` と `rotatedFacingCount` がどちらも低い場合、core 周辺の 3x7x3 実配置が崩れている可能性がある。
- UI に対象コンテナが出ない場合、`/colonylogistics container diagnose <dock>` で距離、契約状態、Dock 一致、担当プレイヤーを確認する。
