# ace-windows-narrator

ACE (Assetto Corsa EVO) Windows版のWAV音声再生・自由文言TTSとアナウンス制御。`feature:lmu-windows-narrator` /
`feature:gt7-ps5-narrator` に相当する ACE 版。

`AceWindowsNarratorViewModel` が `ObserveAceWindowsFuelUseCase` の燃料残量と
`ObserveAceWindowsRemainingFuelThresholdPercentageUseCase` の閾値を監視し、
`AceWindowsNarratorEventProcessor` を通じて `SpeechEvent.AceWindowsRemainingFuelWarning` を
`TextToSpeechEngine` 実装の `AceWindowsWavNarratorEngine`（`:core:narrator` の `WavNarratorEngine` に委譲する薄いアダプタ）に
渡して WAV（`remaining_fuel_caution.wav`）を再生する。`WavNarratorEngine` の生成時に渡すイベント→WAVファイルパスのマップと `Res::readBytes` は
`AceWindowsNarratorModule.kt` で定義する。`SoundPlayer` 等の音声再生基盤の実装は `:core:narrator` を参照。

Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes の全10種は、`AceWindowsReadoutTextSpeaker` が保存文言（既定値は各SpeechEventのnarratedTextと同じ）を
OS標準TTSで読み上げる。`WavNarratorEngine` の `customSpeak` 経路を使い、WAVへフォールバックしない。
空白文言・TTS利用不可では本文も開始音も要求せず、テレメトリログに空文字と `SKIPPED` を記録する。
開始音・優先度・キューは `ReadoutItemKey.AceWindows.Flag.Root` を参照する。全フラッグが自由文言のOS標準TTSで、フラッグのWAVは使用しない。

車両接近も保存した固定の自由文言を `AceWindowsReadoutTextSpeaker` がOS標準TTSで読み上げる。
既定文言は「車両接近」で、既存の車両接近DataStoreのフィールド5に保存する。プレースホルダーはない。
Narratorで都度文言を解決し、空白・TTS利用不可では開始音も本文も要求せず、空文字と `SKIPPED` をログに保存する。
有効判定は `VehicleApproach.Root` と `StartReadout` を維持し、開始音・優先度・キューは `VehicleApproach.Root` を使う。
`vehicle_approach.wav` は廃止し、WAVへのフォールバックは行わない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->

自由文言の解決で例外が発生した場合はエラーを記録し、当該イベントの開始音・本文を要求せず、
空文字と `SKIPPED` をテレメトリログに保存する。同じ入力の後続イベントと次回以降の収集は継続する。
`CancellationException` は再スローし、キャンセル後の読み上げ・ログ保存は行わない。
