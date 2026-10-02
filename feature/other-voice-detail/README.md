# other-voice-detail

Windows専用の音声選択画面。システム既定と日本語の音声をラジオ選択し、即時保存する。取得中はスピナー、空の一覧では音声追加の案内と再読み込みを表示する。保存済みIDが不在の場合は保存値を保持し、システム既定を選択表示する。

GetAvailableVoicesUseCase・ObserveVoiceUseCase・SaveVoiceUseCaseを使用する。Repositoryはcore:dataとcore:text-to-speech-dataのJVM実装に依存する。試聴とTTSへの音声反映は対象外。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-other-voice-detail.svg)
<!-- MODULE-GRAPH-END -->
