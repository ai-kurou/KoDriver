# speech-recognition-data

マイクからの音声入力と音声認識、マイク権限の確認を実装するJVM / Androidマルチプラットフォームモジュールです。`:core:domain` の `SpeechRecognitionRepository` / `MicrophoneRepository` / `AudioInputDeviceRepository` / `RecordAudioPermissionRepository` を実装し、Koin には `speechRecognitionDataModule` で登録します（Desktop は jvmMain、Android は androidMain の同名モジュール）。`Context` は Koin から解決します。

- `speech`: マイクからの常時聞き取り（音声認識）。Android の `AndroidSpeechRecognitionRepository` が、1回分の聞き取りを行う `SpeechRecognitionEngine` を結果通知やエラーのたびに生成し直して常時聞き取りにする。エラーごとの再開・停止は `speechRecognitionErrorAction` で決める（無音・認識不能は300msで再開して連続失敗を数え直し、通信や音声入力の不調は一時的な失敗として1秒から倍々に最大30秒まで待ち時間を延ばして再開し、10回続いたら `Failed(Unavailable)` を流して完了する。認識結果が得られたときも連続失敗を数え直す。権限不足・言語未対応は即座に `Failed` を流して完了する）。実際の `SpeechRecognizer` は `PlatformSpeechRecognitionEngine` が包み、端末内認識が使えれば優先する。端末内認識が言語エラー（日本語モデル未導入など）になった場合は、通常の認識サービスに切り替えて直ちに再開し、それも失敗したときに利用不可として完了する。認識途中の文字列は最新の1件だけを保持して収集側が遅くてもキューを伸ばさず（確定結果とエラーは取りこぼさない）、30秒間コールバックが来ないセッションは打ち切って再開する。エンジンの開始で想定外の例外が出た場合は `captureError` で記録してから利用不可として完了する。`SpeechRecognizer` はメインスレッドでしか扱えないため、聞き取りは `Dispatchers.Main.immediate` 上で行う。Desktop は `JvmSpeechRecognitionRepository` が常に利用不可を返す。
- `microphone`: Desktop（JVM）のマイク入力。`JvmAudioInputDeviceRepository` が入力デバイスの名前一覧を、`JvmMicrophoneRepository` が指定デバイス（null は既定）の音声を16kHz・モノラル・16bit PCM の100ミリ秒ごとの断片（`MicrophoneAudioEvent.Chunk`）として流す。OS依存部分は `AudioInputSource` / `AudioCapture` に分け、実機は `JavaSoundAudioInputSource`（`javax.sound.sampled`）が担う。デバイスを開けない・取得中に使えなくなったときは `Failed(Unavailable)`、完全な無音（全バイト0）が5秒続いたら `Failed(NoSignal)` を流して完了する。Windows のデスクトップアプリにはマイク権限のダイアログがなく、プライバシー設定でブロックされていても開けて全ゼロが届くため、権限の代わりに無音で検出する。デバイスは名前で選ぶが、名前は安定したIDではない。取得は別コルーチンで行い、収集のキャンセル時は読み込み中でもデバイスを閉じて解除する。0バイトの読み込みが100回続いたときも `Failed(Unavailable)` にする。同名のデバイスは一覧に1件で、開くときは名前が一致するものを順に試す。認識エンジンには接続していない。Android には登録しない。
- `device`: マイク（RECORD_AUDIO）権限の確認。Android は `AndroidRecordAudioPermissionRepository`、Desktop は常に許可済みを返す `JvmRecordAudioPermissionRepository`。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-speech-recognition-data.svg)
<!-- MODULE-GRAPH-END -->
