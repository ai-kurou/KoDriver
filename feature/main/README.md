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

`SpeechRecognitionViewModel` が `ObserveSpeechRecognitionUseCase` の常時聞き取りを開始・停止し、状態（停止中・準備中・聞き取り中・マイクの権限が許可されていません・この端末では音声認識を利用できません・しばらく発話がなかったため停止しました）、認識途中の文字列、確定結果の履歴（新しい順に最大5件）、入力音量（`level`、0.0〜1.0。Android は認識エンジンの通知、デスクトップは取得した音声の断片ごとの音量。届いた間だけカードにゲージを表示し、停止・失敗で消す。無発話の自動停止の数え直しには使わない）、入力デバイスの一覧と選択中のデバイス名（デスクトップのみ。後述）を `SpeechRecognitionUiState` で保持します。アプリ全体で1つの ViewModel を共有するため、タブを切り替えても聞き取りは止まりません。

- `rememberSpeechRecognitionSection()`: `AppScreen` から一度だけ呼ぶ。マイクの権限の要求（`rememberRecordAudioPermissionRequester`、Android のみ実行時ダイアログ）、アプリが見えなくなったとき（ON_STOP）の停止、前面に戻ったとき（ON_START）の再開を担い、デバッグ状態画面（`DebugStateDetailPane`）のグリッド先頭に差し込むカード（`SpeechRecognitionCard`）を返す。カードは `isSpeechRecognitionSupportedOnPlatform()` が true のプラットフォーム（Android とデスクトップ）で返し、false なら何も出さない。カードを出すときに入力デバイスの一覧も取得する（デスクトップのみ）。
- 見えなくなる前に聞き取り中だった場合だけ、前面に戻ったときに再開する（権限が外されていれば再開しない）。スイッチを OFF にした場合や失敗・無発話の自動停止の後は再開しない。ON/OFF の状態はメモリ上にのみ持つため、アプリのプロセスが終了すると OFF に戻る。
- 認識途中の文字列も確定結果も届かないまま5分経つと自動で止める（暫定値）。
- 確定結果（`Recognized`）の候補を `matchLmuWindowsManualReadoutItemKey`（`:core:lmu-windows-domain`）・`matchGt7Ps5ManualReadoutItemKey`（`:core:gt7-ps5-domain`）・`matchAceWindowsManualReadoutItemKey`（`:core:ace-windows-domain`）で手動読み上げ項目の語句と照合し、一致したシミュレーター宛ての `ManualReadoutItemRequest` を `RequestManualReadoutItemUseCase` で発行する（照合規則は `:core:domain` の `matchManualReadoutItemKey`）。該当シミュレーターの Narrator が受け取り、保存済みセットの編集文言を使わず既定文言で1項目だけ読み上げる。選択中のシミュレーターでなければ Narrator 側が無視する。ウェイクワードは持たないため、常時聞き取り中の会話にも反応する。
- 音声認識由来の依頼は `playStartSound = false` で発行し、Narrator は開始音を鳴らさずに本文だけを読み上げる（`TextToSpeechEngine.speak` の `playStartSound`）。自動タブの開始音スイッチには関係なく、音声認識では常に開始音を鳴らさない。ボタン・ホットキー・保存済みセットからの手動読み上げと自動読み上げは従来どおり。

## 入力デバイスの選択（デスクトップ）

音声認識カードのドロップダウンで入力デバイスを選ぶ。デバイスの一覧と選択は `AudioInputDeviceSelector` が扱い（`GetAudioInputDevicesUseCase` で一覧を取得し、`ObserveSelectedAudioInputDeviceNameUseCase` / `SaveSelectedAudioInputDeviceNameUseCase` で選択を読み書きする）、`SpeechRecognitionViewModel` が `devices` / `deviceName` として保持する。一覧はカードを出すときとドロップダウンを開いたときに取得し直す。保存済みのデバイスが一覧にない場合や未選択の場合は先頭を選択中として表示し、保存値は書き換えない（そのデバイスが接続されれば次の取得で選ばれる）。音声認識側（`JvmSpeechRecognitionRepository`）も同じ規則で開くデバイスを決めるため、表示と実際の入力が一致する。聞き取り中に選び直すと、選択を保存してから聞き取りをやり直す。聞き取り中に選択中のデバイスが一覧から消えた場合も、先頭に切り替えて聞き取りをやり直す。デバイス関連の Repository を登録していない Android では `AudioInputDeviceSelector` を渡さず、ドロップダウンは表示しない。
