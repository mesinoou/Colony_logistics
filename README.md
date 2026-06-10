# Colony Logistics

MineColonies のコロニー間物流を、プレイヤー自身が担えるようにする Minecraft Mod です。
Minecraft 1.21.1 / NeoForge 21.1.227 / MineColonies 1.1.1300 向けに開発しています。

## 概要

Colony Logistics は、MineColonies の複数コロニーを「物流拠点」として扱い、プレイヤーが荷物や大型コンテナを運ぶことで報酬を得られるようにするアドオン Mod です。

通常の MineColonies では、コロニーは主に建築・住民・生産の拠点として機能します。
この Mod ではそこに「都市間輸送」「契約」「報酬」「プレイヤー間取引」の仕組みを追加し、プレイヤーが乗り物や輸送手段を使ってコロニー間を移動する目的を作ることを目指しています。

## 企画意図

この Mod の目的は、Create / Create Aeronautics などで作った乗り物に、ゲーム内で継続的な役割を与えることです。

乗り物を作ること自体は楽しい一方で、完成後に「どこへ行くのか」「何を運ぶのか」「なぜ移動するのか」が弱くなりがちです。
そこで、MineColonies のコロニー間に物流契約を発生させ、プレイヤーが荷物を運ぶことで通貨報酬を得るループを作りました。

また、本 Mod では単に荷物を運ぶだけでなく、コンテナに重量や取り扱い難易度などの物理特性を持たせています。
これにより、軽量な貨物は小型航空機や高速輸送向け、重量貨物は大型飛行船や列車向けといった形で輸送手段ごとの役割分担が生まれます。

例えば、高速だが積載量の少ない航空機は軽量貨物の高速配送に適し、低速でも大容量の輸送船や飛行船は大量輸送に適するといった選択が発生します。
プレイヤーは契約内容や貨物特性に応じて最適な輸送手段を選択する必要があり、単なる移動ではなく「物流計画」を考えるゲームプレイを目指しています。

想定しているプレイサイクルは以下です。

```text
依頼を確認する
→ 荷物やコンテナを受け取る
→ 貨物特性に適した輸送手段を選ぶ
→ 乗り物で別コロニーへ輸送する
→ 納品して報酬を得る
→ 報酬で設備や輸送手段を強化する
→ より難しい依頼に挑戦する
```

## 主な機能

### Logistics Office

物流事務所です。
コロニーに発生した輸送契約を確認し、受注・キャンセル・納品管理を行う中心拠点です。

従来の Freight Board 機能は Logistics Office に統合されています。
現在は MineColonies の標準建築 UI 内に Colony Logistics タブを追加し、そこから専用 UI を開く構成にしています。

### Container Dock

大型コンテナを生成・管理するための建築物です。
通常のインベントリに入らない荷物を、3×7×3 のコンテナブロックとして生成します。

生成されたコンテナは Create / Create Aeronautics などの移動・組み立て機能で運ぶことを想定しています。
コンテナは複数設置に対応し、建築物の向きに応じて生成位置が変わるように調整しています。

### Trade Terminal

プレイヤー間取引を行うための端末です。
出品者が要求アイテムと報酬を設定し、他のプレイヤーが要求アイテムを納品すると報酬を受け取れる、簡易エスクロー型の取引システムです。

マルチプレイでの使用を想定し、報酬の預託、取引キャンセル、履歴表示、他コロニーの Logistics Office からの取引確認に対応しています。

## 対応環境

開発・確認対象は以下です。

```text
Minecraft: 1.21.1
NeoForge: 21.1.227
MineColonies: 1.1.1300
Java: 21
```

主な連携 Mod:

```text
MineColonies
Structurize
BlockUI
Domum Ornamentum
Multi-Piston
Create / Create Aeronautics
Sable
Trade Post for MineColonies
```

Trade Post for MineColonies とは、主に通貨アイテム `mctradepost:mctp_coin` の利用で連携しています。
輸送契約や物流処理そのものは Colony Logistics 側で管理します。

## 現在の実装状況

実装済みの主な内容:

```text
- Logistics Office / Container Dock / Trade Terminal の MineColonies 建築登録
- MineColonies 標準建築 UI への Colony Logistics タブ追加
- Inventory に入る小包配送
- 3×7×3 コンテナ配送
- 複数コロニー間の契約生成
- 契約の受注・キャンセル・納品
- mctradepost:mctp_coin を用いた報酬支払い
- Gold / Diamond coin への両替対応
- Trade Terminal によるプレイヤー間取引
- マルチプレイ向けのネットワーク安全化
- Dedicated server での client-only class 分離
- 協力者作成の MineColonies 建築スタイルへの置き換え
- Container Dock の実測オフセット調整
```

## 開発上の特徴

このプロジェクトでは、単にブロックや UI を追加するだけでなく、既存 Mod の仕様に合わせた統合を重視しています。

特に意識している点:

```text
- MineColonies の Hut / Building システムに合わせた建築登録
- Build Tool から配置できる blueprint pack の整備
- Dedicated server で client-only class がロードされない構成
- マルチプレイで複数プレイヤーが同時に操作しても状態が混線しない設計
- 契約・取引・コンテナ状態を BlockEntity ではなく SavedData 側で管理
- Create / Create Aeronautics による物理的なコンテナ輸送を妨げないブロック仕様
- コンテナの物理特性を活用した輸送手段の差別化を見据えた設計
```

## ビルド方法

Java 21 を使用してください。

Windows:

```powershell
.\gradlew.bat clean build
```

Linux / macOS:

```bash
./gradlew clean build
```

生成された jar は以下に出力されます。

```text
build/libs/
```

## 開発用コマンド例

コンテナ生成候補の確認:

```mcfunction
/colonylogistics container candidates <dockX> <dockY> <dockZ> standard
```

MineColonies 建築の向き解決確認:

```mcfunction
/colonylogistics minecolonies resolve <dockX> <dockY> <dockZ>
```

テスト用ループバックコンテナ契約の有効化:

```mcfunction
/colonylogistics testing loopbackcontainer true
```

確認後は必ず戻します。

```mcfunction
/colonylogistics testing loopbackcontainer false
```

## マルチプレイβテストについて

現在はマルチプレイβテスト段階です。
テスト時は、サーバー側とクライアント側で同じ jar と依存 Mod バージョンを使用してください。

確認対象:

```text
- 複数コロニー間で依頼が生成されるか
- Logistics Office から契約一覧を確認できるか
- Container Dock から正しい位置にコンテナが生成されるか
- Create Aeronautics でコンテナを組み立て・輸送できるか
- Trade Terminal のプレイヤー間取引が他コロニーからも確認できるか
- サーバー側ログに不自然なエラーが出ないか
```
