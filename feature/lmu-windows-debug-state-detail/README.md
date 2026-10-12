# lmu-windows-debug-state-detail

LMU Windows 専用のデバッグ状態画面。テレメトリ、レース状態、タイヤ・ブレーキ、燃料消費、並走時間などを18枚のカードで表示し、ドラッグしたカード順序を保存する。
自己完結モジュールとして、表示ヘルパーもこのモジュール内に保持する。
「その他」タブのバージョンを5回タップすると、選択中のシミュレーターに対応するペインを表示する。

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

カードキーは feature 専用の `LmuWindowsDebugStateCardKey` に定義する。`DebugStateCardOrderUseCases` で保存名と enum を変換し、未知の保存名を除外する。保存名は従来と同じ enum の `name` を使用する。

ドメイン層の Observe / Resolve / Save UseCase を使用し、保存済み順序を優先して不足カードを既定順で補完する。

保存先はシミュレーターごとに別ファイル（`debug_state_card_order_preferences_<Simulator.id>.pb`）で、Repository は `named(Simulator.LmuWindows.id)` で Koin に登録される。
未受信カードは薄く表示する。ブレーキ残量は各輪の残量（小数1桁の %）と厚さ（小数3桁の mm）を表示し、欠損した輪は `--`、取得失敗時は「取得できません」と表示する。

## オーバーレイ表示テスト

グリッドの先頭に、入力した文字列をオーバーレイへ表示するカードを全幅で固定表示する。このカードは並べ替え・順序保存の対象外。ドラッグの移動位置はカードキーから順序上のインデックスへ変換する。

このシミュレーターに属する全読み上げ項目からキーを選び、空白以外の文字列を送信できる。入力文を trim して既存の `SaveTelemetryLogUseCase` で保存し、同一プロセスのデスクトップオーバーレイへ届ける。音声は鳴らさず、接頭辞も付けない。オーバーレイの購読対象にするため `SPOKEN` としてログに残す。送信後は入力文だけをクリアして選択項目を保持する。Android からの配信は対象外。

利用側は `TelemetryLogRepository` も Koin に登録する。読み上げモジュールと同じ `factory { SaveTelemetryLogUseCase(get()) }` で既存 UseCase を登録する。

## テスト

`src/jvmTest` に LMU の購読、カード順序、並走時間、カード表示、フォーマッター、Pane の配線を検証するテストを置く。
Desktop のスクリーンショットテスト（`LmuWindowsDebugStateDetailPaneScreenshotTest`）を持つ。ゴールデンPNGはCIが記録するためコミットしない。Android と Desktop で表示が異なるケースはないため Android 向けは追加しない。
