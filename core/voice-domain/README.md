# voice-domain

読み上げ音声の一覧取得と、音声・速度・高さの保存の UseCase。

- `:core:domain` にのみ依存する。パッケージ名は `kurou.kodriver.domain` のまま（`:core:domain` と同一パッケージを分割して持つ）。
- 音声・速度・高さの Observe UseCase、設定の Repository インターフェース、既定値は、`:core:domain` の `SpeakTextUseCase`・`TextToSpeechRepository` が参照するため `:core:domain` に置く。
