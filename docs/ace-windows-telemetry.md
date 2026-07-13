# Assetto Corsa EVO 共有メモリ テレメトリ リファレンス

Assetto Corsa EVO（ACE、Kunos Simulazioni）は、初代 Assetto Corsa / Assetto Corsa Competizione と同様に **Windows の名前付き共有メモリ（メモリマップドファイル）** 経由でテレメトリを外部公開する。UDP 送信は行わないため、テレメトリ取得はゲームと同一 Windows マシン上のプロセスからに限られる（初代 AC の `Local\acpmf_*` と同じ方式）。

本ドキュメントは、公式 Steam ガイド（#3707421508）を正典とする 2 つの OSS 実装 — [albertowd/live-telemetry-evo](https://github.com/albertowd/live-telemetry-evo) の `docs/SHARED_MEMORY.md`（実測検証付き）と [dSyncro/acevo-shared-memory](https://github.com/dSyncro/acevo-shared-memory) の `wrapper.hpp`（公式ヘッダの C++ 転記、`static_assert` によるサイズ検証付き）— を突き合わせて作成した。

> **注意（早期アクセス）**: ACE は本ドキュメント作成時点（2026年7月）で早期アクセス中であり、共有メモリのレイアウト・フィールド・列挙値は更新で変更される可能性がある。物理ブロックの先頭部（0〜415バイト）は初代 AC の `SPageFilePhysics` と互換だが、それ以降の領域や Graphics ブロックは ACE 独自。Static ブロックの `sm_version` でフォーマットバージョンを検証すること。

---

## 共有メモリブロック一覧

ACE は `Local\` 名前空間に3つの名前付きファイルマッピングを公開する。

| ブロック名 | 構造体名 | 実データサイズ | 更新頻度 | 用途 |
|---|---|---|---|---|
| `Local\acevo_pmf_physics` | `SPageFilePhysics` | 800 bytes | 物理ステップ毎（約 333 Hz） | 高頻度に変化する車両ダイナミクス |
| `Local\acevo_pmf_graphics` | `SPageFileGraphicEvo` | 約 6〜8 KB（60台分の座標・ID テーブルを内包） | 描画フレーム毎（HUD レート） | HUD / UI / セッション・順位情報 |
| `Local\acevo_pmf_static` | `SPageFileStaticEvo` | 約 210 bytes | セッションロード時に1回 | トラック・セッションのメタデータ |

読み取り側は実データより大きめの領域（例: 4096 / 8192 / 2048 bytes）をマップしてよい。Windows では書き込みサイズを超えた領域はゼロ埋めで読めるため無害で、正確なバイトサイズを事前に知らなくて済む。

- Physics はシミュレーションステップ毎に更新され、最も遅延の少ない生の物理値を持つ。
- Graphics は描画フレーム毎（HUD 表示レート）に更新され、タイヤ・ダメージ・エレクトロニクス・タイミング・セッション状態のサブ構造体を内包する。
- Static はセッション開始時に1度だけ書き込まれ、走行中は変化しない。

### 共通の規約

- **リトルエンディアン**（Windows x86-64）。
- **`_pack_=4`**: `int32` / `float` は 4 バイト境界に整列するが、`char[33]` や 1 バイトの `bool` の後には暗黙のパディングが入る。Graphics / Static ブロックのオフセットはこのパディングの影響を受ける。
- **ホイール順**: 配列はすべて `[FL, FR, RL, RR]`。ただし Graphics のタイヤ構造体のフィールド名は `tyre_lf / tyre_rf / tyre_lr / tyre_rr`（`lf` 表記）である点に注意。
- **文字列**: 大半は `char[33]` の NULL 埋め ASCII。Static のバージョン文字列のみ `char[15]`。
- **bool 表現**: Graphics / Static は 1 バイトの `bool`、Physics は 4 バイトの `int32` フラグを使う。
- **同期なし**: 書き込み側との同期機構はないため、読み取り側は torn read を許容すること。順序が重要な場合は `packetId` の連続性を確認する。

---

## Physics ブロック（`Local\acevo_pmf_physics`）

合計 800 bytes。先頭 416 bytes は初代 AC の `SPageFilePhysics` と互換で、残り 384 bytes が ACE 独自の追加。このブロックには `char[N]` / `bool` が無いためオフセットは規則的。

### 初代 AC 互換プレフィックス（0〜415 bytes）

| オフセット | 型 | フィールド | 単位・備考 |
|---|---|---|---|
| 0 | int32 | `packetId` | シーケンス番号（torn read 検出にも使う） |
| 4 | float | `gas` | 0–1（スロットル） |
| 8 | float | `brake` | 0–1 |
| 12 | float | `fuel` | L（非推奨ミラー。Graphics の `fuel_liter_current_quantity` が現行ソース） |
| 16 | int32 | `gear` | 0=R, 1=N, 2以上=前進ギア（表示ギア = gear−1） |
| 20 | int32 | `rpms` | RPM |
| 24 | float | `steerAngle` | 符号付き（負=左）。live-telemetry-evo は rad、acevo-shared-memory は -1〜1 の正規化値と記述しており解釈が分かれる |
| 28 | float | `speedKmh` | km/h |
| 32 | float[3] | `velocity` | m/s（ワールド XYZ） |
| 44 | float[3] | `accG` | G。live-telemetry-evo の検証では `[0]=横, [1]=上下, [2]=前後`（acevo-shared-memory のコメントは `[横, 前後, 上下]` で不一致。実測では前者） |
| 56 | float[4] | `wheelSlip` | 各輪の複合スリップ |
| 72 | float[4] | `wheelLoad` | N |
| 88 | float[4] | `wheelsPressure` | psi |
| 104 | float[4] | `wheelAngularSpeed` | rad/s |
| 120 | float[4] | `tyreWear` | **死んでいるフィールド**。仕様上は 0=新品〜1=完全摩耗だが、現行ビルドは常に 0.0 を書き込む（下記注意事項参照） |
| 136 | float[4] | `tyreDirtyLevel` | 0–4（概算。コース外走行での汚れ） |
| 152 | float[4] | `tyreCoreTemperature` | ℃（カーカス中心温度） |
| 168 | float[4] | `camberRAD` | rad。**符号は各輪ローカル**: セットアップ画面の「負=トップイン」は左側車輪でのみ一致し、右側車輪は逆符号で報告される。左右を統一比較する場合は右側（[1], [3]）を符号反転する |
| 184 | float[4] | `suspensionTravel` | m。**符号規約は車種依存**: 多くの車はフル伸長からの正値だが、アクティブサス搭載車は静止基準からの符号付き変位（静止時約-0.03）。`abs()` を取ってから使うと両規約に対応できる |
| 200 | float | `drs` | 0–1（展開状態） |
| 204 | float | `tc` | 0–1（**設定強度**であり介入中フラグではない。介入は `tcInAction`） |
| 208 | float | `heading` | rad（ヨー） |
| 212 | float | `pitch` | rad |
| 216 | float | `roll` | rad |
| 220 | float | `cgHeight` | m（重心高） |
| 224 | float[5] | `carDamage` | `[前, 後, 左, 右, 中央]`。**0–1 ではない**: 現行ビルドは絶対値を書き込み、1.0 を大きく超える（検証例: 前面+中央クラッシュで front=198.64, centre=237.11）。単位未較正のため UI 側で独自閾値によりクランプ・正規化すること |
| 244 | int32 | `numberOfTyresOut` | 0–4（コース外のタイヤ数） |
| 248 | int32 | `pitLimiterOn` | 0/1 |
| 252 | float | `abs` | 0–1（設定強度。介入は `absInAction`） |
| 256 | float | `kersCharge` | 0–1（充電状態） |
| 260 | float | `kersInput` | 0–1（ドライバー要求） |
| 264 | int32 | `autoShifterOn` | 0/1 |
| 268 | float[2] | `rideHeight` | `[前, 後]`。**単位は車種依存**（m または mm）。`|h| ≥ 1.0` なら mm とみなす自動判定が実用的 |
| 276 | float | `turboBoost` | bar（相対） |
| 280 | float | `ballast` | kg |
| 284 | float | `airDensity` | kg/m³ |
| 288 | float | `airTemp` | ℃（外気温） |
| 292 | float | `roadTemp` | ℃（路面温度） |
| 296 | float[3] | `localAngularVel` | rad/s（車体座標系） |
| 308 | float | `finalFF` | フォースフィードバック出力（符号付き。live-telemetry-evo は -1〜1、acevo-shared-memory は Nm と記述） |
| 312 | float | `performanceMeter` | s（基準ラップとのデルタ、正=遅い） |
| 316 | int32 | `engineBrake` | 設定インデックス |
| 320 | int32 | `ersRecoveryLevel` | 0以上 |
| 324 | int32 | `ersPowerLevel` | 0以上 |
| 328 | int32 | `ersHeatCharging` | ヒートチャージモード |
| 332 | int32 | `ersIsCharging` | 0/1 |
| 336 | float | `kersCurrentKJ` | kJ |
| 340 | int32 | `drsAvailable` | 0/1（現在位置で DRS 使用可） |
| 344 | int32 | `drsEnabled` | 0/1（ドライバーが DRS 作動中） |
| 348 | float[4] | `brakeTemp` | ℃（各輪ディスク温度） |
| 364 | float | `clutch` | 0–1 |
| 368 | float[4] | `tyreTempI` | ℃（接地面内側） |
| 384 | float[4] | `tyreTempM` | ℃（接地面中央） |
| 400 | float[4] | `tyreTempO` | ℃（接地面外側） |

### ACE 拡張領域（416〜799 bytes）

初代 AC には無い ACE 独自フィールド。

| オフセット | 型 | フィールド | 単位・備考 |
|---|---|---|---|
| 416 | int32 | `isAIControlled` | 0/1 |
| 420 | float[4][3] | `tyreContactPoint` | ワールド XYZ（m、各輪接地点） |
| 468 | float[4][3] | `tyreContactNormal` | 単位ベクトル（接地点の路面法線） |
| 516 | float[4][3] | `tyreContactHeading` | 単位ベクトル（接地点のタイヤ進行方向） |
| 564 | float | `brakeBias` | 0–1（フロント側割合。0.56=56%） |
| 568 | float[3] | `localVelocity` | m/s（車体座標系） |
| 580 | int32 | `P2PActivations` | Push-to-Pass 残り回数 |
| 584 | int32 | `P2PStatus` | 0=非作動, 1=作動（acevo-shared-memory の定義） |
| 588 | int32 | `currentMaxRpm` | RPM。**AC1 の静的 `maxRpm` の後継**（ACE の Static からは削除された）。ティック毎に変動しうる |
| 592 | float[4] | `mz` | N·m（セルフアライニングトルク） |
| 608 | float[4] | `fx` | N（縦方向タイヤ力） |
| 624 | float[4] | `fy` | N（横方向タイヤ力） |
| 640 | float[4] | `slipRatio` | 無次元（縦スリップ） |
| 656 | float[4] | `slipAngle` | rad（横スリップ角） |
| 672 | int32 | `tcInAction` | 0/1（**現在 TC がパワーカット中**。UI の点滅表示にはこちらを使う） |
| 676 | int32 | `absInAction` | 0/1（現在 ABS が介入中） |
| 680 | float[4] | `suspensionDamage` | 0–1（各輪） |
| 696 | float[4] | `tyreTemp` | ℃（代表表面温度。`tyreTempI/M/O` とはサンプリングが異なる） |
| 712 | float | `waterTemp` | ℃（冷却水） |
| 716 | float[4] | `brakeTorque` | N·m（各輪） |
| 732 | int32 | `frontBrakeCompound` | ブレーキパッドコンパウンドID（車種依存） |
| 736 | int32 | `rearBrakeCompound` | 〃 |
| 740 | float[4] | `padLife` | **1=新品から減少**（AC1 SDK と同じ意味論）。HUD のパッド残量表示は本値の ×1000 に一致（例: 0.029 → HUD "29.00"）。絶対スケールは未較正のため相対的な摩耗指標として扱う |
| 756 | float[4] | `discLife` | `padLife` と同じ意味論。フロントはリアの約2倍の速さで減る（通常のブレーキ配分時） |
| 772 | int32 | `ignitionOn` | 0/1 |
| 776 | int32 | `starterEngineOn` | 0/1 |
| 780 | int32 | `isEngineRunning` | 0/1 |
| 784 | float | `kerbVibration` | 0–1（FFB エフェクト: 縁石） |
| 788 | float | `slipVibrations` | 0–1（FFB: タイヤスリップ） |
| 792 | float | `roadVibrations` | 0–1（FFB: 路面テクスチャ） |
| 796 | float | `absVibrations` | 0–1（FFB: ABS パルス） |

---

## Graphics ブロック（`Local\acevo_pmf_graphics`）

描画フレーム毎に更新される。以下は `SPageFileGraphicEvo` の**宣言順**のフィールド一覧（`char[33]` / `bool` によるアライメントパディングが入るため固定オフセットは記載しない。オフセットが必要な場合は ctypes / C++ の `offsetof` で実行時に算出すること）。

### ヘッダー・フォーカス

| 型 | フィールド | 備考 |
|---|---|---|
| int32 | `packetId` | シーケンス番号 |
| int32 | `status` | enum（`ACEVO_STATUS`） |
| uint64 | `focused_car_id_a` | 注視車両 128bit GUID（下位） |
| uint64 | `focused_car_id_b` | 〃（上位） |
| uint64 | `player_car_id_a` | 自車 GUID（下位） |
| uint64 | `player_car_id_b` | 〃（上位） |

### エンジン・シフト状態

| 型 | フィールド | 備考 |
|---|---|---|
| uint16 | `rpm` | HUD レート RPM |
| bool | `is_rpm_limiter_on` | レブリミッター作動中 |
| bool | `is_change_up_rpm` | シフトアップ推奨 |
| bool | `is_change_down_rpm` | シフトダウン推奨 |
| bool | `tc_active` | TC 作動中（HUD レート。精密なタイミングは physics の `tcInAction`） |
| bool | `abs_active` | ABS 作動中 |
| bool | `esc_active` | スタビリティコントロール作動中 |
| bool | `launch_active` | ローンチコントロール作動中 |
| bool | `is_ignition_on` | イグニッション ON |
| bool | `is_engine_running` | エンジン稼働中 |
| bool | `kers_is_charging` | KERS 回生中 |
| bool | `is_wrong_way` | 逆走中 |
| bool | `is_drs_available` | DRS ゾーン内 |
| bool | `battery_is_charging` | ハイブリッドバッテリー充電中 |
| bool | `is_max_kj_per_lap_reached` | ラップあたり最大放出エネルギー到達 |
| bool | `is_max_charge_kj_per_lap_reached` | ラップあたり最大充電到達 |

### 速度・ペダル・入力

| 型 | フィールド | 単位 |
|---|---|---|
| int16 | `display_speed_kmh` | km/h（丸め済み） |
| int16 | `display_speed_mph` | mph |
| int16 | `display_speed_ms` | m/s |
| float | `pitspeeding_delta` | km/h（ピット制限超過分、負=制限内） |
| int16 | `gear_int` | ギア（physics の `gear` と同じエンコード） |
| float | `rpm_percent` | 0–1（レッドライン比） |
| float | `gas_percent` | 0–1 |
| float | `brake_percent` | 0–1 |
| float | `handbrake_percent` | 0–1 |
| float | `clutch_percent` | 0–1 |
| float | `steering_percent` | -1〜1 |
| float | `ffb_strength` | 0–1（1.0=クリッピング） |
| float | `car_ffb_multiplier` | 車種別 FFB スケール |

### エンジン計器値

| 型 | フィールド | 単位 |
|---|---|---|
| float | `water_temperature_percent` | 0–1（レッドライン比） |
| float | `water_pressure_bar` | bar |
| float | `fuel_pressure_bar` | bar |
| int8 | `water_temperature_c` | ℃（int8 なのでキャストに注意） |
| int8 | `air_temperature_c` | ℃（int8） |
| float | `oil_temperature_c` | ℃ |
| float | `oil_pressure_bar` | bar |
| float | `exhaust_temperature_c` | ℃ |
| float | `g_forces_x` | G（横） |
| float | `g_forces_y` | G（上下または前後。physics の `accG` と同じ並びだがソース間で解釈が分かれる） |
| float | `g_forces_z` | G（〃） |
| float | `turbo_boost` | bar |
| float | `turbo_boost_level` | ブーストマップインデックス（小数） |
| float | `turbo_boost_perc` | 0–1（`max_turbo_boost` 比） |
| int32 | `steer_degrees` | 度（符号付き、ホイール総回転角） |

### 距離・時刻・ラップタイム

| 型 | フィールド | 単位 |
|---|---|---|
| float | `current_km` | km（現ラップ走行距離） |
| uint32 | `total_km` | km（総走行距離） |
| uint32 | `total_driving_time_s` | s |
| int32 | `time_of_day_hours` | 0–23 |
| int32 | `time_of_day_minutes` | 0–59 |
| int32 | `time_of_day_seconds` | 0–59 |
| int32 | `delta_time_ms` | ms（基準ラップ比、正=遅い） |
| int32 | `current_lap_time_ms` | ms |
| int32 | `predicted_lap_time_ms` | ms（現セクターに基づく予測） |

### 燃料（第1グループ）

| 型 | フィールド | 単位 |
|---|---|---|
| float | `fuel_liter_current_quantity` | L |
| float | `fuel_liter_current_quantity_percent` | 0–1（`max_fuel` 比） |
| float | `fuel_liter_per_km` | L/km（平滑化済み消費率） |
| float | `km_per_fuel_liter` | km/L（逆数） |

### エンジン出力

| 型 | フィールド | 単位 |
|---|---|---|
| float | `current_torque` | N·m（過給込み、現在 RPM 時点） |
| int32 | `current_bhp` | BHP（過給込み） |

### タイヤ状態（`SMEvoTyreState` × 4、各 256 bytes）

`tyre_lf` / `tyre_rf` / `tyre_lr` / `tyre_rr` の順で4輪分が埋め込まれる（**`lf` 表記**。physics 配列の FL/FR/RL/RR と同じ車輪の別名）。フィールドは「[SMEvoTyreState](#smevotyrestate256-bytes)」を参照。

### 位置・KERS・制御状態

| 型 | フィールド | 単位 |
|---|---|---|
| float | `npos` | 0–1（正規化ラップ位置） |
| float | `kers_charge_perc` | 0–1 |
| float | `kers_current_perc` | 0–1 |
| float | `control_lock_time` | s（スピン・接触後の入力ロック残り） |

### ダメージ・位置・ピット

| 型 | フィールド | 備考 |
|---|---|---|
| SMEvoDamageState (128 B) | `car_damage` | ゾーン別+サスペンション別ダメージ（下記参照） |
| int32 | `car_location` | enum（`ACEVO_CAR_LOCATION`） |
| SMEvoPitInfo (64 B) | `pit_info` | ピットストップ作業状態（下記参照） |

### 燃料（第2グループ）・バッテリー

| 型 | フィールド | 単位 |
|---|---|---|
| float | `fuel_liter_used` | L（現スティント消費量） |
| float | `fuel_liter_per_lap` | L/lap（直近平均） |
| float | `laps_possible_with_fuel` | 残燃料で走行可能な周回数（現在の部分ラップ込み） |
| float | `battery_temperature` | ℃ |
| float | `battery_voltage` | V |
| float | `instantaneous_fuel_liter_per_km` | L/km（瞬間値） |
| float | `instantaneous_km_per_fuel_liter` | km/L（瞬間値） |
| float | `gear_rpm_window` | 現在ギアの最適 RPM 帯適合度 |

### 計装・エレクトロニクス（`SMEvoInstrumentation` / `SMEvoElectronics`、各 128 bytes）

| フィールド | 備考 |
|---|---|
| `instrumentation` | ライブ計装値（下記 SMEvoInstrumentation 参照） |
| `instrumentation_min_limit` | チャネル別下限 |
| `instrumentation_max_limit` | チャネル別上限 |
| `electronics` | ドライバー設定値（下記 SMEvoElectronics 参照） |
| `electronics_min_limit` | チャネル別下限 |
| `electronics_max_limit` | チャネル別上限 |
| `electronics_is_modifiable` | チャネル別の走行中変更可否フラグ |

### 順位・ラップ

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `total_lap_count` | セッション総ラップ数 |
| uint32 | `current_pos` | 順位（1ベース） |
| uint32 | `total_drivers` | 参加台数 |
| int32 | `last_laptime_ms` | ms（未完了時は -1/0） |
| int32 | `best_laptime_ms` | ms |
| int32 | `flag` | enum（`ACEVO_FLAG_TYPE`、自車提示） |
| int32 | `global_flag` | enum（`ACEVO_FLAG_TYPE`、セッション全体） |

### 車両スペック・エンジン種別

| 型 | フィールド | 備考 |
|---|---|---|
| uint32 | `max_gears` | 前進ギア数（R・N は含まない） |
| int32 | `engine_type` | enum（`ACEVO_ENGINE_TYPE`） |
| bool | `has_kers` | KERS 搭載 |
| bool | `is_last_lap` | 最終ラップ |
| char[33] | `performance_mode_name` | プリセット名（"WET", "QUAL" 等。非搭載車は空） |
| float | `diff_coast_raw_value` | デフ（コースト）生値（車種依存単位） |
| float | `diff_power_raw_value` | デフ（パワー）生値 |

### レースカット・トラックリミット

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `race_cut_gained_time_ms` | ms（コースカットで得た時間） |
| int32 | `distance_to_deadline` | m（返還期限までの距離） |
| float | `race_cut_current_delta` | クリーン走行基準とのデルタ |

### セッション・タイミング（`SMEvoSessionState` / `SMEvoTimingState`、各 256 bytes）

| フィールド | 備考 |
|---|---|
| `session_state` | セッションライフサイクル情報（下記 SMEvoSessionState 参照） |
| `timing_state` | HUD ラップタイム・デルタ表示（下記 SMEvoTimingState 参照） |

### ネットワーク・パフォーマンス

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `player_ping` | ms（オンラインのみ） |
| int32 | `player_latency` | ms |
| int32 | `player_cpu_usage` | 0–100 |
| int32 | `player_cpu_usage_avg` | 移動平均 |
| int32 | `player_qos` | 0以上 |
| int32 | `player_qos_avg` | — |
| int32 | `player_fps` | — |
| int32 | `player_fps_avg` | — |

### プレイヤー・車両識別

| 型 | フィールド | 備考 |
|---|---|---|
| char[33] | `driver_name` | ASCII（名） |
| char[33] | `driver_surname` | ASCII（姓） |
| char[33] | `car_model` | 内部ID |
| bool | `is_in_pit_box` | ガレージ内 |
| bool | `is_in_pit_lane` | ピットレーン内 |
| bool | `is_valid_lap` | ラップ有効（カットなし） |

### マルチカー座標・ギャップ

| 型 | フィールド | 単位 |
|---|---|---|
| float[60][3] | `car_coordinates` | ワールド XYZ（最大60台、720 B。`active_cars` 超は0埋め） |
| float | `gap_ahead` | s（前車とのギャップ、符号付き） |
| float | `gap_behind` | s（後車とのギャップ） |
| uint8 | `active_cars` | `car_coordinates` の有効エントリ数 |

### 燃料（第3グループ）・アシスト・末尾

| 型 | フィールド | 備考 |
|---|---|---|
| float | `fuel_per_lap` | L/lap（セッション平均） |
| float | `fuel_estimated_laps` | 推定残周回数 |
| SMEvoAssistsState (64 B) | `assists_state` | ドライバーアシスト設定（下記参照） |
| float | `max_fuel` | L（最大容量。AC1 の静的 `maxFuel` の後継） |
| float | `max_turbo_boost` | bar（NA 車は 0。AC1 の静的 `maxTurboBoost` の後継） |
| bool | `use_single_compound` | 単一コンパウンドルール適用中 |
| uint64[60][2] | `car_ids` | `car_coordinates` に対応する車両 128bit GUID テーブル（960 B） |

---

## Graphics 内サブ構造体

acevo-shared-memory（公式ヘッダ転記）による定義。各構造体は将来拡張用の予約領域（`place_holder`）を末尾に持ち、固定サイズが保証されている。

### SMEvoTyreState（256 bytes）

| 型 | フィールド | 単位・備考 |
|---|---|---|
| float | `slip` | 複合スリップ |
| bool | `lock` | ロック中（ゲーム提供の判定） |
| float | `tyre_pressure` | psi |
| float | `tyre_temperature_c` | ℃（カーカス平均） |
| float | `brake_temperature_c` | ℃ |
| float | `brake_pressure` | bar（このコーナーの油圧） |
| float | `tyre_temperature_left` | ℃（接地面の左フェイス） |
| float | `tyre_temperature_center` | ℃（中央） |
| float | `tyre_temperature_right` | ℃（右フェイス） |
| char[33] | `tyre_compound_front` | フロントコンパウンド名（4輪すべてに同値が複製される） |
| char[33] | `tyre_compound_rear` | リアコンパウンド名（〃） |
| float | `tyre_normalized_pressure` | 1.0=最適。**上限 2.000 でクランプ**、低圧側は 1.0 未満の小数 |
| float | `tyre_normalized_temperature_left` | 1.0=最適 |
| float | `tyre_normalized_temperature_center` | 1.0=最適 |
| float | `tyre_normalized_temperature_right` | 1.0=最適 |
| float | `brake_normalized_temperature` | 1.0=最適 |
| float | `tyre_normalized_temperature_core` | 1.0=最適 |
| byte[128] | （予約） | 将来拡張用 |

`left/right` は**車外から見た接地面フェイス**: 左側車輪では left=外側・right=内側、右側車輪では鏡像になる。ゲーム内 HUD の「OMI」3温度表示はこの `tyre_temperature_left/center/right` の生値（℃）と一致することが実測で確認されている。

### SMEvoDamageState（128 bytes）

| 型 | フィールド | 備考 |
|---|---|---|
| float | `damage_front` | 前部ダメージ |
| float | `damage_rear` | 後部 |
| float | `damage_left` | 左側面 |
| float | `damage_right` | 右側面 |
| float | `damage_center` | 中央・床下 |
| float | `damage_suspension_lf` | FL サスペンション |
| float | `damage_suspension_rf` | FR サスペンション |
| float | `damage_suspension_lr` | RL サスペンション |
| float | `damage_suspension_rr` | RR サスペンション |
| byte[92] | （予約） | — |

### SMEvoPitInfo（64 bytes）

各作業の状態は int8 で、**-1=実施しない, 0=完了, 1=実施中**。

| 型 | フィールド | 備考 |
|---|---|---|
| int8 | `damage` | 車体修理 |
| int8 | `fuel` | 給油 |
| int8 | `tyres_lf` | FL タイヤ交換 |
| int8 | `tyres_rf` | FR タイヤ交換 |
| int8 | `tyres_lr` | RL タイヤ交換 |
| int8 | `tyres_rr` | RR タイヤ交換 |
| byte[58] | （予約） | — |

### SMEvoElectronics（128 bytes）

`electronics` / `electronics_min_limit` / `electronics_max_limit` / `electronics_is_modifiable` の4か所で同じレイアウトが使われる。

| 型 | フィールド | 備考 |
|---|---|---|
| int8 | `tc_level` | TC レベル（0=オフ） |
| int8 | `tc_cut_level` | TC スロットルカット強度 |
| int8 | `abs_level` | ABS レベル（0=オフ） |
| int8 | `esc_level` | スタビリティコントロールレベル |
| int8 | `ebb_level` | 電子ブレーキバランスレベル |
| float | `brake_bias` | フロント側割合（0.56=56%） |
| int8 | `engine_map_level` | エンジンマップ |
| float | `turbo_level` | ターボブースト目標 |
| int8 | `ers_deployment_map` | ERS 放出マップ |
| float | `ers_recharge_map` | ERS 回生設定 |
| bool | `is_ers_heat_charging_on` | ERS ヒートチャージ有効 |
| bool | `is_ers_overtake_mode_on` | ERS オーバーテイクモード |
| bool | `is_drs_open` | DRS 開 |
| int8 | `diff_power_level` | デフロック（パワー側） |
| int8 | `diff_coast_level` | デフロック（コースト側） |
| int8 | `front_bump_damper_level` | フロントバンプダンパー |
| int8 | `front_rebound_damper_level` | フロントリバウンドダンパー |
| int8 | `rear_bump_damper_level` | リアバンプダンパー |
| int8 | `rear_rebound_damper_level` | リアリバウンドダンパー |
| bool | `is_ignition_on` | イグニッション |
| bool | `is_pitlimiter_on` | ピットリミッター |
| int8 | `active_performance_mode` | パフォーマンスモードインデックス |
| byte[88] | （予約） | — |

### SMEvoInstrumentation（128 bytes）

| 型 | フィールド | 備考 |
|---|---|---|
| int8 | `main_light_stage` | メインライト段階（0=オフ） |
| int8 | `special_light_stage` | 補助ライト |
| int8 | `cockpit_light_stage` | コックピット照明 |
| int8 | `wiper_level` | ワイパー速度（0=オフ） |
| bool | `rain_lights` | レインライト |
| bool | `direction_light_left` | 左ウインカー |
| bool | `direction_light_right` | 右ウインカー |
| bool | `flashing_lights` | パッシング |
| bool | `warning_lights` | ハザード |
| int8 | `selected_display_index` | フォーカス中ディスプレイ |
| int8[16] | `display_current_page_index` | ディスプレイ別ページ |
| bool | `are_headlights_visible` | ヘッドライト点灯（他車から可視） |
| byte[101] | （予約） | — |

### SMEvoSessionState（256 bytes）

| 型 | フィールド | 備考 |
|---|---|---|
| char[33] | `phase_name` | セッションフェーズ名（"Race", "Qualify" 等） |
| char[15] | `time_left` | 残り時間（HH:MM:SS 書式済み文字列） |
| int32 | `time_left_ms` | 残り時間（ms） |
| char[15] | `wait_time` | 開始までの待機時間（書式済み） |
| int32 | `total_lap` | セッション予定ラップ数 |
| int32 | `current_lap` | 現在ラップ番号 |
| int32 | `lights_on` | 点灯中のスタートライト数 |
| int32 | `lights_mode` | スタートライトシーケンスモード |
| float | `lap_length_km` | ラップ長（km） |
| int32 | `end_session_flag` | セッション終了時に非0 |
| char[15] | `time_to_next_session` | 次セッションまでのカウントダウン（書式済み） |
| bool | `disconnected_from_server` | サーバー切断 |
| bool | `restart_season_enabled` | シーズン再開可 |
| bool | `ui_enable_drive` | UI の Drive ボタン有効 |
| bool | `ui_enable_setup` | UI の Setup 画面有効 |
| bool | `is_ready_to_next_blinking` | 進行可能インジケータ点滅中 |
| bool | `show_waiting_for_players` | プレイヤー待機画面表示中 |
| byte[140] | （予約） | — |

### SMEvoTimingState（256 bytes）

タイム系は**書式済み文字列**で提供される点に注意（ms の数値は Graphics 直下の `current_lap_time_ms` 等を使う）。

| 型 | フィールド | 備考 |
|---|---|---|
| char[15] | `current_laptime` | 現在ラップタイム（書式済み） |
| char[15] | `delta_current` | 基準ラップとのデルタ（書式済み） |
| int32 | `delta_current_p` | デルタ符号: +1=遅い, -1=速い, 0=非表示 |
| char[15] | `last_laptime` | 前ラップタイム（書式済み） |
| char[15] | `delta_last` | 前ラップとのデルタ（書式済み） |
| int32 | `delta_last_p` | 〃の符号 |
| char[15] | `best_laptime` | ベストラップ（書式済み） |
| char[15] | `ideal_laptime` | 理論ベスト（ベストセクター合算、書式済み） |
| char[15] | `total_time` | セッション経過時間（書式済み） |
| bool | `is_invalid` | 現在ラップ無効化済み |
| byte[137] | （予約） | — |

### SMEvoAssistsState（64 bytes）

| 型 | フィールド | 備考 |
|---|---|---|
| uint8 | `auto_gear` | オートシフト（0=オフ） |
| uint8 | `auto_blip` | オートブリップ |
| uint8 | `auto_clutch` | オートクラッチ |
| uint8 | `auto_clutch_on_start` | スタート時オートクラッチ |
| uint8 | `manual_ignition_e_start` | 手動イグニッション・スターター要求 |
| uint8 | `auto_pit_limiter` | 自動ピットリミッター |
| uint8 | `standing_start_assist` | スタンディングスタート補助 |
| float | `auto_steer` | 0–1（オートステア強度） |
| float | `arcade_stability_control` | 0–1（アーケード安定化） |
| byte[48] | （予約） | — |

---

## Static ブロック（`Local\acevo_pmf_static`）

セッションロード時に1回だけ書き込まれ、走行中は変化しない。**AC1 の Static にあった車両スペック（`maxRpm`, `maxPower`, `maxTorque`, `maxTurboBoost`, `suspensionMaxTravel` 等）はすべて削除された**（代替は下記「実装上の注意」参照）。

| 型 | フィールド | 単位・備考 |
|---|---|---|
| char[15] | `sm_version` | 共有メモリ仕様バージョン（例: "1.0"） |
| char[15] | `ac_evo_version` | ゲームビルドバージョン |
| int32 | `session` | enum（`ACEVO_SESSION_TYPE`） |
| char[33] | `session_name` | UI 表示名 |
| uint8 | `event_id` | イベントインデックス |
| uint8 | `session_id` | セッションインデックス |
| int32 | `starting_grip` | enum（`ACEVO_STARTING_GRIP`） |
| float | `starting_ambient_temperature_c` | ℃ |
| float | `starting_ground_temperature_c` | ℃ |
| bool | `is_static_weather` | 天候固定 |
| bool | `is_timed_race` | タイムレース（周回数レースではない） |
| bool | `is_online` | マルチプレイヤー |
| int32 | `number_of_sessions` | セッション数 |
| char[33] | `nation` | トラック所在国（例: "GBR"） |
| float | `longitude` | 経度（十進度） |
| float | `latitude` | 緯度（十進度） |
| char[33] | `track` | トラックID（例: "silverstone"） |
| char[33] | `track_configuration` | レイアウトID（"gp" 等、空=デフォルト） |
| float | `track_length_m` | m |

---

## 列挙値（enum）

acevo-shared-memory（公式ヘッダ転記）の定義。早期アクセスのため今後追加・変更されうる。

### ACEVO_STATUS（`graphics.status`）

| 値 | 意味 |
|---|---|
| 0 | AC_OFF（未起動・セッションなし） |
| 1 | AC_REPLAY（リプレイ再生中） |
| 2 | AC_LIVE（走行中） |
| 3 | AC_PAUSE（ポーズ中） |

### ACEVO_SESSION_TYPE（`static.session`）

| 値 | 意味 |
|---|---|
| -1 | AC_UNKNOWN（未確定） |
| 0 | AC_TIME_ATTACK（タイムアタック・予選） |
| 1 | AC_RACE（レース） |
| 2 | AC_HOT_STINT（ホットスティント練習） |
| 3 | AC_CRUISE（クルーズ） |

### ACEVO_FLAG_TYPE（`graphics.flag`, `graphics.global_flag`）

| 値 | 意味 |
|---|---|
| 0 | AC_NO_FLAG（フラッグなし） |
| 1 | AC_WHITE_FLAG（前方に低速車両） |
| 2 | AC_GREEN_FLAG（クリア・レース再開） |
| 3 | AC_RED_FLAG（セッション中断） |
| 4 | AC_BLUE_FLAG（周回遅れ、先頭に道を譲る） |
| 5 | AC_YELLOW_FLAG（危険あり・追い越し禁止） |
| 6 | AC_BLACK_FLAG（失格・即時ピットイン） |
| 7 | AC_BLACK_WHITE_FLAG(非スポーツマン行為警告) |
| 8 | AC_CHECKERED_FLAG（セッション終了） |
| 9 | AC_ORANGE_CIRCLE_FLAG（機械的トラブル・要ピットイン） |
| 10 | AC_RED_YELLOW_STRIPES_FLAG（前方路面スリッピー） |

### ACEVO_CAR_LOCATION（`graphics.car_location`）

| 値 | 意味 |
|---|---|
| 0 | ACEVO_UNASSIGNED（未確定） |
| 1 | ACEVO_PITLANE（ピットレーン内） |
| 2 | ACEVO_PITENTRY（ピット入口） |
| 3 | ACEVO_PITEXIT（ピット出口） |
| 4 | ACEVO_TRACK（コース上） |

### ACEVO_ENGINE_TYPE（`graphics.engine_type`）

| 値 | 意味 |
|---|---|
| 0 | ACEVO_INTERNAL_COMBUSTION（内燃機関） |
| 1 | ACEVO_ELECTRIC_MOTOR（電気モーター） |

### ACEVO_STARTING_GRIP（`static.starting_grip`）

| 値 | 意味 |
|---|---|
| 0 | ACEVO_GREEN（グリップ最低） |
| 1 | ACEVO_FAST（ファスト） |
| 2 | ACEVO_OPTIMUM（最適グリップ） |

---

## 実装上の注意

- **Windows 専用**: 共有メモリは Windows の `OpenFileMappingA` / `MapViewOfFile` で `Local\acevo_pmf_*` を開く。ゲームと同一マシン上でのみ読み取れる。KoDriver の LMU 共有メモリ読み取り（`:core:lmu-windows-data`）と同じ制約が当てはまる。
- **マッピング存在確認の罠**: `CreateFileMapping` 系 API（Python の `mmap.mmap(-1, size, tagname=...)` を含む）は、名前が存在しないと**新規作成してしまい**、ゼロ埋めの空マッピングを読み続けることになる。「ゲームが起動済みか」の判定には `OpenFileMapping`（存在しなければ NULL / `ERROR_FILE_NOT_FOUND`）を使うこと。
- **torn read**: 書き込み側と同期しないため、フィールド単位でなくブロック全体をスナップショットしてから読む。順序が重要な用途では `packetId` の連続性を確認する。
- **AC1 互換プレフィックス**: Physics ブロック先頭 0〜415 バイトは初代 AC の `SPageFilePhysics` と一致するため、既存の AC 用ライブラリで基本的な走行データ（速度・RPM・ギア・入力・タイヤ温度など）を取得できる。
- **タイヤ温度の単位は ℃**（LMU の Kelvin と異なる）。
- **タイヤ摩耗は取得不可**: `tyreWear`（physics 120）は常に 0.0。live-telemetry-evo の3回の独立検証（通常走行 A/B 比較、極端な左右非対称セットアップ、HUD 値ターゲットスキャン）でも、physics / graphics のどこにも摩耗値は書き込まれていないことが確認された。HUD の摩耗表示はゲーム内部計算で共有メモリには出力されない。摩耗系の指標が必要なら `padLife` / `discLife` の流用か、`slipRatio × wheelLoad × dt` の積算で代替する。
- **車両スペックの取得先**: AC1 の Static にあった値は移動した。`maxRpm` → `physics.currentMaxRpm`（ティック毎）、`maxFuel` → `graphics.max_fuel`、`maxTurboBoost` → `graphics.max_turbo_boost`、`maxPower`/`maxTorque` → `graphics.current_bhp`/`current_torque` のローリング最大で観測。`suspensionMaxTravel` は `physics.suspensionTravel` のローリング最大で較正する。
- **ギア表現**: physics の `gear` も graphics の `gear_int` も `0=R, 1=N, 2以上=前進`（同一エンコード）。表示ギア = 値−1。
- **早期アクセスによる変動**: フィールドの追加・オフセット変更がありうる。取得側は `sm_version` / `ac_evo_version`（Static）でバージョンを検証し、未知バージョンでは慎重に扱う。追加フィールドは各構造体末尾の予約領域（`place_holder`）を使って行われる見込みのため、既知フィールドのオフセットは比較的安定と期待できる。
- **同一内容の連続値**: LMU の WebSocket 配信同様、外部配信する場合は前回と同一の値を送らない差分配信を検討する（KoDriver の Ktor サーバー実装方針に準拠）。

---

## 参考情報源

| 情報源 | 種別 | 概要 |
|---|---|---|
| [Assetto Corsa EVO — Shared Memory Documentation（Steam ガイド #3707421508）](https://steamcommunity.com/sharedfiles/filedetails/?id=3707421508) | 公式 | Kunos による一次情報。構造体・列挙・整数値の正典 |
| [albertowd/live-telemetry-evo](https://github.com/albertowd/live-telemetry-evo)（[docs/SHARED_MEMORY.md](https://github.com/albertowd/live-telemetry-evo/blob/develop/docs/SHARED_MEMORY.md)） | OSS | オーバーレイ実装。Physics の全オフセットと実測検証（tyreWear 死亡、carDamage 絶対値、camber 符号など）が豊富 |
| [dSyncro/acevo-shared-memory](https://github.com/dSyncro/acevo-shared-memory)（[crate](https://lib.rs/crates/acevo-shared-memory)） | OSS (Rust) | 公式ヘッダの C++ 転記（`src/bindings/source/wrapper.hpp`）。全サブ構造体・列挙値の定義と `static_assert` によるサイズ検証を含む |
| [Live Telemetry Evo（OverTake.gg）](https://www.overtake.gg/downloads/live-telemetry-evo.84121/) | 配布 | 上記オーバーレイの配布ページ |
