# ReadoutItemKey の配線（listPane / detailPane と Narrator の読み上げ判定の一致）

共通の `ReadoutItemKey` インターフェースと `ReadoutItemKeyRegistry` は `:core:domain` に置く。各シミュレーターの sealed key、`*ReadoutItemKeys.kt` の順序付き一覧/spec、`*ReadoutDefaults.kt` は該当する simulator domain に置く。新しい key を追加するときは必ずその simulator の `*ReadoutItemKeys.kt` にも含める。アプリ起動時に `:core:data` の登録関数が3 simulator の一覧を登録する。


自動読み上げの保存キーとシミュレーター別DataStoreファイル名は、`lmu_windows_` / `ace_windows_` / `gt7_ps5_` の直後に `auto_` を置く。共通の一覧設定は Desktop では `auto_readout_preferences.pb`、Android では `auto_readout_preferences.preferences_pb` に保存する。`auto_` 導入時はユーザー了承のもと設定を意図的に初期化し、設定の旧値・旧ファイルの移行は行わない。テレメトリログは Room の 4→5 マイグレーションで旧キーを新キーへ変換して保持する。開始音・ホットキーなど自動読み上げ以外の設定ファイル名は変更しない。
`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目を Narrator で読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`AutoReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `core/lmu-windows-domain/src/commonMain/kotlin/kurou/kodriver/domain/lmuwindows/usecase/` の `DetermineLmuWindowsVehicleReadoutUseCase.kt`・`DetermineLmuWindowsTyreReadoutUseCase.kt`・`DetermineLmuWindowsBrakeReadoutUseCase.kt`・`DetermineLmuWindowsFuelReadoutUseCase.kt`・`DetermineLmuWindowsVirtualEnergyReadoutUseCase.kt`・`DetermineLmuWindowsPitTimingReadoutUseCase.kt`・`DetermineLmuWindowsRaceFlagReadoutUseCase.kt`・`DetermineLmuWindowsMyBestLapReadoutUseCase.kt`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態が Narrator に未反映だった
- #472: listPane の `VehicleDamage` スイッチが当時の `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage`（現在は責務別に分割済みで `DetermineLmuWindowsVehicleReadoutUseCase.determineVehicleDamage`）から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する読み上げ判定 UseCase のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

### 自動読み上げの項目表（ACE Windows）

| 項目 | listPane キー | detailPane キー | Narrator の判定 | 既定文言（定数名） | 補足 |
| --- | --- | --- | --- | --- | --- |
| フラッグ（全10種） | `Flag.Root` | 種別ごとの `Flag.WhiteFlag` 等（`Flag.OrangeCircleFlag`・`Flag.RedYellowStripesFlag` を含む10種） | 種別キーと `Flag.Root` の有効状態 | 種別ごとの `ACE_WINDOWS_*_FLAG_READOUT_TEXT_DEFAULT`（例: `ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT`） | 保存文言をOS標準TTSのみで読み上げ（WAVなし）。開始音・優先度・キュー・試聴は `Flag.Root` |
| 車両接近 | `VehicleApproach.Root` | `VehicleApproach.StartReadout` | `Root` と `StartReadout` の有効状態 | `ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT`（「車両接近」） | 固定文言でプレースホルダーなし。開始音・優先度・キュー・試聴は `Root` |
| タイヤ過熱 | `TyreTemperature.Root` | `TyreTemperature.OverheatWarning` | 過熱・解除判定と `Root` / `OverheatWarning` の有効判定 | `ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT`（「タイヤ過熱 {celsius}度」） | `{celsius}` は判定時の全輪最大カーカス温度を四捨五入。保存は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`） |
| 残り燃料 | `RemainingFuel.Root` | `RemainingFuel.DetailEnabled` | 閾値判定と `Root` / `DetailEnabled` の有効判定 | `ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」） | `{percent}` は判定時の実残量。閾値は `ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT`。保存は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`） |
| 残り燃料の周回数 | `RemainingFuelLaps.Root` | `RemainingFuelLaps.DetailEnabled` | 閾値判定と有効判定は従来どおり | `ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周以下は `ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT`（「燃料残り1周未満」） | 閾値は `ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT`。保存は `AceWindowsRemainingFuelLapsPreferences` のフィールド2・3。旧データは閾値を維持して既定文言を使用 |
| 自己ベストラップ | `MyBestLap.Root` | `MyBestLap.DetailEnabled` | 更新判定と `Root` / `DetailEnabled` の有効判定 | `ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT`（「自己ベストラップ更新 {laptime}」） | `{laptime}` は「1分23秒456」形式。保存は共有 `MyBestLapPreferences` の `aceWindowsReadoutText`（ProtoNumber 4） |

ACE の補足（各項目の動作詳細は下の段落を参照）:

- 全項目で判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。WAVへのフォールバックは行わない。
- 車両接近の保存先は `ace_windows_auto_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）。プレースホルダーはない。
- 試聴は空白・TTS利用不可・音量0以下では再生しない。


ACEの全10フラッグ（Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes）は保存した自由文言をOS標準TTSのみで読み上げ、WAVは使用しない。既定文言はドメイン定数を参照し、OrangeCircleは「オレンジボールフラッグ、車両に不具合があります」、RedYellowStripesは「レッド・イエローストライプフラッグ、路面が滑りやすいです」。保存先は `ace_windows_auto_flag_readout_text_preferences.pb`。既存の `ace_windows_auto_flag_preferences.pb`（enabledStates）は変更せず、詳細の個別キー（`AceWindowsReadoutItemKey.Flag.WhiteFlag` 等）と一覧の `Flag.Root` の有効状態を従来どおり判定する。空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。全10種の `SpeechEvent.readoutItemKey` は `Flag.Root` のため、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Flag.Root` を参照する。試聴は空白・TTS利用不可・音量0以下では再生しない。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACE車両接近は保存した固定の自由文言をOS標準TTSで読み上げる。保存先は既存の `ace_windows_auto_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）、既定文言はドメイン定数の「車両接近」。一覧の `AceWindowsReadoutItemKey.VehicleApproach.Root` と詳細の `StartReadout` の有効状態は従来どおり判定する。`AceWindowsVehicleApproach` は `VehicleApproach.Root` をキーに持ち、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Root` を参照する。Narratorで本文を都度解決し、空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。試聴は空白・TTS利用不可・音量0以下では再生しない。プレースホルダーはなく、文言は固定文字列として扱う。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACEタイヤ過熱は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`）、既定文言は「タイヤ過熱 {celsius}度」。`{celsius}` は判定時点の全輪最大カーカス温度を `roundToInt` で四捨五入した整数に置換する。過熱・解除判定と `TyreTemperature.Root` / `OverheatWarning` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.TyreTemperature.Root` を使う。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では過熱文言の編集・既定値へのリセット・`{celsius}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の高温閾値をサンプル温度に使い、`AceWindowsReadoutItemKey.TyreTemperature.Root` の開始音とTTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では試聴の本文も開始音も再生しない。

ACE燃料残量警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`）、既定文言は「燃料は残り{percent}パーセント」。`{percent}` は判定時の実残量を四捨五入した整数（0〜100）に置換する。閾値判定と `RemainingFuel.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuel.Root` を維持する。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中に設定が変わってもログと発話を一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに空文字と `SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では自由文言の編集・既定値へのリセット・`{percent}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の残量閾値を使い、`RemainingFuel.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE燃料残り周回数警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelLapsPreferences` のフィールド2（`readoutText`）とフィールド3（`emptyReadoutText`）。通常の既定文言は「燃料は残り約{laps}周」で、`{laps}` を判定時の整数周回数に置換する。0周以下は0周用文言（既定「燃料残り1周未満」）をそのまま使う。旧データは閾値を維持して既定文言を使用する。閾値・有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuelLaps.Root` を維持する。Processorで確定した文言を `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。WAVへフォールバックしない。詳細画面では通常文言・0周以下用文言の自由文言編集と既定値へのリセットを提供し、通常文言には `{laps}` の末尾挿入と未知プレースホルダーの警告を表示する。試聴は通常文言を画面に表示中の閾値周回数で置換し、0周以下用文言はそのまま使い、`RemainingFuelLaps.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。保存先は共有 `MyBestLapPreferences` のACE専用 `@ProtoNumber(4) aceWindowsReadoutText`、既定文言は「自己ベストラップ更新 {laptime}」。`{laptime}` は更新後の `bestLapTimeMs`（Int）を「1分23秒456」形式に置換し、1分未満では分を省略、ミリ秒は3桁固定、60分以上も分で表す。更新判定と `MyBestLap.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.MyBestLap.Root` を使う。Processorで確定した文言を `AceWindowsMyBestLap` の `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。詳細画面で文言編集・`{laptime}` 挿入・未知プレースホルダー警告・リセット・サンプルタイム83456msでの試聴を提供する。旧口調設定（`voiceType`）は読み取らず、既存ユーザーは既定文言になる。WAVへのフォールバックは行わない。

### 自動読み上げの項目表（LMU Windows）

| 項目 | listPane キー | detailPane キー | Narrator の判定 | 既定文言（定数名） | 補足 |
| --- | --- | --- | --- | --- | --- |
| 車両接近 | `VehicleApproach.Root` | `VehicleApproach.StartReadout` / `VehicleApproach.Sustained` | 両詳細と `Root` の有効状態 | 開始時 左 `LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT`（「カーレフト」）・右 `..._START_RIGHT_...`（「カーライト」）。継続時 左 `..._SUSTAINED_LEFT_...`（「キープライト」）・右 `..._SUSTAINED_RIGHT_...`（「キープレフト」） | 開始音・優先度・キュー・試聴は `Root`。開始時・継続時とも `Root` をイベントキーに持つ |
| バーチャルエネルギー残量 | `RemainingVirtualEnergy.Root` | `RemainingVirtualEnergy.WarningReadout` | `Root` と `WarningReadout` の有効判定（VEクラスのみ） | `LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT`（「バーチャルエネルギー残量{percent}%以下」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| 燃料残量 | `RemainingFuel.Root` | `RemainingFuel.WarningReadout` | `Root` と `WarningReadout` の有効判定（燃料クラスのみ） | `LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| バーチャルエネルギー残量（現在値） | `RemainingVirtualEnergy.Root` | `RemainingVirtualEnergy.WarningReadout` | 現在値イベントも同じ2スイッチで判定 | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT`（「バーチャルエネルギー残量は{percent}パーセント」） | 実残量を四捨五入した整数%を `{percent}` に入れる。閾値警告用の自由文言は参照しない |
| タイヤ摩耗（残存率） | `TyreWear.Root` | `TyreWear.WarningReadout` | `Root` と `WarningReadout` の有効判定 | `LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT`（「タイヤ残存率{percent}%以下」） | `{percent}` は設定閾値に置換。閾値は `LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT`（30） |
| ブレーキ摩耗（残量） | `BrakeWear.Root` | `BrakeWear.WarningReadout` | `Root` と `WarningReadout` の有効判定。いずれかの輪が閾値以下で1回（全輪が閾値超で再武装） | `LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT`（「ブレーキ残量{percent}%以下」） | 閾値は車両クラス別の `LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_*_DEFAULT`（各20）。`{percent}` は閾値に置換 |
| ブレーキ過熱（温度） | `BrakeTemperature.Root` | `BrakeTemperature.WarningReadout` | `Root` と `WarningReadout` の有効判定 | `LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT`（「ブレーキ温度{celsius}℃以上」） | 閾値は車両クラス別の `LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_*_DEFAULT`。`{celsius}` は閾値に置換 |
| タイヤ過熱 | `TyreTemperature.Root` | `TyreTemperature.OverheatWarning` | 過熱・解除判定と `Root` / `OverheatWarning` の有効判定 | `LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT`（「タイヤ過熱 {celsius}度」） | `{celsius}` は判定時の全輪最高カーカス温度 |
| タイヤ低温 | `TyreTemperature.Root` | `TyreTemperature.LowWarning` | 低温判定と `Root` / `LowWarning` の有効判定 | `LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT`（「タイヤ低温 {celsius}度」） | `{celsius}` は判定時の全輪最高温度 |
| 車両故障（オーバーヒート） | `VehicleDamage.Root` | `VehicleDamage.Overheat` | `Root` と `Overheat` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT`（「オーバーヒート」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| 車両故障（部品脱落） | `VehicleDamage.Root` | `VehicleDamage.PartDetached` | `Root` と `PartDetached` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT`（「部品脱落」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| 車両故障（タイヤ脱落） | `VehicleDamage.Root` | `VehicleDamage.TyreDetached` | `Root` と `TyreDetached` の有効判定 | `LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT`（「タイヤ脱落」） | 固定文言。開始音・優先度・キュー・試聴は `Root` |
| ピットタイミング（バーチャルエネルギー） | `PitTiming.Root` | `PitTiming.VirtualEnergy` | `Root` と `VirtualEnergy` の有効判定（VEクラスのみ発火） | `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT`（「エネルギー切れのため{laps}周以内にピットイン」）。切迫時は `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT`（「エネルギー切れのため必ずピットイン」） | 閾値は `LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT`（3） |
| ピットタイミング（タイヤ摩耗） | `PitTiming.Root` | `PitTiming.TyreWear` | `Root` と `TyreWear` の有効判定 | `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT`（「タイヤ摩耗のため{laps}周以内にピットイン」）。切迫時は `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT`（「タイヤ摩耗のため必ずピットイン」） | 閾値は `LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT`（3） |
| ピットタイミング（燃料） | `PitTiming.Root` | `PitTiming.Fuel` | `Root` と `Fuel` の有効判定（燃料クラスのみ発火） | `LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。切迫時は `LMU_WINDOWS_PIT_TIMING_FUEL_IMMINENT_READOUT_TEXT_DEFAULT`（「燃料残り1周未満」） | 閾値は `LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT`（3）。`ObserveLmuWindowsPitTimingEnabledStatesUseCase` の既定は有効 |
| 自己ベストラップ | `MyBestLap.Root` | `MyBestLap.DetailEnabled` | 更新判定と `Root` / `DetailEnabled` の有効判定 | `LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT`（「自己ベストラップ更新 {laptime}」） | `{laptime}` は「1分23秒456」形式 |
| フラッグ（4種） | `Flag.Root` | `Flag.BlueFlag` / `Flag.SectorYellowFlag` / `Flag.FullCourseYellow` / `Flag.RedFlag` | 種別キーと `Flag.Root` の有効状態 | `LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT`（「ブルーフラッグ」）・`LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT`（「イエローフラッグ」）・`LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT`（「フルコースイエロー」）・`LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT`（「レッドフラッグ」） | 自由文言TTSのみ。WAVは使わない |

LMU の補足（各項目の動作詳細は下の段落を参照）:

- 車両故障の文言は1つずつのグローバル文言で、保存先は `LmuWindowsVehicleDamagePreferences`。保存時は `trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)`（30文字）で正規化する。
- 車両クラス別の閾値（ブレーキ・ピットタイミング等）と `{percent}` / `{celsius}` の置換値の扱いは表の補足と下の段落を参照する。
- 空白文言・TTS利用不可では本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。収録WAVへのフォールバックは行わない。


LMU車両接近では、詳細ペインの `StartReadout` / `Sustained` と一覧の `Root` の有効状態を既存どおり判定する。開始時・継続時の自由文言TTSはともに `VehicleApproach.Root` をイベントキーに持つため、開始音・優先度・キュー設定も `Root` を参照する。開始時・継続時の文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。

LMUバーチャルエネルギー残量警告では、一覧の `RemainingVirtualEnergy.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残量閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残量は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `RemainingVirtualEnergy.Root` を参照する。詳細画面の試聴も現在の閾値に置換し、同じキーの開始音とOS標準TTSを使用する。残量警告の収録WAVは使用せず、WAVへはフォールバックしない。

LMU燃料残量警告は、一覧の `RemainingFuel.Root` と詳細の `WarningReadout` がともに有効な燃料クラスでのみ判定する。燃料クラスでは `{percent}` を閾値（%）に置換してOS標準TTSで読み上げ、`RemainingFuel.Root` の開始音・優先度・キューを使う。バーチャルエネルギークラスではVE警告のみを判定し、燃料警告は発生しない。逆に燃料クラスではVE警告を判定しない。警告は閾値を下回った瞬間に一度だけ発生し、閾値を上回ると再武装する。

LMUバーチャルエネルギー残量の現在値読み上げも既存の `RemainingVirtualEnergy.Root` と `WarningReadout` の有効状態を判定する。新しいキーや保存設定は追加しない。現在値イベントは実残量を四捨五入した整数%を持ち、既定文言 `LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT`（「バーチャルエネルギー残量は{percent}パーセント」）を使う。閾値警告用の自由文言は参照せず、閾値・警告済み状態も変更しない。開始音・優先度・キュー設定は警告と同じ `RemainingVirtualEnergy.Root` を参照する。

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

GT7タイヤ過熱警告は自由文言のOS標準TTSのみを使用する。既定文言は「タイヤ過熱 {celsius}度」。
`{celsius}` は判定時の全輪の最高タイヤ温度を整数に丸めた摂氏に置換し、解決文言をイベントの
`resolvedText` に保持するためキュー待機中も発話とログが一致する。判定条件・閾値・ヒステリシスは従来どおり。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。
WAVへのフォールバックは行わない。開始音・優先度・キュー設定は
`TyreTemperature.Root` を参照する。詳細画面の試聴は現在の高温閾値をサンプル温度として同じキーの開始音とTTSで再生する。

LMUのタイヤ過熱・低温警告、車両故障、自己ベストラップ、自由文字列イベントの共通処理は [`feature/lmu-windows-narrator/README.md`](../feature/lmu-windows-narrator/README.md) を参照。

LMUブレーキ過熱警告では一覧の `BrakeTemperature.Root` と詳細の `WarningReadout` の有効状態を判定する。イベントは判定時の車両クラス別温度閾値を持ち、全クラス共通文言の `{celsius}` をその閾値（℃）に置換する。実測温度は使用しない。既存の `LmuWindowsVehicleClassBrakeTemperaturePreferences` に文言を保存する。判定時に `resolvedText` を確定してログと発話を一致させる。空白・TTS利用不可では本文と開始音を要求せず空文字と `SKIPPED` を記録する。開始音・優先度・キューと試聴は `BrakeTemperature.Root` を参照する。詳細ペインの試聴は選択中クラスのスライダー閾値を使う。WAVへはフォールバックしない。

GT7の自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。既定文言は「自己ベストラップ更新 {laptime}」。
`{laptime}` は判定時の更新後の `bestLapTimeMs` を「1分23秒456」の形式に置換する。1分未満では分を省略し、
ミリ秒は3桁固定、60分以上も時間にせず分で表す。解決した文言は `resolvedText` に保持し、キュー待機中も発話とログを一致させる。
判定条件と `MyBestLap.Root` / `DetailEnabled` の有効状態は従来どおり。開始音・優先度・キューは `MyBestLap.Root` を参照する。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。
WAVへフォールバックしない。共有protoの `voiceType` は旧データの互換性のため残し、GT7は `readoutText`、LMUは `lmuWindowsReadoutText`、ACEは `aceWindowsReadoutText` を使用する。

LMUブレーキ摩耗警告では一覧の `BrakeWear.Root` と詳細の `WarningReadout` の有効状態を判定する。`LmuWindowsNarratorViewModel` は `ObserveLmuWindowsBrakeWearRemainingUseCase`（REST APIの `wearables.brakes` と車両クラスから、観測した最大の厚さを新品時とみなして残量%を計算）の値を `determineBrakeWearLow` で判定し、いずれかの輪が車両クラス別の残量閾値以下になった時点で1回だけ読み上げる（全輪が閾値を超えるまで再読み上げしない）。イベントは判定時の閾値（%）を持ち、全クラス共通文言の `{percent}` を置換する。実測の残量は使用しない。文言と閾値は `LmuWindowsVehicleClassBrakeWearPreferences` に保存する。判定時に `resolvedText` を確定してログと発話を一致させる。空白・TTS利用不可では本文と開始音を要求せず空文字と `SKIPPED` を記録する。開始音・優先度・キューと試聴は `BrakeWear.Root` を参照する。詳細ペインの試聴は選択中クラスのスライダー閾値を使う。WAVへはフォールバックしない。デスクトップ版はLMU REST APIから取得し、Android版はKoDriverサーバーの `/ws/lmu_windows/brake_wear` から受信して同じ残量判定・読み上げを行う。取得失敗時はJSON `null` が配信される。

自由文言TTSの本文と詳細画面の試聴は`SpeakTextUseCase`を通し、呼び出し時点の保存済み音声ID・読み上げ速度・声の高さを使う。音声IDを明示した試聴でも速度と声の高さは保存値を使用する。`TextToSpeechRepository.speak(text, queue, volume, voiceId, speed, pitch)`の`speed`・`pitch`はともに既定1.0、範囲0.5〜2.0で、Android・Windows実装で範囲外を制限する。開始音のWAVには速度・声の高さを適用しない。

## LMU の現在値読み上げホットキー

LMU Windows デバッグ状態詳細ペインで設定したホットキーで残りバーチャルエネルギーの現在値を読み上げる。
既定は `Ctrl + Alt + Shift + F1`（VK `0x70`）。修飾キーは固定で、末尾の F1〜F12・A〜Z・0〜9 を選択して保存する。
SimHub Control Mapper 側も、デバッグペインと同じ組み合わせを送るように設定する。
既存の `RemainingVirtualEnergy.Root` / `RemainingVirtualEnergy.WarningReadout` スイッチに従う。
この設定はデバッグ専用のテスト段階。登録結果（競合・エラーコード）と最大10秒の動作確認を表示する。データ未受信時は何もしない。
Android と非 Windows ではホットキー押下を通知しない。

## デバッグ読み上げの例外

デバッグ状態詳細ペインの要求は `ReadoutItemKey.topLevelEntriesOf` の項目を使用する。ログと優先度・キュー判定のキーは通常の Narrator と同じ TopLevel で、項目スイッチと determine 系の判定を意図的に通さず入力文字列を読み上げる。この経路は設定スイッチの配線確認には使わない。通常テレメトリ経路のスイッチ動作は従来どおり確認する。

## 手動読み上げセットのホットキー

セット詳細で保存した `ManualReadoutSet.hotkeyVirtualKeyCode` は、Windows の
`ManualReadoutHotkeyRepositoryImpl` が全シミュレーターの設定から観測して登録する。
押下は `ManualReadoutRequest(simulatorId, setId)` で配信し、LMU・GT7・ACE Narrator はタブからの依頼と
`merge` して同じ処理へ渡す。自分宛ての保存済みセットの項目と文言を参照し、手動読み上げの判定を使う。
セット詳細は同じ Repository の登録状態を観測し、競合・失敗時の再試行もセット識別子付きで委譲する。
Android は非対応実装を使い、キー未割り当て・非対応時は登録状態を表示しない。

### 共通ルール

- 対象は保存済みセットの項目のうち、シミュレーターごとの手動項目一覧（下表）に含まれるものだけとする。
- 最新値は受信から5秒以内（`CURRENT_VALUE_MAX_AGE_MS`）のものだけを使う。未受信・受信から5秒超・非有限・範囲外は「データ無し」として扱う。
- データ無しの項目は「<項目名>はデータがありません」を保存順に「、」で連結する。各項目を個別に判定し、全項目がデータ無しでも項目ごとの文を連結する（先頭 `MANUAL_READOUT_SET_ITEM_MAX` 件まで）。
- 本文は保存順に項目の文言を並べ、空欄（空白のみの文言）を除外して「、」で連結した1イベントにまとめる。全項目の文言が空欄なら空文字とする。
- 文言はセットの `itemTexts` を使い、未保存なら手動専用の既定文言（下表の定数名）を使う。プレースホルダーは既存の `format…ReadoutText` で置換する。
- 対応項目がない場合は「読み上げる項目がありません」（`MANUAL_READOUT_NO_ITEMS_TEXT`）を読む。
- 自動読み上げのスイッチ・しきい値・重複抑止・周回内タイミング・警告済み状態（`remainingFuelWarned` 等）は変更しない。手動側は自動のスイッチや判定に従わない。
- 開始音・ログ・優先度のキーは、LMU は先頭の対応項目から決まるキー、GT7 と ACE は `RemainingFuel.Root` 固定（LMU の詳細は下の補足）。
- 1イベントは常にキュー末尾へ追加し、同じ本文をログに保存する。

### LMU Windows（12項目）

キーは `LmuWindowsReadoutItemKey.` の接頭辞を省略し、`value` の値（`lmu_windows_` 接頭辞）を括弧内に示す。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| バーチャルエネルギー残量 | `RemainingVirtualEnergy.Root.value`（`lmu_windows_auto_remaining_virtual_energy`） | バーチャルエネルギーの残量比率（0〜1）を整数％ | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT`（「バーチャルエネルギー残量は{percent}パーセント」） | 未受信・鮮度切れ | ― |
| 燃料残量 | `RemainingFuel.Root.value`（`lmu_windows_auto_remaining_fuel`） | 受信値 `remainingPercent` を比率（0〜1）に変換し整数％ | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」） | 未受信・鮮度切れ | 自動燃料警告の `remainingFuelWarned` は変更しない。 |
| タイヤ摩耗 | `TyreWear.Root.value`（`lmu_windows_auto_tyre_wear`） | 4輪の最小残存率から求めた整数％ | `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT`（「タイヤ摩耗残り{percent}%」） | 未受信・鮮度切れ | 「タイヤ摩耗の残り周回数」とは別項目。 |
| ブレーキ残量 | `BrakeWear.Root.value`（`lmu_windows_auto_brake_wear`） | 4輪 `remainingPercent`（0〜100）の最小値を四捨五入した整数％ | `LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT`（「ブレーキ残量{percent}%」） | 未受信・鮮度切れ・空の `wheels` | 自動警告の前回値・警告済み状態は変更しない。 |
| ブレーキ温度 | `BrakeTemperature.Root.value`（`lmu_windows_auto_brake_temperature`） | `LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃ | `LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT`（「ブレーキ温度{celsius}℃」） | 未受信・鮮度切れ・空の `wheels` | 自動過熱警告の前回値・警告済み状態は変更しない。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`lmu_windows_auto_tyre_temperature`） | `LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃ | `LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度{celsius}℃」） | 未受信・鮮度切れ・空の `wheels` | 自動過熱・低温警告の前回値・警告済み状態は変更しない。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`lmu_windows_auto_my_best_lap`） | セッションの `timing.bestLapTimeMs`（ms）。「1分23秒456」形式に整形 | `LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下 | 自動の `MyBestLap.Root`・`DetailEnabled` スイッチとは独立。`determineMyBestLap`・`personalBestMs` の状態は変更しない。 |
| バーチャルエネルギーの残り周回数 | `PitTiming.VirtualEnergy.value`（`lmu_windows_auto_pit_timing_virtual_energy`） | 蓄積済みの `pitTimingVirtualEnergyTrackingState` から算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT`（「バーチャルエネルギーは残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT`（「バーチャルエネルギー残り1周未満」） | 蓄積前（`currentLap == -1`）、消費量が null・0以下、算出結果が負、最新エネルギーが未受信・鮮度切れ | 算出式は下の補足。自動のスイッチ・閾値・ベストラップ経過時間には依存しない。追跡状態・自動判定状態（`lastAnnouncedPitTimingVirtualEnergyLaps` 等）は変更しない。 |
| タイヤ摩耗の残り周回数 | `PitTiming.TyreWear.value`（`lmu_windows_auto_pit_timing_tyre_wear`） | 蓄積済みの `pitTimingTyreWearTrackingState` から同じ式で算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT`（「タイヤは残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT`（「タイヤ残り1周未満」） | 蓄積前、消費量が null・0以下・非有限、算出結果が負、最新タイヤ摩耗が未受信・鮮度切れ | 「タイヤ摩耗」(%) 項目とも、バーチャルエネルギーの残り周回数とも別項目。 |
| 燃料の残り周回数 | `RemainingFuel.Laps.value`（`lmu_windows_auto_remaining_fuel_laps`） | 蓄積済みの `fuelLapsTrackingState` から同じ式で算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT`（「燃料残り1周未満」） | 蓄積前、消費量が null・0以下・非有限・算出不能、最新燃料が未受信・鮮度切れ | 自動側の燃料判定は変更しない。 |
| クラス内前方車とのギャップ | `ClassCarGap.Ahead.value`（`lmu_windows_auto_class_car_gap_ahead`） | クラス内前方車との差（秒） | `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT`（「クラス内前方{seconds}秒」）。クラス内先頭は `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT`（「クラス内先頭です」） | ギャップが null・非有限・負 | 先頭キーは `VehicleApproach.Root`（下の補足）。先頭の空欄テンプレートは空文字。 |
| クラス内後方車とのギャップ | `ClassCarGap.Behind.value`（`lmu_windows_auto_class_car_gap_behind`） | クラス内後方車との差（秒） | `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_TEXT_DEFAULT`（「クラス内後方{seconds}秒」）。クラス内最後尾は `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT`（「クラス内最後尾です」） | ギャップが null・非有限・負 | 先頭キーは `VehicleApproach.Root`（下の補足）。最後尾の空欄テンプレートは空文字。 |

LMU の補足は次のとおり。

- 残り周回数（バーチャルエネルギー・タイヤ摩耗・燃料）は、現在値（`currentValue`）を `lastValidLapConsumption` で割り、整数へ切り捨てる。1,000周（`LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX`）を超える算出結果はデータ無しとする。ただし丸め誤差の範囲（1e-9 以内）の超過だけは1,000周に丸める。
- 0周の固定文言は表の定数を読む。空欄テンプレートなら0周でも空文字にする。1周以上は `formatLmuWindowsPitTimingReadoutText` で `{laps}` を置換する。
- 開始音・ログ・優先度のキーは先頭の対応項目から決まる。`PitTiming.VirtualEnergy`・`PitTiming.TyreWear` は `PitTiming.Root`、`RemainingFuel.Laps` は `RemainingFuel.Root`、`ClassCarGap.Ahead`・`ClassCarGap.Behind` は `VehicleApproach.Root` とする。それ以外は先頭項目自身の Root を使い、対応項目がない場合は `RemainingVirtualEnergy.Root` を使う。
- 比率から整数％への四捨五入は `determineManualReadout` に集約し、既存の `PERCENTAGE_SCALE` を使う。

### GT7 PS5（4項目）

キーは `Gt7Ps5ReadoutItemKey.` の接頭辞を省略し、`value` の値（`gt7_ps5_` 接頭辞）を括弧内に示す。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| 残り燃料 | `RemainingFuel.Root.value`（`gt7_ps5_auto_remaining_fuel`） | 最新テレメトリの `gasLevel / gasCapacity * 100` を四捨五入し0〜100に制限した整数％ | `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」） | 未受信・鮮度切れ・不正な燃料値は「残り燃料はデータがありません」 | 既存の燃料用フォーマット関数を再利用する。開始音・ログ・常時キュー追加のキーもこの項目。 |
| 残り燃料の周回数 | `RemainingFuelLaps.Root.value`（`gt7_ps5_auto_remaining_fuel_laps`） | 残り燃料の推定周回数（周）。算出式は下の補足 | `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周以下は `GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT`（「燃料残り1周未満」） | 未受信・鮮度切れは「残り燃料の周回数はデータがありません」。開始燃料・開始周回が未設定、完走周回数が0以下、消費燃料が0以下は null | 自動側のしきい値・重複抑止・有効スイッチは適用しない。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`gt7_ps5_auto_tyre_temperature`） | 4輪の有限値の最高温度を `roundToInt()` した整数℃（`currentMaxTyreTemperatureCelsius`） | `GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度{celsius}℃」） | 未受信・鮮度切れ・全輪が非有限または0（取得不能）は「タイヤ温度はデータがありません」 | 詳細画面のカテゴリはタイヤ、プレビューは90℃。自動のしきい値・ヒステリシス・スイッチ判定は適用しない。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`gt7_ps5_auto_my_best_lap`） | テレメトリの `bestLapTimeMs`（ms）（`currentBestLapTimeMs`）。既存の `Gt7Ps5MyBestLap` で整形 | `GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下（未確定）は「自己ベストラップはデータがありません」 | 詳細画面のカテゴリはラップ、プレビューは83456ms。自動の `determineMyBestLap` の状態（`personalBestMs` 等）は変更しない。 |

GT7 PS5 の補足は次のとおり。

- 対応項目は保存順に先頭 `MANUAL_READOUT_SET_ITEM_MAX`（3件）まで処理する。
- 残り燃料の周回数は、開始燃料＋総給油量－現在燃料を、完走周回数＋`CURRENT_LAP_CONSUMPTION_WEIGHT`（0.9）で割った平均消費を使い、現在燃料÷平均消費を整数へ切り捨てる。最終受信から5秒以内だけ推定値を使う。テレメトリごとに `determineRemainingFuelLaps` が呼ばれ、有効スイッチの判定より前に燃料追跡が更新されるため、自動読み上げが無効でも手動用の追跡が続く。
- 空欄テンプレートなら、0周以下やデータ無しでも読まない。
- GT7 と LMU のキーはそれぞれ `gt7_ps5_`・`lmu_windows_` 接頭辞を持ち衝突しない。

### ACE Windows（4項目）

キーは `AceWindowsReadoutItemKey.` の接頭辞を省略し、`value` の値（`ace_windows_` 接頭辞）を括弧内に示す。手動項目の順は「残り燃料」「残り燃料の周回数」「自己ベストラップ」「タイヤ温度」。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| 残り燃料 | `RemainingFuel.Root.value`（`ace_windows_auto_remaining_fuel`） | `AceWindowsFuelData.remainingPercent`（0〜100）を四捨五入した整数％（`currentRemainingFuelPercent`） | `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」） | 未受信・鮮度切れ・非有限・0未満・100超は「残り燃料はデータがありません」 | 既存の `formatAceWindowsRemainingFuelReadoutText` で置換する。`remainingFuelWarned` 等の判定状態は変更しない。 |
| 残り燃料の周回数 | `RemainingFuelLaps.Root.value`（`ace_windows_auto_remaining_fuel_laps`） | `AceWindowsRemainingFuelLapsData.remainingLaps` の整数部（周）（`currentRemainingFuelLaps`） | `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」） | 未受信・鮮度切れ・非有限・負値・1,000周（`ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX`）超は「残り燃料の周回数はデータがありません」 | ちょうど0と1周未満の正数は整数部へ切り捨てて0周を返す。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`ace_windows_auto_my_best_lap`） | 最新ベストラップ（ms）。5秒以内かつ正値のときだけ渡す | `ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下はデータ無し | 既定文言を `formatAceWindowsMyBestLapReadoutText` で整形する。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`ace_windows_auto_tyre_temperature`） | `AceWindowsTyreCarcassTemperatureData.wheels` の有限かつ0より大きい最高値を四捨五入した整数℃（`currentMaxTyreTemperatureCelsius`） | `ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度{celsius}℃」） | 未受信・鮮度切れ・有効な温度なし・ACE以外への切り替え後は「タイヤ温度はデータがありません」 | 既存の `formatAceWindowsTyreTemperatureReadoutText` と `findUnknownAceWindowsTyreTemperatureReadoutPlaceholders` を使う。詳細画面のカテゴリはタイヤ、プレビューは90℃。自動過熱警告のしきい値・ヒステリシス・スイッチ判定は適用せず、警告状態も変更しない。 |

ACE Windows の補足は次のとおり。

- 対応項目は保存順に先頭 `MANUAL_READOUT_SET_ITEM_MAX`（3件）まで処理する。
- 手動値は鮮度判定済みの `AceWindowsManualReadoutItem(key, textTemplate, value)` で受け取る。シミュレーター切替時は最新値と受信時刻をクリアする。
- 全項目の文言が空白なら空文字とする。
- 一覧は既存の `AceWindowsReadoutItemKey.RemainingFuel.Root` を含むため、手動の「残り燃料」を選択できる。開始音・ログ・常時キュー末尾追加には既存の `RemainingFuel.Root` を使う。

# ReadoutItemKey の配置と登録

手動の「燃料の残り周回数」は `LmuWindowsReadoutItemKey.RemainingFuel.Laps.value`（`lmu_windows_auto_remaining_fuel_laps`）に対応する。
`RemainingFuel` 配下の手動専用項目で、TopLevel・自動タブ・スイッチには追加しない。手動項目定義の末尾（タイヤ摩耗の残り周回数の後）に並ぶ。
`fuelLapsTrackingState` の蓄積値を `manualFuelRemainingLaps` で読み、1周以上は `formatLmuWindowsPitTimingReadoutText` で `{laps}` を置換する。
既定文言は `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）、0周は
`LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT`（「燃料残り1周未満」）。空欄テンプレートなら0周でも空文字にする。
蓄積前・消費量が null／0以下／非有限・算出不能・最新燃料が未受信または受信から5秒超の場合はデータ無しとする。
鮮度は `latestFuelReceivedAtMs` で判定し、ピットタイミングの鮮度には依存しない。
複数項目では「燃料の残り周回数はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `RemainingFuel.Root` を使う。編集画面はACE・GT7の燃料項目と同じ「燃料」カテゴリに配置し、既存の
`LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER` を挿入・検証に使う。項目一覧・ホットキーは共通の手動項目定義を参照する。
手動の燃料読み上げは自動警告の有効状態・評価履歴を変更しない。


LMUピットタイミングの燃料は `LmuWindowsReadoutItemKey.PitTiming.Fuel`（`lmu_windows_auto_pit_timing_fuel`）で保存する。
詳細のスイッチ・閾値（既定3周、1〜5周）・通常／切迫時文言は既存の PitTiming DataStore を共有し、
ProtoNumber 8〜10を追加する。既存フィールド1〜7は維持し、旧データでは燃料の既定値を使う。
`ObserveLmuWindowsPitTimingEnabledStatesUseCase` の既定値は有効で、Narratorのマージ済み有効状態と
`determinePitTimingFuel` が `PitTiming.Root`／`PitTiming.Fuel` を判定する。
燃料警告は `energySource == LmuWindowsEnergySource.Fuel`（P2/P2Elms/P3/GTE/Unknown）でのみ発火し、
VEクラス・無効設定でも燃料消費の追跡と評価周の更新を続ける。詳細の燃料設定はクラスに関わらず表示し、listPaneは変更しない。
`pitTimingJob` が `exactPercent / 100.0` を一度だけ追跡し、手動の燃料残り周回数と `fuelLapsTrackingState` を共有する。
VE・タイヤ・燃料の候補の最小周回数を選び、共通の `PitTimingLapGate` を通った候補だけ読み上げ履歴へ記録する。
給油周を消費量の推定から除外し、給油で警告履歴を再武装、周回巻き戻りで追跡・評価履歴を初期化する。
通常の既定文言は「燃料は残り約{laps}周」、0周以下は「燃料残り1周未満」。保存文言の解決・開始音・優先度・キュー・ログは
既存の `PitTiming.Root` の流儀を使い、ログに燃料スナップショットと判定状態を含める。

手動の「クラス内前方車／後方車とのギャップ」は `LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value`（`lmu_windows_auto_class_car_gap_ahead`）・`ClassCarGap.Behind.value`（`lmu_windows_auto_class_car_gap_behind`）に対応する。
TopLevel・自動タブ・スイッチ・`LmuWindowsReadoutDefaults.kt` の列挙には追加しない手動専用項目で、`LMU_WINDOWS_READOUT_ITEM_KEYS` の末尾（`MyBestLap.DetailEnabled` の後）と手動項目定義の末尾（燃料の残り周回数の後）に並ぶ。
`ObserveLmuWindowsClassCarGapUseCase` の最新値（`LmuWindowsClassCarGapData`）を `latestClassCarGap` / `latestClassCarGapReceivedAtMs` に保持し、受信から5秒（`CURRENT_VALUE_MAX_AGE_MS`）以内のものだけを項目へ渡す。未受信・鮮度切れはデータ無しとする。
周回遅れ・周回先行の車両も前後の対象に含む。トップ・最後尾は `gapAheadSeconds` / `gapBehindSeconds` ではなく `classPosition == 1` / `classPosition == classVehicleCount`（両方非 null）で判定し、固定文言
`LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT`（「クラス内先頭です」）・`LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT`（「クラス内最後尾です」）を読む（データありとして扱う。空欄テンプレートなら空文字）。
それ以外は null・非有限・負値の秒差をデータ無しとし、0以上の有限値を `formatLmuWindowsClassCarGapReadoutText` で小数1桁に丸めて `{seconds}`（`LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER`）を置換する。
既定文言は `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT`（「クラス内前方{seconds}秒」）・`..._BEHIND_TEXT_DEFAULT`（「クラス内後方{seconds}秒」）。
複数項目ではデータ無しの項目に「<項目名>はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `VehicleApproach.Root` を使う。編集画面は既存の「ラップ」カテゴリに配置し、`{seconds}` を挿入・検証に使う。
