# Assetto Corsa EVO 共有メモリ テレメトリ リファレンス

Assetto Corsa EVO（ACE、Kunos Simulazioni）は、初代 Assetto Corsa / Assetto Corsa Competizione と同様に **Windows の名前付き共有メモリ（メモリマップドファイル）** 経由でテレメトリを外部公開する。UDP 送信は行わないため、テレメトリ取得はゲームと同一 Windows マシン上のプロセスからに限られる（初代 AC の `Local\acpmf_*` と同じ方式）。

> **注意（早期アクセス）**: ACE は本ドキュメント作成時点（2026年7月）で早期アクセス中であり、共有メモリのレイアウト・フィールド・列挙値は更新で変更される可能性がある。物理ブロックの先頭部（0〜415バイト）は初代 AC の `SPageFilePhysics` と互換だが、それ以降の領域や Graphics ブロックは ACE 独自であり、**一部のオフセット・列挙値の整数値はコミュニティによるリバースエンジニアリングで未確定（TBC）** の箇所がある。実装時は公式の Steam ガイドを一次情報として参照すること。

---

## 共有メモリブロック一覧

ACE は `Local\` 名前空間に3つの名前付きファイルマッピングを公開する。

| ブロック名 | 構造体名 | サイズ（目安） | 更新頻度 | 用途 |
|---|---|---|---|---|
| `Local\acevo_pmf_physics` | `SPageFilePhysics` | 約 800 bytes | 物理ステップ毎（約 333 Hz） | 高頻度に変化する車両ダイナミクス |
| `Local\acevo_pmf_graphics` | `SPageFileGraphicEvo` | 約 8192 bytes | 描画フレーム毎（HUD レート） | HUD / UI / セッション・順位情報 |
| `Local\acevo_pmf_static` | `SPageFileStaticEvo` | 約 2048 bytes | セッションロード時に1回 | トラック・セッションのメタデータ |

- Physics はシミュレーションステップ毎に更新され、最も遅延の少ない生の物理値を持つ。
- Graphics は描画フレーム毎（HUD 表示レート）に更新され、タイヤ・ダメージ・エレクトロニクス・タイミング・セッション状態のサブ構造体を内包する。
- Static はセッション開始時に1度だけ書き込まれ、走行中は変化しない。

---

## Physics ブロック（`Local\acevo_pmf_physics`）

### 初代 AC 互換プレフィックス（0〜415 bytes）

初代 Assetto Corsa の `SPageFilePhysics` と同一レイアウト。既存の AC 用ツールがそのまま利用できる領域。

| オフセット | 型 | フィールド | 単位・備考 |
|---|---|---|---|
| 0 | int32 | `packetId` | シーケンス番号 |
| 4 | float | `gas` | 0–1（スロットル） |
| 8 | float | `brake` | 0–1 |
| 12 | float | `fuel` | L（非推奨・Graphics 側を使用） |
| 16 | int32 | `gear` | 0=R, 1=N, 2以上=前進ギア |
| 20 | int32 | `rpms` | RPM |
| 24 | float | `steerAngle` | rad（負=左） |
| 28 | float | `speedKmh` | km/h |
| 32 | float[3] | `velocity` | m/s（ワールド XYZ） |
| 44 | float[3] | `accG` | G（[横, 上下, 前後]） |
| 56 | float[4] | `wheelSlip` | 各輪の複合スリップ |
| 72 | float[4] | `wheelLoad` | N |
| 88 | float[4] | `wheelsPressure` | psi |
| 104 | float[4] | `wheelAngularSpeed` | rad/s |
| 120 | float[4] | `tyreWear` | 常に 0.0（未公開） |
| 136 | float[4] | `tyreDirtyLevel` | 0–4（概算） |
| 152 | float[4] | `tyreCoreTemperature` | ℃ |
| 168 | float[4] | `camberRAD` | rad（各輪ローカル符号） |
| 184 | float[4] | `suspensionTravel` | m（符号は車種依存） |
| 200 | float | `drs` | 0–1（展開状態） |
| 204 | float | `tc` | 0–1（設定強度） |
| 208 | float | `heading` | rad（ヨー） |
| 212 | float | `pitch` | rad |
| 216 | float | `roll` | rad |
| 220 | float | `cgHeight` | m |
| 224 | float[5] | `carDamage` | [前, 後, 左, 右, 中央] |
| 244 | int32 | `numberOfTyresOut` | 0–4（コース外のタイヤ数） |
| 248 | int32 | `pitLimiterOn` | 0/1 |
| 252 | float | `abs` | 0–1（設定強度） |
| 256 | float | `kersCharge` | 0–1（充電状態） |
| 260 | float | `kersInput` | 0–1（ドライバー要求） |
| 264 | int32 | `autoShifterOn` | 0/1 |
| 268 | float[2] | `rideHeight` | m または mm（[前, 後]） |
| 276 | float | `turboBoost` | bar（相対） |
| 280 | float | `ballast` | kg |
| 284 | float | `airDensity` | kg/m³ |
| 288 | float | `airTemp` | ℃（外気温） |
| 292 | float | `roadTemp` | ℃（路面温度） |
| 296 | float[3] | `localAngularVel` | rad/s（車体座標系） |
| 308 | float | `finalFF` | -1〜1（フォースフィードバック） |
| 312 | float | `performanceMeter` | s（基準ラップとのデルタ） |
| 316 | int32 | `engineBrake` | 設定インデックス |
| 320 | int32 | `ersRecoveryLevel` | 0以上 |
| 324 | int32 | `ersPowerLevel` | 0以上 |
| 328 | int32 | `ersHeatCharging` | enum（`ERS_HEAT_CHARGING`） |
| 332 | int32 | `ersIsCharging` | 0/1 |
| 336 | float | `kersCurrentKJ` | kJ |
| 340 | int32 | `drsAvailable` | 0/1 |
| 344 | int32 | `drsEnabled` | 0/1 |
| 348 | float[4] | `brakeTemp` | ℃（各輪） |
| 364 | float | `clutch` | 0–1 |
| 368 | float[4] | `tyreTempI` | ℃（内側面） |
| 384 | float[4] | `tyreTempM` | ℃（中央） |
| 400 | float[4] | `tyreTempO` | ℃（外側面） |

### ACE 拡張領域（416〜799 bytes）

初代 AC には無い ACE 独自フィールド。

| オフセット | 型 | フィールド | 単位・備考 |
|---|---|---|---|
| 416 | int32 | `isAIControlled` | 0/1 |
| 420 | float[4][3] | `tyreContactPoint` | ワールド XYZ（m） |
| 468 | float[4][3] | `tyreContactNormal` | 単位ベクトル |
| 516 | float[4][3] | `tyreContactHeading` | 単位ベクトル |
| 564 | float | `brakeBias` | 0–1（フロント側割合） |
| 568 | float[3] | `localVelocity` | m/s（車体座標系） |
| 580 | int32 | `P2PActivations` | Push-to-Pass 発動回数 |
| 584 | int32 | `P2PStatus` | enum（`P2P_STATUS`） |
| 588 | int32 | `currentMaxRpm` | RPM（Static の maxRpm を上書き） |
| 592 | float[4] | `mz` | N·m（セルフアライニングトルク） |
| 608 | float[4] | `fx` | N（縦方向力） |
| 624 | float[4] | `fy` | N（横方向力） |
| 640 | float[4] | `slipRatio` | 無次元 |
| 656 | float[4] | `slipAngle` | rad |
| 672 | int32 | `tcInAction` | 0/1（現在 TC が介入中） |
| 676 | int32 | `absInAction` | 0/1（現在 ABS が介入中） |
| 680 | float[4] | `suspensionDamage` | 0–1（各輪） |
| 696 | float[4] | `tyreTemp` | ℃（代表値） |
| 712 | float | `waterTemp` | ℃（冷却水） |
| 716 | float[4] | `brakeTorque` | N·m（各輪） |
| 732 | int32 | `frontBrakeCompound` | enum（`BRAKE_COMPOUND`） |
| 736 | int32 | `rearBrakeCompound` | enum（`BRAKE_COMPOUND`） |
| 740 | float[4] | `padLife` | 1=新品〜0 |
| 756 | float[4] | `discLife` | 1=新品〜0 |
| 772 | int32 | `ignitionOn` | 0/1 |
| 776 | int32 | `starterEngineOn` | 0/1 |
| 780 | int32 | `isEngineRunning` | 0/1 |
| 784 | float | `kerbVibration` | 0–1（FFB） |
| 788 | float | `slipVibrations` | 0–1（FFB） |
| 792 | float | `roadVibrations` | 0–1（FFB） |
| 796 | float | `absVibrations` | 0–1（FFB） |

---

## Graphics ブロック（`Local\acevo_pmf_graphics`）

描画フレーム毎に更新される。60台分の座標テーブルやタイヤ・ダメージ・エレクトロニクスのサブ構造体を内包する。以下はカテゴリ別のフィールド一覧（宣言順、固定オフセットはリバースエンジニアリングのため省略）。

### ヘッダー・フォーカス

| 型 | フィールド | 備考 |
|---|---|---|
| int32 | `packetId` | シーケンス番号 |
| int32 | `status` | enum（`ACEVO_STATUS`） |
| uint64 | `focused_car_id_a` | 注視車両 GUID（下位128bit） |
| uint64 | `focused_car_id_b` | 注視車両 GUID（上位128bit） |
| uint64 | `player_car_id_a` | 自車 GUID（下位） |
| uint64 | `player_car_id_b` | 自車 GUID（上位） |

### エンジン・シフト状態

| 型 | フィールド | 備考 |
|---|---|---|
| uint16 | `rpm` | HUD レート RPM |
| bool | `is_rpm_limiter_on` | レブリミッター作動中 |
| bool | `is_change_up_rpm` | シフトアップ推奨 |
| bool | `is_change_down_rpm` | シフトダウン推奨 |
| bool | `tc_active` | TC 作動中 |
| bool | `abs_active` | ABS 作動中 |
| bool | `esc_active` | スタビリティコントロール作動中 |
| bool | `launch_active` | ローンチコントロール作動中 |
| bool | `is_ignition_on` | イグニッション ON |
| bool | `is_engine_running` | エンジン稼働中 |
| bool | `kers_is_charging` | KERS 回生中 |
| bool | `is_wrong_way` | 逆走中 |
| bool | `is_drs_available` | DRS ゾーン内 |
| bool | `battery_is_charging` | ハイブリッドバッテリー充電中 |
| bool | `is_max_kj_per_lap_reached` | ラップあたり最大エネルギー到達 |
| bool | `is_max_charge_kj_per_lap_reached` | ラップあたり最大充電到達 |

### 速度・ペダル・入力

| 型 | フィールド | 単位 |
|---|---|---|
| int16 | `display_speed_kmh` | km/h（丸め済み） |
| int16 | `display_speed_mph` | mph |
| int16 | `display_speed_ms` | m/s |
| float | `pitspeeding_delta` | km/h（ピット制限超過分） |
| int16 | `gear_int` | ギア |
| float | `rpm_percent` | 0–1（レッドライン比） |
| float | `gas_percent` | 0–1 |
| float | `brake_percent` | 0–1 |
| float | `handbrake_percent` | 0–1 |
| float | `clutch_percent` | 0–1 |
| float | `steering_percent` | -1〜1 |
| float | `ffb_strength` | 0–1 |
| float | `car_ffb_multiplier` | 車種別スケール |

### エンジン計器値

| 型 | フィールド | 単位 |
|---|---|---|
| float | `water_temperature_percent` | 0–1（レッドライン比） |
| float | `water_pressure_bar` | bar |
| float | `fuel_pressure_bar` | bar |
| int8 | `water_temperature_c` | ℃ |
| int8 | `air_temperature_c` | ℃ |
| float | `oil_temperature_c` | ℃ |
| float | `oil_pressure_bar` | bar |
| float | `exhaust_temperature_c` | ℃ |
| float | `g_forces_x` | G（横） |
| float | `g_forces_y` | G（上下） |
| float | `g_forces_z` | G（前後） |
| float | `turbo_boost` | bar（相対） |
| float | `turbo_boost_level` | 0以上（インデックス） |
| float | `turbo_boost_perc` | 0–1 |
| int32 | `steer_degrees` | 度（符号付き） |

### 距離・時刻・ラップタイム

| 型 | フィールド | 単位 |
|---|---|---|
| float | `current_km` | km |
| uint32 | `total_km` | km |
| uint32 | `total_driving_time_s` | s |
| int32 | `time_of_day_hours` | 0–23 |
| int32 | `time_of_day_minutes` | 0–59 |
| int32 | `time_of_day_seconds` | 0–59 |
| int32 | `delta_time_ms` | ms（正=遅い） |
| int32 | `current_lap_time_ms` | ms |
| int32 | `predicted_lap_time_ms` | ms |

### 燃料

| 型 | フィールド | 単位 |
|---|---|---|
| float | `fuel_liter_current_quantity` | L |
| float | `fuel_liter_current_quantity_percent` | 0–1 |
| float | `fuel_liter_per_km` | L/km（消費率） |
| float | `km_per_fuel_liter` | km/L（逆数） |
| float | `fuel_liter_used` | L（現スティント） |
| float | `fuel_liter_per_lap` | L/lap（平均） |
| float | `laps_possible_with_fuel` | 残燃料で走行可能な周回数 |
| float | `instantaneous_fuel_liter_per_km` | L/km（瞬間値） |
| float | `instantaneous_km_per_fuel_liter` | km/L（瞬間値） |
| float | `fuel_per_lap` | L/lap（セッション平均） |
| float | `fuel_estimated_laps` | 推定残周回数 |
| float | `max_fuel` | L（最大容量） |

### エンジン出力

| 型 | フィールド | 単位 |
|---|---|---|
| float | `current_torque` | N·m（過給込み） |
| int32 | `current_bhp` | BHP（過給込み） |
| float | `gear_rpm_window` | RPM 帯域 |
| float | `max_turbo_boost` | bar（NA 車は 0） |

### タイヤ状態（各輪 `SMEvoTyreState`、256 bytes × 4）

FL / FR / RL / RR の4輪分が埋め込まれる。

| 型 | フィールド | 単位 |
|---|---|---|
| float | `slip` | 複合スリップ |
| bool | `lock` | ロック中 |
| float | `tyre_pressure` | psi |
| float | `tyre_temperature_c` | ℃ |
| float | `brake_temperature_c` | ℃ |
| float | `brake_pressure` | bar |
| float | `tyre_temperature_left` | ℃ |
| float | `tyre_temperature_center` | ℃ |
| float | `tyre_temperature_right` | ℃ |
| char[33] | `tyre_compound_front` | フロントコンパウンド名 |
| char[33] | `tyre_compound_rear` | リアコンパウンド名 |
| float | `tyre_normalized_pressure` | 1.0=最適（超過あり） |
| float | `tyre_normalized_temperature_left` | 1.0=最適 |
| float | `tyre_normalized_temperature_center` | 1.0=最適 |
| float | `tyre_normalized_temperature_right` | 1.0=最適 |
| float | `brake_normalized_temperature` | 1.0=最適 |
| float | `tyre_normalized_temperature_core` | 1.0=最適 |

### 位置・KERS・制御状態

| 型 | フィールド | 単位 |
|---|---|---|
| float | `npos` | 0–1（正規化ラップ位置） |
| float | `kers_charge_perc` | 0–1 |
| float | `kers_current_perc` | 0–1 |
| float | `control_lock_time` | s |

### ダメージ・ピット（不透明ブロック）

| 型 | フィールド | 備考 |
|---|---|---|
| byte[128] | `car_damage` | `SMEvoDamageState`（不透明） |
| int32 | `car_location` | enum（`ACEVO_CAR_LOCATION`） |
| byte[64] | `pit_info` | `SMEvoPitInfo`（不透明） |

### バッテリー

| 型 | フィールド | 単位 |
|---|---|---|
| float | `battery_temperature` | ℃ |
| float | `battery_voltage` | V |

### 計装・エレクトロニクス（不透明ブロック）

各128バイトブロック（車種依存の生値）。

| フィールド | 備考 |
|---|---|
| `instrumentation`（128 B） | ライブ計装値 |
| `instrumentation_min_limit`（128 B） | チャネル別下限 |
| `instrumentation_max_limit`（128 B） | チャネル別上限 |
| `electronics`（128 B） | ドライバー設定 |
| `electronics_min_limit`（128 B） | チャネル別下限 |
| `electronics_max_limit`（128 B） | チャネル別上限 |
| `electronics_is_modifiable`（128 B） | 書き込み可否フラグ |

### 順位・ラップ

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `total_lap_count` | 総ラップ数 |
| uint32 | `current_pos` | 順位（1ベース） |
| uint32 | `total_drivers` | 参加台数 |
| int32 | `last_laptime_ms` | ms（未設定時は -1/0） |
| int32 | `best_laptime_ms` | ms |
| int32 | `flag` | enum（`ACEVO_FLAG_TYPE`、自車提示） |
| int32 | `global_flag` | enum（`ACEVO_FLAG_TYPE`、セッション全体） |
| bool | `is_last_lap` | 最終ラップ |

### 車両スペック・エンジン種別

| 型 | フィールド | 備考 |
|---|---|---|
| uint32 | `max_gears` | 前進ギア数 |
| int32 | `engine_type` | enum（`ACEVO_ENGINE_TYPE`） |
| bool | `has_kers` | KERS 搭載 |
| char[33] | `performance_mode_name` | プリセット名 |
| float | `diff_coast_raw_value` | デフ（コースト）生値 |
| float | `diff_power_raw_value` | デフ（パワー）生値 |
| bool | `use_single_compound` | 単一コンパウンドルール適用中 |

### レースカット・トラックリミット

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `race_cut_gained_time_ms` | ms |
| int32 | `distance_to_deadline` | 距離 |
| float | `race_cut_current_delta` | デルタ |

### セッション・タイミング（不透明ブロック）

| 型 | フィールド | 備考 |
|---|---|---|
| byte[256] | `session_state` | `SMEvoSessionState`（不透明） |
| byte[256] | `timing_state` | `SMEvoTimingState`（不透明） |
| byte[64] | `assists_state` | `SMEvoAssistsState`（不透明） |

### ネットワーク・パフォーマンス

| 型 | フィールド | 単位 |
|---|---|---|
| int32 | `player_ping` | ms |
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
| char[33] | `driver_name` | ASCII |
| char[33] | `driver_surname` | ASCII |
| char[33] | `car_model` | 内部ID |
| bool | `is_in_pit_box` | ガレージ内 |
| bool | `is_in_pit_lane` | ピットレーン内 |
| bool | `is_valid_lap` | ラップ有効（カットなし） |

### マルチカー座標・ギャップ

| 型 | フィールド | 単位 |
|---|---|---|
| float[60][3] | `car_coordinates` | ワールド XYZ（60台分、720 B） |
| float | `gap_ahead` | s（前車とのギャップ） |
| float | `gap_behind` | s（後車とのギャップ） |
| uint8 | `active_cars` | アクティブ台数 |

---

## Static ブロック（`Local\acevo_pmf_static`）

セッションロード時に1回だけ書き込まれ、走行中は変化しない。

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
| char[33] | `nation` | トラック所在国コード（例: "GBR"） |
| float | `longitude` | 経度（十進度） |
| float | `latitude` | 緯度（十進度） |
| char[33] | `track` | トラックID（例: "silverstone"） |
| char[33] | `track_configuration` | レイアウトID（例: "gp"） |
| float | `track_length_m` | m |

---

## 列挙値（enum）

> **重要**: 以下の列挙は名前・メンバーは判明しているが、**整数値の対応は公式に確認されておらず未確定（TBC）** の箇所が多い。実装時は公式 Steam ガイドで整数値を確認すること。

| enum 名 | 使用箇所 | メンバー（値は未確定） |
|---|---|---|
| `ACEVO_STATUS` | `graphics.status` | OFF, REPLAY, LIVE, PAUSE |
| `ACEVO_CAR_LOCATION` | `graphics.car_location` | track, pit lane, pit box, on grid, approaching pit, leaving pit |
| `ACEVO_FLAG_TYPE` | `graphics.flag`, `graphics.global_flag` | none, blue, yellow, black, white, checkered, penalty, orange, FCY, SC, VSC |
| `ACEVO_ENGINE_TYPE` | `graphics.engine_type` | internal-combustion, hybrid, electric, formula-style hybrid |
| `ACEVO_SESSION_TYPE` | `static.session` | practice, qualifying, race, hotlap, time-attack, drift, drag |
| `ACEVO_STARTING_GRIP` | `static.starting_grip` | low, optimum, greenline, fast, damp, wet, flooded |
| `ERS_HEAT_CHARGING` | `physics.ersHeatCharging` | off, from kinetic only, from heat (MGU-H) |
| `BRAKE_COMPOUND` | `physics.frontBrakeCompound`, `physics.rearBrakeCompound` | 車種別のパッド+ディスクコンパウンドID |
| `P2P_STATUS` | `physics.P2PStatus` | idle, armed, active, cooling-down |

---

## 実装上の注意

- **Windows 専用**: 共有メモリは Windows の `OpenFileMappingA` / `MapViewOfFile`（またはメモリマップドファイル API）で `Local\acevo_pmf_*` を開く。ゲームと同一マシン上でのみ読み取れる。KoDriver の LMU 共有メモリ読み取り（`:core:lmu-windows-data`）と同じ制約が当てはまる。
- **AC1 互換プレフィックス**: Physics ブロック先頭 0〜415 バイトは初代 AC の `SPageFilePhysics` と一致するため、既存の AC 用ライブラリで基本的な走行データ（速度・RPM・ギア・入力・タイヤ温度など）を取得できる。
- **タイヤ温度の単位は ℃**（LMU の Kelvin と異なる）。
- **`tyreWear`（physics 120）は常に 0.0** で公開されない。摩耗はブレーキの `padLife` / `discLife` を含め Graphics 側や別フィールドを参照する。
- **ギア表現**: physics の `gear` は `0=R, 1=N, 2以上=前進`。Graphics の `gear_int` とは基準が異なる可能性があるため用途に応じて選ぶ。
- **早期アクセスによる変動**: フィールドの追加・オフセット変更・列挙値確定がありうる。取得側は `sm_version` / `ac_evo_version`（Static）でバージョンを検証し、未知バージョンでは慎重に扱うのが望ましい。
- **同一内容の連続値**: LMU の WebSocket 配信同様、外部配信する場合は前回と同一の値を送らない差分配信を検討する（KoDriver の Ktor サーバー実装方針に準拠）。

---

## 参考情報源

| 情報源 | 種別 | 概要 |
|---|---|---|
| [Assetto Corsa EVO — Shared Memory Documentation（Steam ガイド #3707421508）](https://steamcommunity.com/sharedfiles/filedetails/?id=3707421508) | 公式 | Kunos による一次情報。構造体・列挙・整数値の正典 |
| [albertowd/live-telemetry-evo](https://github.com/albertowd/live-telemetry-evo)（[docs/SHARED_MEMORY.md](https://github.com/albertowd/live-telemetry-evo/blob/develop/docs/SHARED_MEMORY.md)） | OSS | オーバーレイ実装。物理ブロックの詳細オフセット・フィールド解説が豊富 |
| [dSyncro/acevo-shared-memory](https://github.com/dSyncro/acevo-shared-memory)（[crate](https://lib.rs/crates/acevo-shared-memory)） | OSS (Rust) | ACE 共有メモリアクセスライブラリ。型付きアクセサと生 C 構造体アクセスを提供 |
| [Live Telemetry Evo（OverTake.gg）](https://www.overtake.gg/downloads/live-telemetry-evo.84121/) | 配布 | 上記オーバーレイの配布ページ |
