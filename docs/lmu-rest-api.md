# Le Mans Ultimate REST API 調査メモ

Le Mans Ultimate（LMU）はガレージ画面・観戦画面などのゲーム内 WebUI が動作する土台として、ローカル REST API サーバーを内蔵している。本ドキュメントは、[`:core:windows-shared-memory`](../core/windows-shared-memory) が読み取る `LMU_Data` 共有メモリ（→ [`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md)）には**存在しない情報**（天候予報、Virtual Energy 消費履歴、ピットメニューの状態等）を KoDriver に取り込めるかどうかを検討するための事前調査メモである。

> **調査時点の限界**: 本ドキュメントはサードパーティのリバースエンジニアリング成果物・コミュニティの一次情報を基にまとめた調査結果である。`/rest/sessions/weather` / `/rest/strategy/usage` / `/rest/garage/UIScreen/RepairAndRefuel` / `/rest/watch/standings` については実機（Windows 機の `localhost:6397`）でのレスポンス実測を行い、該当セクションに反映済み。それ以外のエンドポイントの実測・疎通確認は別タスクで行う。

---

## 目次

1. [共有メモリとの違い・使い分け](#共有メモリとの違い使い分け)
2. [基本情報](#基本情報)
3. [判明しているエンドポイント一覧](#判明しているエンドポイント一覧)
4. [`/rest/sessions/weather` のレスポンス構造](#restsessionsweather-のレスポンス構造)
5. [`/rest/strategy/usage` のレスポンス構造](#reststrategyusage-のレスポンス構造)
6. [`/rest/garage/UIScreen/RepairAndRefuel` のレスポンス構造](#restgarageuiscreenrepairandrefuel-のレスポンス構造)
7. [`/rest/watch/standings` のレスポンス構造](#restwatchstandings-のレスポンス構造)
8. [既知の注意点・落とし穴](#既知の注意点落とし穴)
9. [KoDriver への組み込みを検討する場合の論点](#kodriver-への組み込みを検討する場合の論点)
10. [未確認・追加調査が必要な点](#未確認追加調査が必要な点)
11. [参考リポジトリ・情報源](#参考リポジトリ情報源)

---

## 共有メモリとの違い・使い分け

KoDriver の現行実装は `OpenFileMappingA` / `MapViewOfFile` で `LMU_Data` 共有メモリセグメントを直接パースしている（[`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md) 参照）。REST API はこれとは全く別レイヤーの、HTTP 経由でゲーム内 WebUI 用のデータを取得する仕組みであり、以下の点で異なる。

| 観点 | 共有メモリ（`LMU_Data`） | REST API |
|---|---|---|
| 転送方式 | メモリマップドファイル（プッシュ型、ポーリングで読む） | HTTP（クライアント側でポーリングする必要あり） |
| 更新頻度 | 高頻度（テレメトリは毎フレーム相当） | エンドポイント依存。低頻度・スナップショット的なものが多い |
| 主な内容 | 物理演算値、順位/ギャップ、ラップ/セクタータイム、ブレーキバランス等 | 天候予報、Virtual Energy 消費履歴、ピット/リペア/ダメージのメニュー状態、ガレージ設定スナップショット等、LMU 固有の UI 向けデータ |
| プラットフォーム制約 | Windows のみ（`SharedMemoryReader` の制約） | HTTP なので原理上プラットフォーム非依存（ただし LAN 内の Windows 版 KoDriver からのみアクセス可能な運用になる） |

[race-engineer プロジェクトの統合ドキュメント](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) では、「共有メモリで取得できない・リアルタイム性を要求しない情報だけを REST API から補う」という使い分けが推奨されている。

---

## 基本情報

- **ベース URL**: `http://localhost:6397`（既定ポート。IPv4 推奨。古いビルドでは IPv6 の `http://[::1]:6397` へのフォールバックが必要という報告もある）
- **バインド範囲**: **ループバック（`localhost`）限定**（実機確認済み）。LMU が動作している Windows 機自身の `http://localhost:6397/...` へは到達できるが、同一 LAN 内の別端末から `http://<Windows機のIP>:6397/...` へアクセスすると接続がタイムアウトし応答が返らない（`curl` の接続タイムアウト・ブラウザの読み込み中止まりを確認済み）。KoDriver がこの API を利用する場合、**Windows 版デスクトップアプリ自身のプロセスから `localhost` 経由で叩く構成が前提**になる。
- **有効化設定**: 明示的な有効化は不要。ゲーム内 WebUI（ガレージ・観戦画面等）自体がこの REST API サーバー上で動作しており、LMU が起動していれば常時待ち受けている。
- **認証**: なし（ローカルの非暗号化 HTTP API）。
- **HTTP メソッド**: GET/POST/PUT/DELETE が混在するフル REST API。ゲーム内 UI の操作（セットアップ変更・ピットメニュー設定・リプレイ操作等）に使う書き込み系エンドポイントが大半を占める。
- **API 仕様書**: LMU 実行中に `http://localhost:6397/swagger-schema.json` で OpenAPI 2.0 形式の定義を取得できる（`swagger/index.html` に Swagger UI もある）。ある時点のビルドで **全 179 パス、うち非 GET（書き込み系）が 107** という報告がある（[snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) による）。
- **プッシュ型 API**: 存在しない。WebSocket 等は提供されておらず、すべてステートレスな HTTP ポーリングで取得する。
- **公式ドキュメント**: Studio 397 による一般公開の公式リファレンスは確認できていない。上記の `swagger-schema.json` が事実上の一次情報源。

---

## 判明しているエンドポイント一覧

`swagger-schema.json` を解析するサードパーティツール [snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) の解析結果から判明したものを、テレメトリ・アナウンス用途での有用性を軸にカテゴリ別に整理する。**書き込み系（POST/PUT/DELETE）はゲーム内 UI 操作用が大半のため、KoDriver での利用は GET 系に限定する想定。**

### セッション系（`/rest/sessions/...`）

| エンドポイント | メソッド | 概要 |
|---|---|---|
| `/rest/sessions/weather` | GET | 天候予報（→ [次章](#restsessionsweather-のレスポンス構造)） |
| `/rest/sessions/GetGameState` | GET | ゲームフェーズ・ピット状態・マルチスティント状態などの enum 文字列 |
| `/rest/sessions/GetSessionsInfoForEvent` | GET | イベントのセッション構成情報 |
| `/rest/sessions/amount` | GET | セッション数 |
| `/rest/sessions/getAllVehicles` | GET | インストール済み車両一覧 |
| `/rest/sessions/getTracksInSeries` | GET | シリーズ内のコース一覧 |
| `/rest/sessions/opponents`, `/opponents/all` | GET | 対戦相手情報 |
| `/rest/sessions/setting/SESSSET_race_timescale` | GET | タイムスケール設定 |
| `/rest/sessions/setting/SESSSET_private_qual` | GET | プライベート予選設定 |
| `/rest/sessions/weather/{session}/{node}/{setting}`, `/weather/{session}/{preset}` | POST | 天候設定変更（プラクティスの UI 操作用、書き込み系） |
| `/rest/sessions/Championship/...`, `/Coop/...`, `/MultiStintRace/...`, `/SessionPresets/...`, `/ai/...`, `/SaveLoad/...` | 主に POST | セーブ/ロード・チャンピオンシップ・AI 設定等の UI 操作用（書き込み系中心） |

### ガレージ・セットアップ系（`/rest/garage/...`）

| エンドポイント | メソッド | 概要 |
|---|---|---|
| `/rest/garage/getPlayerGarageData` | GET | プレイヤーのガレージ基準値（TC/ABS index、ブレーキバランス、VE、タイヤコンパウンド等）。**フリーズしたスナップショット**（[後述](#既知の注意点落とし穴)） |
| `/rest/garage/getVehicleCondition` | GET | 燃料・タイヤ摩耗・ホイールごとのブレーキ状態 |
| `/rest/garage/UIScreen/RepairAndRefuel` | GET | 現在のスティントの車両状態（燃料、Virtual Energy、ブレーキ摩耗、ダメージ、ピット推奨） |
| `/rest/garage/UIScreen/TireManagement` | GET | 利用可能タイヤセットと消費予測 |
| `/rest/garage/UIScreen/CarSetupOverview` | GET | セットアップ全体とプリセット |
| `/rest/garage/UIScreen/SessionSetup`, `/CoopOverview` | GET | セッション/協力プレイ関連セットアップ画面情報 |
| `/rest/garage/summary` | GET | ガレージ概要 |
| `/rest/garage/tireinfo` | GET | タイヤ情報 |
| `/rest/garage/brakeinfo` | GET | ブレーキ情報（`[]float64`） |
| `/rest/garage/PitMenu/receivePitMenu` | GET | ピットメニューの現在状態 |
| `/rest/garage/isRefreshInProgress`, `/showOnlyRelevantSetups` | GET | UI 状態フラグ |
| `/rest/garage/setup` | GET/POST/PUT | セットアップの取得・保存 |
| `/rest/garage/{aerodynamics,brakes,chassis,drivetrain,electronics,fuel,gears,suspension,tires}` | POST | 各カテゴリの値変更（書き込み系） |

### 観戦・スタンディング系（`/rest/watch/...`）

| エンドポイント | メソッド | 概要 |
|---|---|---|
| `/rest/watch/standings` | GET | 各車の座標、燃料/VE 割合、タイム差、クラス、区間タイム等 |
| `/rest/watch/standings/history` | GET | スタンディング履歴 |
| `/rest/watch/sessionInfo` | GET | セッションフェーズ、最大時間、セクターフラグ、気温等 |
| `/rest/watch/trackmap` | GET | トラックマップ座標 |
| `/rest/watch/focus`, `/focus/{slotid}`, `/focusForward`, `/focusBackward` | GET/PUT | 観戦カメラ制御 |
| `/rest/watch/replays`, `/replay/getReplayFolder`, `/replayCommand/{command}`, `/replaytime/{time}` | 主に GET/POST | リプレイ操作 |

### 戦略系（`/rest/strategy/...`）

| エンドポイント | メソッド | 概要 |
|---|---|---|
| `/rest/strategy/usage` | GET | ドライバーごとの Virtual Energy 消費履歴 |
| `/rest/strategy/pitstop-estimate` | GET | ピットストップ所要時間の内訳予測（燃料/タイヤ/ブレーキ/ダメージ等） |
| `/rest/strategy/overall` | GET | 全体戦略サマリー |

### レース設定系（`/rest/race/...`）

| エンドポイント | メソッド | 概要 |
|---|---|---|
| `/rest/race/car` | GET | 車種一覧 |
| `/rest/race/car/{id}/image` | GET | 車種画像 |
| `/rest/race/track` | GET/POST | コース一覧 |
| `/rest/race/track/{id}/trackmap` | GET | トラックマップ |
| `/rest/race/getAllowedToStartRacing` | GET | レース開始可否判定 |
| `/rest/race/startRace` | POST | レース開始（書き込み系） |

### その他カテゴリ（今回は深掘りせず）

`/rest/profile/...`（プロフィール・DLC・EAC 状態）、`/rest/options/...`（ゲーム設定、大半が UI 操作用）、`/rest/multiplayer/...`（マルチプレイヤー参加状態）、`/rest/replay/...` `/rest/liveryeditor/...` `/rest/materialeditor/...`（リプレイ・リバリーエディタ）、`/rest/chat/` `/rest/hud` `/navigation/...`（チャット・HUD・ナビゲーション状態）、`/webdata/.*`（汎用 Web データ）。

---

## `/rest/sessions/weather` のレスポンス構造

**実機（Windows 機で LMU 起動中に `http://localhost:6397/rest/sessions/weather` へブラウザでアクセス）で取得したレスポンスを基に記載する。**

トップレベルは `PRACTICE` / `QUALIFY` / `RACE` の各セッション種別をキーとするオブジェクトで、それぞれの値が以下 5 つの予報ノードを持つオブジェクトになっている。

- `START` / `NODE_25` / `NODE_50` / `NODE_75` / `FINISH`

各ノードは次の 6 フィールドを持ち、各フィールドは `{"currentValue": <number>, "stringValue": <string>}` の形式になっている。

| フィールド | 概要 | `stringValue` の例 |
|---|---|---|
| `WNV_SKY` | 空模様の種別インデックス（`0`=晴天、`1`=薄曇り、`2`=一部曇り 等） | `"晴天"` 等（**日本語ロケール時は文字化けした状態で返ってくる**。エンコーディングの取り扱いに要注意） |
| `WNV_TEMPERATURE` | 気温（℃） | `"22 °"` |
| `WNV_RAIN_CHANCE` | 降雨確率。`currentValue` は `stringValue` の `%` 表記とそのまま一致する整数（例: `currentValue: 0` に対し `stringValue: "0%"`）。**実測ではすべてのノードで降雨確率が 0% のケースしか確認できておらず、降雨がある場合の値域・小数の有無は未確認** | `"0%"` |
| `WNV_HUMIDITY` | 湿度（%）。`currentValue` はそのまま整数の湿度 | `"67%"` |
| `WNV_WINDDIRECTION` | 風向インデックス（`0`=North, `1`=North East, `2`=East, `3`=South East, `5`=South West 等の 8 方位相当） | `"North East"` |
| `WNV_WINDSPEED` | 風速。`currentValue` は km/h とは異なる内部単位の整数値で、`stringValue` が `currentValue × 3.6` きっかりの km/h 表記になっている（例: `currentValue: 5` → `"18.0 kph"`、`currentValue: 11` → `"39.6 kph"`）。**表示用には `stringValue` を使うのが安全** | `"18.0 kph"` |

各ノード名（`START`/`NODE_25`/`NODE_50`/`NODE_75`/`FINISH`）はセッション長に対する相対位置を表すと推測されるが、**レスポンス自体にはノードの相対位置を示す数値フィールド（`start` 等）は含まれていない**。「あと何分で天候が変わるか」を算出するには、ノード名から相対位置を固定値（`START`=0%, `NODE_25`=25%, `NODE_50`=50%, `NODE_75`=75%, `FINISH`=100%）として解釈し、別途取得したセッション長・経過時間と組み合わせてクライアント側で計算する必要がある。

<details>
<summary>実測レスポンス例（`RACE`.`START` ノード抜粋）</summary>

```json
{
  "WNV_HUMIDITY": { "currentValue": 75, "stringValue": "75%" },
  "WNV_RAIN_CHANCE": { "currentValue": 0, "stringValue": "0%" },
  "WNV_SKY": { "currentValue": 0, "stringValue": "晴天" },
  "WNV_TEMPERATURE": { "currentValue": 23, "stringValue": "23 °" },
  "WNV_WINDDIRECTION": { "currentValue": 1, "stringValue": "North East" },
  "WNV_WINDSPEED": { "currentValue": 5, "stringValue": "18.0 kph" }
}
```

</details>

---

## `/rest/strategy/usage` のレスポンス構造

**実機（Windows 機で LMU 起動中に `http://localhost:6397/rest/strategy/usage` へブラウザでアクセス）で取得したレスポンスを基に記載する。**

トップレベルはドライバー名（表示名）をキーとするオブジェクトで、値は「スティントごとのレコード」を要素とする配列になっている。

| フィールド | 概要 |
|---|---|
| `fuel` | 燃料消費量（単位未確認。0〜1 に近い小数のため、タンク容量に対する割合、または L 単位の消費量の可能性がある） |
| `lap` | そのレコード時点の周回数 |
| `pit` | ピットイン中かどうかの真偽値 |
| `stint` | スティント番号（1 始まり） |
| `tyres` | タイヤ 4 輪分の残量配列（`[FL, FR, RL, RR]` の順と推測。実測ではすべて `100.0` で新品タイヤの状態） |
| `ve` | Virtual Energy 残量（0.0〜1.0 の割合と推測。実測では `1.0` で満タンの状態） |

<details>
<summary>実測レスポンス例</summary>

```json
{
  "yusuke saito": [
    {
      "fuel": 0.904347836971283,
      "lap": 0,
      "pit": false,
      "stint": 1,
      "tyres": [100.0, 100.0, 100.0, 100.0],
      "ve": 1.0
    }
  ]
}
```

</details>

> 実測はセッション開始直後・1 スティント目のみのため、複数スティント時に配列がどう追記されるか、`fuel`/`ve` の単位・値域、タイヤ摩耗が進んだ場合の `tyres` の値の意味は未確認。

---

## `/rest/garage/UIScreen/RepairAndRefuel` のレスポンス構造

**実機（Windows 機で LMU 起動中に `http://localhost:6397/rest/garage/UIScreen/RepairAndRefuel` へブラウザでアクセス）で取得したレスポンスを基に記載する。** 他のエンドポイントと比べて情報量が非常に多く、複数のドメインをまとめて返す構造になっている。

トップレベルは以下のキーを持つオブジェクト。

| キー | 概要 |
|---|---|
| `currentWeather` | 現在の天候（気圧・気温・湿度・雲量・降雨強度等）。単位はいずれもケルビン・SI 単位系相当で、`/rest/sessions/weather` の `WNV_*`（`currentValue`/`stringValue` 形式）とは**異なるフォーマット**。気温は `ambientTempKelvin`（絶対温度）、路面温度は `trackTempKelvin` |
| `fuelInfo` | `currentFuel`/`maxFuel`（燃料、単位はおそらく L）、`currentVirtualEnergy`/`maxVirtualEnergy`（Virtual Energy。実測値は `851000000.0` のような巨大な数値で、`/rest/strategy/usage` の `ve`（0.0〜1.0 の割合）とは**異なるスケール**。`currentBattery`/`maxBattery` は今回の車両（LMP2 相当）では `0.0` で未使用 |
| `pitMenu.pitMenu` | ピットメニューの全項目を配列で返す（`DAMAGE:`/`DRIVER:`/`VIRTUAL ENERGY:`/`FUEL RATIO:`/`TIRES:`/`FL TIRE:`等の各輪別/`R WING:`/`GRILLE:`/各輪の`PRESS:`/`BRAKE DUCT:`/`REPLACE BRAKES:`）。各項目は `PMC Value`（内部コード）、`currentSetting`（現在選択中のインデックス）、`default`、`name`、`settings`（選択可能な全選択肢の配列。ラベル文字列に日本語ロケールでの文字化けが多数含まれる）を持つ。**選択肢一覧を含むため 1 項目あたりの情報量が大きく、ピット設定 UI 操作向けのデータであり、読み上げ用途では `currentSetting` に対応する `settings[currentSetting].text` だけを使う想定になりそう** |
| `pitRecommendations` | タイヤ（`TIRES:`/輪別）・`fuel`・`virtualEnergy` それぞれの推奨要否フラグ（実測はすべて `0` = 走行直後で推奨なし） |
| `pitStopLength.timeInSeconds` | 現在の設定でのピットストップ所要時間予測（秒） |
| `pitStopTimes.times` | ピット作業ごとの所要時間定数テーブル（`FourTireChange`/`FuelFillRate`/`BrakeChange`等）。コース・レギュレーション固有の定数と推測され、走行状況によって変化しない可能性が高い |
| `racePosition` | 順位（`placeOverall`/`placeInClass`）、クラス内トップ・最後尾とのギャップ（`gapToFirstInClassTime`/`gapToLastInClassTime`等） |
| `sessionTime.timeOfDay` | セッション内の時刻（秒。実測値 `29659.5` は 1 日 86400 秒に対する経過秒数の可能性がある） |
| `teamInfo` | `teamName`/`vehicleName`（チーム名・車両名の文字列）に加え、`driverNames` はドライバー名を **1 文字ずつの ASCII コード配列** として返す（例: `[121, 117, ...]` は `"yusuke saito"` の各文字コード＋null終端）。文字列としてそのまま返す `teamName`/`vehicleName` と扱いが異なる点に注意 |
| `wearables` | `body.aero`（エアロダメージ）、`body.detachableParts`（脱落可能パーツごとの脱落フラグ配列）、`brakes`/`suspension`/`tires`（各 4 輪分の摩耗・状態を表す配列） |
| `weatherForecast.nodes` | `Duration`/`Humidity`/`RainChance`/`Sky`/`StartTime`/`Temperature`/`WindDirection`/`WindSpeed` の各キーが **5 要素の配列**（`START`/`NODE_25`/`NODE_50`/`NODE_75`/`FINISH` に対応すると推測）になっている。`/rest/sessions/weather` と同じ天候予報情報を**フィールド名ごとに配列化した別フォーマット**で重複して持っている。`StartTime` は実測ではすべて `0` で、ノードの相対位置を表す値は今回も確認できなかった |

<details>
<summary>実測レスポンス例（`fuelInfo` / `racePosition` / `weatherForecast` 抜粋。`pitMenu` は情報量が大きいため省略）</summary>

```json
{
  "fuelInfo": {
    "currentBattery": 0.0,
    "currentFuel": 103.27770233154297,
    "currentVirtualEnergy": 851000000.0,
    "maxBattery": 0.0,
    "maxFuel": 115.0,
    "maxVirtualEnergy": 851000000.0
  },
  "racePosition": {
    "gapToFirstInClassLaps": 0,
    "gapToFirstInClassTime": 0.0,
    "gapToLastInClassLaps": 0,
    "gapToLastInClassTime": 0.0,
    "placeInClass": 1,
    "placeOverall": 1
  },
  "weatherForecast": {
    "nodes": {
      "Duration": [0, 0, 0, 0, 0],
      "Humidity": [84, 78, 72, 70, 67],
      "RainChance": [0, 0, 0, 0, 0],
      "Sky": [1, 1, 0, 0, 0],
      "StartTime": [0, 0, 0, 0, 0],
      "Temperature": [18, 19, 21, 21, 22],
      "WindDirection": [2, 0, 1, 1, 1],
      "WindSpeed": [2, 5, 11, 10, 5]
    }
  }
}
```

</details>

> `pitMenu` は UI 操作（ピット設定変更）向けの選択肢一覧を含むため情報量が非常に大きい。読み上げ・テレメトリ用途で有用なのは主に `fuelInfo`/`racePosition`/`wearables`/`weatherForecast` で、`pitMenu`/`pitStopTimes` はコース・車両固有の設定値テーブルとしての参照に留まりそう。

---

## `/rest/watch/standings` のレスポンス構造

**実機（Windows 機で LMU 起動中に `http://localhost:6397/rest/watch/standings` へブラウザでアクセス）で取得したレスポンスを基に記載する。**

レスポンスは**出走車両ごとのレコードを要素とする配列**（トップレベルが `[...]`）で、既存の共有メモリ（`Scoring`/`Telemetry`）に近い、ドライバー単位のリアルタイム系情報をまとめて返す。実測は 1 台のみ出走のセッションのため配列要素は 1 件のみ確認できた。主なフィールドは以下の通り。

| フィールド | 概要 |
|---|---|
| `driverName`/`fullTeamName`/`vehicleName`/`vehicleFilename`/`carClass`/`carNumber`/`carId` | ドライバー名・チーム名・車両名・車両クラス等の識別情報（文字列のまま取得できる。`RepairAndRefuel.teamInfo.driverNames` のような ASCII コード配列ではない） |
| `position`/`qualification`/`placeInClass` 相当 | `position`（総合順位）。予選順位は `qualification` |
| `lapsCompleted`/`lapDistance`/`lapStartET`/`estimatedLapTime`/`bestLapTime`/`lastLapTime` | 周回数・コース上距離・推定/ベスト/直前ラップタイム |
| `bestLapSectorTime1`/`bestLapSectorTime2`/`bestSectorTime1`/`bestSectorTime2`/`currentSectorTime1`/`currentSectorTime2`/`lastSectorTime1`/`lastSectorTime2`/`sector` | 各セクタータイムと現在のセクター（`SECTOR1`等の文字列 enum） |
| `timeBehindLeader`/`timeBehindNext`/`timeBehindClassLeader`/`lapsBehindLeader`/`lapsBehindNext`/`lapsBehindClassLeader` | 前走車・首位とのタイム差・周回差 |
| `carPosition`（`x`/`y`/`z`）、`carVelocity`（`velocity`+`x`/`y`/`z`）、`carAcceleration`（同） | 3 次元座標・速度ベクトル・加速度ベクトル |
| `fuelFraction`/`veFraction` | 燃料・Virtual Energy の残量割合（0.0〜1.0）。`RepairAndRefuel.fuelInfo` とは異なり**割合表現**で、`/rest/strategy/usage` の `fuel`/`ve` に近い形式 |
| `pitting`/`pitState`/`pitLapDistance`/`pitGroup`/`pitstops`/`penalties` | ピット関連状態（`pitState` は `EXITING` 等の文字列 enum）とペナルティ数 |
| `flag`/`gamePhase`/`underYellow`/`countLapFlag`/`finishStatus` | フラッグ状態（`GREEN` 等）、ゲームフェーズ、イエロー中かどうか、周回カウント方式、リザルト状態（`FSTAT_NONE` 等） |
| `drsActive`/`headlights`/`inControl`/`inGarageStall`/`player`/`focus`/`hasFocus`/`serverScored` | DRS 作動・ヘッドライト点灯・操作主体・ガレージ内かどうか・自車かどうか・観戦フォーカス中かどうか等の真偽値/フラグ |
| `attackMode` | `remainingCount`/`totalCount`/`timeRemaining`（Attack Mode 系。今回のクラス・イベントでは全て `0`） |
| `pathLateral`/`trackEdge`/`timeIntoLap` | コース基準でのライン取り（横方向オフセット）・コース端からの距離・ラップ内経過時間相当（実測は `-1.87` 等の負値も含まれ、意味は未確認） |
| `steamID`/`slotID`/`upgradePack` | Steam ID（実測は `0`）、スロット番号、アップグレードパック識別文字列 |

<details>
<summary>実測レスポンス例（1 台分、抜粋）</summary>

```json
{
  "driverName": "yusuke saito",
  "fullTeamName": "Proton Competition",
  "vehicleName": "Proton Competition 2025 #77:LM",
  "carClass": "GT3",
  "carNumber": "77",
  "position": 1,
  "qualification": 1,
  "lapsCompleted": 0,
  "fuelFraction": 0.8941177129745483,
  "veFraction": 1.0,
  "flag": "GREEN",
  "gamePhase": "GREEN",
  "pitting": true,
  "pitState": "EXITING",
  "carPosition": { "type": -1, "x": 23.500583648681640, "y": 8.878329277038574, "z": 16.617443084716797 }
}
```

</details>

> 共有メモリの `Scoring`/`Telemetry` 構造体（[`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md)）とかなり重複する内容だが、REST API 側は文字列 enum（`"GREEN"`/`"SECTOR1"` 等）で状態を返すため、共有メモリの数値コードより人間可読な形式になっている点が異なる。共有メモリで取得できないアプリ内表示用の追加情報（`focus`/`hasFocus` 等の観戦フォーカス状態、`attackMode`）も含む。

---

## 既知の注意点・落とし穴

[race-engineer プロジェクトの統合ドキュメント](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) が報告している実運用上の注意点。

- **`getPlayerGarageData` はフリーズしたスナップショット**: 走行中にドライバーがコックピット内で TC/ABS 等のアシストレベルを変更しても、このエンドポイントの値はライブ更新されない（数分間走行しても値が変わらないことを確認済みとの報告）。ライブのアシストレベルは REST API からは外部的に読み取れない可能性が高い。
- **書き込み系は使わない方針が無難**: CrewChief はピットメニュー設定（燃料/リペア選択）に POST 系エンドポイントを使っているとの言及があるが、KoDriver は読み取り専用アプリであるため、意図せずゲーム状態を変更しないよう **GET 専用の利用に限定すべき**。
- **ポーリング頻度**: 1〜5Hz 程度でキャッシュしながらポーリングすることが推奨されている（ゲーム側・API サーバーへの負荷軽減のため）。共有メモリ（16ms/60fps 相当）と比べてはるかに低頻度が前提。
- **バージョン依存**: 公式ドキュメントがなく非公式リバースエンジニアリングに頼っているため、LMU のアップデートでエンドポイントやレスポンス構造が変わる可能性がある（v1.3.3 前後で挙動が変化したという報告あり）。

---

## KoDriver への組み込みを検討する場合の論点

現時点ではあくまで調査メモであり、実装の可否・要否を判断するものではない。実装を検討する場合に整理が必要な論点を列挙する。

- **新規データソースの追加になる**: 既存の `:core:windows-shared-memory` / `:core:lmu-windows-data` の共有メモリパース実装の延長では実現できない。HTTP クライアントで REST API に問い合わせる**別モジュール**（例: `core:lmu-rest-api` 相当）が必要になる規模の変更。
- **プラットフォーム制約の見直しが必要**: 共有メモリ読み取りは Windows 専用という制約（CLAUDE.md 参照）とは別に、HTTP ベースであれば原理上 Windows 以外からもアクセス可能。ただし LMU 自体が Windows 版デスクトップアプリと同一マシンで動くゲームであるため、実際には Windows 版 KoDriver から `localhost:6397` を叩く構成になると想定される。
- **ポーリング設計**: 共有メモリのような高頻度ポーリングは不要（不適切）。天候予報のような低頻度更新のデータに適したポーリング間隔・キャッシュ戦略の設計が必要。
- **未検証情報への依存**: 本ドキュメントの内容は非公式情報に基づくため、実装前に実機での疎通確認・レスポンス実測（別タスク）が必須。

---

## 未確認・追加調査が必要な点

- `/rest/sessions/weather` / `/rest/strategy/usage` / `/rest/garage/UIScreen/RepairAndRefuel` / `/rest/watch/standings` 以外のエンドポイントの正確な JSON レスポンス構造（実機で `http://localhost:6397/swagger-schema.json` を取得するか、[go-lmu-api](https://github.com/snipem/go-lmu-api) の生成コードを確認する必要がある）。
- `/rest/watch/standings` の `pathLateral`/`trackEdge`/`timeIntoLap` が負値を取る場合の意味、複数台出走時の配列の並び順（順位順か固定スロット順か）。
- `WNV_SKY`・ピットメニューの選択肢テキスト等、日本語ロケールで文字化けする文字列の原因（レスポンスヘッダーの文字コード指定、クライアント側のデコード方法等）。
- `WNV_RAIN_CHANCE`（および `RepairAndRefuel` の `weatherForecast.nodes.RainChance`）が 0% 以外の値を取るケースでの値域（整数か小数か、100 分率か 1 分率か）。
- `/rest/strategy/usage` の `fuel`/`ve` の正確な単位・値域（実測はセッション開始直後の 1 レコードのみ）、複数スティント時の配列の追記され方、タイヤ摩耗後の `tyres` 値の意味。
- `RepairAndRefuel` の `fuelInfo.currentVirtualEnergy`（巨大な数値）と `/rest/strategy/usage` の `ve`（0.0〜1.0 の割合）の関係・換算方法。
- `weatherForecast.nodes.StartTime` が常に `0` であった理由（今回のセッションが START ノード付近だったため他ノードの値が未確定なだけか、そもそも別の意味を持つ値か）と、各ノードの相対位置（開始からの時間・割合）を取得する正式な方法。
- `sessionTime.timeOfDay` の基準（1 日 86400 秒に対する経過秒数かどうか）。
- `teamInfo.driverNames` が ASCII コード配列で返る理由・固定長（実測では 32 要素）の仕様。
- サードパーティ製オーバーレイアプリの REST API 呼び出し実装本体（リクエスト間隔、タイムアウト、エラー時のフォールバック処理）の詳細。
- CrewChief の実装（C#）における書き込み系エンドポイントの具体的なリクエストボディ形式。
- rFactor2（非 LMU）と LMU で REST API 仕様がどこまで共通か（rF2 用と LMU 用でアダプタ実装が分離されている事例があることから差異があることは分かっているが、詳細な差分は未整理）。

---

## 参考リポジトリ・情報源

| リソース | 概要 |
|---|---|
| [snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) | `swagger-schema.json` から Go 構造体を推論生成するサードパーティツール。全エンドポイント一覧の把握に利用 |
| [Alexander-Gro/race-engineer `docs/03-LMU-INTEGRATION.md`](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) | 共有メモリと REST API の使い分け・落とし穴に関する実運用知見 |
| [thecrewchief.org フォーラム](https://thecrewchief.org/archive/index.php/t-38.html) | CrewChief における LMU REST API 利用に関する言及 |
| [news.racecontrol.gg の関連記事](https://news.racecontrol.gg/general-tips/le-mans-ultimate-working-with-crew-chief/) | CrewChief × LMU 連携の解説記事 |
