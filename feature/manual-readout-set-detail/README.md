# manual-readout-set-detail

手動読み上げセットの詳細画面。シミュレーター ID とセット ID を受け取り、一覧上の番号によるタイトル・選択可能な項目と保存済み選択状態を表示する。LMU は「バーチャルエナジー残量」の1項目、ACE・GT7 は未対応の説明を表示する。文言編集は後続 PR。

`ManualReadoutSetDetailViewModel` は `ObserveManualReadoutSetsUseCase` で対象セットを観測し、`UpdateManualReadoutSetUseCase` で選択を保存する。未知キーは保持し、連続操作は直前の保存結果を参照する。セットが見つからない場合は説明を表示する。確認ダイアログから `DeleteManualReadoutSetUseCase` を実行し、削除完了時に Content の `onDeleted` を通知する。

`manualReadoutSetDetailModule` は `named(simulatorId)` の Repository を解決する。Android・Desktop 共通の `featureModules` に登録する。一覧モジュールはこのモジュールに依存せず、`app:shared` が詳細スロットに接続する。

JVM テストで ViewModel・Koin のパラメータ解決・項目選択・削除確認と配線を検証する。Desktop スクリーンショットテストで項目あり・項目なし・削除ダイアログを確認する。Android との表示差がないため Android 向けの複製は追加しない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-set-detail.svg)
<!-- MODULE-GRAPH-END -->
