# ReadoutItemKey の配線（listPane / detailPane と Narrator の読み上げ判定の一致）

`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目を Narrator で読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`ReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `DetermineLmuWindowsNarratorReadoutUseCase`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態が Narrator に未反映だった
- #472: listPane の `VehicleDamage` スイッチが `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage` から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する `Determine*NarratorReadoutUseCase` のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

ACEの全10フラッグ（Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes）は保存した自由文言をOS標準TTSのみで読み上げ、WAVは使用しない。既定文言はドメイン定数を参照し、OrangeCircleは「オレンジボールフラッグ、車両に不具合があります」、RedYellowStripesは「レッド・イエローストライプフラッグ、路面が滑りやすいです」。保存先は `ace_windows_flag_readout_text_preferences.pb`。既存の `ace_windows_flag_preferences.pb`（enabledStates）は変更せず、詳細の個別キー（`ReadoutItemKey.AceWindows.Flag.WhiteFlag` 等）と一覧の `Flag.Root` の有効状態を従来どおり判定する。空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。全10種の `SpeechEvent.readoutItemKey` は `Flag.Root` のため、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Flag.Root` を参照する。試聴は空白・TTS利用不可・音量0以下では再生しない。WAVフォールバックは行わず、フラッグのWAVはすべて廃止する。

ACE車両接近は保存した固定の自由文言をOS標準TTSで読み上げる。保存先は既存の `ace_windows_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）、既定文言はドメイン定数の「車両接近」。一覧の `ReadoutItemKey.AceWindows.VehicleApproach.Root` と詳細の `StartReadout` の有効状態は従来どおり判定する。`SpeechEvent.AceWindowsVehicleApproach` は `VehicleApproach.Root` をキーに持ち、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Root` を参照する。Narratorで本文を都度解決し、空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。試聴は空白・TTS利用不可・音量0以下では再生しない。プレースホルダーはなく、文言は固定文字列として扱う。車両接近WAVは廃止し、WAVフォールバックは行わない。

ACEタイヤ過熱は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`）、既定文言は「タイヤ過熱 {celsius}度」。`{celsius}` は判定時点の全輪最大カーカス温度を `roundToInt` で四捨五入した整数に置換する。過熱・解除判定と `TyreTemperature.Root` / `OverheatWarning` の有効判定は従来どおりで、開始音・優先度・キューは `ReadoutItemKey.AceWindows.TyreTemperature.Root` を使う。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。タイヤ過熱WAVを廃止し、WAVフォールバックは行わない。詳細画面では過熱文言の編集・既定値へのリセット・`{celsius}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の高温閾値をサンプル温度に使い、`ReadoutItemKey.AceWindows.TyreTemperature.Root` の開始音とTTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では試聴の本文も開始音も再生しない。

ACE燃料残量警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`）、既定文言は「燃料は残り{percent}パーセント」。`{percent}` は判定時の実残量を四捨五入した整数（0〜100）に置換する。閾値判定と `RemainingFuel.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `ReadoutItemKey.AceWindows.RemainingFuel.Root` を維持する。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中に設定が変わってもログと発話を一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに空文字と `SKIPPED` を記録する。`remaining_fuel_caution.wav` を廃止し、WAVフォールバックは行わない。詳細画面では自由文言の編集・既定値へのリセット・`{percent}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の残量閾値を使い、`RemainingFuel.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

LMU車両接近では、詳細ペインの `StartReadout` / `Sustained` と一覧の `Root` の有効状態を既存どおり判定する。開始時・継続時の自由文言TTSはともに `VehicleApproach.Root` をイベントキーに持つため、開始音・優先度・キュー設定も `Root` を参照する。開始時・継続時の文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。

LMUバーチャルエナジー残量警告では、一覧の `RemainingVirtualEnergy.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残量閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残量は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `RemainingVirtualEnergy.Root` を参照する。詳細画面の試聴も現在の閾値に置換し、同じキーの開始音とOS標準TTSを使用する。残量警告の収録WAVは使用せず、WAVへはフォールバックしない。

LMUタイヤ摩耗警告では、一覧の `TyreWear.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残存率閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残存率は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `TyreWear.Root` を参照する。詳細画面の試聴も現在の閾値と編集中の文言を解決したイベントを再生し、同じキーの開始音とOS標準TTSを使用する。摩耗警告の収録WAVは使用せず、WAVへはフォールバックしない。

GT7燃料残量警告では、一覧の `RemainingFuel.Root` と詳細の `DetailEnabled` の有効状態を既存どおり判定する。
自由文言の `{percent}` は `gasLevel / gasCapacity * 100` を `roundToInt()` で四捨五入し、0..100 に収めた実際の残量（整数%）に置換する。
LMUバーチャルエナジー残量の閾値置換とは異なり、GT7では設定閾値を置換値に使用しない。
文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。
開始音・優先度・キュー設定は `RemainingFuel.Root` を参照し、OS標準TTSのみを使用してWAVへフォールバックしない。
判定時に解決した文言をイベントに保持し、キュー待機中の設定変更によってログと発話がずれないようにする。
詳細画面で自由文言を設定でき、試聴では現在の閾値をサンプル残量として `{percent}` に置換する。`RemainingFuel.Root` キーの開始音とOS標準TTSを使用し、空白文言・TTS利用不可・音量0以下では本文も開始音も再生しない。

LMUピットタイミングでは、一覧の `Root` と詳細の `VirtualEnergy` / `TyreWear` の有効状態を既存どおり判定する。両ソースの警告は自由文言TTSで読み上げ、残り1周以上は通常文言の `{laps}` を予想残り周回数で置換し、0周以下は専用の切迫時文言を使用する。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `PitTiming.Root` を参照する。詳細画面で通常・切迫時文言を設定でき、試聴も `PitTiming.Root` キーの開始音と自由文言TTSを使用する（通常文言は `{laps}` を5周に置換、切迫時は置換なし）。両ソースとも自由文言TTSを使用し、ピットタイミング用の収録WAVは使用しない。

GT7タイヤ過熱警告は自由文言のOS標準TTSのみを使用する。既定文言は「タイヤ過熱 {celsius}度」。
`{celsius}` は判定時の全輪の最高タイヤ温度を整数に丸めた摂氏に置換し、解決文言をイベントの
`resolvedText` に保持するためキュー待機中も発話とログが一致する。判定条件・閾値・ヒステリシスは従来どおり。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。
`tyre_overheat.wav` は使用せず、WAVへのフォールバックは行わない。開始音・優先度・キュー設定は
`TyreTemperature.Root` を参照する。詳細画面の試聴は現在の高温閾値をサンプル温度として同じキーの開始音とTTSで再生する。

LMUタイヤ過熱・低温警告は保存した自由文言の `{celsius}` を、警告を発生させた判定時点の全輪の最高カーカス温度を四捨五入した整数（℃）に置換してOS標準TTSで読み上げる。低温警告も全輪の最高温度を使い、`{wheel}` は対応しない。判定時に解決した本文をイベントの `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。既定文言は「タイヤ過熱 {celsius}度」「タイヤ低温 {celsius}度」。DataStore の保存済み文言は変更せず、未設定の場合と「デフォルトに戻す」操作で新しい既定文言を使用する。DataStore の移行は不要。詳細画面は挿入チップと未知プレースホルダーの警告を提供し、試聴では過熱は選択中車両クラスの高温閾値（スライダーの現在値）、低温は60℃固定に置換する。空白文言・TTS利用不可時は開始音も要求せず、ログに空文字と `SKIPPED` を記録する。収録WAVへのフォールバックは行わない。開始音・優先度・キュー設定と試聴の開始音は `TyreTemperature.Root` を参照する。

LMU車両故障のオーバーヒート・部品脱落・タイヤ脱落は、各1つのグローバルな自由文言をOS標準TTSで読み上げる。既定文言は「オーバーヒート」「部品脱落」「タイヤ脱落」。文言は既存の `LmuWindowsVehicleDamagePreferences` に保存し、保存時は `trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)` で正規化する。判定時に解決した本文をイベントの `resolvedText` に保持し、キュー待機中の設定変更でもログと発話を一致させる。空白文言・TTS利用不可時は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。一覧の `VehicleDamage.Root` と詳細の `Overheat` / `PartDetached` / `TyreDetached` のスイッチ配線は維持し、開始音・優先度・キューと試聴は `VehicleDamage.Root` を参照する。旧オーバーヒート音声タイプは廃止し、3種類の警告は収録WAVへフォールバックしない。

LMUブレーキ過熱警告では一覧の `BrakeTemperature.Root` と詳細の `WarningReadout` の有効状態を判定する。イベントは判定時の車両クラス別温度閾値を持ち、全クラス共通文言の `{celsius}` をその閾値（℃）に置換する。実測温度は使用しない。既存の `LmuWindowsVehicleClassBrakeTemperaturePreferences` に文言を保存する。判定時に `resolvedText` を確定してログと発話を一致させる。空白・TTS利用不可では本文と開始音を要求せず空文字と `SKIPPED` を記録する。開始音・優先度・キューと試聴は `BrakeTemperature.Root` を参照する。詳細ペインの試聴は選択中クラスのスライダー閾値を使う。WAVへはフォールバックしない。

GT7の自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。既定文言は「自己ベストラップ更新 {laptime}」。
`{laptime}` は判定時の更新後の `bestLapTimeMs` を「1分23秒456」の形式に置換する。1分未満では分を省略し、
ミリ秒は3桁固定、60分以上も時間にせず分で表す。解決した文言は `resolvedText` に保持し、キュー待機中も発話とログを一致させる。
判定条件と `MyBestLap.Root` / `DetailEnabled` の有効状態は従来どおり。開始音・優先度・キューは `MyBestLap.Root` を参照する。
空白文言・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` を記録する。formal/casual の収録WAVは削除し、
WAVへフォールバックしない。共有protoの `voiceType` はACEと旧データの互換性のため残し、GT7は `readoutText`、LMUは `lmuWindowsReadoutText` を使用する。

LMU自己ベストラップ更新は自由文言のOS標準TTSを使う。既定文言は「自己ベストラップ更新 {laptime}」。`{laptime}` は更新後の `telemetry.timing.bestLapTimeMs`（Long）を「1分23秒456」形式に置換し、1分未満では分を省略、ミリ秒は3桁固定、60分以上も分で表す。判定時に本文を `resolvedText` へ保持して発話とログを一致させる。空白・TTS利用不可では開始音も本文も要求せず、ログに空文字と `SKIPPED` を記録する。更新判定と一覧の `MyBestLap.Root`・詳細の `MyBestLap.DetailEnabled` のスイッチ配線は維持し、開始音・優先度・キュー・試聴は `MyBestLap.Root` を使う。詳細画面で文言編集・`{laptime}` 挿入・未知プレースホルダー警告・リセット・サンプルタイム83456msでの試聴を提供する。LMUの口調設定は移行せず参照しない。自己ベスト用WAVは廃止し、WAVへのフォールバックは行わない。

LMUの全自由文字列イベント（フラッグ・車両接近・ピットタイミングを含む18種類）は `FreeTextSpeechEvent` を実装する。
`LmuWindowsNarratorEventProcessor` の共通 `processEvents` が判定時の本文を `withResolvedText` で保持し、同じ本文をログに保存する。
自己ベストラップの処理名は `processMyBestLap`。空白本文は共通処理でスキップする。
`LmuWindowsReadoutTextSpeaker` は解決済み本文を優先し、未解決の場合だけ設定を参照する。
`LmuWindowsNarratorModule` は `isCustomSpeakEvent = { it is FreeTextSpeechEvent }` で対象を判定する。
ピットタイミングの周回ゲートとソース選択は本文ではなく `laps` を使い続ける。
