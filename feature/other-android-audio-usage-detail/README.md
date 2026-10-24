# other-android-audio-usage-detail

Android の音声の用途を6種類から選択して保存する詳細ペイン。既定値はメディア。

`ObserveAndroidAudioUsageUseCase` で保存済みの用途を監視し、`SaveAndroidAudioUsageUseCase` で選択した用途を保存する。各用途の右側にある試聴ボタンからサンプル文を読み上げ、再押下またはペインを閉じると停止する。試聴は保存済みの音声・速度・高さ・音量設定を使い、選択した用途を Android TTS の `AudioAttributes` に反映する。

用途カード一覧の下には音量調整セクションを表示し、音量パネル（`Settings.Panel.ACTION_VOLUME`）または端末のサウンド設定（`Settings.ACTION_SOUND_SETTINGS`）を開ける。API 29 未満では音量パネルボタンを表示せず、デスクトップではセクション全体を表示しない。

Koin モジュールは用途・音量の監視、用途の保存、読み上げ UseCase を提供する。`:core:data` の `AndroidAudioUsagePreferencesRepository`・`SoundVolumePreferencesRepository`・`VoicePreferencesRepository`・`VoiceSpeedPreferencesRepository`・`VoicePitchPreferencesRepository` と、`:core:text-to-speech-data` の `TextToSpeechRepository` を消費する。設定は DataStore に永続化される。


<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-android-audio-usage-detail.svg)
<!-- MODULE-GRAPH-END -->
