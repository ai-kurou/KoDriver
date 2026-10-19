# manual-readout-detail

手動読み上げセットの詳細・編集画面を提供する feature モジュール。シミュレーターIDとセットIDを受け取り、項目の選択・順序・読み上げ文・セット名・Windows デスクトップ用ホットキーを編集する。一覧画面とは `app:shared` の詳細スロットで接続する。

画面と読み上げの全体仕様、シミュレーター別項目、データ取得・欠損時のルール、保存とホットキーの制約は [`docs/manual-readout.md`](../../docs/manual-readout.md) を参照する。

## 画面の責務

- 選択済み項目を表示し、最大3項目まで追加、削除、ドラッグによる並べ替えを保存する。
- 項目ごとの文言を編集し、既定文言へのリセット、プレースホルダーの挿入・検査を提供する。
- 全項目の文言を選択順に連結した試聴文を表示・再生する。
- セット名をインライン編集し、セット削除は確認後に実行する。
- Windows デスクトップでのみ、セットごとのグローバルホットキーと登録状態を表示・編集する。

## 主要なファイル

- `ManualReadoutSetDetailPane.kt`: 詳細画面、項目ピッカー、文言入力、試聴、削除確認、ホットキーUI。
- `ManualReadoutSetDetailViewModel.kt` / `ManualReadoutSetDetailUiState.kt`: セットの観測、編集内容の保存、プレビュー文の生成。
- `ManualReadoutSetDetailModule.kt`: シミュレーター名付き依存を解決する Koin 定義。
- `ManualReadoutSetDetailPaneTest.kt`, `ManualReadoutSetDetailViewModelTest.kt`, `ManualReadoutSetDetailModuleTest.kt`: UI、状態更新、DI と操作の検証。
- `ManualReadoutSetDetailPaneScreenshotTest.kt`: 詳細・項目ピッカー・文言・削除・ホットキー表示のスクリーンショット検証。

一覧モジュールはこのモジュールに依存しない。Android・Desktop 共通の composition root が詳細モジュールを登録し、`app:shared` がスロットを接続する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-manual-readout-detail.svg)
<!-- MODULE-GRAPH-END -->
