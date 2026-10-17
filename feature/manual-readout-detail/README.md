# manual-readout-detail

手動読み上げセットの詳細画面。シミュレーター ID とセット ID を受け取り、セット名（未設定時は一覧上の番号）によるタイトルと保存済みの選択項目を表示する。LMU は「バーチャルエナジー残量」「タイヤ摩耗」「ブレーキ残量」「ブレーキ温度」「タイヤ温度」の5項目、GT7 は「残り燃料」「残り燃料の周回数」の2項目、ACE は未対応の説明を表示する。「その他」詳細と同じ `DetailPaneScaffold` を使い、`canNavigateBack` が true の場合に戻るボタンを表示し、`onBack` を通知する。「読み上げる項目」カードには `selectedItemKeys` の順で選択済み項目のみを表示し、各行にドラッグハンドル・項目名・削除ボタンと既存の文言入力欄を置く。

「＋ 項目を追加」から Desktop / Android 共通の `AlertDialog` を開き、未選択項目をカテゴリ別に表示する。カテゴリと表示名の対応は feature 内で管理し、現在は「エネルギー」の LMU「バーチャルエナジー残量」・GT7「残り燃料」「残り燃料の周回数」と「タイヤ」の「タイヤ摩耗」「タイヤ温度」、「ブレーキ」の「ブレーキ残量」「ブレーキ温度」。選択すると末尾へ追加して閉じる。未選択項目がない場合、または domain の `MANUAL_READOUT_SET_ITEM_MAX`（3件）に達した場合は追加ボタンを無効化し、上限時は説明を表示する。未選択時は短い説明と追加ボタンを表示する。

`libs.reorderable` の `ReorderableColumn` でドラッグ終了時に `moveItem` を呼び、`ManualReadoutSet.itemKeys` の順序を保存する。最大3項目のため、画面のスクロール内で通常の Column として配置する。試聴文は選択順に空欄を除いて「、」でつなぎ、domain の置換関数で仮想エネルギー・タイヤ摩耗・ブレーキ残量・GT7 の残り燃料のパーセントをサンプル値50に、ブレーキ温度の `{celsius}` をサンプル値500℃に、タイヤ温度の `{celsius}` をサンプル値90℃に置換する。選択項目がある場合は区切り線の下に空でない試聴文を大きめに表示し、その下に試聴ボタンを右寄せで配置する。項目がなければ試聴ボタンを表示しない。Narrator の読み上げロジックは変更しない。

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

OS のホットキーはシミュレーターをまたいで共有されるため、全シミュレーターの他セットとの「修飾キー＋キー」の重複をシミュレーター名とセット名（未設定時は一覧番号）で表示し、全シミュレーター共通のデバッグ用ホットキーとの重複は Ctrl + Alt + Shift の場合のみ拒否する。修飾キー変更時も同じ判定を行い、競合時は保存しない。保存後にデバッグ用ホットキーが同じ組み合わせへ変更された場合も、画面表示中は重複として表示し続ける。自セットの同じキーを再入力すると他セットとの重複エラーを解除する。保存は項目編集と同じ Mutex で直列化し、最新の保存状態を参照する。保存済みキーがある場合は登録中・登録済み・競合・失敗（エラーコード）を表示し、競合・失敗時に「再試行」を提供する。未割り当て・非対応時は登録状態を表示しない。
`manualReadoutSetDetailModule` は共通の `ReadoutHotkeyPreferencesRepository` から `ObserveReadoutHotkeyVirtualKeyCodeUseCase` を、`ManualReadoutHotkeyRepository` から登録状態の購読・再試行 UseCase を解決する。JVM テストで状態の表示分岐と再試行の配線を、Desktop スクリーンショットで登録競合と再試行ボタンを確認する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-detail.svg)
<!-- MODULE-GRAPH-END -->

LMU の手動セットは `determineManualReadout` で各項目の文を保存順に「、」で連結し、`LmuWindowsManualReadoutText` の1イベントで読み上げる。項目のデータが無い場合は `manualReadoutItemName` の日本語名を使った「<項目名>はデータがありません」を連結する。全項目がデータ無しなら、1項目の場合も含め「データがありません」のみ。対象項目なしは「読み上げる項目がありません」を維持する。

手動ブレーキ残量は `BrakeWear.Root` に対応し、4輪の `remainingPercent`（0〜100）の最小値を四捨五入して、既定文言「ブレーキ残量{percent}%」へ置換する。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。未受信・受信から5秒超・空の wheels はデータ無しとして扱い、自動警告の前回値・警告済み状態は変更しない。

手動ブレーキ温度は `BrakeTemperature.Root` に対応し、`LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃を、既定文言「ブレーキ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱警告の前回値・警告済み状態は変更しない。

手動タイヤ温度は `TyreTemperature.Root` に対応し、`LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃を、既定文言「タイヤ温度{celsius}℃」へ置換する。未受信・受信から5秒超・空の wheels はデータ無しとして扱う。セットの保存順に他項目と連結し、先頭項目の開始音と常時キュー追加を使う。自動過熱・低温警告の前回値・警告済み状態は変更しない。

ブレーキ温度・タイヤ温度の文言欄は `{celsius}` の挿入・最高温度への置換説明と、温度用の未知プレースホルダー検出を使う。1セットの選択上限は従来どおり3件。

GT7 の残り燃料は `Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value` を使い、既定文言は「燃料は残り{percent}パーセント」。`{percent}` の挿入・現在の残量（%）の説明は LMU の割合項目と共通で、文言編集・リセット・試聴に対応する。保存した項目と文言は GT7 Narrator の `determineManualReadout` が参照する。未受信・受信から5秒超は「残り燃料はデータがありません」、対応項目なしは「読み上げる項目がありません」を読む。


GT7 の「残り燃料の周回数」は `Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value` を使う。
既定文言は「燃料は残り約{laps}周」。編集欄の挿入チップ・説明・未知プレースホルダーの検証は `{laps}` に対応し、
試聴では3周に置換する。エネルギーカテゴリで残り燃料の次に選択できる。
実際の手動読み上げでは0周以下は「燃料残り1周未満」、推定不能・鮮度切れは項目名付きのデータなし文言を読む。
空欄なら値の有無にかかわらず読まない。

GT7 の手動「タイヤ温度」は `TyreTemperature.Root` を使い、`currentMaxTyreTemperatureCelsius(telemetry)` で
4輪の有限値の最高温度を `roundToInt()` した整数℃を取得する。未受信・5秒超・全輪が非有限値または0（取得不能）は
「タイヤ温度はデータがありません」。既定文言は `GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`
（「タイヤ温度{celsius}℃」）で、既存の GT7 用フォーマッターとプレースホルダー検証を使う。
詳細のカテゴリはタイヤ、プレビューは90℃。空文言は読まず、自動のしきい値・ヒステリシス・重複抑止・
スイッチ判定は適用しない。最大3項目を「、」で連結し、開始音・ログのキーは `RemainingFuel.Root` を維持する。

セット名は詳細画面でインライン編集できる（最大20文字、重複可）。未設定時は「セット N」を表示する。
名前は表示専用で、読み上げ文には含めない。ACE・LMU・GT7 共通仕様。
