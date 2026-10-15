# manual-readout-set-detail

手動読み上げセットの詳細画面。シミュレーター ID とセット ID を受け取り、一覧上の番号によるタイトルと保存済みの選択項目を表示する。LMU は「バーチャルエナジー残量」の1項目、ACE・GT7 は未対応の説明を表示する。「その他」詳細と同じ `DetailPaneScaffold` を使い、`canNavigateBack` が true の場合に戻るボタンを表示し、`onBack` を通知する。「読み上げる項目」カードには `selectedItemKeys` の順で選択済み項目のみを表示し、各行にドラッグハンドル・項目名・削除ボタンと既存の文言入力欄を置く。

「＋ 項目を追加」から Desktop / Android 共通の `AlertDialog` を開き、未選択項目をカテゴリ別に表示する。カテゴリと表示名の対応は feature 内で管理し、現在は「エネルギー」の「バーチャルエナジー残量」のみ。選択すると末尾へ追加して閉じる。未選択項目がない場合、または domain の `MANUAL_READOUT_SET_ITEM_MAX`（3件）に達した場合は追加ボタンを無効化し、上限時は説明を表示する。未選択時は短い説明と追加ボタンを表示する。

`libs.reorderable` の `ReorderableColumn` でドラッグ終了時に `moveItem` を呼び、`ManualReadoutSet.itemKeys` の順序を保存する。最大3項目のため、画面のスクロール内で通常の Column として配置する。試聴文は選択順に空欄を除いて「、」でつなぎ、domain の置換関数で仮想エネルギーをサンプル値50に置換する（画面には表示しない）。項目がなければ試聴ボタンを表示しない。Narrator の読み上げロジックは変更しない。

`ManualReadoutSetDetailViewModel` は `ObserveManualReadoutSetsUseCase` で対象セットを観測し、`UpdateManualReadoutSetUseCase` で追加・削除・並べ替えと項目ごとの文言を保存する。追加は重複・未対応キー・3件上限を検証する。未知キーは保持し、連続操作は直前の保存結果を参照する。セットが見つからない場合は説明を表示する。上部バー右端の削除アイコン（`DetailPaneScaffold` の `actions`）から確認ダイアログを開き、`DeleteManualReadoutSetUseCase` を実行し、削除完了時に Content の `onDeleted` を通知する。

文言は入力ごとに前後の空白を除去して30文字まで保存し、未保存なら項目の既定文言を表示する。
空欄は読み上げず、リセットで既定文言へ戻す。選択を解除しても編集した文言は保持する。
LMU の残量では `{percent}` を現在の残量（%）に置換し、挿入ボタンと未知プレースホルダーの警告を提供する。挿入はカーソル位置に行い、選択中ならその範囲を置き換え、挿入直後へカーソルを移動する。置換後に文字数上限を超える場合は挿入しない。挿入は共通の `TextInsertState` で行う。
自動読み上げと同じ共通入力部品を使い、項目ごとの試聴ボタンは表示しない。
「試聴」ボタン（他の設定画面と同じ `FilledTonalButton`、再生中は「試聴を停止」）で、試聴文全体を `ReadoutTextPreviewHelper` により試聴する。
選択済み項目の先頭キーに対応する開始音を再生し、現在の音量で読み上げる。未知の先頭キーでは再生しない。
再押しやペイン離脱で停止し、TTS 利用不可または試聴文が空のときはボタンを無効にする。
DI は TTS・音量の Repository と、Narrator 登録済みの `named(simulatorId)` の開始音・読み上げ UseCase を消費する。

`manualReadoutSetDetailModule` は `named(simulatorId)` の Repository を解決する。Android・Desktop 共通の `featureModules` に登録する。一覧モジュールはこのモジュールに依存せず、`app:shared` が詳細スロットに接続する。

JVM テストで追加・削除・並べ替え・上限・未知キーのガードとプレビュー、ViewModel・Koin のパラメータ解決・項目選択・文言保存（空欄・正規化・上限・リセット）・削除確認・戻るボタンの表示とクリック・配線を検証する。Desktop スクリーンショットテストで項目あり・項目なし・戻るボタンなし・1ペインの戻るボタン・削除ダイアログ・項目追加ダイアログ・選択項目の既定文言・編集文言の警告・空欄を確認する。既存の画面全体のスクリーンショットは `showHotkey = false` で維持し、Windows デスクトップ向けにホットキー未割り当て・割り当て済み・他セット重複・デバッグ重複のケースを追加する。

Windows デスクトップ版のみ（macOS/Linux では表示しない）「ホットキー」カードを表示する。読み取り専用の入力欄にフォーカスして数字0–9または英字A–Zを押すと、左のドロップダウンで選択した修飾キー（Ctrl + Alt + Shift / Ctrl + Alt / Ctrl + Shift / Alt + Shift）と組み合わせて割り当てる。入力欄はキー名のみ表示する。キー未割り当てでも修飾キーを保存できる。解除すると null を保存する。Android ではカードを表示しない。Content / Pane の `showHotkey` で表示を注入できる。

OS のホットキーはシミュレーターをまたいで共有されるため、全シミュレーターの他セットとの「修飾キー＋キー」の重複をシミュレーター名と一覧番号で表示し、全シミュレーター共通のデバッグ用ホットキーとの重複は Ctrl + Alt + Shift の場合のみ拒否する。修飾キー変更時も同じ判定を行い、競合時は保存しない。保存後にデバッグ用ホットキーが同じ組み合わせへ変更された場合も、画面表示中は重複として表示し続ける。自セットの同じキーを再入力すると他セットとの重複エラーを解除する。保存は項目編集と同じ Mutex で直列化し、最新の保存状態を参照する。保存済みキーがある場合は登録中・登録済み・競合・失敗（エラーコード）を表示し、競合・失敗時に「再試行」を提供する。未割り当て・非対応時は登録状態を表示しない。
`manualReadoutSetDetailModule` は共通の `ReadoutHotkeyPreferencesRepository` から `ObserveReadoutHotkeyVirtualKeyCodeUseCase` を、`ManualReadoutHotkeyRepository` から登録状態の購読・再試行 UseCase を解決する。JVM テストで状態の表示分岐と再試行の配線を、Desktop スクリーンショットで登録競合と再試行ボタンを確認する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-set-detail.svg)
<!-- MODULE-GRAPH-END -->
