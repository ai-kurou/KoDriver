# lmu-windows-debug-state-detail

LMU Windows 専用のデバッグ状態画面。テレメトリ、レース状態、タイヤ・ブレーキ、燃料消費、並走時間などを18枚のカードで表示し、ドラッグしたカード順序を保存する。
既存の `:feature:debug-state-detail` と並存する自己完結モジュールで、表示ヘルパーもこのモジュール内に保持する。
現在はアプリから使われておらず、Koin モジュールもアプリには登録していない。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-debug-state-detail.svg)
<!-- MODULE-GRAPH-END -->

## 主要ファイル

- `LmuWindowsDebugStateDetailPane.kt`: ViewModel を取得する Pane と、状態・操作を受け取る公開 PaneContent。画面幅に合わせて1〜3列に切り替える。
- `LmuWindowsDebugStateDetailViewModel.kt` / `LmuWindowsDebugStateDetailUiState.kt`: LMU 状態、受信済みカード、カード順序を管理する。シミュレーターカードは常に LMU を表示し、有効にする。
- `DebugStateCardOrderUseCases.kt`: カード順序の Observe / Resolve / Save UseCase を束ねる `DebugStateCardOrderUseCases`。
- `LmuWindowsDebugStateSource.kt` / `LmuWindowsDebugState.kt`: LMU の UseCase 購読、状態の集約、受信済みカードの通知。
- `LmuWindowsSideBySideDurationTracker.kt`: 左右それぞれの最長並走時間を車両IDごとに追跡する。Source が並走中に周期的に更新する。
- `LmuWindowsDebugStateCards.kt` / `DebugState*Card.kt`: LMU 専用のカード表示。フレーム、未取得表示、温度・摩耗・ラップタイム等の整形ヘルパーも同梱する。
- `LmuWindowsDebugStateDetailModule.kt` / `LmuWindowsDebugStateModule.kt`: ViewModel と UseCase の Koin 定義。利用側が必要な Repository を登録する。

## カード順序と未取得表示

ドメイン層の Observe / Resolve / Save UseCase を使用し、保存済み順序を優先して不足カードを既定順で補完する。
未受信カードは薄く表示する。ブレーキ残量は各輪の残量（小数1桁の %）と厚さ（小数3桁の mm）を表示し、欠損した輪は `--`、取得失敗時は「取得できません」と表示する。

## テスト

`src/jvmTest` に LMU の購読、カード順序、並走時間、カード表示、フォーマッター、Pane の配線を検証するテストを置く。
Desktop のスクリーンショットテスト（`LmuWindowsDebugStateDetailPaneScreenshotTest`）を持つ。ゴールデンPNGはCIが記録するためコミットしない。Android と Desktop で表示が異なるケースはないため Android 向けは追加しない。
