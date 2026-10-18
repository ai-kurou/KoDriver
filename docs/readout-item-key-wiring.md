# ReadoutItemKey の配線（listPane / detailPane と Narrator の読み上げ判定の一致）


自動読み上げの保存キーとシミュレーター別DataStoreファイル名は、`lmu_windows_` / `ace_windows_` / `gt7_ps5_` の直後に `auto_` を置く。共通の一覧設定は Desktop では `auto_readout_preferences.pb`、Android では `auto_readout_preferences.preferences_pb` に保存する。`auto_` 導入時はユーザー了承のもと設定を意図的に初期化し、設定の旧値・旧ファイルの移行は行わない。テレメトリログは Room の 4→5 マイグレーションで旧キーを新キーへ変換して保持する。開始音・ホットキーなど自動読み上げ以外の設定ファイル名は変更しない。
`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目を Narrator で読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`AutoReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `DetermineLmuWindowsNarratorReadoutUseCase`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態が Narrator に未反映だった
- #472: listPane の `VehicleDamage` スイッチが `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage` から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する `Determine*NarratorReadoutUseCase` のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

ACEの全10フラッグ（Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes）は保存した自由文言をOS標準TTSのみで読み上げ、WAVは使用しない。既定文言はドメイン定数を参照し、OrangeCircleは「オレンジボールフラッグ、車両に不具合があります」、RedYellowStripesは「レッド・イエローストライプフラッグ、路面が滑りやすいです」。保存先は `ace_windows_auto_flag_readout_text_preferences.pb`。既存の `ace_windows_auto_flag_preferences.pb`（enabledStates）は変更せず、詳細の個別キー（`AceWindowsReadoutItemKey.Flag.WhiteFlag` 等）と一覧の `Flag.Root` の有効状態を従来どおり判定する。空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。全10種の `SpeechEvent.readoutItemKey` は `Flag.Root` のため、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Flag.Root` を参照する。試聴は空白・TTS利用不可・音量0以下では再生しない。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACE車両接近は保存した固定の自由文言をOS標準TTSで読み上げる。保存先は既存の `ace_windows_auto_vehicle_approach_preferences.pb` のフィールド5（`readoutText`）、既定文言はドメイン定数の「車両接近」。一覧の `AceWindowsReadoutItemKey.VehicleApproach.Root` と詳細の `StartReadout` の有効状態は従来どおり判定する。`AceWindowsVehicleApproach` は `VehicleApproach.Root` をキーに持ち、開始音・優先度・キュー・詳細画面のTTS試聴の開始音も `Root` を参照する。Narratorで本文を都度解決し、空白文言またはTTS利用不可の場合は本文と開始音を要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。試聴は空白・TTS利用不可・音量0以下では再生しない。プレースホルダーはなく、文言は固定文字列として扱う。WAVフォールバックは行わない。
判定時に解決した本文を `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。

ACEタイヤ過熱は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsTyreTemperaturePreferences` のフィールド3（`overheatReadoutText`）、既定文言は「タイヤ過熱 {celsius}度」。`{celsius}` は判定時点の全輪最大カーカス温度を `roundToInt` で四捨五入した整数に置換する。過熱・解除判定と `TyreTemperature.Root` / `OverheatWarning` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.TyreTemperature.Root` を使う。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに `narratedText` 空文字と `narrationOutcome=SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では過熱文言の編集・既定値へのリセット・`{celsius}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の高温閾値をサンプル温度に使い、`AceWindowsReadoutItemKey.TyreTemperature.Root` の開始音とTTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では試聴の本文も開始音も再生しない。

ACE燃料残量警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`）、既定文言は「燃料は残り{percent}パーセント」。`{percent}` は判定時の実残量を四捨五入した整数（0〜100）に置換する。閾値判定と `RemainingFuel.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuel.Root` を維持する。Processorで本文を解決してイベントの `resolvedText` に保持し、キュー待機中に設定が変わってもログと発話を一致させる。空白文言・TTS利用不可では本文も開始音も要求せず、ログに空文字と `SKIPPED` を記録する。WAVフォールバックは行わない。詳細画面では自由文言の編集・既定値へのリセット・`{percent}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の残量閾値を使い、`RemainingFuel.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE燃料残り周回数警告は保存した自由文言をOS標準TTSで読み上げる。保存先は `AceWindowsRemainingFuelLapsPreferences` のフィールド2（`readoutText`）とフィールド3（`emptyReadoutText`）。通常の既定文言は「燃料は残り約{laps}周」で、`{laps}` を判定時の整数周回数に置換する。0周以下は0周用文言（既定「燃料残り1周未満」）をそのまま使う。旧データは閾値を維持して既定文言を使用する。閾値・有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.RemainingFuelLaps.Root` を維持する。Processorで確定した文言を `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。WAVへフォールバックしない。詳細画面では通常文言・0周以下用文言の自由文言編集と既定値へのリセットを提供し、通常文言には `{laps}` の末尾挿入と未知プレースホルダーの警告を表示する。試聴は通常文言を画面に表示中の閾値周回数で置換し、0周以下用文言はそのまま使い、`RemainingFuelLaps.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

ACE自己ベストラップ更新は保存した自由文言をOS標準TTSで読み上げる。保存先は共有 `MyBestLapPreferences` のACE専用 `@ProtoNumber(4) aceWindowsReadoutText`、既定文言は「自己ベストラップ更新 {laptime}」。`{laptime}` は更新後の `bestLapTimeMs`（Int）を「1分23秒456」形式に置換し、1分未満では分を省略、ミリ秒は3桁固定、60分以上も分で表す。更新判定と `MyBestLap.Root` / `DetailEnabled` の有効判定は従来どおりで、開始音・優先度・キューは `AceWindowsReadoutItemKey.MyBestLap.Root` を使う。Processorで確定した文言を `AceWindowsMyBestLap` の `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。空白文言・TTS利用不可・文言解決失敗では本文も開始音も要求せず、空文字と `SKIPPED` をログに保存する。詳細画面で文言編集・`{laptime}` 挿入・未知プレースホルダー警告・リセット・サンプルタイム83456msでの試聴を提供する。旧口調設定（`voiceType`）は読み取らず、既存ユーザーは既定文言になる。WAVへのフォールバックは行わない。

LMU車両接近では、詳細ペインの `StartReadout` / `Sustained` と一覧の `Root` の有効状態を既存どおり判定する。開始時・継続時の自由文言TTSはともに `VehicleApproach.Root` をイベントキーに持つため、開始音・優先度・キュー設定も `Root` を参照する。開始時・継続時の文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。

LMUバーチャルエネルギー残量警告では、一覧の `RemainingVirtualEnergy.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残量閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残量は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `RemainingVirtualEnergy.Root` を参照する。詳細画面の試聴も現在の閾値に置換し、同じキーの開始音とOS標準TTSを使用する。残量警告の収録WAVは使用せず、WAVへはフォールバックしない。

LMUバーチャルエネルギー残量の現在値読み上げも既存の `RemainingVirtualEnergy.Root` と `WarningReadout` の有効状態を判定する。新しいキーや保存設定は追加しない。現在値イベントは実残量を四捨五入した整数%を持ち、固定文言「バーチャルエネルギー残量は{N}パーセント」を使う。閾値警告用の自由文言は参照せず、閾値・警告済み状態も変更しない。開始音・優先度・キュー設定は警告と同じ `RemainingVirtualEnergy.Root` を参照する。

LMUタイヤ摩耗警告では、一覧の `TyreWear.Root` と詳細の `WarningReadout` の有効状態を判定する。警告イベントは設定した残存率閾値を持ち、自由文言の `{percent}` をその閾値（%）に置換する。実際の残存率は置換値に使用しない。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `TyreWear.Root` を参照する。詳細画面の試聴も現在の閾値と編集中の文言を解決したイベントを再生し、同じキーの開始音とOS標準TTSを使用する。摩耗警告の収録WAVは使用せず、WAVへはフォールバックしない。

GT7燃料残量警告では、一覧の `RemainingFuel.Root` と詳細の `DetailEnabled` の有効状態を既存どおり判定する。
自由文言の `{percent}` は `gasLevel / gasCapacity * 100` を `roundToInt()` で四捨五入し、0..100 に収めた実際の残量（整数%）に置換する。
LMUバーチャルエネルギー残量の閾値置換とは異なり、GT7では設定閾値を置換値に使用しない。
文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。
開始音・優先度・キュー設定は `RemainingFuel.Root` を参照し、OS標準TTSのみを使用してWAVへフォールバックしない。
判定時に解決した文言をイベントに保持し、キュー待機中の設定変更によってログと発話がずれないようにする。
詳細画面で自由文言を設定でき、試聴では現在の閾値をサンプル残量として `{percent}` に置換する。`RemainingFuel.Root` キーの開始音とOS標準TTSを使用し、空白文言・TTS利用不可・音量0以下では本文も開始音も再生しない。

LMUピットタイミングでは、一覧の `Root` と詳細の `VirtualEnergy` / `TyreWear` の有効状態を既存どおり判定する。両ソースの警告は自由文言TTSで読み上げ、残り1周以上は通常文言の `{laps}` を予想残り周回数で置換し、0周以下は専用の切迫時文言を使用する。文言が空白またはTTS利用不可の場合は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。開始音・優先度・キュー設定は `PitTiming.Root` を参照する。詳細画面で通常・切迫時文言を設定でき、試聴も `PitTiming.Root` キーの開始音と自由文言TTSを使用する（通常文言は `{laps}` を各スライダーに表示中の周回数に置換、切迫時は置換なし）。両ソースとも自由文言TTSを使用し、ピットタイミング用の収録WAVは使用しない。

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

手動 LMU セットは `RemainingVirtualEnergy.Root.value`・`TyreWear.Root.value`・`BrakeWear.Root.value`・`BrakeTemperature.Root.value`・`TyreTemperature.Root.value`・`MyBestLap.Root.value`・`PitTiming.VirtualEnergy.value`・`PitTiming.TyreWear.value` に対応する。保存した項目順に文を「、」で連結した1イベントとして読み、自動スイッチを無視し、先頭の対応項目の Root を `readoutItemKey` にしてそのキュー設定を true に上書きする。文言はセットの `itemTexts`（未保存なら手動専用の既定文言）を使う。比率から整数%への四捨五入は `determineManualReadout` に集約し、既存の `PERCENTAGE_SCALE` を使う。タイヤ摩耗の `{percent}` は4輪の最小残存率から求めた整数で、既定文言は「タイヤ摩耗残り{percent}%」。未受信・受信から5秒超・空の wheels は「<項目名>はデータがありません」を連結する。全項目がデータ無しなら、1項目の場合も含め「データがありません」のみを読む。項目名は domain の `manualReadoutItemName` で解決する。いずれの対応項目もない場合のみ項目なし文言を読む。自動警告の設定・状態と従来ホットキーの挙動は変えない。

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。

手動タイヤ温度は `TyreTemperature.Root` に対応し、`LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃を、既定文言「タイヤ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱・低温警告の前回値・警告済み状態は変更しない。

手動自己ベストラップは `LmuWindowsReadoutItemKey.MyBestLap.Root.value` に対応し、セッションの `timing.bestLapTimeMs` を読む。既定文言は `LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）。既存の `formatLmuWindowsMyBestLapReadoutText` で `{laptime}` を「1分23秒456」形式に置換する。未受信・最後の受信から `CURRENT_VALUE_MAX_AGE_MS`（5秒）超・0以下はデータ無しとし、複数項目では「自己ベストラップはデータがありません」を連結、全項目がデータ無しなら「データがありません」のみ。自動の `MyBestLap.Root` / `DetailEnabled` スイッチとは独立し、`determineMyBestLap`・`personalBestMs` の自動更新判定の状態は変更しない。保存順に連結した1イベントを常にキュー末尾へ追加し、開始音・優先度・ログには先頭項目のキーを使う。

手動エネルギー残り周回数は `LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value` に対応し、蓄積済みの `pitTimingVirtualEnergyTrackingState` から `(currentValue / lastValidLapConsumption).toInt()` で算出する。既定文言は `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT`（「エネルギーは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「エネルギー残り1周未満」を読む。蓄積前（`currentLap == -1`）、消費量が null・0以下、算出結果が負、最新エネルギーが未受信・受信から5秒超の場合はデータ無しとし、複数項目では「エネルギーの残り周回数はデータがありません」を連結、全項目がデータ無しなら「データがありません」のみ。自動のスイッチ・閾値・ベストラップ経過時間には依存せず、追跡状態・自動判定状態（`lastAnnouncedPitTimingVirtualEnergyLaps` 等）は変更しない。保存順の1イベントを常にキュー末尾へ追加し、先頭がこの項目なら開始音・優先度・ログには `PitTiming.Root` を使う。タイヤ摩耗の残り周回数は別項目として扱う。

手動タイヤ摩耗残り周回数は `LmuWindowsReadoutItemKey.PitTiming.TyreWear.value`（項目名「タイヤ摩耗の残り周回数」）に対応し、蓄積済みの `pitTimingTyreWearTrackingState` からエネルギーと同じ式で算出する（共通の private ヘルパーを `manualPitTimingTyreWearRemainingLaps` から呼ぶ）。既定文言は `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT`（「タイヤは残り約{laps}周」）で、1周以上なら `formatLmuWindowsPitTimingReadoutText` で置換し、0周なら固定文言「タイヤ残り1周未満」を読む（空欄テンプレートなら0周でも空文字）。蓄積前・消費量が null・0以下・非有限、算出結果が負、最新タイヤ摩耗が未受信・受信から5秒超の場合はデータ無しとし、`PitTiming.Root` を開始音・優先度・ログに使う。エネルギーの残り周回数とは別項目で、既存の「タイヤ摩耗」(%) 項目とも別。

GT7 の手動読み上げは、一覧の ▶ と Windows のセットホットキーからの依頼を合流し、
`Simulator.Gt7Ps5.id` 宛てで保存済みセットが見つかり、GT7 が選択中の場合だけ処理する。
`manualReadoutItemKeys(Simulator.Gt7Ps5.id)` は「残り燃料」の
`Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value`、続いて「残り燃料の周回数」の
`Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value`、続いて「タイヤ温度」の
`Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value` に対応する。詳細画面で保存したセットの
`itemKeys`・`itemTexts` を ViewModel が参照し、`Gt7Ps5ManualReadoutItem` を判定へ渡す。
最新テレメトリの `gasLevel / gasCapacity * 100` を四捨五入して0〜100に制限する。
未受信・受信から5秒超・不正な燃料値は null とし「残り燃料はデータがありません」を読む。
既定文言は `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT` の
「燃料は残り{percent}パーセント」で、既存の燃料用フォーマット関数を再利用する。
判定は対応項目を最大3件、保存順に空欄を除いて「、」で連結し、
`Gt7Ps5ManualReadoutText` の1イベントを返す。現在値イベントは `Gt7Ps5RemainingFuelCurrentValue` と `Gt7Ps5RemainingFuelLapsCurrentValue`。
対応項目なしの場合は従来どおり「読み上げる項目がありません」を読む。
自動読み上げのスイッチ・警告済み状態は変更せず、常にキュー末尾へ追加して同じ本文をログに保存する。
開始音とログには、項目なしの場合も既存の `RemainingFuel.Root` を使う。
GT7 と LMU のキーはそれぞれ `gt7_ps5_`・`lmu_windows_` 接頭辞を持ち衝突しない。
手動項目の選択・文言はシミュレーター別のセット設定に保存し、自動項目のスイッチとは独立して判定する。


手動の残り燃料周回数は `currentRemainingFuelLaps(state)` で `fuelTrackingState` から算出する。
開始燃料＋総給油量－現在燃料を、完走周回数＋`CURRENT_LAP_CONSUMPTION_WEIGHT`（0.9）で割った平均消費を使い、
現在燃料÷平均消費を整数へ切り捨てる。開始燃料・開始周回が未設定、完走周回数が0以下、消費燃料が0以下なら null。
最終受信から5秒以内だけ推定値を使い、未受信・鮮度切れは「残り燃料の周回数はデータがありません」を読む。
`Gt7Ps5ManualReadoutItem.value` の単位は残り燃料が％、残り燃料の周回数が周、タイヤ温度が℃。
既定文言は `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）で、
`formatGt7Ps5RemainingFuelLapsReadoutText` で置換する。0周以下は「燃料残り1周未満」。
テンプレートが空欄なら、0周以下やデータなしでも読まない。
自動側のしきい値・重複抑止・周回内タイミング・有効スイッチは手動側に適用しない。
テレメトリごとに `determineRemainingFuelLaps` が呼ばれ、有効スイッチの判定より前に燃料追跡が更新されるため、
自動読み上げが無効でも手動用の追跡が続く。

GT7 の手動「タイヤ温度」は `TyreTemperature.Root` を使い、`currentMaxTyreTemperatureCelsius(telemetry)` で
4輪の有限値の最高温度を `roundToInt()` した整数℃を取得する。未受信・5秒超・全輪が非有限値または0（取得不能）は
「タイヤ温度はデータがありません」。既定文言は `GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`
（「タイヤ温度{celsius}℃」）で、既存の GT7 用フォーマッターとプレースホルダー検証を使う。
詳細のカテゴリはタイヤ、プレビューは90℃。空文言は読まず、自動のしきい値・ヒステリシス・重複抑止・
スイッチ判定は適用しない。最大3項目を「、」で連結し、開始音・ログのキーは `RemainingFuel.Root` を維持する。

GT7 の手動「自己ベストラップ」は `MyBestLap.Root` を使い、`currentBestLapTimeMs(telemetry)` でテレメトリの
`bestLapTimeMs`（ms）を取得する。未受信・5秒超・0以下（未確定）は「自己ベストラップはデータがありません」。
既定文言は `GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」）で、
既存の `Gt7Ps5MyBestLap` を手動文言を `resolvedText` に指定して再利用し、既存の GT7 用フォーマッターと
プレースホルダー検証を使う。詳細のカテゴリはラップ、プレビューは83456ms。空文言は読まず、自動の
`determineMyBestLap` の状態（personalBestMs 等）は変更しない。開始音・ログのキーは `RemainingFuel.Root` を維持する。

ACE の手動読み上げは、一覧の ▶ と Windows のセットホットキーからの依頼を合流し、
`Simulator.AceWindows.id` 宛てで保存済みセットが見つかり、ACE が選択中の場合だけ処理する。
`manualReadoutItemKeys(Simulator.AceWindows.id)` は既存の `AceWindowsReadoutItemKey.RemainingFuel.Root.value`
（`ace_windows_auto_remaining_fuel`）を返し、手動の「残り燃料」を選択できる。
`currentRemainingFuelPercent(data)` は `AceWindowsFuelData.remainingPercent`（0〜100）を四捨五入した整数％を返す。
未受信・受信から5秒超・非有限・0未満・100超は null とし、シミュレーター切替時は最新値と受信時刻をクリアする。
鮮度判定済みの `AceWindowsManualReadoutItem(key, textTemplate, value)` を保存順に最大3件処理し、
手動専用の `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}パーセント」）か
セットの編集文言を `formatAceWindowsRemainingFuelReadoutText` で置換する。
空白文言を除外して「、」で連結し、`AceWindowsManualReadoutText` の1イベントにまとめる。
値が null の項目は「残り燃料はデータがありません」を読む（全項目がデータ無しでも同じ）。
対応項目なしは
「読み上げる項目がありません」、全項目が空白なら空文字とする。
自動読み上げのスイッチ・閾値・`remainingFuelWarned` 等の判定状態は変更せず、
開始音・ログ・常時キュー末尾追加には既存の `RemainingFuel.Root` を使う。
`manualReadoutItemKeys(Simulator.AceWindows.id)` は続けて既存の `AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value`（`ace_windows_auto_remaining_fuel_laps`）を返し、手動の「残り燃料の周回数」を選択できる。
`currentRemainingFuelLaps(data)` は `AceWindowsRemainingFuelLapsData.remainingLaps` の整数部を返し、未受信・受信から5秒超・非有限・0以下・上限超過は null（データなし）とする。
手動専用の `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）を `formatAceWindowsRemainingFuelLapsReadoutText` で置換し、値が null の項目は「残り燃料の周回数はデータがありません」を読む。
