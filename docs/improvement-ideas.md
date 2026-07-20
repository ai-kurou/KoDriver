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

## mockk テストの any() 使用

- **対象**: `:core:*data`、`:core:domain`、`:server`、`:feature:other*`、`:feature:lmu-windows*` は対応済み。残りは `feature:gt7-ps5-narrator`（`*EventProcessorTest.kt`・`*ViewModelTest.kt`）、`feature:gt7-ps5-connection`・`feature:readout-list`・`feature:server-connection` の各 `*ViewModelTest.kt` など（計 37 箇所、5 ファイル）。
  **課題**: mockk の `every`/`coEvery`/`verify` で `any()` を多用しており、引数の実値を検証できていない箇所がある。具体的な値が既知（固定の `ReadoutItemKey`・`Simulator.id` 文字列など）でも `any()` になっているケースと、可変長引数のスタブ（`saveTelemetryLog` の `createdAt`/`telemetryJson` など呼び出しごとに値が変わるもの）で本当に `any()` が必要なケースが混在している。
  **改善案**: `:server`（`KoDriverServiceAdvertiserTest`）で先行して `any()` → `withArg { }` / 具体値へ置き換え済み。残りのモジュールも同様に、実値を指定できる箇所は具体値化し、呼び出しごとに値が変わり検証不能な箇所のみ `any()` を残す方針で、モジュール単位に分割して段階的に対応する。
