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

## モジュール構成

- **対象**: `core/ace-windows-data/src/main/kotlin/kurou/kodriver/core/acewindowsdata/datasource/{SharedMemoryReader.kt,Kernel32FileMapping.kt,WindowsSharedMemoryReader.kt}`、`core/lmu-windows-data` の同名ファイル群
  **課題**: `:core:ace-windows-data` は `:core:lmu-windows-data` の共有メモリ読み取りロジック（`SharedMemoryReader` インターフェース・`Kernel32FileMapping`（JNA）・`WindowsSharedMemoryReader`）をほぼそのままコピーして作成したため、SonarQubeの重複コード指摘（New Code の Duplicated Lines 17.3%、`WindowsSharedMemoryReader.kt` 84.7%・`Kernel32FileMapping.kt` 73.3%・`AceWindowsGraphicsSharedMemorySource.kt` 46.3%）が発生している（PR #781）。
  **改善案**: `SharedMemoryReader`/`Kernel32FileMapping`/`WindowsSharedMemoryReader` を新規共通モジュール（例: `:core:windows-shared-memory-data`）に切り出し、`:core:lmu-windows-data` と `:core:ace-windows-data` の両方がそれに依存する形にリファクタリングする。新規モジュール追加は `moduleGraphAssert` の `allowed` 配列変更を伴うため、着手前にユーザー確認が必要。

## Simulator の表示名・アイコン分岐が複数モジュールに重複している

- **対象**: `feature:readout-list`（`ReadoutListPane.kt`）, `feature:debug-state-detail`（`DebugStateDetailPane.kt`）, `feature:telemetry-log-list`（`TelemetryLogListPane.kt`）
  **課題**: `Simulator` の表示名（`simulatorDisplayName`）とアイコン（`simulatorIcon`）を返す `when` 式が3モジュールにほぼ同一の内容で重複定義されている。文字列リソースも `feature:readout-list` と `feature:debug-state-detail` の `strings.xml` に同じキー（`simulator_name_lmu` など）が重複している。新しい `Simulator` を追加するたびに同じ分岐を複数箇所へ手作業で追加する必要があり、追加漏れがコンパイルエラーで検出される（網羅的 `when` のため）ものの、手間と重複が大きい。
  **改善案**: `Simulator` の表示名・アイコンを `core:designsystem` または `core:domain` に集約したユーティリティ（例: `Simulator.displayNameRes()` 拡張、共通の `SimulatorIconRepository` 的な仕組み）としてまとめ、各 feature モジュールから参照する形に統一する。ただし `core:designsystem` は Compose リソースを持つが `core:domain` は持たない現状の依存方向を踏まえた設計検討が必要。

