# Le Mans Ultimate REST API 調査メモ

Le Mans Ultimate（LMU）はガレージ画面・観戦画面などのゲーム内 WebUI が動作する土台として、ローカル REST API サーバーを内蔵している。本ドキュメントは、[`:core:windows-shared-memory`](../core/windows-shared-memory) が読み取る `LMU_Data` 共有メモリ（→ [`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md)）には**存在しない情報**（天候予報、Virtual Energy 消費履歴、ピットメニューの状態等）を KoDriver に取り込めるかどうかを検討するための事前調査メモである。

> **調査時点の限界**: 本ドキュメントはサードパーティのリバースエンジニアリング成果物・コミュニティの一次情報を基にまとめた調査結果である。`/rest/sessions/weather` / `/rest/strategy/usage` / `/rest/garage/UIScreen/RepairAndRefuel` / `/rest/watch/standings` については実機（Windows 機の `localhost:6397`）でのレスポンス実測を行い、該当セクションに反映済み。加えて `/swagger-schema.json` に載っている **GET 系 79 パスのうち、パス引数が必須のものと副作用がありそうなもの（後述）を除く 65 パスすべて**を実機で叩き、疎通確認・レスポンス概要の把握を行った（→ [その他の実測エンドポイント](#その他の実測エンドポイント) / [GET エンドポイント全数実測結果](#get-エンドポイント全数実測結果)）。非 GET（書き込み系）108 パス（107 エンドポイント、`/rest/garage/setup` のみ POST/PUT の 2 メソッド）は、KoDriver が読み取り専用アプリであり意図せずゲーム状態を変更するリスクを避けるため、**実機での呼び出しは行わず** `swagger-schema.json` の定義情報のみを整理した（→ [非 GET（書き込み系）エンドポイント一覧](#非-get書き込み系エンドポイント一覧)）。調査の過程で実機呼び出しを試みたところ、ブラウザの CORS 制約によりリクエスト自体がほぼ全て失敗（`Failed to fetch`）した直後に LMU 本体がクラッシュする事象が発生したため、以降は安全のため書き込み系エンドポイントの実機実行を打ち切っている（因果関係は未特定）。

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
10. [非 GET（書き込み系）エンドポイント一覧](#非-get書き込み系エンドポイント一覧)
11. [既知の注意点・落とし穴](#既知の注意点落とし穴)
12. [KoDriver への組み込みを検討する場合の論点](#kodriver-への組み込みを検討する場合の論点)
13. [未確認・追加調査が必要な点](#未確認追加調査が必要な点)
14. [参考リポジトリ・情報源](#参考リポジトリ情報源)

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

### `/rest/garage/tireinfo`

4 輪分のタイヤ状態を返す、テレメトリ用途で有用性が高いエンドポイント。トップレベルは `frontLeft`/`frontRight`/`rearLeft`/`rearRight`（各輪オブジェクト）+ `unitSystem`。

| フィールド | 概要 |
|---|---|
| `centerTemperature`/`leftTemperature`/`rightTemperature` | タイヤ接地面の中央/左/右のトレッド温度（**ケルビン**。実測 `350.26K` ≒ `77.1℃`）。共有メモリの `Telemetry.mWheel[].mTemperature` に近い情報を REST 経由でも取得可能 |
| `load` | タイヤ荷重（単位不明。実測ではフロント `365.16`、リア `369.34` で、リアの方がわずかに大きい） |
| `pressure` | タイヤ空気圧（実測 `136.0`。単位系は `unitSystem` に依存すると推測されるが、`kPa` 相当の値域） |
| `unitSystem` | `"US_METRIC"` 等、値の単位系（実測は `US_METRIC` 固定） |

実測時は 3 本のトレッド温度（`center`/`left`/`right`）がすべて同一値（`350.260009765625`）だったため、直進走行直後の均一な熱分布か、あるいは静止状態のため差が出ていないだけかは未確認。

### `/rest/garage/brakeinfo`

4 輪分の数値配列のみを返すシンプルなエンドポイント。`[0.036, 0.036, 0.032, 0.032]`（実測、フロント 2 輪が `0.036`、リア 2 輪が `0.032`）の並びは `[FL, FR, RL, RR]` と推測され、値域・小数第 2 桁までの精度からブレーキパッド摩耗率（0.0〜1.0）またはブレーキバイアス関連の係数の可能性がある。`getPlayerGarageData` の `WM_BRAKEPAD-W_FL` 等（0〜100 のセットアップ値）とは値域が異なるため、**別の表現**（消耗の割合等）と推測されるが、正確な意味・単位は未確認。

### `/rest/garage/UIScreen/TireManagement`

タイヤ管理 UI 向けのエンドポイントで、燃料/VE 消費予測を含む点でテレメトリ用途上の価値が高い。主要なサブキーは以下の通り。

| キー | 概要 |
|---|---|
| `expectedUsage.compoundsWearPerLap` | コンパウンド種別（`Medium`/`Wet` 等）ごとの 1 周あたり摩耗率予測の配列 |
| `expectedUsage.fuelConsumption`/`fuelFractionPerLap` | 1 周あたりの燃料消費量（実測 `3.55`、単位は L と推測）と、タンク容量に対する割合（実測 `0.0309`） |
| `expectedUsage.virtualEnergyConsumption`/`virtualEnergyFractionPerLap` | 1 周あたりの VE 消費。`virtualEnergyConsumption`（実測 `29641884`）は `RepairAndRefuel.fuelInfo.currentVirtualEnergy`（実測 `851000000` オーダー）と同じ巨大な内部単位、`virtualEnergyFractionPerLap`（実測 `0.0348`）は `/rest/strategy/usage` の `ve` と同じ 0.0〜1.0 の割合表現。**VE の 2 つの表現（内部単位/割合）が 1 レスポンス内に混在**する点に注意 |
| `optimalCompoundConditions.compounds` | コンパウンドごとの最適温度（実測: `Medium`=92、`Wet`=50。単位は `tireinfo` と同じケルビンか摂氏かは未確認だが、値域からすると摂氏の可能性が高い） |
| `wheelInfo.wheelLocs` | **4 輪分のライブ値**の配列。`brakeTemp`（ケルビン、実測 `291.16K`≒`18℃`）・`tireTemp`（ケルビン、実測 `310.0K`≒`36.9℃`）・`tirePressure`（実測 `159.0`）・`compound`（コンパウンド種別インデックス、実測 `0`）。**`getPlayerGarageData` と異なりフリーズしたスナップショットではなく、`tireinfo`/`getVehicleCondition` に近いライブ値**と推測される（要継続実測での裏付け） |
| `tireInventory` | `maxAvailableTires`/`newTires`（利用可能タイヤ本数）、`bestConditionsUsed`（コンディション別の使用状況、実測は `[-1,-1,-1,-1]` と `[100,100,100,100]` の 2 要素配列で意味不明瞭） |
| `tireInvGarageOptions.selectedTires`/`tireOptions` | ピット時に選択可能なタイヤセットの一覧。各セットは `compoundIndex`/`index`/`isUsed`/`type`/`wearValue`（0〜100 の残量）を持つ |

`wheelInfo.wheelLocs` は 4 輪すべて実測でほぼ同一の値（走行直後の均一な状態）だったため、輪ごとの並び順（`[FL, FR, RL, RR]` 等）は他の輪別配列との整合性からの推測に留まり、確定的な検証はできていない。

### `/rest/garage/UIScreen/CarSetupOverview`

`carSetup.garageValues` は `/rest/garage/getPlayerGarageData` と**同一のキー構造**（`VM_*`/`WM_*`、`value`/`stringValue`/`minValue`/`maxValue` 等を持つオブジェクト）であることを実機で確認した。つまり `getPlayerGarageData` 相当のデータを `carSetup.garageValues` というパスでも取得できる（実装上は同じデータソースを異なる UI 画面向けにラップして返している可能性が高い）。`carPresetSetups.presets` は保存済みプリセットの一覧（`/rest/garage/setup` の GET 結果に近いと推測されるが、要素の詳細構造までは未確認）。

---

## GET エンドポイント全数実測結果

`swagger-schema.json` に載っている GET 系 79 パスのうち、以下を除く **65 パス**を実機（Windows 機、LMU 起動中の `http://localhost:6397`）で実際に叩いた。

**除外したもの（14 パス）:**

- パスパラメータが必須で、有効な ID 等を都度取得しないと叩けないもの: `/rest/materialeditor/download/{materialGuid}` `/rest/materialeditor/{materialGuid}` `/rest/materialeditor/{materialGuid}/{map}` `/rest/race/car/{id}/image` `/rest/race/track/{id}/trackmap` `/rest/garage/setup/notes/(.*)` `/webdata/.*`
- レスポンスが画像・バイナリ等でテキストとして構造把握する意義が薄いもの: 上記の `.../image` 系
- 副作用がある（ゲームの状態や UI を変更しうる）ため、GET であっても意図的に叩かなかったもの: `/rest/multiplayer/join`（マルチプレイヤーへの参加を試みる）、`/rest/watch/play/{id}`（リプレイ再生を開始する）、`/rest/options/resetVRView`（VR ビューをリセットする）、`/rest/options/assign/changestatus`（入力デバイスの割り当て状態を変更する）
- 認証情報を含む可能性が高く、取得・記録を避けたもの: `/rest/profile/getAuthSessionTicket`（Steam 認証チケットを返すエンドポイント）

**実測結果一覧**（本ドキュメントの他セクションで詳細を記載済みの `weather`/`GetGameState`/`sessionInfo`/`standings`/`usage`/`RepairAndRefuel`/`getPlayerGarageData`/`pitstop-estimate`/`overall`/`PitMenu/receivePitMenu` は表中の「詳細」列で参照先を示す）。

実測時のステータスは、`/rest/garage/UIScreen/CoopOverview`（協力プレイ専用画面のため非協力プレイセッションでは **404**）を除き全て **200**。以下、カテゴリ別にフィールド単位で構造を記載する（キー名は実際のレスポンスのアルファベット順ではなく、実測 JSON の出現順）。

### `/navigation/...` `/rest/chat/`

**`/navigation/GetLoadingScreen`** — `selectedCar`（後述の `race/car` 1 件分と同一スキーマ: `desc`/`engine`/`fullPathTree`/`fullTeam`/`id`/`manufacturer`/`number`/`sig`/`team`/`vehFile`/`vehicle` 等）+ `trackInfo`（`race/track` に `corners`/`countryCode`/`defaultPracticeStartTime`/`defaultPracticeWeather`（5 要素の天候配列）/`defaultRaceLengthLaps`/`officialEvent`/`openingYear` 等のイベント既定値を加えた上位互換スキーマ、後述の `sessions/getTracksAll` と共通）。ロード画面に表示する車両・コース情報のスナップショット。

**`/navigation/getReferrer`** — `{"referrer": ""}` のみ。遷移元 URL/画面の識別子と推測されるが実測は空文字列。

**`/navigation/state`** — `loadingStatus`（`loading`(bool)/`loadingData`(内部状態を表す JSON 文字列、実測は `loading:false` のため空に近い断片)/`percentage`(実測 `-1` = ロード中でない)/`track`）+ `state`（`appBuild`(実測 `14130`、ビルド番号)/`gamePhase`/`gameSession`/`gameState`(`"GSTATE_DYN"`)/`internalStateCode`(`"OP_RAN_REALTIME_PASS"`)/`navigationState`(`"NAV_REALTIME"`)/`settingMode`(`"SETTING_GRANDPRIX"`)/`steamBetaBranchName`/`user`(`admin`(bool)/`userState`)）。アプリのビルド番号・内部状態遷移を把握できる、デバッグ用途寄りのエンドポイント。

**`/rest/chat/`** — `[]`。チャットメッセージの配列と推測されるが実測は未発言のため空。

### `/rest/garage/UIScreen/...`

**`/rest/garage/UIScreen/CarSetupOverview`** — `carPresetSetups.presets`（保存済みプリセットの配列、実測 3 件）、`carSetup.garageValues`（[`getPlayerGarageData`](#restgaragegetplayergaragedata) と**同一のキー構造**の `VM_*`/`WM_*` オブジェクト。実装上同じデータソースを異なる画面向けに再利用していると推測される）、`currentWeather`（`airPressure`(実測 `99`)/`ambientTempKelvin`/`cloudCoverage`(0.0〜1.0)/`humidity`(0.0〜1.0)/`lightLevel`(0.0〜1.0)/`rainIntensity`/`raining`/`trackTempKelvin`。[`RepairAndRefuel.currentWeather`](#restgarageuiscreenrepairandrefuel-のレスポンス構造)と同一スキーマ）、`racePosition`/`sessionTime`/`teamInfo`/`weatherForecast` は `RepairAndRefuel` と共通のサブ構造。

**`/rest/garage/UIScreen/CoopOverview`** — 実測は **404**（協力プレイでないセッションのため）。構造未確認。

**`/rest/garage/UIScreen/SessionSetup`** — `classesSelection`（選択中のクラス配列、実測 `["GTE"]`）、`fullGrid`(bool)、`selectedCar`/`trackInfo` は `GetLoadingScreen` と同一スキーマ。レースウィークエンド設定画面（クラス選択・グリッド）向け。

**`/rest/garage/UIScreen/TireManagement`** — 詳細は[「その他の実測エンドポイント」](#その他の実測エンドポイント)を参照。上記に加え `pitMenu.pitMenu`（`RepairAndRefuel` と同型のピットメニュー全項目配列）、`wearables`（`body.aero`(数値)/`body.detachableParts`/`brakes`/`suspension`/`tires` の 4 輪配列、`RepairAndRefuel.wearables` と同一スキーマ）を含む。

### `/rest/garage/...`（タイヤ・車両状態・セットアップ管理）

**`/rest/garage/brakeinfo`** / **`/rest/garage/getVehicleCondition`** / **`/rest/garage/tireinfo`** / **`/rest/garage/UIScreen/TireManagement`** は[「その他の実測エンドポイント」](#その他の実測エンドポイント)の各節に詳細を記載済み。

**`/rest/garage/isRefreshInProgress`** — `false`（bool のみ）。セットアップ一覧の再読み込み中かどうかのフラグ（`refreshSetups` POST と対）。

**`/rest/garage/setup`** — 保存済みセットアッププリセットの配列（実測 25 件）。各要素は `created`/`modified`（実測は空文字列 `""`。タイムスタンプ未設定 or 未実装の可能性）、`name`（プリセット名、実測に日本語コース名を含む例あり）、`numDiffUpgrades`（アップグレードパーツとの差分数）、`sameVehicleClass`（bool、現在の車両クラスと一致するプリセットかどうか）を持つ。

**`/rest/garage/showOnlyRelevantSetups`** — `false`（bool のみ）。

**`/rest/garage/summary`** — ガレージ画面全体のサマリー。`activeSetup`（適用中セットアップ名）、`activeSetupRawData`（16 進数文字列、内部エンコード済みセットアップデータと推測）、`car`（`displayProperties.displayName`/`fullTreePath` を含む `race/car` 相当のオブジェクト）、`compareToSetup`（比較対象セットアップ名、実測は空）、`currentTrackFolder`、`defaultSetup`（実測 `"<出荷時の設定>"`）、`fixedSetupRace`（bool、レースでセットアップ固定かどうか）、`track`（`race/track` 相当）、`unsavedChanges`（bool）に加えて、**`settingSummaries`** が特徴的: `AERODYNAMICS_FRONT`/`AERODYNAMICS_REAR`/`ENGINE`/`GEARS`/`SUSPENSION_FRONT`/`SUSPENSION_REAR`/`TIRES_FRONT`/`TIRES_REAR` のカテゴリ別に関連する `VM_*`/`WM_*` キーをグルーピングし、各カテゴリに `diffCount`（既定値からの変更項目数）を付与した集計ビュー。セットアップ UI のカテゴリタブ表示に対応すると推測される。

### `/rest/hud` `/rest/materialeditor/...` `/rest/multiplayer/...`

**`/rest/hud`** — `{"chat":true,"mfd":true,"speedo":true,"timing":true,"trackMap":true}`。HUD 各要素の表示 ON/OFF 状態（`POST /rest/hud/toggle/{component}` の `component` 名と一致）。

**`/rest/materialeditor/liveryeditor/getCustomSkinInfo`** — `albedo_texture`/`region_texture`。いずれも `data:image/png;base64,...` 形式の **Base64 埋め込み画像**（リバリーエディタのプレビューテクスチャ）。

**`/rest/multiplayer/join/state`** — `"JOIN_IDLE"`（文字列 enum。参加試行中は別の値になると推測されるが未確認）。

**`/rest/multiplayer/steam/status`** — `true`（bool。Steam 接続状態）。

**`/rest/multiplayer/teams`** — `null`（ソロセッションのため。マルチプレイ時にチーム編成情報が入ると推測）。

### `/rest/options/...`

**`/rest/options/UIScreen/Controls`** — `allControls.directInput`/`gamepad`/`keyboard` それぞれに `Device`/`Input`（デバイス別の割当一覧）+ `Type` を持ち、`global` にステアリング/フォースフィードバック等の共通設定（`Alternate Neutral Activation`/`Auto Reverse`/`Force Feedback`/`Steering Wheel Range` 等）を持つ、コントロール設定画面の全データ。

**`/rest/options/commandline`** — `{"commandLine": "\"...\\Le Mans Ultimate.exe\""}`。LMU 実行ファイルのローカルパスを含む（本ドキュメントには実際のフルパスを転記していない）。

**`/rest/options/display`** — `GAMEOPT_*`/`GRAPHOPT_*` の表示・グラフィック設定 26 キー。各値は共通スキーマ `{currentValue, maxValue, minValue, stepValue, stringValue, valueType}` を持つ（`stringValue` が表示用文字列、`valueType` は `"LONG"` 等）。例: `GAMEOPT_units`（`currentValue:0` → `"metric"`）、`GRAPHOPT_starting_view`（`currentValue:1` → `"Cockpit"`）。この `{currentValue/maxValue/minValue/stepValue/stringValue/valueType}` パターンは `/rest/options/settings`・`/rest/sessions/?` の値オブジェクトとも共通する定型フォーマット。

**`/rest/options/getAllHapticsDevices`** / **`/rest/options/getAllSoundDevices`** — 共通スキーマ: `Available`（`HardwareDevice`/`Name`/`OutputOnDevice`/`Renderer` を持つデバイス一覧、実測 Haptics 3 件・Sound 4 件）+ `Selected`（現在選択中のデバイス。実測は Haptics が `"Disabled"`、Sound が `"Default"`/`Renderer:"OpenAL Soft"`）。

**`/rest/options/getAllResolutions`** — 解像度候補の配列（実測 12 件）。各要素は `Height`/`Width`（ピクセル）+ `RefreshRate`（対応リフレッシュレートの配列、実測 6 件、Hz 単位と推測）。

**`/rest/options/getLanguage`** — `{"language": "japanese"}`。

**`/rest/options/liveInputs`** — `liveInputs.di`（DirectInput デバイスごとの生値の配列）、`gamepad`/`keyboard`（`minmax`/`raw inputs`）、**`processed inputs`**（`brakes`/`clutch`/`handbrake`/`handfrontbrake`/`steerLeft`/`steerRight`/`throttle`(数値、0.0〜1.0 と推測) + `directManualShift`/`launchControl`/`shiftDown`/`shiftToNeutral`/`shiftUp`/`tcOverride`(bool)）。**ライブのペダル/ステアリング入力値を返す**ため、共有メモリ以外からドライバー入力を取得できる数少ない経路。ポーリング頻度次第では入力デバイスのデバッグ・可視化に使える可能性がある。

**`/rest/options/occlusionCullingSupported`** — `{"occlusionCullingSupported": true}`。

**`/rest/options/settings`** — `/rest/options/display` と同じ `{currentValue/maxValue/minValue/stepValue/stringValue/valueType}` スキーマを持つ約 220 キーの巨大な設定スナップショット。プレフィックスの内訳（実測）は概ね `DRIVEAIDS_*`（運転支援。ABS/TC/オートブリップ等）、`GAMEOPT_*`（AI 強度・ダメージ倍率・自動保存等）、`GRAPHOPT_*`（画質・VR・HUD 表示詳細）、`SERVEROPT_*`（マルチプレイサーバー設定。ソロセッションでも既定値が存在）、`SOUNDOPT_*`（音量各種）、`VIDEOOPT_*`（解像度・vsync・アップスケール）、`CONTROL_*`/`COPY_*`/`DISPLAYOPT_*`/`MP_NETWORK_*`/`REPLAYOPT_*`。運転支援設定（`DRIVEAIDS_stability_control` 等）の現在値も含むため、コックピット内でのアシスト変更が実際にライブ反映されるかは別途要検証（[既知の注意点](#既知の注意点落とし穴)の `getPlayerGarageData` 同様フリーズの可能性あり）。全 220 キーの個別列挙は情報量過多のため本ドキュメントでは省略し、プレフィックスからカテゴリを推測できる形に留める。

### `/rest/profile/...`

**`/rest/profile/`** — `name`/`nick`（表示名、実測は同一値）+ `steamID`（64bit SteamID64 文字列）。**Steam アカウント識別情報のため、本ドキュメントには実際の値を記載しない。**

**`/rest/profile/eacActive`** — `true`（Easy Anti-Cheat 有効化状態）。

**`/rest/profile/firstRun`** — `false`（初回起動フラグ）。

**`/rest/profile/inDevMode`** — `false`（開発者モードフラグ）。

**`/rest/profile/profileInfo/getProfileInfo`** — `/rest/profile/` に `nationality`（実測 `"JP"`）と `language`（実測 `"japanese"`）を加えた上位互換の構造。同様に SteamID を含むため実値は記載しない。

### `/rest/race/...` `/rest/replay/...`

**`/rest/race/car`** — インストール済み全車両の配列（実測 566 件、レスポンス約 44 万文字）。各要素は `displayProperties`（`displayName`/`fullTreePath`）、`dlcappID`、`engine`（実測例: `"Twin-turbo 4.0-litre V8"`）、`fullPathTree`（シリーズ名を含む階層パス文字列、実測例: `"ELMS 2025, GT3, Aston Martin Vantage AMR"`）、`id`（車両固有 ID）、`image`/`thumbnail`（画像を返す `/rest/race/car/{id}/image` への相対パス）、`manufacturer`、`name`、`owned`(bool)、`premId`、`sig`（署名/ハッシュ文字列）、`vehFile`（ローカルファイルパス）を持つ。

**`/rest/race/getAllowedToStartRacing`** — `true`（bool。レース開始条件を満たしているか）。

**`/rest/race/track`** — 全コースの配列（実測 37 件）。各要素は `displayProperties`（`name`/`shortName`）、`length`（文字列、km 単位、実測例 `"4.653"`）、`sceneDesc`（内部シーン識別子）、`trackLength`、`type`（実測 `"Road Course"`）等、`race/car` と対になる構造。

**`/rest/replay/CameraController/getCameraInfo`** — `{"cameraName":"COCKPIT","currentCameraGroup":"Driving"}`。現在の観戦/走行カメラ状態。

**`/rest/replay/isActive`** — `false`（bool。リプレイ再生中かどうか）。

### `/rest/sessions/...`（設定・イベント情報）

**`/rest/sessions/?`** — `SESSSET_*` 形式のレースウィークエンド設定、実測約 60 キー。値は `/rest/options/display` に近いが `{currentValue, numStepsTotal, settingID, stringValue, uiSelectionType, valueType}` という**別のスキーマ**（`uiSelectionType` が `"Arrow"`/`"Switch"` 等の UI 部品種別を表す点が特徴）。代表的なキー: `SESSSET_AI_Aggression`/`SESSSET_AI_Strength`（AI 強度）、`SESSSET_Damage_Multi`（ダメージ倍率、%）、`SESSSET_Finish_Criteria`（決勝の終了条件）、`SESSSET_Fuel_Usage`/`SESSSET_Tire_Wear`（燃料/タイヤ消費倍率、実測は両方 `"Realistic"`）、`SESSSET_Practice_Length`/`SESSSET_Qualify_Length`/`SESSSET_WarmUp_Length`（各セッション長、分単位）、`SESSSET_blue_flags`/`SESSSET_cut_rules`/`SESSSET_flag_rules`（フラッグ・カット判定ルール）、`SESSSET_pract1`〜`4`（各プラクティスセッションの有効/無効）、`SESSSET_pract1_realroad_init`等（各セッションのリアルロード＝路面グリップ初期状態プリセット）。全 60 キーの完全な列挙は情報量過多のため代表例に留める。

**`/rest/sessions/GetSessionsInfoForEvent`** — `{"scheduledSessions":[{"airTemp":20,"lengthTime":135,"name":"PRACTICE","rainChance":0}]}`。イベントで予定されているセッションの配列（実測はプラクティスのみの 1 件）。

**`/rest/sessions/SaveLoad/getSaveJSON`** — **セーブデータそのもの**に近い網羅的な構造（約 14 万文字）。主なフィールド: `RealRoad`（実測 `null`）、`SessionPreset`（`Grid`/`Player`（`DRIVER`/`Game Options`/`Mechanical Failures`/`Race Conditions`/`SCENE`）/`Weather`（`Practice`/`Qualifying`/`Race`）というプリセット全体）、`VehicleSetup`（実測は空文字列）、`Weather`（3 要素の配列、各要素が `Nodes`(内部データ文字列)/`unCompressedDataSize`/`unEncodedDataSize` を持つ圧縮天候データ）、`aiVehicles`（実測空配列）、`allowedVehiclesFilter`（`Optional`/`Required` の車両フィルタ）、`currentSession`（実測 `1`）、`endET`/`greenET`/`redLightET`/`startET`（セッション内の各種イベント経過時間、秒）、`gamePhase`（数値、`GetGameState.gamePhase` の文字列 enum とは異なる**数値表現**）、`maxLaps`（実測 `2147483647` = `int32` の最大値、無制限を表すセンチネル値と推測）、`playerVehicle.slotID`（実測 `-2`）、`sessionState`（数値 enum、実測 `8`）、`sessionTimescale`（実測 `1` = 等倍）、`startTime`/`timeOfDay`（実測どちらも `28800` 秒 = 8:00、`GetGameState.timeOfDay` と同系列だが値が異なりセーブ時点のスナップショット）、`uniqueSessionID`（セッション固有 ID、実測 10 桁の数値）。

**`/rest/sessions/amount`** — `{"PRACTICE":1,"QUALIFY":0,"RACE":0,"WARMUP":0}`（各セッション種別の設定数）。

**`/rest/sessions/getAllVehicles`** — インストール済み全車両×チーム構成の配列（実測 486 件）。`race/car` と多くのフィールドを共有しつつ、`classes`（クラス配列）、`classesOverride`、`drivers`（実測 6 要素、複数ドライバー編成に対応するエンデュランスチーム想定の配列）、`fullTeam`/`team`/`teamFounded`/`teamHeadquarters`（チーム詳細）、`isOwned`（`race/car` の `owned` と同義だが命名が異なる）、`number`（ゼッケン番号）を持つ、より参戦チーム目線の情報。

**`/rest/sessions/getTracksAll`** — 全コース情報の配列（実測 37 件）。`race/track` に `corners`（コーナー数、文字列）、`countryCode`、`defaultPracticeStartTime`/`defaultQualifyStartTime`/`defaultRaceStartTime`（既定開始時刻、分単位と推測）、`defaultPracticeWeather`/`defaultQualifyWeather`/`defaultRaceWeather`（各 5 要素の天候既定値配列）、`eventName`/`grandPrixName`、`location`、`officialEvent`(bool)、`openingYear` を加えたイベント既定値付き上位互換。

**`/rest/sessions/getTracksInSeries`** — 現在のシリーズで選択可能なコースの配列（実測 32 件）。スキーマは `getTracksAll` と同一。

**`/rest/sessions/opponents`** / **`/rest/sessions/opponents/all`** — `id`(number)/`name` のみを持つ軽量な対戦相手一覧（実測どちらも同じ 486 件）。両エンドポイントの違い（`all` が付く/付かない）は実測条件下では確認できず。

**`/rest/sessions/restartStintAvailable`** — `{"isRestartStintActionAvailable": false}`（bool。スティント再開始アクションの実行可否）。

### `/rest/watch/...`

**`/rest/watch/focus`** — `0`（number。現在の観戦フォーカス対象 `slotID`）。

**`/rest/watch/getBookmarkedTimestamps`** — `[]`（リプレイのブックマーク時刻一覧、未設定のため空配列）。

**`/rest/watch/replay/getReplayFolder`** — `custom`/`default` の 2 キー、いずれもリプレイ保存先のローカルディレクトリパス（実測では同一パス）。

**`/rest/watch/replays`** — 保存済みリプレイの配列（実測 242 件）。各要素は `id`（number）、`metadata`（`eventType`/`sceneDesc`/`session`）、`replayDirectory`（ローカルパス）、`replayName`（イベント名）、`size`（バイト数）、`timestamp`（Unix 時刻と推測される 10 桁の数値）。

**`/rest/watch/standings/history`** — `{"0": [...]}` のように**キーが `"0"` から始まる文字列インデックス**（実測はキー `"0"` のみ）を持つオブジェクトで、各値が `/rest/watch/standings` の簡略版（`carClass`/`driverName`/`finishStatus`/`lapTime`/`pitting`/`position`/`sectorTime1`/`sectorTime2`/`slotID`/`totalLaps`/`vehicleName`）の配列。キー `"0"` が「1 周目」を表すのか、それとも別のスナップショット単位かは実測（1 周未満のセッション）では判別できず、複数ラップ経過後にキーがどう増えるかも未確認。

**`/rest/watch/trackmap`** — コース形状の座標点配列（実測 1891 点）。各要素は `type`（number、実測 `0`。コーナー/ストレート等の区間種別と推測）、`x`/`y`/`z`（3 次元座標、共有メモリの `carPosition` と同じスケール感の数値）。

**`/rest/watch/getIncidentsList/{minTimeBetweenContacts}`** — `[]`（インシデント履歴。パラメータは接触判定の最小間隔秒数と推測、実測は `0` を指定し空配列が返った。接触イベントが発生した場合の要素構造は未確認）。

---

## 非 GET（書き込み系）エンドポイント一覧

> **本セクションは実機での呼び出し結果ではなく、`swagger-schema.json` の定義（パス・メソッド・パラメータ）のみを整理したものである。** 実機呼び出しを試みた際に LMU がクラッシュする事象が発生したため（詳細は上記の限界の記載を参照）、書き込み系エンドポイントは意図的に**実行していない**。KoDriver は読み取り専用アプリであり、これらのエンドポイントを実装で使う予定はないが、REST API の全体像を把握するための参考情報として一覧化する。

`swagger-schema.json` の非 GET（POST/PUT/DELETE）は 107 パス・108 メソッド定義（`/rest/garage/setup` のみ POST と PUT の 2 定義を持つ）。カテゴリ別に、パス・メソッド・パスパラメータ／ボディの有無と、パス名・既存の GET 系調査から推測される用途をまとめる。**「推測用途」はパス名からの推測であり実機検証はしていない点に注意。**

### `/navigation/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/navigation/action/{action}` | POST | パス: `action` | UI ナビゲーションアクションの実行（戻る等） |
| `/navigation/openLiveryEditor` | POST | なし | リバリーエディタ画面を開く |
| `/navigation/sendToLog` | POST | ボディ | ゲーム内ログへの書き込み |
| `/navigation/setReferrer` | POST | ボディ | `/navigation/getReferrer` が返す参照元情報の設定 |

### `/rest/cancelSteamAuth` `/rest/chat/`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/cancelSteamAuth` | POST | なし | Steam 認証フローのキャンセル |
| `/rest/chat/` | POST | ボディ | チャットメッセージ送信 |

### `/rest/garage/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/garage/` | PUT | ボディ | ガレージ状態の一括更新 |
| `/rest/garage/(VM_.*)` | POST | パス: `mod`（正規表現）、ボディ | `VM_*`（車両全体セットアップ項目）の値変更。CarSetupOverview 等の UI 操作用 |
| `/rest/garage/(WM_.*)-(.*)` | POST | パス: `mod`/`wheel`（正規表現）、ボディ | `WM_*`（4 輪個別項目）の値変更（例: タイヤ圧・キャンバー） |
| `/rest/garage/PitMenu/loadPitMenu` | POST | なし | ピットメニュー状態の読み込み |
| `/rest/garage/SetCurrentVehicle` | POST | ボディ | 現在選択中の車両の設定 |
| `/rest/garage/SetPreviewSaveFile` | POST | ボディ | プレビュー用セーブファイルの指定 |
| `/rest/garage/drive` | POST | なし | ガレージから走行を開始する（コースイン） |
| `/rest/garage/refreshSetups` | POST | ボディ | 保存済みセットアップ一覧の再読み込み |
| `/rest/garage/setup` | POST | ボディ | セットアップの新規保存 |
| `/rest/garage/setup` | PUT | ボディ | セットアップの上書き保存 |
| `/rest/garage/setup/(.*)` | **DELETE** | パス: `setup`（正規表現） | 指定したセットアップの削除（**恒久的なデータ削除に該当するため実行対象外**） |
| `/rest/garage/setup/compare` | POST | ボディ | セットアップ比較 |
| `/rest/garage/setup/default` | POST | なし | セットアップを既定値にリセット |
| `/rest/garage/setup/notes` | POST | ボディ | セットアップのメモ保存 |
| `/rest/garage/showOnlyRelevantSetups` | POST | ボディ | 「関連セットアップのみ表示」フラグの変更（GET 版と対） |
| `/rest/garage/toRaceMenu` | POST | なし | レースメニューへ遷移 |

### `/rest/hud/...` `/rest/liveryeditor/...` `/rest/materialeditor/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/hud/toggle/{component}` | POST | パス: `component` | HUD 個別要素（`speedo`/`chat`/`mfd`/`timing`/`trackMap` 等）の表示切替 |
| `/rest/hud/toggleAllComponents/{visible}` | POST | パス: `visible`(bool) | HUD 全要素の表示・非表示一括切替 |
| `/rest/liveryeditor/setCamera/{camera}` | POST | パス: `camera` | リバリーエディタのカメラ切替 |
| `/rest/liveryeditor/showRegionTexture/{active}` | POST | パス: `active`(bool) | リバリーエディタの領域テクスチャ表示切替 |
| `/rest/liveryeditor/submitCustomSkin` | POST | なし | カスタムスキンの適用申請 |
| `/rest/materialeditor/liveryeditor/reloadCustomSkin` | POST | なし | カスタムスキンの再読み込み |
| `/rest/materialeditor/{materialGuid}` | PUT | パス: `materialGuid`、ボディ | マテリアル定義の更新 |
| `/rest/materialeditor/{materialGuid}/persist` | POST | パス: `materialGuid`、ボディ | マテリアル定義の永続化保存 |
| `/rest/materialeditor/{materialGuid}/shader` | PUT | パス: `materialGuid`、ボディ | マテリアルのシェーダー設定更新 |

### `/rest/multiplayer/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/multiplayer/cancelJoinRequest` | POST | なし | マルチプレイヤー参加リクエストのキャンセル |
| `/rest/multiplayer/exitVehicle` | POST | なし | 車両からの降車（観戦モードへの移行等） |
| `/rest/multiplayer/takeControlOfVehicle` | POST | なし | 車両の操作権を取得 |

### `/rest/options/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/options/ApplyVideoOptions` | POST | なし | 変更したビデオ設定の適用 |
| `/rest/options/assign/cancel` | POST | なし | 入力デバイス割り当てのキャンセル |
| `/rest/options/assign/confirm` | POST | なし | 入力デバイス割り当ての確定 |
| `/rest/options/float` | POST | ボディ | float 型オプション値の設定（汎用） |
| `/rest/options/graphics/confirmgraphics` | POST | なし | グラフィック設定変更の確定 |
| `/rest/options/graphics/resetgraphics` | POST | なし | グラフィック設定を既定値にリセット |
| `/rest/options/long` | POST | ボディ | 整数型オプション値の設定（汎用） |
| `/rest/options/setConfigControl` | POST | ボディ | キー/ボタン割り当ての設定 |
| `/rest/options/setControls` | POST | ボディ | コントロール設定全般の保存 |
| `/rest/options/setHapticsDevice` | POST | クエリ: `Name` | ハプティクスデバイスの選択 |
| `/rest/options/setInMenuGfxEffects` | PUT | ボディ | メニュー内グラフィックエフェクトの設定 |
| `/rest/options/setInputAxisProperties` | POST | ボディ | 入力軸（ステアリング等）のプロパティ設定 |
| `/rest/options/setSoundDevice` | POST | クエリ: `Name` | サウンドデバイスの選択 |
| `/rest/options/unsetConfigControl` | POST | ボディ | キー/ボタン割り当ての解除 |

### `/rest/profile/...` `/rest/race/...` `/rest/replay/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/profile/DLC/viewDLC` | POST | なし | DLC 購入ページ（Steam ストア等）を開く |
| `/rest/profile/profileInfo/setProfileInfo` | POST | ボディ | プロフィール情報（表示名・国籍等）の更新 |
| `/rest/race/startRace` | POST | なし | レース（セッション）の開始 |
| `/rest/race/track` | POST | ボディ | コース情報の登録・更新 |
| `/rest/replay/CameraController/setCamera` | POST | ボディ | 観戦カメラの切替 |
| `/rest/replay/toggleactive` | POST | なし | リプレイコントローラーの有効/無効切替 |

### `/rest/sessions/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/sessions/Championship/getCurrentChampTemplate` | POST | なし | 現在のチャンピオンシップテンプレート取得 |
| `/rest/sessions/Championship/getGrid` | POST | なし | チャンピオンシップのグリッド取得 |
| `/rest/sessions/Championship/setCurrentChampionshipTemplate` | POST | ボディ | チャンピオンシップテンプレートの設定 |
| `/rest/sessions/Coop/setCoopDriverID` | POST | ボディ | 協力プレイのドライバー ID 設定 |
| `/rest/sessions/FFtoRaceEnd` | POST | なし | セッションをレース終了までファストフォワード |
| `/rest/sessions/MultiStintRace/Drive` | POST | なし | マルチスティントレースでの走行開始 |
| `/rest/sessions/MultiStintRace/UnPause` | POST | なし | マルチスティントレースの一時停止解除 |
| `/rest/sessions/MultiStintRace/setDriverInfo` | POST | ボディ | マルチスティントのドライバー情報設定 |
| `/rest/sessions/SaveLoad/compressSaveFile` | POST | なし | セーブファイルの圧縮 |
| `/rest/sessions/SaveLoad/decompressSaveFile` | POST | なし | セーブファイルの展開 |
| `/rest/sessions/SaveLoad/deleteSaveFile` | POST | なし | セーブファイルの削除（**恒久的なデータ削除に該当するため実行対象外**） |
| `/rest/sessions/SaveLoad/doesBackupExistForThisSession` | POST | なし | 現セッションのバックアップ存在確認 |
| `/rest/sessions/SaveLoad/generateSaveFileFromSessionPreset` | POST | ボディ | セッションプリセットからのセーブファイル生成 |
| `/rest/sessions/SaveLoad/getEveryLocalSave` | POST | なし | ローカルセーブファイル一覧取得 |
| `/rest/sessions/SaveLoad/getNumSaves` | POST | なし | セーブファイル数取得 |
| `/rest/sessions/SaveLoad/isSaveNameValid` | POST | ボディ | セーブ名のバリデーション |
| `/rest/sessions/SaveLoad/loadGame` | POST | なし | セーブデータのロード |
| `/rest/sessions/SaveLoad/saveGame` | POST | なし | 現在の状態をセーブ |
| `/rest/sessions/SaveLoad/saveLastBackup` | POST | なし | 直近バックアップの保存 |
| `/rest/sessions/SaveLoad/saveTemplateToFile` | POST | なし | テンプレートのファイル保存 |
| `/rest/sessions/SessionPresets/applyPreset` | POST | なし | セッションプリセットの適用 |
| `/rest/sessions/SessionPresets/getDefaultPresetForTrack` | POST | なし | コースの既定プリセット取得 |
| `/rest/sessions/SessionPresets/requestPreset` | POST | なし | プリセットのリクエスト |
| `/rest/sessions/ai/TakeDriverControl` | POST | なし | AI にドライバー操作を引き渡す |
| `/rest/sessions/ai/forcePlayerVehAiPit` | POST | なし | プレイヤー車両を強制的に AI 操作でピットへ |
| `/rest/sessions/clearEventNotification` | POST | ボディ | イベント通知のクリア |
| `/rest/sessions/continueGame` | POST | なし | ゲームの続行（ポーズ解除等） |
| `/rest/sessions/getAllAvailableVehicles` | POST | ボディ | 選択可能な車両一覧取得（フィルタ条件付き） |
| `/rest/sessions/notifyInPauseSettings` | POST | ボディ | ポーズ中設定変更の通知 |
| `/rest/sessions/playVOTrigger` | POST | なし | ボイスオーバー（アナウンス音声）トリガーの再生 |
| `/rest/sessions/playerSettings/backupPlayerSettings` | POST | なし | プレイヤー設定のバックアップ |
| `/rest/sessions/playerSettings/restorePlayerSettingsFromBackup` | POST | なし | バックアップからのプレイヤー設定復元 |
| `/rest/sessions/raceControlVerification` | POST | なし | レースコントロールの検証 |
| `/rest/sessions/restartStintAvailable` | POST | なし | スティント再開始可否フラグの設定（GET 版と対） |
| `/rest/sessions/resumePitStop` | POST | なし | ピットストップ処理の再開 |
| `/rest/sessions/returnToMonitor` | POST | なし | 観戦モニター画面へ戻る |
| `/rest/sessions/saveload/getSaveFileJSONFromFilename` | POST | ボディ | 指定ファイル名のセーブデータ JSON 取得 |
| `/rest/sessions/setEventNotification` | POST | ボディ | イベント通知の設定 |
| `/rest/sessions/setHudOnWatchScreen` | POST | ボディ | 観戦画面での HUD 表示設定 |
| `/rest/sessions/settings` | POST | ボディ | セッション設定（`SESSSET_*`、GET `/rest/sessions/?` 相当）の一括更新 |
| `/rest/sessions/weather/{session}/{node}/{setting}` | POST | パス: `session`/`node`/`setting`、ボディ | 天候予報ノードの個別設定値変更（プラクティス UI 操作用） |
| `/rest/sessions/weather/{session}/{preset}` | POST | パス: `session`/`preset` | 天候プリセットの適用 |
| `/rest/sessions/{session}/sessions` | POST | パス: `session` | セッション構成（プラクティス/予選/レース数等）の変更 |

### `/rest/start/...` `/rest/watch/...` `/webdata/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/start/openExternalBrowserToURL` | POST | ボディ | 既定の外部ブラウザで指定 URL を開く |
| `/rest/watch/focus/{cameraType}/{trackSideGroup}/{shouldAdvance}` | PUT | パス: `cameraType`/`trackSideGroup`(int)/`shouldAdvance`(bool) | 観戦カメラのフォーカス対象を条件指定で変更 |
| `/rest/watch/focus/{slotid}` | PUT | パス: `slotid`(int) | 観戦フォーカス対象をスロット ID で指定（GET `/rest/watch/focus` と対） |
| `/rest/watch/focusBackward` | PUT | なし | 観戦フォーカスを前の車両へ |
| `/rest/watch/focusForward` | PUT | なし | 観戦フォーカスを次の車両へ |
| `/rest/watch/replay/setCurrentMetadata` | PUT | ボディ | リプレイのメタデータ設定 |
| `/rest/watch/replay/setReplayFolder` | PUT | ボディ | リプレイ保存先フォルダの設定 |
| `/rest/watch/replay/setReplayUIVisible` | POST | ボディ | リプレイ UI の表示切替 |
| `/rest/watch/replayCommand/{command}` | PUT | パス: `command` | リプレイ操作コマンド（再生/一時停止等）の実行 |
| `/rest/watch/replaytime/{time}` | PUT | パス: `time`(number) | リプレイ再生位置（時刻）の指定 |
| `/webdata/.*` | POST | ワイルドカード | 汎用 Web データエンドポイント（用途不明） |

---

## 既知の注意点・落とし穴

[race-engineer プロジェクトの統合ドキュメント](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) が報告している実運用上の注意点。

- **`getPlayerGarageData` はフリーズしたスナップショット**: 走行中にドライバーがコックピット内で TC/ABS 等のアシストレベルを変更しても、このエンドポイントの値はライブ更新されない（数分間走行しても値が変わらないことを確認済みとの報告）。ライブのアシストレベルは REST API からは外部的に読み取れない可能性が高い。
- **文字化けの原因は `Content-Type` の `charset` 未指定**: 実機で確認したところ、GET エンドポイントのレスポンスはいずれも `Content-Type: text/plain`（`charset` パラメータなし）で返る。ボディの実バイト列は UTF-8 だが、`charset` 指定がないため、ブラウザ等クライアント側のデフォルトエンコーディング（Latin-1/windows-1252 相当）で解釈すると `WNV_SKY` の `"晴天"` が `"æ™´å¤©"` のように文字化けする。**レスポンスボディを明示的に UTF-8 としてデコードすれば正しく復元できる**ことを確認済み（例: ブラウザの `fetch` で `arrayBuffer()` を取得し `new TextDecoder('utf-8').decode(...)` する。素朴に `res.text()` や `String(bytes)` に頼らず、UTF-8 デコーダを明示的に使う実装が必要）。
- **書き込み系は使わない方針が無難**: CrewChief はピットメニュー設定（燃料/リペア選択）に POST 系エンドポイントを使っているとの言及があるが、KoDriver は読み取り専用アプリであるため、意図せずゲーム状態を変更しないよう **GET 専用の利用に限定すべき**。
- **書き込み系エンドポイントの実機呼び出し試行時に LMU がクラッシュした事例あり**: 本調査で非 GET 系エンドポイント（[一覧](#非-get書き込み系エンドポイント一覧)参照）をブラウザの `fetch` で呼び出そうとしたところ、CORS 制約によりリクエストの大半が `TypeError: Failed to fetch` でブロックされたにもかかわらず、その直後に LMU 本体がクラッシュする事象が発生した。リクエストが実際に LMU へ到達していたかどうかは確認できておらず、書き込み系の実行との因果関係は特定できていないが、**書き込み系エンドポイントを不用意に呼び出すことに対しては、通常のリバースエンジニアリング一般のリスクに加えてアプリケーションクラッシュの実例が伴った**という事実として記録しておく。KoDriver の実装で書き込み系エンドポイントを利用することは想定していないが、追加調査を行う場合は保存中のデータがない状態で行う、事前にセーブする等の対策を推奨する。
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

- **[未着手]** 非 GET（書き込み系）108 メソッド定義の実機での実際のレスポンス・挙動: 実機呼び出しの試行中に LMU がクラッシュする事象が発生したため、実行を打ち切った。[「非 GET（書き込み系）エンドポイント一覧」](#非-get書き込み系エンドポイント一覧)は `swagger-schema.json` のパス・パラメータ定義とパス名からの用途推測のみであり、実際のリクエストボディの形式・レスポンス形式・副作用は未検証。特にゲーム状態を変更する系統（`/rest/garage/(VM_.*)` 等のセットアップ変更、`/rest/sessions/settings`、`/rest/race/startRace` 等）を安全に検証するには、専用の検証環境（本番の走行セッションとは別のセーブデータ等）を用意した上で改めて調査する必要がある。
- **[解決]** `WNV_SKY`・ピットメニューの選択肢テキスト等が日本語ロケールで文字化けする原因: レスポンスの `Content-Type: text/plain` に `charset` パラメータが付与されておらず、実バイト列（UTF-8）がクライアント側でデフォルトエンコーディング（Latin-1 相当）として解釈されるためと判明。UTF-8 として明示的にデコードすれば正しい文字列を取得できる（→ [既知の注意点・落とし穴](#既知の注意点落とし穴)）。
- **[解決]** `swagger-schema.json` の全パス数・非 GET 数: 実機取得で全 179 パス・GET 79・非 GET を含むパス 107 と確認（→ [基本情報](#基本情報)）。
- **[部分解決]** `sessionTime.timeOfDay` の基準: `/rest/sessions/GetGameState` の `timeOfDay` と同一系列の値であることを確認した（→ [その他の実測エンドポイント](#その他の実測エンドポイント)）。一方で `/rest/watch/sessionInfo` の `currentEventTime`/`startEventTime`/`maxTime` は明らかに異なるスケール（数百〜数千秒オーダー）の「セッション経過時間系」の値であり、両者の関係・`timeOfDay` が「1 日 86400 秒に対する経過秒数」かどうかは未確認のまま。
- **[部分解決]** `teamInfo.driverNames` の構造: 実機では **ドライバーごとの 32 要素配列を要素とする配列**（`[[121, 117, ...], ...]`）であり、当初想定していた単一の 32 要素フラット配列ではなかった。また実測では ASCII 文字列の null 終端（`0`）の直後にも非ゼロの値（`97`）が含まれており、終端後のバイトが必ずしも全てゼロにはならない点が判明。複数ドライバー（マルチスティント/エンデュランス）時に配列がどう埋まるかは未確認。
- **[解決]** GET 系エンドポイントの網羅的な疎通確認・フィールド単位の詳細化: パス引数必須・副作用ありと判断したものを除く GET 系 65 パスすべてを実機で叩き、[「GET エンドポイント全数実測結果」](#get-エンドポイント全数実測結果)にフィールド単位の意味・型・実測値を記載した。ただし `/rest/options/settings`（約 220 キー）・`/rest/sessions/?`（約 60 キー）はプレフィックス／代表例からのカテゴリ推測に留め、全キーの個別列挙はしていない（情報量過多のため）。パス引数必須の 7 パス（`/rest/materialeditor/...` 各種、`/rest/race/car/{id}/image`、`/rest/race/track/{id}/trackmap`、`/rest/garage/setup/notes/(.*)`、`/webdata/.*`）と、副作用のリスクから意図的に見送った 4 パス（`/rest/multiplayer/join`、`/rest/watch/play/{id}`、`/rest/options/resetVRView`、`/rest/options/assign/changestatus`）、認証情報を含む `/rest/profile/getAuthSessionTicket` は未実測のまま。
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
