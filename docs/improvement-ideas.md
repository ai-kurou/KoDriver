# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  **課題**: <現状の問題・気になっている点>
  **改善案**: <どう変えたいか>
```

## readout-list のキュー追加トグルのUIテスト未追加

- **対象**: `feature:readout-list` の `ReadoutListPane.kt`（`FilledIconToggleButton` → `onQueueEnabledChanged`）と `ReadoutContentTest.kt`
  **課題**: listPane のキュー追加トグル（`FilledIconToggleButton`）は、既存の読み上げON/OFF `Switch` と同じ行に並んでいるが、`ReadoutContentTest.kt` では常に空実装（`onQueueEnabledChanged = { _, _ -> }`）しか渡されておらず、実際にクリックして `ReadoutItemKey`・有効値が正しく伝播することを検証するテストがない。隣接する `Switch`（`onReadoutEnabledChanged`）には `hasSwitchRole()` を使った `performClick()` 検証の先例テストが存在する（PR #667 レビュー時に指摘）。
  **改善案**: `ReadoutContentTest.kt` に、キュー追加トグルをクリックして `onQueueEnabledChanged` が正しい `ReadoutItemKey` と値で呼ばれることを検証するテストケースを追加する。

## mockk テストの any() 使用

- **対象**: `:app` 以下・`:server` 以外のほぼ全モジュール。特に `core:domain`（`*UseCaseTest.kt` 多数）、`feature:lmu-windows-narrator`・`feature:gt7-ps5-narrator`（`*EventProcessorTest.kt`・`*ViewModelTest.kt`）、`feature:gt7-ps5-connection`・`feature:lmu-windows-connection`・`feature:other-list`・`feature:other-server-ip-detail`・`feature:other-theme-detail`・`feature:readout-list`・`feature:server-connection` の各 `*ViewModelTest.kt` など（計 656 箇所、58 ファイル）。
  **課題**: mockk の `every`/`coEvery`/`verify` で `any()` を多用しており、引数の実値を検証できていない箇所がある。具体的な値が既知（固定の `ReadoutItemKey`・`Simulator.id` 文字列など）でも `any()` になっているケースと、可変長引数のスタブ（`saveTelemetryLog` の `createdAt`/`telemetryJson` など呼び出しごとに値が変わるもの）で本当に `any()` が必要なケースが混在している。
  **改善案**: `:server`（`KoDriverServiceAdvertiserTest`）で先行して `any()` → `withArg { }` / 具体値へ置き換え済み。残りのモジュールも同様に、実値を指定できる箇所は具体値化し、呼び出しごとに値が変わり検証不能な箇所のみ `any()` を残す方針で、モジュール単位に分割して段階的に対応する。
