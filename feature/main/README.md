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

`SpeechRecognitionViewModel` が `ObserveSpeechRecognitionUseCase` の常時聞き取りを開始・停止し、状態（停止中・準備中・聞き取り中・マイクの権限が許可されていません・この端末では音声認識を利用できません・しばらく発話がなかったため停止しました）、認識途中の文字列、確定結果の履歴（新しい順に最大5件）、入力音量（`level`、0.0〜1.0。認識エンジンが音量を通知するプラットフォームだけで、Android が該当する。届いた間だけカードにゲージを表示し、停止・失敗で消す。無発話の自動停止の数え直しには使わない）を `SpeechRecognitionUiState` で保持します。アプリ全体で1つの ViewModel を共有するため、タブを切り替えても聞き取りは止まりません。

- `rememberSpeechRecognitionSection()`: `AppScreen` から一度だけ呼ぶ。マイクの権限の要求（`rememberRecordAudioPermissionRequester`、Android のみ実行時ダイアログ）、アプリが見えなくなったとき（ON_STOP）の停止、前面に戻ったとき（ON_START）の再開を担い、デバッグ状態画面（`DebugStateDetailPane`）のグリッド先頭に差し込むカード（`SpeechRecognitionCard`）を返す。音声認識のカードは `isSpeechRecognitionSupportedOnPlatform()` が true のプラットフォーム（Android とデスクトップ）で、マイク入力の確認用カード（`MicrophoneInputCard`、後述）は `isMicrophoneInputSupportedOnPlatform()` が true のプラットフォーム（デスクトップ）で返す。デスクトップは両方が true で、音声認識のカードの下にマイク入力の確認用カードを並べる（認識は選択中のマイクを使う）。どちらも false なら何も出さない。
- 見えなくなる前に聞き取り中だった場合だけ、前面に戻ったときに再開する（権限が外されていれば再開しない）。スイッチを OFF にした場合や失敗・無発話の自動停止の後は再開しない。ON/OFF の状態はメモリ上にのみ持つため、アプリのプロセスが終了すると OFF に戻る。
- 認識途中の文字列も確定結果も届かないまま5分経つと自動で止める（暫定値）。
- 確定結果（`Recognized`）の候補を `matchLmuWindowsManualReadoutItemKey`（`:core:lmu-windows-domain`）で手動読み上げ項目の語句と照合し、一致したら `ManualReadoutItemRequest`（LMU宛て）を `RequestManualReadoutItemUseCase` で発行する。LMU の Narrator が受け取り、保存済みセットの編集文言を使わず既定文言で1項目だけ読み上げる。選択中のシミュレーターが LMU でなければ Narrator 側が無視する。ACE・GT7 の照合は未対応。ウェイクワードは持たないため、常時聞き取り中の会話にも反応する。

## マイク入力の確認（Windows）

`MicrophoneInputViewModel` が `ObserveMicrophoneInputStatusUseCase` でマイクの取得を開始・停止し、入力デバイス名と一覧（`GetAudioInputDevicesUseCase`。カードの表示時とドロップダウンを開いたときに取得し直す。ドロップダウンで選んだデバイス、未選択なら先頭を、表示した名前のデバイスとして明示して取得する。選んだデバイス名は `SaveSelectedAudioInputDeviceNameUseCase` で保存し、起動後の最初の一覧取得時に `ObserveSelectedAudioInputDeviceNameUseCase` で読み込んで使う。保存済みのデバイスが一覧にない場合は今回の起動だけ先頭を使い、保存値は書き換えない（次回起動でそのデバイスが接続されていれば自動で選ばれる）。選んだデバイスが一覧から消えたときも先頭に戻し、再接続されても画面を開いている間は切り替えない。取得中に選び直すとそのデバイスで取得し直す）・状態（停止中・準備中・入力中・利用できない・無音）・音量（0.0〜1.0、線形）を `MicrophoneInputUiState` で保持する。無音（`NoSignal`）のときは Windows のプライバシー設定の案内を表示する。ON_STOP で停止し ON_START で再開する。`MicrophoneRepository` を持つ JVM の `mainPlatformModule` でのみ登録する。
