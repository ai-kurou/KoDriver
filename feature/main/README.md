# main

AppScreen の設定・接続状態に加え、端末または KoDriver の音量低下警告を監視する `LowVolumeWarningViewModel` を提供します。警告を閉じた状態は ViewModel のメモリにのみ保持し、音量が閾値以上に戻る（警告が解消する）までバナーを再表示しません。音量詳細設定から警告バナーをオフにできます。

音量警告の状態生成は `LowVolumeWarningFlow` が cold Flow として担当し、警告解消時の dismissal リセットと表示状態の合成を行います。`LowVolumeWarningViewModel` は閉じた状態と操作、`viewModelScope`・`SharingStarted.WhileSubscribed(5_000)`・初期値による `stateIn` を管理します。

接続バナーの状態生成は `ConnectionBannerFlow` が担当します。選択中のシミュレーターに応じた LMU / ACE のステータス購読、GT7 のコンソール IP 設定監視と周期的な接続確認を cold Flow として構成します。`ConnectionBannerViewModel` は入力の配線と、`viewModelScope`・`SharingStarted.WhileSubscribed()`・既定の `ConnectionBannerVmUiState` による `stateIn` を担当します。
AppScreen の UI 状態の合成は `AppScreenFlow` が cold Flow として担います。共有・初期値・`viewModelScope` での実行は `AppScreenViewModel` が管理します。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-main.svg)
<!-- MODULE-GRAPH-END -->

## 音声認識（常時聞き取り）

`SpeechRecognitionViewModel` が `ObserveSpeechRecognitionUseCase` の常時聞き取りを開始・停止し、状態（停止中・準備中・聞き取り中・マイクの権限が許可されていません・この端末では音声認識を利用できません・しばらく発話がなかったため停止しました）、認識途中の文字列、確定結果の履歴（新しい順に最大5件）を `SpeechRecognitionUiState` で保持します。アプリ全体で1つの ViewModel を共有するため、タブを切り替えても聞き取りは止まりません。

- `rememberSpeechRecognitionSection()`: `AppScreen` から一度だけ呼ぶ。マイクの権限の要求（`rememberRecordAudioPermissionRequester`、Android のみ実行時ダイアログ）、アプリが見えなくなったとき（ON_STOP）の停止、前面に戻ったとき（ON_START）の再開を担い、デバッグ状態画面（`DebugStateDetailPane`）のグリッド先頭に差し込むカード（`SpeechRecognitionCard`）を返す。デスクトップ版は `isSpeechRecognitionSupportedOnPlatform()` が false のため音声認識のカードは出さず、`isMicrophoneInputSupportedOnPlatform()` が true の場合は代わりにマイク入力の確認用カード（`MicrophoneInputCard`、後述）を返す。

## マイク入力の確認（Windows）

`MicrophoneInputViewModel` が `ObserveMicrophoneInputStatusUseCase` でマイクの取得を開始・停止し、入力デバイス名（`GetAudioInputDevicesUseCase` の先頭）・状態（停止中・準備中・入力中・利用できない・無音）・音量（0.0〜1.0、線形）を `MicrophoneInputUiState` で保持する。無音（`NoSignal`）のときは Windows のプライバシー設定の案内を表示する。ON_STOP で停止し ON_START で再開する。`MicrophoneRepository` を持つ JVM の `mainPlatformModule` でのみ登録する。
- 見えなくなる前に聞き取り中だった場合だけ、前面に戻ったときに再開する（権限が外されていれば再開しない）。スイッチを OFF にした場合や失敗・無発話の自動停止の後は再開しない。ON/OFF の状態はメモリ上にのみ持つため、アプリのプロセスが終了すると OFF に戻る。
- 認識途中の文字列も確定結果も届かないまま5分経つと自動で止める（暫定値）。
- 認識結果の解釈や読み上げの起動はまだ行わない。
