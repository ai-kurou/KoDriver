# manual-readout-set-detail

手動読み上げセットの詳細画面。シミュレーター ID とセット ID を受け取り、一覧上の番号によるタイトル・選択可能な項目と保存済み選択状態を表示する。LMU は「バーチャルエナジー残量」の1項目、ACE・GT7 は未対応の説明を表示する。「その他」詳細と同じ `DetailPaneScaffold` を使い、`canNavigateBack` が true の場合に戻るボタンを表示し、`onBack` を通知する。選択中の項目の下に文言入力欄を表示する。

`ManualReadoutSetDetailViewModel` は `ObserveManualReadoutSetsUseCase` で対象セットを観測し、`UpdateManualReadoutSetUseCase` で選択と項目ごとの文言を保存する。未知キーは保持し、連続操作は直前の保存結果を参照する。セットが見つからない場合は説明を表示する。確認ダイアログから `DeleteManualReadoutSetUseCase` を実行し、削除完了時に Content の `onDeleted` を通知する。

文言は入力ごとに前後の空白を除去して30文字まで保存し、未保存なら項目の既定文言を表示する。
空欄は読み上げず、リセットで既定文言へ戻す。選択を解除しても編集した文言は保持する。
LMU の残量では `{percent}` を現在の残量（%）に置換し、挿入ボタンと未知プレースホルダーの警告を提供する。
自動読み上げと同じ共通入力部品を使い、試聴ボタンは表示しない。

`manualReadoutSetDetailModule` は `named(simulatorId)` の Repository を解決する。Android・Desktop 共通の `featureModules` に登録する。一覧モジュールはこのモジュールに依存せず、`app:shared` が詳細スロットに接続する。

JVM テストで ViewModel・Koin のパラメータ解決・項目選択・文言保存（空欄・正規化・上限・リセット）・削除確認・戻るボタンの表示とクリック・配線を検証する。Desktop スクリーンショットテストで項目あり・項目なし・戻るボタンなし・1ペインの戻るボタン・削除ダイアログ・選択項目の既定文言・編集文言の警告・空欄を確認する。Android との表示差がないため Android 向けの複製は追加しない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-set-detail.svg)
<!-- MODULE-GRAPH-END -->
