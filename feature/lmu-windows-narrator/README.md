# lmu-windows-narrator

LMUの4種類のフラッグ本文と車両接近開始時・継続時の左右文言とバーチャルエナジー残量警告とタイヤ摩耗警告とブレーキ過熱警告と車両故障警告とタイヤ過熱警告とバーチャルエナジー・タイヤ摩耗由来のピットタイミング警告は、保存した自由文字列をOS標準TTSで読み上げる。
空白文言またはTTS利用不可時は本文を読み上げず、収録WAVへのフォールバックは行わない。
開始音は既存の設定に従う。

LMU車両故障のオーバーヒート・部品脱落・タイヤ脱落は、各1つのグローバルな自由文言をOS標準TTSで読み上げる。既定文言は「オーバーヒート」「部品脱落」「タイヤ脱落」。文言は既存の `LmuWindowsVehicleDamagePreferences` に保存し、保存時は `trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)` で正規化する。判定時に解決した本文をイベントの `resolvedText` に保持し、キュー待機中の設定変更でもログと発話を一致させる。空白文言・TTS利用不可時は本文と開始音を要求せず、ログに空文字と `SKIPPED` を記録する。一覧の `VehicleDamage.Root` と詳細の `Overheat` / `PartDetached` / `TyreDetached` のスイッチ配線は維持し、開始音・優先度・キューと試聴は `VehicleDamage.Root` を参照する。旧オーバーヒート音声タイプは廃止し、3種類の警告は収録WAVへフォールバックしない。

Koinモジュールは`ObserveVoiceSpeedUseCase`・`ObserveVoicePitchUseCase`を提供し、`:core:data`の`VoiceSpeedPreferencesRepository`・`VoicePitchPreferencesRepository`を消費する。
`SpeakTextUseCase`は試聴・本文の読み上げに保存済み速度・声の高さ（ともに0.5〜2.0、既定1.0）を使用する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->

フラッグ・車両接近開始時・継続時のテレメトリログには、読み上げ要求時点の自由文字列を記録する。空欄・TTS利用不可の場合は本文を要求せず、空文字と `SKIPPED` を記録する。`SPOKEN` / `QUEUED` は再生要求の結果であり、非同期発話の完了を保証しない。

`LmuWindowsReadoutTextSpeaker` が各設定の最新文言を取得し、OS標準TTSへ渡す。
車両接近開始時・継続時の既定文言は左「カーレフト」、右「カーライト」。継続時は左側車両「キープライト」、右側車両「キープレフト」。旧開始時・継続時の種別は移行せず、既定文言を使用する。保存時は前後の空白を除去し、30文字までに制限する。

ピットタイミングの両ソースの警告は、残り1周以上で通常文言の `{laps}` を置換し、0周以下で専用文言を使う。空白・TTS利用不可時は開始音も要求せず、空文字と `SKIPPED` を記録する。通常・切迫時文言はピットタイミング詳細画面で設定・試聴できる。通常文言の試聴は `{laps}` を各スライダーに表示中の周回数に置換し、切迫時文言はそのまま読む。両ソースとも自由文言TTSを使用し、ピットタイミング用の収録WAVは使用しない。

バーチャルエナジー残量警告は設定した閾値（%）をイベントに持ち、保存文言の `{percent}` をその閾値に置換する。実際の残量は置換値に使わない。空白文言・TTS利用不可時は開始音も要求せず、ログに空文字と `SKIPPED` を記録する。残量警告の収録WAVは使用しない。

LMUタイヤ過熱・低温警告は保存した自由文言の `{celsius}` を、警告を発生させた判定時点の全輪の最高カーカス温度を四捨五入した整数（℃）に置換してOS標準TTSで読み上げる。低温警告も全輪の最高温度を使い、`{wheel}` は対応しない。判定時に解決した本文をイベントの `resolvedText` に保持し、キュー待機中に設定が変わっても発話とログを一致させる。既定文言は「タイヤ過熱 {celsius}度」「タイヤ低温 {celsius}度」。DataStore の保存済み文言は変更せず、未設定の場合と「デフォルトに戻す」操作で新しい既定文言を使用する。DataStore の移行は不要。詳細画面は挿入チップと未知プレースホルダーの警告を提供し、試聴では過熱は選択中車両クラスの高温閾値（スライダーの現在値）、低温は60℃固定に置換する。空白文言・TTS利用不可時は開始音も要求せず、ログに空文字と `SKIPPED` を記録する。収録WAVへのフォールバックは行わない。開始音・優先度・キュー設定と試聴の開始音は `TyreTemperature.Root` を参照する。

タイヤ摩耗警告は設定した残存率閾値（%）をイベントに持ち、保存文言の `{percent}` をその閾値に置換する。実際の残存率は置換値に使わない。判定時に解決した文言を `resolvedText` に保持し、キュー待機中の設定変更でもログと発話を一致させる。空白文言・TTS利用不可時は開始音も要求せず、ログに空文字と `SKIPPED` を記録する。`tyre_wear_caution.wav` は廃止し、WAVへはフォールバックしない。

ブレーキ過熱警告は設定した車両クラスの温度閾値（℃）をイベントに持ち、全クラス共通の自由文言の `{celsius}` をその閾値に置換する。実測温度は使用しない。既定文言は「ブレーキ温度{celsius}℃以上」。判定時に文言を `resolvedText` へ保持し、キュー待機中もログと発話を一致させる。空欄・TTS利用不可では開始音も要求せず空文字と `SKIPPED` を記録する。`brake_overheat.wav` は廃止し、WAVへはフォールバックしない。

LMU自己ベストラップ更新は自由文言のOS標準TTSを使う。既定文言は「自己ベストラップ更新 {laptime}」。`{laptime}` は更新後の `telemetry.timing.bestLapTimeMs`（Long）を「1分23秒456」形式に置換し、1分未満では分を省略、ミリ秒は3桁固定、60分以上も分で表す。判定時に本文を `resolvedText` へ保持して発話とログを一致させる。空白・TTS利用不可では開始音も本文も要求せず、ログに空文字と `SKIPPED` を記録する。更新判定と一覧の `MyBestLap.Root`・詳細の `MyBestLap.DetailEnabled` のスイッチ配線は維持し、開始音・優先度・キュー・試聴は `MyBestLap.Root` を使う。詳細画面で文言編集・`{laptime}` 挿入・未知プレースホルダー警告・リセット・サンプルタイム83456msでの試聴を提供する。LMUの口調設定は移行せず参照しない。自己ベスト用WAVは廃止し、WAVへのフォールバックは行わない。

LMUの全自由文字列イベント（フラッグ・車両接近・ピットタイミングを含む18種類）は `ReadoutTextEvent` を実装する。
`LmuWindowsNarratorEventProcessor` の共通 `processEvents` が判定時の本文を `withResolvedText` で保持し、同じ本文をログに保存する。
自己ベストラップの処理名は `processMyBestLap`。空白本文は共通処理でスキップする。
`LmuWindowsReadoutTextSpeaker` は解決済み本文を優先し、未解決の場合だけ設定を参照する。
`LmuWindowsNarratorModule` は `isCustomSpeakEvent = { it is ReadoutTextEvent }` で対象を判定する。
ピットタイミングの周回ゲートとソース選択は本文ではなく `laps` を使い続ける。
読み上げ済みの周回数は、ソース選択と周回ゲートを通過した警告のみViewModelで記録する。

自由文言の解決で例外が発生した場合はエラーを記録し、当該イベントの開始音・本文を要求せず、
空文字と `SKIPPED` をテレメトリログに保存する。同じ入力の後続イベントと次回以降の収集は継続する。
`CancellationException` は再スローし、キャンセル後の読み上げ・ログ保存は行わない。

ピットタイミングの消費量推定は初回取得周を除外し、周回番号が1つ進む境界から計測した周だけを採用する。セッション変更・周回番号の巻き戻りでは再計測し、番号が飛んだ区間とその直後の周、補給・タイヤ交換を検出した周は採用しない。有効な消費量が得られるまで予想周回数の読み上げを待つ（残量閾値警告は独立して動作する）。

## デバッグ読み上げ要求

ViewModel の独立した Job が `ObserveDebugReadoutRequestUseCase` を購読し、自分のシミュレーター宛ての要求だけを EventProcessor の `processDebugReadout` に渡す。テレメトリ未受信でも処理し、項目の有効スイッチと determine 系の判定は通さない。

`DebugReadoutText` の本文は入力をそのまま保持する。通常の文言解決・優先度・キュー・開始音・TTS・安全なログ保存の経路を使い、空白または TTS 利用不可の本文は SKIPPED として記録する。ログの JSON は現在の `state`、`settings`、`observedAtMs` と `debugReadout: true` を含む。ログを購読するオーバーレイは通常の最新読み上げログ経路で表示する。

JVM テストで未受信・項目無効の要求の発話とログ保存、別シミュレーター宛ての要求の無視、空白スキップとテンプレートをそのまま TTS に渡すことを検証する。

## 手動読み上げ依頼

`RemainingVirtualEnergyUseCases` に束ねた `ObserveManualReadoutRequestsUseCase` を独立した Job で購読する。
依頼の `simulatorId` が `Simulator.LmuWindows.id` で、選択中のシミュレーターも LMU の場合だけ処理する。
現段階では `setId` とセットの `itemKeys` に関係なく仮想エネルギー残量を読み上げる。
F9 と共通の `readRemainingVirtualEnergyCurrentValue` が、最新値と受信から5秒以内の鮮度を確認し、
`determineRemainingVirtualEnergyCurrentValue` → `processRemainingVirtualEnergy` の既存経路へ渡す。
開始音・文言・有効スイッチ・キュー設定・テレメトリログは F9 と同じ動作で、自動警告の読み上げ済み状態は変えない。
Android でも WebSocket で受信した値を使う。未受信・鮮度切れでは読み上げず、「データがありません」は後続 PR で対応する。

JVM テストで連続依頼・警告状態の維持・未受信・鮮度の境界・宛先と選択中シミュレーターの照合・
切り替え後の古い値の破棄・キューとログ、Koin の UseCase 群の解決を確認する。
