# ace-windows-narrator

ACE (Assetto Corsa EVO) Windows版のWAV音声再生とアナウンス制御。`feature:lmu-windows-narrator` /
`feature:gt7-ps5-narrator` に相当する ACE 版。

`AceWindowsNarratorViewModel` が `ObserveAceWindowsFuelUseCase` の燃料残量と
`ObserveAceWindowsRemainingFuelThresholdPercentageUseCase` の閾値を監視し、
`AceWindowsNarratorEventProcessor` を通じて `SpeechEvent.AceWindowsRemainingFuelWarning` を
`:core:narrator` の `WavNarratorEngine`（`TextToSpeechEngine` 実装）に渡して WAV（`remaining_fuel_caution.wav`）を
再生する。`WavNarratorEngine` の生成時に渡すイベント→WAVファイルパスのマップと `Res::readBytes` は
`AceWindowsNarratorModule.kt` で定義する。`SoundPlayer` 等の音声再生基盤の実装は `:core:narrator` を参照。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-narrator.svg)
<!-- MODULE-GRAPH-END -->
