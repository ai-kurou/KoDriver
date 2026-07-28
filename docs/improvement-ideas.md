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

## Simulator の表示名・アイコン分岐が複数モジュールに重複している

- **対象**: `feature:readout-list`（`ReadoutListPane.kt`）, `feature:debug-state-detail`（`DebugStateDetailPane.kt`）, `feature:telemetry-log-list`（`TelemetryLogListPane.kt`）
  **課題**: `Simulator` の表示名（`simulatorDisplayName`）とアイコン（`simulatorIcon`）を返す `when` 式が3モジュールにほぼ同一の内容で重複定義されている。文字列リソースも `feature:readout-list` と `feature:debug-state-detail` の `strings.xml` に同じキー（`simulator_name_lmu` など）が重複している。新しい `Simulator` を追加するたびに同じ分岐を複数箇所へ手作業で追加する必要があり、追加漏れがコンパイルエラーで検出される（網羅的 `when` のため）ものの、手間と重複が大きい。
  **改善案**: `Simulator` の表示名・アイコンを `core:designsystem` または `core:domain` に集約したユーティリティ（例: `Simulator.displayNameRes()` 拡張、共通の `SimulatorIconRepository` 的な仕組み）としてまとめ、各 feature モジュールから参照する形に統一する。ただし `core:designsystem` は Compose リソースを持つが `core:domain` は持たない現状の依存方向を踏まえた設計検討が必要。

## narrator系featureモジュール（LMU/GT7/ACE）の commonMain が js/wasmJs ターゲットでコンパイルできない

- **対象**: `feature:lmu-windows-narrator`・`feature:gt7-ps5-narrator`・`feature:ace-windows-narrator` の `*NarratorViewModel.kt`（`System.currentTimeMillis()` を直接参照）・`*WavNarratorEngine.kt`（`@Volatile` を使用）
- **課題**: `System.currentTimeMillis()` は `java.lang.System` の呼び出しであり、`@Volatile`（`kotlin.jvm.Volatile`）も JVM/Android 専用アノテーションのため、どちらも commonMain からは js/wasmJs ターゲット向けにコンパイルできない（`compileKotlinJs`/`compileKotlinWasmJs` が `Unresolved reference` で失敗する）。この問題は ACE 追加以前から LMU/GT7 の narrator モジュールに存在する既存のバグで、`:app:webApp` がビルド設定のみで未実装のため `preMergeCheck` では検出されず、CIも通ってしまっている。
- **改善案**: `System.currentTimeMillis()` は `kotlinx.datetime.Clock.System.now()` 等のマルチプラットフォーム対応APIに置き換える。`@Volatile` は `kotlin.concurrent.Volatile`（Kotlin 1.9+ のマルチプラットフォーム版アノテーション）に置き換える。3モジュールまとめて対応するのが望ましい。

