# Le Mans Ultimate REST API 調査メモ

Le Mans Ultimate（LMU）はガレージ画面・観戦画面などのゲーム内 WebUI が動作する土台として、ローカル REST API サーバーを内蔵している。本ドキュメントは、[`:core:windows-shared-memory`](../core/windows-shared-memory) が読み取る `LMU_Data` 共有メモリ（→ [`docs/lmu-windows-telemetry.md`](lmu-windows-telemetry.md)）には**存在しない情報**（天候予報、Virtual Energy 消費履歴、ピットメニューの状態等）を KoDriver に取り込めるかどうかを検討するための事前調査メモである。

> **調査時点の限界**: 本ドキュメントは TinyPedal 等のソースコード・サードパーティのリバースエンジニアリング成果物・コミュニティの一次情報を基にまとめた**未検証の調査結果**であり、KoDriver 実機での REST API 疎通確認・レスポンス実測は行っていない。実機確認は別タスクで行う。

---

## 目次

1. [共有メモリとの違い・使い分け](#共有メモリとの違い使い分け)
2. [基本情報](#基本情報)
3. [判明しているエンドポイント一覧](#判明しているエンドポイント一覧)
4. [`/rest/sessions/weather` のレスポンス構造](#restsessionsweather-のレスポンス構造)
5. [既知の注意点・落とし穴](#既知の注意点落とし穴)
6. [KoDriver への組み込みを検討する場合の論点](#kodriver-への組み込みを検討する場合の論点)
7. [未確認・追加調査が必要な点](#未確認追加調査が必要な点)
8. [参考リポジトリ・情報源](#参考リポジトリ情報源)

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
- **有効化設定**: 明示的な有効化は不要。ゲーム内 WebUI（ガレージ・観戦画面等）自体がこの REST API サーバー上で動作しており、LMU が起動していれば常時待ち受けている（TinyPedal 側では利用の ON/OFF 設定があるが、これはあくまでクライアント側の取得可否設定であり、サーバー側の有効化ではない）。
- **認証**: なし（ローカルの非暗号化 HTTP API）。
- **HTTP メソッド**: GET/POST/PUT/DELETE が混在するフル REST API。ゲーム内 UI の操作（セットアップ変更・ピットメニュー設定・リプレイ操作等）に使う書き込み系エンドポイントが大半を占める。
- **API 仕様書**: LMU 実行中に `http://localhost:6397/swagger-schema.json` で OpenAPI 2.0 形式の定義を取得できる（`swagger/index.html` に Swagger UI もある）。ある時点のビルドで **全 179 パス、うち非 GET（書き込み系）が 107** という報告がある（[snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) による）。
- **プッシュ型 API**: 存在しない。WebSocket 等は提供されておらず、すべてステートレスな HTTP ポーリングで取得する。
- **公式ドキュメント**: Studio 397 による一般公開の公式リファレンスは確認できていない。上記の `swagger-schema.json` が事実上の一次情報源。

---

## 判明しているエンドポイント一覧

TinyPedal のソースコード（`tinypedal/adapter/rf2_restapi.py`, `tinypedal/adapter/lmu_restapi.py`）、および `swagger-schema.json` を解析するサードパーティツール [snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) から判明したものを、テレメトリ・アナウンス用途での有用性を軸にカテゴリ別に整理する。**書き込み系（POST/PUT/DELETE）はゲーム内 UI 操作用が大半のため、KoDriver での利用は GET 系に限定する想定。**

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

TinyPedal の `tinypedal/process/weather.py`（`forecast_rf2()`）から読み取れる範囲では、レスポンスは `PRACTICE` / `QUALIFY` / `RACE` の各セッション種別ごとに、以下 5 つの予報ノードを持つ構造になっている。

- `START` / `NODE_25` / `NODE_50` / `NODE_75` / `FINISH`

各ノードは次のフィールドを持つ（TinyPedal 側は `currentValue` を読んでいる）。

| フィールド | 概要 |
|---|---|
| `WNV_SKY` | 空模様の種別インデックス |
| `WNV_TEMPERATURE` | 気温（℃） |
| `WNV_RAIN_CHANCE` | 降雨確率（100 分率。TinyPedal 側で 0.01 倍し 0.0〜1.0 にクランプして扱っている） |

各ノードの `start` は、セッション長に対する相対位置（0.0〜1.0）として扱われている。**API 自体が「あと何分」というタイマー値を返すわけではない**点に注意。TinyPedal は「セッション長 × start − 経過時間」をクライアント側で計算し、「あと何分で天候が変わるか」を逆算して表示している。

> 生の JSON 全体（キーの階層構造・単位・欠損時の挙動等）は未確認。実機での `swagger-schema.json` 取得、または実際にゲームを起動して `curl http://localhost:6397/rest/sessions/weather` 相当のリクエストを送るまでは確定情報とは言えない。

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

- 各エンドポイントの正確な JSON レスポンス構造（実機で `http://localhost:6397/swagger-schema.json` を取得するか、[go-lmu-api](https://github.com/snipem/go-lmu-api) の生成コードを確認する必要がある）。
- `/rest/sessions/weather` の全体構造（セッション種別のキー名、ノード配列かオブジェクトか等）・単位の確定。
- TinyPedal の REST API 呼び出し実装本体（リクエスト間隔、タイムアウト、エラー時のフォールバック処理）の詳細。
- CrewChief の実装（C#）における書き込み系エンドポイントの具体的なリクエストボディ形式。
- rFactor2（非 LMU）と LMU で REST API 仕様がどこまで共通か（TinyPedal 側で `rf2_restapi.py` と `lmu_restapi.py` に分離されていることから差異があることは分かっているが、詳細な差分は未整理）。

---

## 参考リポジトリ・情報源

| リソース | 概要 |
|---|---|
| [TinyPedal/TinyPedal `adapter/rf2_restapi.py`](https://github.com/TinyPedal/TinyPedal/blob/master/tinypedal/adapter/rf2_restapi.py) | rF2/LMU 共通の REST API 呼び出し定義 |
| [TinyPedal/TinyPedal `adapter/lmu_restapi.py`](https://github.com/TinyPedal/TinyPedal/blob/master/tinypedal/adapter/lmu_restapi.py) | LMU 固有の REST API 拡張 |
| [TinyPedal/TinyPedal `process/weather.py`](https://github.com/TinyPedal/TinyPedal/blob/master/tinypedal/process/weather.py) | 天候予報レスポンスの解析・残り時間の逆算ロジック |
| [snipem/go-lmu-api](https://github.com/snipem/go-lmu-api) | `swagger-schema.json` から Go 構造体を推論生成するサードパーティツール。全エンドポイント一覧の把握に利用 |
| [Alexander-Gro/race-engineer `docs/03-LMU-INTEGRATION.md`](https://github.com/Alexander-Gro/race-engineer/blob/main/docs/03-LMU-INTEGRATION.md) | 共有メモリと REST API の使い分け・落とし穴に関する実運用知見 |
| [thecrewchief.org フォーラム](https://thecrewchief.org/archive/index.php/t-38.html) | CrewChief における LMU REST API 利用に関する言及 |
| [news.racecontrol.gg の関連記事](https://news.racecontrol.gg/general-tips/le-mans-ultimate-working-with-crew-chief/) | CrewChief × LMU 連携の解説記事 |
| `docs/tinypedal-guide.md` | TinyPedal 側の REST API 設定（`Enable RestAPI Access` 等）に関する KoDriver 内の既存ドキュメント |
