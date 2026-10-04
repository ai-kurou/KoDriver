# ace-windows-narrator

ACE (Assetto Corsa EVO) Windows版のWAV音声再生・自由文言TTSとアナウンス制御。`feature:lmu-windows-narrator` /
`feature:gt7-ps5-narrator` に相当する ACE 版。

`AceWindowsNarratorViewModel` が `ObserveAceWindowsFuelUseCase` の燃料残量と
`ObserveAceWindowsRemainingFuelThresholdPercentageUseCase` の閾値を監視し、
`AceWindowsNarratorEventProcessor` を通じて `SpeechEvent.AceWindowsRemainingFuelWarning` を
`TextToSpeechEngine` 実装の `AceWindowsWavNarratorEngine`（`:core:narrator` の `WavNarratorEngine` に委譲する薄いアダプタ）に
渡して WAV（`remaining_fuel_caution.wav`）を再生する。`WavNarratorEngine` の生成時に渡すイベント→WAVファイルパスのマップと `Res::readBytes` は
`AceWindowsNarratorModule.kt` で定義する。`SoundPlayer` 等の音声再生基盤の実装は `:core:narrator` を参照。

チェッカーフラッグのみ、`AceWindowsReadoutTextSpeaker` が保存文言（既定値「チェッカーフラッグ」）を
OS標準TTSで読み上げる。`WavNarratorEngine` の `customSpeak` 経路を使い、WAVへフォールバックしない。
空白文言・TTS利用不可では本文も開始音も要求せず、テレメトリログに空文字と `SKIPPED` を記録する。
開始音・優先度・キューは `ReadoutItemKey.AceWindows.Flag.Root` を参照する。他9種はWAVを維持する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->
