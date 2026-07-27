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

## ACE (Assetto Corsa EVO) が Android では走行データを一切取得できない

- **対象**: `core:data`（`AndroidDataModule.kt` の `NoOpAceWindowsFuelRepository`）, `server`, `core:domain`（`AceWindowsFuelRepository`）
- **課題**: `Simulator.AceWindows` は `requiresKoDriverServer = true` で定義されているが、LMU 用の `/ws/lmu_windows/flags` のような KoDriver サーバー経由の WebSocket 配信が ACE には未実装。そのため Android 版では `AceWindowsFuelRepository` を暫定的に空 Flow を返す `NoOpAceWindowsFuelRepository`（`AndroidDataModule.kt`）にバインドしており、Android 端末では ACE の燃料残量（デバッグ画面の燃料カードなど）が常に「未取得」表示になる。
- **改善案**: LMU のフラグ配信（`ObserveLmuWindowsRaceFlagsUseCase` → `LmuWindowsFlagRepository` → `/ws/lmu_windows/flags`）と同様に、`/ws/ace_windows/fuel` 相当のエンドポイントを `server` に追加し、`WebSocketAceWindowsFuelRepository`（`core:data` の androidMain）を実装して `NoOpAceWindowsFuelRepository` を置き換える。

