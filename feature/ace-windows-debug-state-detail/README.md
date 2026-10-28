# ace-windows-debug-state-detail

ACE Windows 専用のデバッグ状態画面。シミュレーター、車両位置、フラッグ情報、周辺車両、自己ベストラップ、タイヤカーカス温度、燃料残量・残り周数、前後車両との時間差を8枚のカードで表示し、ドラッグしたカード順序を保存する。
自己完結モジュールとして、表示ヘルパーもこのモジュール内に保持する。
「その他」タブのバージョンを5回タップすると、選択中のシミュレーターに対応するペインを表示する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-debug-state-detail.svg)
<!-- MODULE-GRAPH-END -->

## 主要ファイル

- `AceWindowsDebugStateDetailPane.kt`: ViewModel を取得する Pane と、状態・操作を受け取るinternal な PaneContent。画面幅に合わせて1〜3列に切り替える。
- `AceWindowsDebugStateDetailViewModel.kt` / `AceWindowsDebugStateDetailUiState.kt` / `AceWindowsDebugStateDetailFlow.kt`: ACE 状態、受信済みカード、カード順序を管理・合成する。シミュレーターカードは常に ACE を表示し、有効にする。
- `AceWindowsDebugStateSource.kt` / `AceWindowsDebugState.kt`: ACE の UseCase 購読、状態の集約、受信済みカードの通知。
- `AceWindowsDebugStateCards.kt` / `DebugState*Card.kt`: ACE 専用のカード表示。フレーム、未取得表示、温度・ラップタイム等の整形ヘルパーも同梱する。
- `AceWindowsDebugStateDetailModule.kt` / `AceWindowsDebugStateModule.kt`: ViewModel と UseCase の Koin 定義。利用側が必要な Repository を登録する。

## カード順序と未取得表示

カードキーは feature 専用の `AceWindowsDebugStateCardKey` に定義する。`DebugStateCardOrderUseCases` で保存名と enum を変換し、未知の保存名を除外する。保存名は従来と同じ enum の `name` を使用する。

ドメイン層の Observe / Resolve / Save UseCase を使用し、保存済み順序を優先して不足カードを既定順で補完する。ACE 非対応カードは保存済み順序から除外する。

保存先はシミュレーターごとに別ファイル（`debug_state_card_order_preferences_<Simulator.id>.pb`）で、Repository は `named(Simulator.AceWindows.id)` で Koin に登録される。
既定順はシミュレーター、車両位置、フラッグ情報、周辺車両、自己ベストラップ、タイヤカーカス温度、燃料消費、前後車両との時間差（CAR_GAP）。
時間差は前車・後車の2行で符号を保持して小数1桁に整形し、非有限値は `--` と表示する。
未受信カードは薄く表示し、データがない場合は「未取得」、温度が欠損した輪は `-` と表示する。燃料または残り周数を受信すると燃料カードを有効にし、残り周数は正の有限値の場合のみ表示する。

## テスト

`src/jvmTest` に ACE の購読、状態集約、カード順序・保存、カード表示、フォーマッター、Pane の配線を検証するテストを置く。
Desktop のスクリーンショットテスト（`AceWindowsDebugStateDetailPaneScreenshotTest`）を持つ。ゴールデンPNGはCIが記録するためコミットしない。Android と Desktop で表示が異なるケースはないため Android 向けは追加しない。

## デバッグ読み上げ

並べ替えグリッドの上の固定カードで、このペインのシミュレーターの TopLevel 読み上げ項目と自由文言を指定する。既定選択は先頭項目で、空白のみでは送信できず、送信後も入力を保持する。固定カードはグリッド外なのでカード順序とドラッグのインデックスには影響しない。

ViewModel は `RequestDebugReadoutUseCase` から要求を送信し、Narrator が項目スイッチやテレメトリ受信に依存せず処理する。文言のテンプレート置換は行わない。利用側は `DebugReadoutRequestRepository` を登録する。
