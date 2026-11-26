# 自動読み上げ項目の配線と動作

[`../readout-item-key-wiring.md`](../readout-item-key-wiring.md) の詳細。シミュレーターごとの自動読み上げ項目について、listPane / detailPane のキー・Narrator の判定・既定文言・保存先をまとめる。

## 全項目に共通する動作

- 自由文言は OS 標準 TTS で読み上げ、収録 WAV へはフォールバックしない。
- 判定時に解決した本文をイベントの `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。
- 空白文言・TTS 利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。
- 詳細画面の試聴は空白文言・TTS 利用不可・音量0以下では再生しない。

以下の各項目の説明にも同じ内容が残っている箇所があるが、上記が共通ルールである。

## 項目表（ACE Windows）

| 項目 | listPane キー | detailPane キー | Narrator の判定 | 既定文言（定数名） | 補足 |
| --- | --- | --- | --- | --- | --- |
| フラッグ（全10種） | `Flag.Root` | 種別ごとの `Flag.WhiteFlag` 等（`Flag.OrangeCircleFlag`・`Flag.RedYellowStripesFlag` を含む10種） | 種別キーと `Flag.Root` の有効状態 | 種別ごとの `ACE_WINDOWS_*_FLAG_READOUT_TEXT_DEFAULT`（例: `ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT`） | 保存文言をOS標準TTSのみで読み上げ（WAVなし）。開始音・優先度・キュー・試聴は `Flag.Root` |
| 車両接近 | `VehicleApproach.Root` | `VehicleApproach.StartReadout` | `Root` と `StartReadout` の有効状態 | `ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT`（「車両接近」） | 固定文言でプレースホルダーなし。開始音・優先度・キュー・試聴は `Root` |
| タイヤ過熱 | `TyreTemperature.Root` | `TyreTemperature.OverheatWarning` | 過熱・解除判定と `Root` / `OverheatWarning` の有効判定 | `ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT`（「タイヤ過熱{celsius}℃」） | `{celsius}` は判定時の全輪最大カーカス温度を四捨五入。保存は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`） |
| ブレーキ温度 | `BrakeTemperature.Root` | `BrakeTemperature.OverheatWarning`（詳細画面のスイッチは未実装） | 未実装（読み上げは行わない） | `ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT`（「ブレーキ温度は{celsius}℃以上」） | 詳細画面は説明文のみ。一覧の並び順・読み上げ・開始音・キューの設定は `Root` に保存する。過熱警告の有効/無効・文言・閾値（600〜1000℃、既定800℃）は `AceWindowsBrakeTemperaturePreferencesRepository` に保存できるが、詳細画面と Narrator はまだ参照しない |
| 燃料残量 | `RemainingFuel.Root` | `RemainingFuel.DetailEnabled` | 閾値判定と `Root` / `DetailEnabled` の有効判定 | `ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り{percent}%」） | `{percent}` は判定時の実残量。閾値は `ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT`。保存は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`） |
| 燃料残り周回数 | `RemainingFuelLaps.Root` | `RemainingFuelLaps.DetailEnabled` | 閾値判定と有効判定は従来どおり | `ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周以下は `ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT`（「燃料は残り1周未満」） | 閾値は `ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT`。保存は `AceWindowsRemainingFuelLapsPreferences` のフィールド2・3。旧データは閾値を維持して既定文言を使用 |
| 自己ベストラップ | `MyBestLap.Root` | `MyBestLap.DetailEnabled` | 更新判定と `Root` / `DetailEnabled` の有効判定 | `ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT`（「自己ベストラップ更新 {laptime}」） | `{laptime}` は「1分23秒456」形式。保存は共有 `MyBestLapPreferences` の `aceWindowsReadoutText`（ProtoNumber 4） |

ACE の補足（各項目の動作詳細は下の段落を参照）:

- 全項目で判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。WAVへのフォールバックは行わない。
- 車両接近の保存先は `ace_windows_auto_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）。プレースホルダーはない。
- 試聴は空白・TTS利用不可・音量0以下では再生しない。


ACEの全10フラッグ（Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes）は保存した自由文言をOS標準TTSのみで読み上げ、WAVは使用しない。既定文言はドメイン定数を参照し、OrangeCircleは「オレンジボールフラッグ、車両に不具合があります」、RedYellowStripesは「レッド・イエローストライプフラッグ、路面が滑りやすいです」。保存先は `ace_windows_auto_flag_readout_text_preferences.pb`。既存の `ace_windows_auto_flag_preferences.pb`（enabledStates）は変更せず、詳細の個別キー（`AceWindowsReadoutItemKey.Flag.WhiteFlag` 等）と一覧の `Flag.Root` の有効状態を従来どおり判定する。空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。全10種の `SpeechEvent.readoutItemKey` は `Flag.Root` のため、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Flag.Root` を参照する。試聴は空白・TTS利用不可・音量0以下では再生しない。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACE車両接近は保存した固定の自由文言をOS標準TTSで読み上げる。保存先は既存の `ace_windows_auto_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）、既定文言はドメイン定数の「車両接近」。一覧の `AceWindowsReadoutItemKey.VehicleApproach.Root` と詳細の `StartReadout` の有効状態は従来どおり判定する。`AceWindowsVehicleApproach` は `VehicleApproach.Root` をキーに持ち、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Root` を参照する。Narratorで本文を都度解決し、空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。試聴は空白・TTS利用不可・音量0以下では再生しない。プレースホルダーはなく、文言は固定文字列として扱う。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACEタイヤ過熱は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`）、既定文言は「タイヤ過熱{celsius}℃」。`{celsius}` は判定時点の全輪最大カーカス温度を `roundToInt` で四捨五入した整数に置換する。過熱・解除判定と `TyreTemperature.Root` / `OverheatWarning` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.TyreTemperature.Root` を使う。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では過熱文言の編集・既定値へのリセット・`{celsius}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の高温閾値をサンプル温度に使い、`AceWindowsReadoutItemKey.TyreTemperature.Root` の開始音とTTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では試聴の本文も開始音も再生しない。

ACE燃料残量警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`）、既定文言は「燃料は残り{percent}%」。`{percent}` は判定時の実残量を四捨五入した整数（0〜100）に置換する。閾値判定と `RemainingFuel.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuel.Root` を維持する。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中に設定が変わってもログと発話を一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに空文字と `SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では自由文言の編集・既定値へのリセット・`{percent}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の残量閾値を使い、`RemainingFuel.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE燃料残り周回数警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelLapsPreferences` のフィールド2（`readoutText`）とフィールド3（`emptyReadoutText`）。通常の既定文言は「燃料は残り約{laps}周」で、`{laps}` を判定時の整数周回数に置換する。0周以下は0周用文言（既定「燃料は残り1周未満」）をそのまま使う。旧データは閾値を維持して既定文言を使用する。閾値・有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuelLaps.Root` を維持する。Processorで確定した文言を `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。WAVへフォールバックしない。詳細画面では通常文言・0周以下用文言の自由文言編集と既定値へのリセットを提供し、通常文言には `{laps}` の末尾挿入と未知プレースホルダーの警告を表示する。試聴は通常文言を画面に表示中の閾値周回数で置換し、0周以下用文言はそのまま使い、`RemainingFuelLaps.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。保存先は共有 `MyBestLapPreferences` のACE専用 `@ProtoNumber(4) aceWindowsReadoutText`、既定文言は「自己ベストラップ更新 {laptime}」。`{laptime}` は更新後の `bestLapTimeMs`（Int）を「1分23秒456」形式に置換し、1分未満では分を省略、ミリ秒は3桁固定、60分以上も分で表す。更新判定と `MyBestLap.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.MyBestLap.Root` を使う。Processorで確定した文言を `AceWindowsMyBestLap` の `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。詳細画面で文言編集・`{laptime}` 挿入・未知プレースホルダー警告・リセット・サンプルタイム83456msでの試聴を提供する。旧口調設定（`voiceType`）は読み取らず、既存ユーザーは既定文言になる。WAVへのフォールバックは行わない。

## 項目表（LMU Windows）

| 項目 | listPane キー | detailPane キー | Narrator の判定 | 既定文言（定数名） | 補足 |
| --- | --- | --- | --- | --- | --- |
| 車両接近 | `VehicleApproach.Root` | `VehicleApproach.StartReadout` / `VehicleApproach.Sustained` | 両詳細と `Root` の有効状態 | 開始時 左 `LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT`（「カーレフト」）・右 `..._START_RIGHT_...`（「カーライト」）。継続時 左 `..._SUSTAINED_LEFT_...`（「キープライト」）・右 `..._SUSTAINED_RIGHT_...`（「キープレフト」） | 開始音・優先度・キュー・試聴は `Root`。開始時・継続時とも `Root` をイベントキーに持つ |
| バーチャルエネルギー残量 | `RemainingVirtualEnergy.Root` | `RemainingVirtualEnergy.WarningReadout` | `Root` と `WarningReadout` の有効判定（VEクラスのみ） | `LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT`（「バーチャルエネルギーは残り{percent}%以下」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| 燃料残量 | `RemainingFuel.Root` | `RemainingFuel.WarningReadout` | `Root` と `WarningReadout` の有効判定（燃料クラスのみ） | `LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り{percent}%」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| バーチャルエネルギー残量（現在値） | `RemainingVirtualEnergy.Root` | `RemainingVirtualEnergy.WarningReadout` | 現在値イベントも同じ2スイッチで判定 | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT`（「バーチャルエネルギーは残り{percent}%」） | 実残量を四捨五入した整数%を `{percent}` に入れる。閾値警告用の自由文言は参照しない |
| タイヤ摩耗（残存率） | `TyreWear.Root` | `TyreWear.WarningReadout` | `Root` と `WarningReadout` の有効判定 | `LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT`（「タイヤは残り{percent}%以下」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| ブレーキ摩耗（残量） | `BrakeWear.Root` | `BrakeWear.WarningReadout` | `Root` と `WarningReadout` の有効判定。いずれかの輪が閾値以下で1回（全輪が閾値超で再武装） | `LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT`（「ブレーキは残り{percent}%以下」） | 閾値は車両クラス別の `LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_*_DEFAULT`（各20）。`{percent}` は閾値に置換 |
| ブレーキ過熱（温度） | `BrakeTemperature.Root` | `BrakeTemperature.WarningReadout` | `Root` と `WarningReadout` の有効判定 | `LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT`（「ブレーキ温度は{celsius}℃以上」） | 閾値は車両クラス別の `LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_*_DEFAULT`。`{celsius}` は閾値に置換 |
| タイヤ過熱 | `TyreTemperature.Root` | `TyreTemperature.OverheatWarning` | 過熱・解除判定と `Root` / `OverheatWarning` の有効判定 | `LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT`（「タイヤ過熱{celsius}℃」） | `{celsius}` は判定時の全輪最高カーカス温度 |
| タイヤ低温 | `TyreTemperature.Root` | `TyreTemperature.LowWarning` | 低温判定と `Root` / `LowWarning` の有効判定 | `LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT`（「タイヤ低温{celsius}℃」） | `{celsius}` は判定時の全輪最高温度 |
| 車両故障（オーバーヒート） | `VehicleDamage.Root` | `VehicleDamage.Overheat` | `Root` と `Overheat` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT`（「オーバーヒート」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| 車両故障（部品脱落） | `VehicleDamage.Root` | `VehicleDamage.PartDetached` | `Root` と `PartDetached` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT`（「部品脱落」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| 車両故障（タイヤ脱落） | `VehicleDamage.Root` | `VehicleDamage.TyreDetached` | `Root` と `TyreDetached` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT`（「タイヤ脱落」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| ピットタイミング（バーチャルエネルギー） | `PitTiming.Root` | `PitTiming.VirtualEnergy` | `Root` と `VirtualEnergy` の有効判定（VEクラスのみ発火） | `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT`（「バーチャルエネルギー切れのため{laps}周以内にピットイン」）。切迫時は `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT`（「バーチャルエネルギー切れのため必ずピットイン」） | 閾値は `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT`（3） |
| ピットタイミング（タイヤ摩耗） | `PitTiming.Root` | `PitTiming.TyreWear` | `Root` と `TyreWear` の有効判定 | `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT`（「タイヤ摩耗のため{laps}周以内にピットイン」）。切迫時は `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT`（「タイヤ摩耗のため必ずピットイン」） | 閾値は `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT`（3） |
| ピットタイミング（燃料） | `PitTiming.Root` | `PitTiming.Fuel` | `Root` と `Fuel` の有効判定（燃料クラスのみ発火） | `LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。切迫時は `LMU_WINDOWS_PIT_TIMING_FUEL_IMMINENT_READOUT_TEXT_DEFAULT`（「燃料は残り1周未満」） | 閾値は `LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT`（3）。`ObserveLmuWindowsPitTimingEnabledStatesUseCase` の既定は有効 |
| 自己ベストラップ | `MyBestLap.Root` | `MyBestLap.DetailEnabled` | 更新判定と `Root` / `DetailEnabled` の有効判定 | `LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT`（「自己ベストラップ更新 {laptime}」） | `{laptime}` は「1分23秒456」形式 |
| フラッグ（4種） | `Flag.Root` | `Flag.BlueFlag` / `Flag.SectorYellowFlag` / `Flag.FullCourseYellow` / `Flag.RedFlag` | 種別キーと `Flag.Root` の有効状態 | `LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT`（「ブルーフラッグ」）・`LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT`（「イエローフラッグ」）・`LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT`（「フルコースイエロー」）・`LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT`（「レッドフラッグ」） | 自由文言TTSのみ。WAVは使わない |

LMU の補足（各項目の動作詳細は下の段落を参照）:

- 車両故障の文言は1つずつのグローバル文言で、保存先は `LmuWindowsVehicleDamagePreferences`。保存時は `trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)`（40文字）で正規化する。
- 車両クラス別の閾値（ブレーキ・ピットタイミング等）と `{percent}` / `{celsius}` の置換値の扱いは表の補足と下の段落を参照する。
- 空白文言・TTS利用不可では本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。収録WAVへのフォールバックは行わない。


LMU車両接近では、詳細ペインの `StartReadout` / `Sustained` と一覧の `Root` の有効状態を既存どおり判定する。開始時・継続時の自由文言TTSはともに `VehicleApproach.Root` をイベントキーに持つため、開始音・優先度・キュー設定も `Root` を参照する。開始時・継続時の文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。

LMUバーチャルエネルギー残量警告では、一覧の `RemainingVirtualEnergy.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残量閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残量は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `RemainingVirtualEnergy.Root` を参照する。詳細画面の試聴も現在の閾値に置換し、同じキーの開始音とOS標準TTSを使用する。残量警告の収録WAVは使用せず、WAVへはフォールバックしない。

LMU燃料残量警告は、一覧の `RemainingFuel.Root` と詳細の `WarningReadout` がともに有効な燃料クラスでのみ判定する。燃料クラスでは `{percent}` を閾値（%）に置換してOS標準TTSで読み上げ、`RemainingFuel.Root` の開始音・優先度・キューを使う。バーチャルエネルギークラスではVE警告のみを判定し、燃料警告は発生しない。逆に燃料クラスではVE警告を判定しない。警告は閾値を下回った瞬間に一度だけ発生し、閾値を上回ると再武装する。

LMUタイヤ摩耗警告では、一覧の `TyreWear.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残存率閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残存率は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `TyreWear.Root` を参照する。詳細画面の試聴も現在の閾値と編集中の文言を解決したイベントを再生し、同じキーの開始音とOS標準TTSを使用する。摩耗警告の収録WAVは使用せず、WAVへはフォールバックしない。

GT7燃料残量警告では、一覧の `RemainingFuel.Root` と詳細の `DetailEnabled` の有効状態を既存どおり判定する。
自由文言の `{percent}` は `gasLevel / gasCapacity * 100` を `roundToInt()` で四捨五入し、0..100 に収めた実際の残量（整数%）に置換する。
LMUバーチャルエネルギー残量の閾値置換とは異なり、GT7では設定閾値を置換値に使用しない。
文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。
開始音・優先度・キュー設定は `RemainingFuel.Root` を参照し、OS標準TTSのみを使用してWAVへフォールバックしない。
判定時に解決した文言をイベントに保持し、キュー待機中の設定変更によってログと発話がずれないようにする。
詳細画面で自由文言を設定でき、試聴では現在の閾値をサンプル残量として `{percent}` に置換する。`RemainingFuel.Root` キーの開始音とOS標準TTSを使用し、空白文言・TTS利用不可・音量0以下では本文も開始音も再生しない。

ピットタイミング（バーチャルエネルギー）の自動警告は `energySource == LmuWindowsEnergySource.VirtualEnergy` の場合のみ発火する。燃料クラスでも消費量の追跡・状態更新・評価周の更新は継続する。

LMUピットタイミングでは、一覧の `Root` と詳細の `VirtualEnergy` / `TyreWear` / `Fuel` の有効状態を既存どおり判定する。3ソースの警告は自由文言TTSで読み上げ、残り1周以上は通常文言の `{laps}` を予想残り周回数で置換し、0周以下は専用の切迫時文言を使用する。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `PitTiming.Root` を参照する。詳細画面で通常・切迫時文言を設定でき、試聴も `PitTiming.Root` キーの開始音と自由文言TTSを使用する（通常文言は `{laps}` を各スライダーに表示中の周回数に置換、切迫時は置換なし）。3ソースとも自由文言TTSを使用し、ピットタイミング用の収録WAVは使用しない。

GT7タイヤ過熱警告は自由文言のOS標準TTSのみを使用する。既定文言は「タイヤ過熱{celsius}℃」。
`{celsius}` は判定時の全輪の最高タイヤ温度を整数に丸めた摂氏に置換し、解決文言をイベントの
`resolvedText` に保持するためキュー待機中も発話とログが一致する。判定条件・閾値・ヒステリシスは従来どおり。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。
WAVへのフォールバックは行わない。開始音・優先度・キュー設定は
`TyreTemperature.Root` を参照する。詳細画面の試聴は現在の高温閾値をサンプル温度として同じキーの開始音とTTSで再生する。

LMUのタイヤ過熱・低温警告、車両故障、自己ベストラップ、自由文字列イベントの共通処理は [`feature/lmu-windows-narrator/README.md`](../../feature/lmu-windows-narrator/README.md) を参照。

LMUブレーキ過熱警告では一覧の `BrakeTemperature.Root` と詳細の `WarningReadout` の有効状態を判定する。イベントは判定時の車両クラス別温度閾値を持ち、全クラス共通文言の `{celsius}` をその閾値（℃）に置換する。実測温度は使用しない。既存の `LmuWindowsVehicleClassBrakeTemperaturePreferences` に文言を保存する。判定時に `resolvedText` を確定してログと発話を一致させる。空白・TTS利用不可では本文と開始音を要求せず空文字と `SKIPPED` を記録する。開始音・優先度・キューと試聴は `BrakeTemperature.Root` を参照する。詳細ペインの試聴は選択中クラスのスライダー閾値を使う。WAVへはフォールバックしない。

GT7の自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。既定文言は「自己ベストラップ更新 {laptime}」。
`{laptime}` は判定時の更新後の `bestLapTimeMs` を「1分23秒456」の形式に置換する。1分未満では分を省略し、
ミリ秒は3桁固定、60分以上も時間にせず分で表す。解決した文言は `resolvedText` に保持し、キュー待機中も発話とログを一致させる。
判定条件と `MyBestLap.Root` / `DetailEnabled` の有効状態は従来どおり。開始音・優先度・キューは `MyBestLap.Root` を参照する。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。
WAVへフォールバックしない。共有protoの `voiceType` は旧データの互換性のため残し、GT7は `readoutText`、LMUは `lmuWindowsReadoutText`、ACEは `aceWindowsReadoutText` を使用する。

LMUブレーキ摩耗警告では一覧の `BrakeWear.Root` と詳細の `WarningReadout` の有効状態を判定する。`LmuWindowsNarratorViewModel` は `ObserveLmuWindowsBrakeWearRemainingUseCase`（REST APIの `wearables.brakes` と車両クラスから、観測した最大の厚さを新品時とみなして残量%を計算）の値を `determineBrakeWearLow` で判定し、いずれかの輪が車両クラス別の残量閾値以下になった時点で1回だけ読み上げる（全輪が閾値を超えるまで再読み上げしない）。イベントは判定時の閾値（%）を持ち、全クラス共通文言の `{percent}` を置換する。実測の残量は使用しない。文言と閾値は `LmuWindowsVehicleClassBrakeWearPreferences` に保存する。判定時に `resolvedText` を確定してログと発話を一致させる。空白・TTS利用不可では本文と開始音を要求せず空文字と `SKIPPED` を記録する。開始音・優先度・キューと試聴は `BrakeWear.Root` を参照する。詳細ペインの試聴は選択中クラスのスライダー閾値を使う。WAVへはフォールバックしない。デスクトップ版はLMU REST APIから取得し、Android版はKoDriverサーバーの `/ws/lmu_windows/brake_wear` から受信して同じ残量判定・読み上げを行う。取得失敗時はJSON `null` が配信される。

自由文言TTSの本文と詳細画面の試聴は`SpeakTextUseCase`を通し、呼び出し時点の保存済み音声ID・読み上げ速度・声の高さを使う。音声IDを明示した試聴でも速度と声の高さは保存値を使用する。`TextToSpeechRepository.speak(text, queue, volume, voiceId, speed, pitch)`の`speed`・`pitch`はともに既定1.0、範囲0.5〜2.0で、Android・Windows実装で範囲外を制限する。開始音のWAVには速度・声の高さを適用しない。
