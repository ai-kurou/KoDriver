# other-voice-speed-detail

読み上げ速度を0.5〜2.0倍の範囲で0.1刻みに設定する詳細ペイン。操作完了時に保存し、デフォルトの1.0倍へリセットできる。試聴ボタンで保存済みの速度・ボイス・声の高さのまま読み上げを確認でき、試聴中にもう一度押すと停止する。音量が0以下のときは読み上げない。

`ObserveVoiceSpeedUseCase` で保存済みの速度を監視し、`SaveVoiceSpeedUseCase` で保存する。試聴は`SpeakTextUseCase`と`ObserveSoundVolumeUseCase`を使う。KoinモジュールはこれらのUseCaseを提供し、`:core:data`の`VoiceSpeedPreferencesRepository`・`VoicePitchPreferencesRepository`・`VoicePreferencesRepository`・`SoundVolumePreferencesRepository`と、`TextToSpeechRepository`を消費する。設定はDataStoreに永続化される。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-voice-speed-detail.svg)
<!-- MODULE-GRAPH-END -->
