# Le Mans Ultimate REST API 調査メモ

Le Mans Ultimate（LMU）はガレージ画面・観戦画面などのゲーム内 WebUI が動作する土台として、ローカル REST API サーバーを内蔵している。本ドキュメントは、[`:core:windows-shared-memory`](../core/windows-shared-memory) が読み取る `LMU_Data` 共有メモリ（→ [`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md)）には**存在しない情報**（天候予報、Virtual Energy 消費履歴、ピットメニューの状態等）を KoDriver に取り込めるかどうかを検討するための事前調査メモである。

> **調査時点の限界**: 本ドキュメントはサードパーティのリバースエンジニアリング成果物・コミュニティの一次情報を基にまとめた調査結果である。`/rest/sessions/weather` / `/rest/strategy/usage` / `/rest/garage/UIScreen/RepairAndRefuel` / `/rest/watch/standings` については実機（Windows 機の `localhost:6397`）でのレスポンス実測を行い、該当セクションに反映済み。加えて `/swagger-schema.json` に載っている **GET 系 79 パスのうち、パス引数が必須のものと副作用がありそうなもの（後述）を除く 65 パスすべて**を実機で叩き、疎通確認・レスポンス概要の把握を行った（→ [その他の実測エンドポイント](#その他の実測エンドポイント) / [GET エンドポイント全数実測結果](#get-エンドポイント全数実測結果)）。非 GET（書き込み系）107 パスは、KoDriver が読み取り専用アプリであり意図せずゲーム状態を変更するリスクを避けるため、本調査では意図的に実行していない。

---

## 目次

1. [共有メモリとの違い・使い分け](#共有メモリとの違い使い分け)
2. [基本情報](#基本情報)
3. [判明しているエンドポイント一覧](#判明しているエンドポイント一覧)
4. [`/rest/sessions/weather` のレスポンス構造](#restsessionsweather-のレスポンス構造)
5. [`/rest/strategy/usage` のレスポンス構造](#reststrategyusage-のレスポンス構造)
6. [`/rest/garage/UIScreen/RepairAndRefuel` のレスポンス構造](#restgarageuiscreenrepairandrefuel-のレスポンス構造)
7. [`/rest/watch/standings` のレスポンス構造](#restwatchstandings-のレスポンス構造)
8. [その他の実測エンドポイント](#その他の実測エンドポイント)
9. [GET エンドポイント全数実測結果](#get-エンドポイント全数実測結果)
10. [既知の注意点・落とし穴](#既知の注意点落とし穴)
11. [KoDriver への組み込みを検討する場合の論点](#kodriver-への組み込みを検討する場合の論点)
12. [未確認・追加調査が必要な点](#未確認追加調査が必要な点)
13. [参考リポジトリ・情報源](#参考リポジトリ情報源)

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
- **API 仕様書**: LMU 実行中に `http://localhost:6397/swagger-schema.json` で OpenAPI 2.0 形式の定義を取得できる（`swagger/index.html` に Swagger UI もある）。**実機（`swagger: "2.0"`）で実測**したところ、全 179 パス中 GET が 79、非 GET（書き込み系）を含むパスが 107 と、[snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) の報告（全 179 パス、非 GET 107）と一致することを確認した。
- **プッシュ型 API**: 存在しない。WebSocket 等は提供されておらず、すべてステートレスな HTTP ポーリングで取得する。
- **公式ドキュメント**: Studio 397 による一般公開の公式リファレンスは確認できていない。上記の `swagger-schema.json` が事実上の一次情報源。
- **レスポンスの `Content-Type`**: 実測した GET エンドポイント（`/rest/sessions/weather` / `/rest/watch/standings` / `/rest/strategy/usage` / `/rest/garage/UIScreen/RepairAndRefuel` / `/rest/sessions/GetGameState` 等）はいずれも `Content-Type: text/plain`（**`charset` パラメータなし**）でレスポンスを返す。ボディの実バイト列は UTF-8 だが `charset` 指定がないため、クライアント側で明示的に UTF-8 としてデコードしないと文字化けする（→ [既知の注意点・落とし穴](#既知の注意点落とし穴)）。

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

### その他カテゴリ

`/rest/profile/...`（プロフィール・DLC・EAC 状態）、`/rest/options/...`（ゲーム設定、大半が UI 操作用）、`/rest/multiplayer/...`（マルチプレイヤー参加状態）、`/rest/replay/...` `/rest/liveryeditor/...` `/rest/materialeditor/...`（リプレイ・リバリーエディタ）、`/rest/chat/` `/rest/hud` `/navigation/...`（チャット・HUD・ナビゲーション状態）、`/webdata/.*`（汎用 Web データ）。テレメトリ・アナウンス用途での有用性は低いと判断し、深掘りはしていないが、GET 系のほぼ全てについて実機での疎通確認・レスポンス概要の把握は行った（→ [GET エンドポイント全数実測結果](#get-エンドポイント全数実測結果)）。

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

## その他の実測エンドポイント

上記 4 エンドポイントほど深掘りはしていないが、実機で疎通確認とレスポンス概要の把握を行ったエンドポイント。

### `/rest/sessions/GetGameState`

ゲームフェーズ・ピット状態等の enum 文字列をまとめたスナップショット。`timeOfDay` は `RepairAndRefuel.sessionTime.timeOfDay`（後述）と同一系列の値を返す（実測差は数秒程度で、同じ内部クロックを指していると推測される）。`closeestWeatherNode`（**綴りは実際にこの typo のまま**）は現在最も近い天候予報ノード 1 件を返す（`weatherForecast.nodes` と同じフィールド構成）。

```json
{
  "MultiStintState": "DRIVING",
  "PitState": "EXITING",
  "closeestWeatherNode": { "Duration": 0, "Humidity": 84, "RainChance": 0, "Sky": 1, "StartTime": 0, "Temperature": 18, "WindDirection": 2, "WindSpeed": 2 },
  "gamePhase": "GPHASE_GREEN",
  "inControlOfVehicle": true,
  "playerVehicleLoaded": true,
  "raceFinished": false,
  "teamVehicleState": "IN CONTROL",
  "timeOfDay": 29115.037109375
}
```

### `/rest/watch/sessionInfo`

セッション全体のメタ情報。`/rest/watch/standings` の各車両レコードとは異なり、**セッション単位で 1 件のみ**返す。`currentEventTime`/`endEventTime`/`startEventTime`/`maxTime` は `timeOfDay`（時刻）とは別系統の「セッション開始からの経過時間・残り時間」を表す値と推測される（実測値は `currentEventTime: 323.4`, `startEventTime: 5.0`, `maxTime: 8100.0` で、いずれも数百〜数千秒オーダーの小さい値であり、`timeOfDay`（約 29115 秒 = 1 日の時刻相当）とスケールが異なる）。`sectorFlag` は 3 要素（セクターごとのフラッグ状態、実測は全て `"UNKNOWN"`）。

```json
{
  "ambientTemp": 18.14,
  "currentEventTime": 323.4,
  "endEventTime": 300.0,
  "lapDistance": 5733.8,
  "maxTime": 8100.0,
  "raceCompletion": { "timeCompletion": 0.0393 },
  "sectorFlag": ["UNKNOWN", "UNKNOWN", "UNKNOWN"],
  "session": "PRACTICE1",
  "startEventTime": 5.0,
  "trackName": "Daytona International Speedway Road Course",
  "yellowFlagState": "NONE"
}
```

### `/rest/garage/getPlayerGarageData`

`VM_*`（車両全体のセットアップ項目）・`WM_*`（4 輪個別の項目）をキーとするフラットなオブジェクト。各値は共通のスキーマを持ち、`value`（内部値）・`stringValue`/`lastSavedStringValue`（表示用文字列）・`minValue`/`maxValue`（設定可能範囲）・`available`（設定可能かどうか）等を含む、セットアップ UI 向けの構造。

```json
{
  "VM_VIRTUAL_ENERGY": {
    "available": true,
    "key": "VM_VIRTUAL_ENERGY",
    "lastSavedStringValue": "100% (28.7 ラップ)",
    "maxValue": 101,
    "minValue": 0,
    "stringValue": "100%",
    "value": 100
  }
}
```

`VM_VIRTUAL_ENERGY.value` は 0〜101 の**パーセンテージ**表現で、`RepairAndRefuel.fuelInfo.currentVirtualEnergy`（巨大な内部単位の数値）・`/rest/strategy/usage` の `ve` および `/rest/watch/standings` の `veFraction`（いずれも 0.0〜1.0 の割合）とは異なる、**3 つ目の VE 表現**であることを確認した。[「既知の注意点」](#既知の注意点落とし穴)で述べた通り、このエンドポイントの値はコックピット内操作に追従しないフリーズしたスナップショットである点にも注意。

### `/rest/strategy/pitstop-estimate`

`/rest/garage/UIScreen/RepairAndRefuel.pitStopLength` と似た、ピット作業ごとの所要時間予測（秒）。実測ではタイヤ・ブレーキ・ダメージ交換が不要な状態のため、`fuel`（給油分の秒数）以外はすべて `0.0` だった。

```json
{ "brakeDucts": 0.0, "brakes": 0.0, "damage": 0.0, "driverSwap": 0.0, "fuel": 2.073, "penalties": 0.0, "tires": 0.0, "total": 2.073, "ve": 0.0 }
```

### `/rest/strategy/overall` / `/rest/garage/PitMenu/receivePitMenu`

`/rest/strategy/overall` は実測（1 台のみ出走のプラクティスセッション）では **空ボディ（`Content-Length: 0`）** が返り、構造を確認できなかった。複数台出走時や特定のセッションフェーズでのみデータを返す可能性がある。`/rest/garage/PitMenu/receivePitMenu` は `RepairAndRefuel.pitMenu.pitMenu` と同様、ピットメニュー全項目（選択肢一覧込み）を返す構造で、情報量が大きいため詳細な構造把握は見送った。

---

## GET エンドポイント全数実測結果

`swagger-schema.json` に載っている GET 系 79 パスのうち、以下を除く **65 パス**を実機（Windows 機、LMU 起動中の `http://localhost:6397`）で実際に叩いた。

**除外したもの（14 パス）:**

- パスパラメータが必須で、有効な ID 等を都度取得しないと叩けないもの: `/rest/materialeditor/download/{materialGuid}` `/rest/materialeditor/{materialGuid}` `/rest/materialeditor/{materialGuid}/{map}` `/rest/race/car/{id}/image` `/rest/race/track/{id}/trackmap` `/rest/garage/setup/notes/(.*)` `/webdata/.*`
- レスポンスが画像・バイナリ等でテキストとして構造把握する意義が薄いもの: 上記の `.../image` 系
- 副作用がある（ゲームの状態や UI を変更しうる）ため、GET であっても意図的に叩かなかったもの: `/rest/multiplayer/join`（マルチプレイヤーへの参加を試みる）、`/rest/watch/play/{id}`（リプレイ再生を開始する）、`/rest/options/resetVRView`（VR ビューをリセットする）、`/rest/options/assign/changestatus`（入力デバイスの割り当て状態を変更する）
- 認証情報を含む可能性が高く、取得・記録を避けたもの: `/rest/profile/getAuthSessionTicket`（Steam 認証チケットを返すエンドポイント）

**実測結果一覧**（本ドキュメントの他セクションで詳細を記載済みの `weather`/`GetGameState`/`sessionInfo`/`standings`/`usage`/`RepairAndRefuel`/`getPlayerGarageData`/`pitstop-estimate`/`overall`/`PitMenu/receivePitMenu` は表中の「詳細」列で参照先を示す）。

| エンドポイント | ステータス | レスポンス概要（キー構造 or 値） |
|---|---|---|
| `/navigation/GetLoadingScreen` | 200 | `selectedCar`/`trackInfo` を持つオブジェクト（ロード画面表示用） |
| `/navigation/getReferrer` | 200 | `{"referrer": ""}` |
| `/navigation/state` | 200 | `loadingStatus`/`state` を持つオブジェクト |
| `/rest/chat/` | 200 | `[]`（チャット履歴。未発言のため空配列） |
| `/rest/garage/UIScreen/CarSetupOverview` | 200 | `carPresetSetups`/`carSetup`/`currentWeather`/`racePosition`/`sessionTime`/`teamInfo`/`weatherForecast`。`RepairAndRefuel` と共通のサブ構造（`currentWeather`/`sessionTime`/`teamInfo`/`weatherForecast`）を持ちつつ、セットアップ全体（`carSetup`）を返す |
| `/rest/garage/UIScreen/CoopOverview` | **404** | ボディなし。協力プレイセッションでない場合は 404 を返す（本調査はソロのプラクティスセッションのため） |
| `/rest/garage/UIScreen/SessionSetup` | 200 | `classesSelection`/`fullGrid`/`selectedCar`/`trackInfo`（セッション設定 UI 向け） |
| `/rest/garage/UIScreen/TireManagement` | 200 | `currentWeather`/`expectedUsage`/`optimalCompoundConditions`/`pitMenu`/`racePosition`/`sessionTime`/`teamInfo`/`tireInvGarageOptions`/`tireInventory`/`wearables`/`weatherForecast`/`wheelInfo`。タイヤ管理 UI 向けで `RepairAndRefuel` と多くのサブ構造を共有 |
| `/rest/garage/brakeinfo` | 200 | `[0.036, 0.036, 0.032, 0.032]` のような 4 輪分の数値配列（単位不明。ブレーキバイアス/摩耗係数の可能性） |
| `/rest/garage/getVehicleCondition` | 200 | `{"brakeCondition":[1,1,1,1],"fuel":103.4,"fuelCapacity":115.0,"suspensionDamage":[0,0,0,0],"tireCondition":[1,1,1,1],"vehicleDamage":0.0}`。4 輪分のコンディション（1.0=良好）とダメージ・燃料をまとめたシンプルな構造 |
| `/rest/garage/isRefreshInProgress` | 200 | `false`（真偽値のみ） |
| `/rest/garage/setup` | 200 | 保存済みセットアッププリセットの配列（実測 25 件）。各要素は `created`/`modified`/`name`/`numDiffUpgrades`/`sameVehicleClass` 等を持つ |
| `/rest/garage/showOnlyRelevantSetups` | 200 | `false`（真偽値のみ） |
| `/rest/garage/summary` | 200 | `activeSetup`/`activeSetupRawData`/`car`/`compareToSetup`/`currentTrackFolder`/`defaultSetup`/`fixedSetupRace`/`settingSummaries`/`track`/`unsavedChanges`。ガレージ画面全体のサマリー |
| `/rest/garage/tireinfo` | 200 | `frontLeft`/`frontRight`/`rearLeft`/`rearRight`/`unitSystem` を持つオブジェクト（4 輪別タイヤ情報） |
| `/rest/hud` | 200 | `{"chat":true,"mfd":true,"speedo":true,"timing":true,"trackMap":true}`。HUD 各要素の表示 ON/OFF 状態 |
| `/rest/materialeditor/liveryeditor/getCustomSkinInfo` | 200 | `albedo_texture`/`region_texture`（リバリーエディタ用テクスチャ情報） |
| `/rest/multiplayer/join/state` | 200 | `"JOIN_IDLE"`（文字列 enum のみ） |
| `/rest/multiplayer/steam/status` | 200 | `true`（真偽値のみ） |
| `/rest/multiplayer/teams` | 200 | `null`（ソロセッションのため） |
| `/rest/options/UIScreen/Controls` | 200 | `allControls` 1 キーのみ（コントローラー割り当て一覧、情報量が大きい） |
| `/rest/options/commandline` | 200 | `{"commandLine": "\"C:\\...\\Le Mans Ultimate.exe\""}`（**実行ファイルのローカルパスを含む**） |
| `/rest/options/display` | 200 | `GAMEOPT_*`/`GRAPHOPT_*` の表示・グラフィック設定値（26 キー） |
| `/rest/options/getAllHapticsDevices` | 200 | `Available`/`Selected`（ハプティクスデバイス一覧） |
| `/rest/options/getAllResolutions` | 200 | 解像度候補の配列（実測 12 件）。各要素は `Height`/`RefreshRate`/`Width` |
| `/rest/options/getAllSoundDevices` | 200 | `Available`/`Selected`（サウンドデバイス一覧） |
| `/rest/options/getLanguage` | 200 | `{"language": "japanese"}` |
| `/rest/options/liveInputs` | 200 | `liveInputs` 1 キー（現在の入力デバイス状態、情報量が大きい） |
| `/rest/options/occlusionCullingSupported` | 200 | `{"occlusionCullingSupported": true}` |
| `/rest/options/settings` | 200 | `DRIVEAIDS_*`/`GAMEOPT_*`/`GRAPHOPT_*`/`SERVEROPT_*`/`SOUNDOPT_*`/`VIDEOOPT_*` 等、**約 220 キー**に及ぶゲーム全設定のスナップショット。運転支援（`DRIVEAIDS_stability_control` 等）の現在値も含むため、コックピット内でのアシスト変更が反映されるかは別途要検証（[既知の注意点](#既知の注意点落とし穴)の `getPlayerGarageData` 同様フリーズの可能性あり） |
| `/rest/profile/` | 200 | `{"name":"yusuke saito","nick":"yusuke saito","steamID":"（64bit SteamID）"}`。**Steam アカウント識別情報を含むため取り扱い注意**（ドキュメントには実値を記載しない） |
| `/rest/profile/eacActive` | 200 | `true`（Easy Anti-Cheat 有効化状態） |
| `/rest/profile/firstRun` | 200 | `false` |
| `/rest/profile/inDevMode` | 200 | `false` |
| `/rest/profile/profileInfo/getProfileInfo` | 200 | `/rest/profile/` に `nationality`（`"JP"`）を加えた上位互換の構造。同様に SteamID を含む |
| `/rest/race/car` | 200 | インストール済み全車両の配列（実測 566 件、レスポンス約 43 万文字）。`displayProperties`/`engine`/`id`/`manufacturer`/`name`/`owned`/`vehFile` 等を持つ |
| `/rest/race/getAllowedToStartRacing` | 200 | `true`（レース開始可否） |
| `/rest/race/track` | 200 | 全コースの配列（実測 37 件）。`length`/`name`/`sceneDesc`/`trackLength`/`type` 等 |
| `/rest/replay/CameraController/getCameraInfo` | 200 | `{"cameraName":"COCKPIT","currentCameraGroup":"Driving"}`（現在の観戦カメラ） |
| `/rest/replay/isActive` | 200 | `false`（リプレイ再生中かどうか） |
| `/rest/sessions/?` | 200 | `SESSSET_*` 形式のセッション設定キー約 60 件（AI 強度・タイヤ摩耗・フラッグルール等、レースウィークエンド設定 UI に対応） |
| `/rest/sessions/GetSessionsInfoForEvent` | 200 | `{"scheduledSessions":[{"airTemp":20,"lengthTime":135,"name":"PRACTICE","rainChance":0}]}` |
| `/rest/sessions/SaveLoad/getSaveJSON` | 200 | `RealRoad`/`SessionPreset`/`VehicleSetup`/`Weather`/`aiVehicles`/`currentSession`/`gamePhase`/`sessionET`/`timeOfDay`/`uniqueSessionID` 等、**セーブデータそのもの**に近い網羅的な構造（約 14 万文字）。`timeOfDay`/`gamePhase` は `GetGameState` と重複するフィールド名だが同一のセーブスキーマ内の値 |
| `/rest/sessions/amount` | 200 | `{"PRACTICE":1,"QUALIFY":0,"RACE":0,"WARMUP":0}`（各セッション種別の設定数） |
| `/rest/sessions/getAllVehicles` | 200 | インストール済み全車両×チーム構成の配列（実測 486 件）。`classes`/`drivers`/`fullTeam`/`number`/`team` 等、`race/car` より参戦チーム目線の情報を持つ |
| `/rest/sessions/getTracksAll` | 200 | 全コース情報の配列（実測 37 件）。`race/track` とほぼ同じだが `corners`/`countryCode`/`defaultRaceWeather` 等イベント既定値を追加で持つ |
| `/rest/sessions/getTracksInSeries` | 200 | 現在のシリーズで選択可能なコースの配列（実測 32 件、`getTracksAll` のサブセット） |
| `/rest/sessions/opponents` | 200 | `id`/`name` のみを持つ軽量な対戦相手一覧（実測 486 件） |
| `/rest/sessions/opponents/all` | 200 | `opponents` と同一内容（実測時は同じ 486 件） |
| `/rest/sessions/restartStintAvailable` | 200 | `{"isRestartStintActionAvailable": false}` |
| `/rest/watch/focus` | 200 | `0`（現在の観戦フォーカス対象の `slotID` のみ） |
| `/rest/watch/getBookmarkedTimestamps` | 200 | `[]`（リプレイのブックマーク、未設定のため空配列） |
| `/rest/watch/replay/getReplayFolder` | 200 | `custom`/`default` のリプレイ保存先パス（**ローカルファイルパスを含む**） |
| `/rest/watch/replays` | 200 | 保存済みリプレイの配列（実測 242 件）。`id`/`metadata`/`replayName`/`size`/`timestamp` |
| `/rest/watch/standings/history` | 200 | `{"0": [...]}` のようにラップ数（or 何らかのインデックス）をキーとするオブジェクトで、各値が `/rest/watch/standings` に近い簡略版のスナップショット配列。実測は 1 スナップショットのみで、複数ラップ経過後の蓄積のされ方は未確認 |
| `/rest/watch/trackmap` | 200 | コース形状の座標点配列（実測 1891 点）。各要素は `type`/`x`/`y`/`z` |
| `/rest/watch/getIncidentsList/{minTimeBetweenContacts}` | 200 | `[]`（インシデント履歴。パラメータは接触判定の最小間隔秒数と推測、実測は `0` を指定し空配列が返った） |

---

## 既知の注意点・落とし穴

[race-engineer プロジェクトの統合ドキュメント](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) が報告している実運用上の注意点。

- **`getPlayerGarageData` はフリーズしたスナップショット**: 走行中にドライバーがコックピット内で TC/ABS 等のアシストレベルを変更しても、このエンドポイントの値はライブ更新されない（数分間走行しても値が変わらないことを確認済みとの報告）。ライブのアシストレベルは REST API からは外部的に読み取れない可能性が高い。
- **文字化けの原因は `Content-Type` の `charset` 未指定**: 実機で確認したところ、GET エンドポイントのレスポンスはいずれも `Content-Type: text/plain`（`charset` パラメータなし）で返る。ボディの実バイト列は UTF-8 だが、`charset` 指定がないため、ブラウザ等クライアント側のデフォルトエンコーディング（Latin-1/windows-1252 相当）で解釈すると `WNV_SKY` の `"晴天"` が `"æ™´å¤©"` のように文字化けする。**レスポンスボディを明示的に UTF-8 としてデコードすれば正しく復元できる**ことを確認済み（例: ブラウザの `fetch` で `arrayBuffer()` を取得し `new TextDecoder('utf-8').decode(...)` する。素朴に `res.text()` や `String(bytes)` に頼らず、UTF-8 デコーダを明示的に使う実装が必要）。
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

- **[解決]** `WNV_SKY`・ピットメニューの選択肢テキスト等が日本語ロケールで文字化けする原因: レスポンスの `Content-Type: text/plain` に `charset` パラメータが付与されておらず、実バイト列（UTF-8）がクライアント側でデフォルトエンコーディング（Latin-1 相当）として解釈されるためと判明。UTF-8 として明示的にデコードすれば正しい文字列を取得できる（→ [既知の注意点・落とし穴](#既知の注意点落とし穴)）。
- **[解決]** `swagger-schema.json` の全パス数・非 GET 数: 実機取得で全 179 パス・GET 79・非 GET を含むパス 107 と確認（→ [基本情報](#基本情報)）。
- **[部分解決]** `sessionTime.timeOfDay` の基準: `/rest/sessions/GetGameState` の `timeOfDay` と同一系列の値であることを確認した（→ [その他の実測エンドポイント](#その他の実測エンドポイント)）。一方で `/rest/watch/sessionInfo` の `currentEventTime`/`startEventTime`/`maxTime` は明らかに異なるスケール（数百〜数千秒オーダー）の「セッション経過時間系」の値であり、両者の関係・`timeOfDay` が「1 日 86400 秒に対する経過秒数」かどうかは未確認のまま。
- **[部分解決]** `teamInfo.driverNames` の構造: 実機では **ドライバーごとの 32 要素配列を要素とする配列**（`[[121, 117, ...], ...]`）であり、当初想定していた単一の 32 要素フラット配列ではなかった。また実測では ASCII 文字列の null 終端（`0`）の直後にも非ゼロの値（`97`）が含まれており、終端後のバイトが必ずしも全てゼロにはならない点が判明。複数ドライバー（マルチスティント/エンデュランス）時に配列がどう埋まるかは未確認。
- **[部分解決]** GET 系エンドポイントの網羅的な疎通確認: パス引数必須・副作用ありと判断したものを除く GET 系 65 パスすべてを実機で叩き、キー構造・値の概要を[「GET エンドポイント全数実測結果」](#get-エンドポイント全数実測結果)に記載した。ただし多くはキー一覧・代表値の把握に留まり、`weather`/`standings`/`usage`/`RepairAndRefuel`/`GetGameState`/`sessionInfo`/`getPlayerGarageData`/`pitstop-estimate` のような**フィールド単位の詳細な意味付け**までは行っていない。特に `/rest/options/settings`（約 220 キー）・`/rest/garage/summary`・`/rest/sessions/SaveLoad/getSaveJSON`（約 14 万文字）等の大規模なレスポンスはキー一覧の把握のみで、各フィールドの詳細調査は未着手。パス引数必須の 7 パス（`/rest/materialeditor/...` 各種、`/rest/race/car/{id}/image`、`/rest/race/track/{id}/trackmap`、`/rest/garage/setup/notes/(.*)`、`/webdata/.*`）と、副作用のリスクから意図的に見送った 4 パス（`/rest/multiplayer/join`、`/rest/watch/play/{id}`、`/rest/options/resetVRView`、`/rest/options/assign/changestatus`）、認証情報を含む `/rest/profile/getAuthSessionTicket` は未実測のまま。
- `/rest/watch/standings` の `pathLateral`/`trackEdge`/`timeIntoLap` が負値を取る場合の意味: 実測では `lapDistance` が負値（`-123.98`、ピットアウト直後でスタートラインを跨いでいない状態）のときに `pathLateral`/`trackEdge`/`timeIntoLap` も負値になっていたため、「ラップカウント開始前（スタート/フィニッシュラインを跨ぐ前）を負値で表す」という仮説は立てられるが、正の値域も含めた正式な意味・複数台出走時の配列の並び順（順位順か固定スロット順か）は未確認。
- `WNV_RAIN_CHANCE`（および `RepairAndRefuel` の `weatherForecast.nodes.RainChance`）が 0% 以外の値を取るケースでの値域（整数か小数か、100 分率か 1 分率か）。実測時は降雨のないセッションのみだったため確認できず。
- `/rest/strategy/usage` の `fuel`/`ve` の正確な単位・値域（実測はセッション開始直後の 1 レコードのみ）、複数スティント時の配列の追記され方、タイヤ摩耗後の `tyres` 値の意味。
- `RepairAndRefuel` の `fuelInfo.currentVirtualEnergy`（巨大な数値）・`/rest/garage/getPlayerGarageData` の `VM_VIRTUAL_ENERGY.value`（0〜101 のパーセンテージ）・`/rest/strategy/usage` の `ve`（0.0〜1.0 の割合）という **VE を表す 3 種類の異なる表現**の相互換算方法。
- `weatherForecast.nodes.StartTime`（および `GetGameState.closeestWeatherNode.StartTime`）が常に `0` であった理由（今回のセッションが START ノード付近だったため他ノードの値が未確定なだけか、そもそも別の意味を持つ値か）と、各ノードの相対位置（開始からの時間・割合）を取得する正式な方法。
- `/rest/strategy/overall` が実測（1 台のみ出走のプラクティスセッション）では空ボディを返した理由。複数台出走時・特定のセッションフェーズ限定でデータを返す可能性があり未確認。
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
