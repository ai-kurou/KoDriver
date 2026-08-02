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

## Narrator の読み上げ判定入力とテレメトリログ記録内容が別々にハードコードされている

- **対象**: `feature:lmu-windows-narrator`（`DetermineLmuWindowsNarratorReadoutUseCase.kt`, `LmuWindowsNarratorEventProcessor.kt` の各 `buildTelemetryLogJson`/`buildPitTimingTelemetryLogJson`）
  **課題**: `TelemetryLog`（`core:domain`）は `telemetryJson: String` という単一フィールドしか持たず、各読み上げイベント種別ごとに `buildTelemetryLogJson` が個別に組み立てている。例えばタイヤ温度低下判定（`determineTyreTemperatureLow`）は `tyreCarcassTemperature` と `raceFlags` の複数ソースを使い、対応する記録処理も両方を含めているが、判定側（Determine）と記録側（build*TelemetryLogJson）は同期する仕組みがなく別々にハードコードされている。今後、判定ロジックに新しい共有メモリセグメント由来の入力を追加した際、対応する記録処理の更新を忘れると、判定には使われているのにログJSONには含まれない「抜け漏れ」が構造的に起こりうる。また `LmuWindowsNarratorState.toJsonString()` は生の `toString()` を `{"raw":"..."}` に包むだけで、他セグメント由来の値が混ざっていても構造化JSONとしては見えない。
  **改善案**: イベント種別ごとに、判定に使う入力データをまとめた1つの入力データクラス（例: `TyreTemperatureReadoutInput` のような型）を定義し、`determine()` と `buildTelemetryLogJson()` の両方にその型を渡す形に統一する。ただし入力データクラスを共有するだけでは、`buildTelemetryLogJson()`/`buildPitTimingTelemetryLogJson()` 側が引き続き手動でフィールドを選んで組み立てる限り、新しいフィールドの記録漏れは防げない。そのため入力データクラス全体を自動的にシリアライズする契約（`kotlinx.serialization` 等による共通 serializer）を導入し、`build*TelemetryLogJson` はその共通 serializer を経由して入力データクラス全体をJSON化する形に統一する。`LmuWindowsNarratorEventProcessor` 側（例: `processTyreTemperature()`）ではイベント種別と入力データクラスを1対1で対応付け、新しいソースを追加する際は入力データクラスへのフィールド追加のみで判定・記録の両方に自動反映されるようにし、同期漏れを構造的に防ぐ。`Determine*UseCase` はログ出力という副作用を持たない純粋な判定ロジックのままにすること（副作用は `LmuWindowsNarratorEventProcessor` 側に置く）。（PR #893 の Sourcery レビューでも、`buildTelemetryLogJson` 内の `state`/`settings`/`finalState` を含めた残りのフィールドも手動連結ではなく専用DTOのシリアライズに統一してはどうかという同趣旨の指摘があった。）
