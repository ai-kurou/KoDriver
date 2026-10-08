# ace-windows-narrator

ACE (Assetto Corsa EVO) Windows版のWAV音声再生・自由文言TTSとアナウンス制御。`feature:lmu-windows-narrator` /
`feature:gt7-ps5-narrator` に相当する ACE 版。

`AceWindowsNarratorViewModel` が `ObserveAceWindowsFuelUseCase` の燃料残量と
`ObserveAceWindowsRemainingFuelThresholdPercentageUseCase` の閾値を監視し、
`AceWindowsNarratorEventProcessor` を通じて `SpeechEvent.AceWindowsRemainingFuelWarning` を
`TextToSpeechEngine` 実装の `AceWindowsWavNarratorEngine`（`:core:narrator` の `WavNarratorEngine` に委譲する薄いアダプタ）に
渡して、`AceWindowsReadoutTextSpeaker` の自由文言をOS標準TTSで読み上げる。
保存先は `AceWindowsRemainingFuelPreferences` のフィールド2（`readoutText`）、既定文言は「燃料は残り{percent}パーセント」。
`{percent}` は判定時の実残量を四捨五入した整数（0〜100）に置換する。閾値・有効判定は従来どおり。
Processorで解決した本文をイベントの `resolvedText` に保持し、キュー待機中の設定変更後も発話とログを一致させる。
空白・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` をログに保存する。
開始音・優先度・キューは `RemainingFuel.Root` を維持する。`remaining_fuel_caution.wav` は廃止し、WAVフォールバックは行わない。
詳細画面では自由文言の編集・既定値へのリセット・`{percent}` の末尾挿入と未知プレースホルダーの警告を提供する。試聴は画面に表示中の残量閾値を使い、`RemainingFuel.Root` の開始音とOS標準TTSで再生する。空白文言・TTS利用不可・音量ゼロ以下では開始音も本文も再生しない。

燃料残り周回数（`SpeechEvent.AceWindowsRemainingFuelLapsWarning`）も同様に、保存した自由文言をOS標準TTSで読み上げる。
保存先は `AceWindowsRemainingFuelLapsPreferences` のフィールド2（`readoutText`、既定「燃料は残り約{laps}周」）と
フィールド3（`emptyReadoutText`、0周以下用、既定「燃料がありません」）。`{laps}` は判定時の整数周回数に置換する。
Processorで解決した本文を `resolvedText` に保持し、空白・TTS利用不可では開始音も本文も要求せず `SKIPPED` を記録する。
開始音・優先度・キューは `RemainingFuelLaps.Root` を維持する。`remaining_fuel_laps_0.wav`〜`remaining_fuel_laps_5.wav` は廃止し、WAVフォールバックは行わない。
詳細画面の入力UI・試聴対応は別PRで追加する（それまで試聴は不可）。
イベント→WAVファイルパスのマップと `Res::readBytes` は `AceWindowsNarratorModule.kt` で定義する。
`SoundPlayer` 等の音声再生基盤の実装は `:core:narrator` を参照。

Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes の全10種は、`AceWindowsReadoutTextSpeaker` が保存文言（既定値は各SpeechEventのnarratedTextと同じ）を
OS標準TTSで読み上げる。`WavNarratorEngine` の `customSpeak` 経路を使い、WAVへフォールバックしない。
空白文言・TTS利用不可では本文も開始音も要求せず、テレメトリログに空文字と `SKIPPED` を記録する。
開始音・優先度・キューは `ReadoutItemKey.AceWindows.Flag.Root` を参照する。全フラッグが自由文言のOS標準TTSで、フラッグのWAVは使用しない。

車両接近も保存した固定の自由文言を `AceWindowsReadoutTextSpeaker` がOS標準TTSで読み上げる。
既定文言は「車両接近」で、既存の車両接近DataStoreのフィールド5に保存する。プレースホルダーはない。
Narratorで都度文言を解決し、空白・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` をログに保存する。
有効判定は `VehicleApproach.Root` と `StartReadout` を維持し、開始音・優先度・キューは `VehicleApproach.Root` を使う。
`vehicle_approach.wav` は廃止し、WAVへのフォールバックは行わない。

タイヤ過熱も保存した自由文言をOS標準TTSで読み上げる。既定文言は「タイヤ過熱 {celsius}度」で、
`AceWindowsTyreTemperaturePreferences` のフィールド3に保存する。`{celsius}` は判定時点の全輪最大カーカス温度を
`roundToInt` で四捨五入した整数に置換する。過熱・解除の判定ロジックは従来どおり。
Processorで解決した本文を `SpeechEvent.AceWindowsTyreOverheat.resolvedText` に保持し、発話とログを一致させる。
空白・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` をログに保存する。
開始音・優先度・キューは `TyreTemperature.Root` を維持し、`tyre_overheat.wav` を廃止する。WAVフォールバックは行わない。
自由文言の入力UI・試聴対応はPR3で追加する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->

自由文言の解決で例外が発生した場合はエラーを記録し、当該イベントの開始音・本文を要求せず、
空文字と `SKIPPED` をテレメトリログに保存する。同じ入力の後続イベントと次回以降の収集は継続する。
`CancellationException` は再スローし、キャンセル後の読み上げ・ログ保存は行わない。
