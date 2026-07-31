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

## 共有メモリのMapper層で「有効なテレメトリが書き込まれたか」を判定していない

- **対象**: `core:ace-windows-data`（`AceWindowsMapper`）、`core:lmu-windows-data`
- **課題**: 起動直後など、ゲーム側がまだ共有メモリにテレメトリを書き込んでいない区間はゼロクリアされた値になりうるが、Mapper はそれをそのまま有効な値として返している（ACEの残燃料が起動直後に `0.0%` になり誤読み上げが発生した不具合はこれが原因の一つ）。
  **改善案**: `AceWindowsMapper` / `LmuWindowsMapper` 側で `status` 等の「有効なテレメトリが書き込まれたか」を示すフィールドを見て、無効なテレメトリをフィルタする仕組みの導入を検討する。

## `String.formatSliderLabel` は printf 形式のサブセットのみサポートする簡易実装

- **対象**: `core:designsystem`（`SliderLabelFormat.kt`）
- **課題**: commonMain からは `java.util.Formatter`（`String.format`）を利用できないため、スライダーラベル表示用に `%1$d`・`%1$s`・`%1$.Nf`・エスケープされた `%%` のみをサポートする自前の簡易フォーマッタを実装した。将来 strings.xml 側で複数引数（`%2$d` など）や他の書式指定子を使うテンプレートを追加すると、無言で正しく置換されない（プレースホルダーがそのまま残る）。
  **改善案**: 新しいプレースホルダーパターンを追加する際は `SliderLabelFormatTest` にケースを追加して検証すること。多様な書式が必要になった場合は `kotlinx-datetime` のような専用ライブラリの採用や、strings.xml 側のテンプレートをプレースホルダーなしの分割文字列（prefix/suffix）に変更する設計も検討する。

