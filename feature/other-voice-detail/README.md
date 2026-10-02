# other-voice-detail

Windows/Androidの音声選択画面。システム既定と日本語の音声をラジオ選択し、即時保存する。取得中は「読み込み中…」の文言、空の一覧では音声追加の案内と再読み込みを表示する。保存済みIDが不在の場合は保存値を保持し、システム既定を選択表示する。

GetAvailableVoicesUseCase・ObserveVoiceUseCase・SaveVoiceUseCaseを使用する。Repositoryはcore:dataとcore:text-to-speech-dataのJVM実装に依存する。SpeakTextUseCase・ObserveSoundVolumeUseCaseで保存済み音声を試聴できる。取得中は試聴ボタンを無効にし、音量が0以下なら読み上げない。試聴に失敗しても画面の操作は継続できる。指定音声が見つからない場合は日本語音声へフォールバックする。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-voice-detail.svg)
<!-- MODULE-GRAPH-END -->
