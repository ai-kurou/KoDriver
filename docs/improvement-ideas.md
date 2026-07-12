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

---

## ドメイン・配線

- **対象**: `LmuWindowsMyBestLapEnabledRepository`（`core/domain`）と
  `lmu-windows-readout-my-best-lap-detail` feature
  **課題**: Repository・対応する `ObserveLmuWindowsMyBestLapEnabledUseCase` /
  `SaveLmuWindowsMyBestLapEnabledUseCase` は `core/domain` に定義され `desktopDataModule` にも
  バインドされているが、feature 側のどの Koin モジュールからも `get()` されていない（実装・UI側で
  一切参照されていないことをgrepで確認済み）。`Gt7Ps5RemainingFuelLapsEnabledRepository` も同様の
  未配線状態だったが、調査の結果「実は`ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root`
  （readout-listの共通スイッチ）経由で別途正しく実現されており、この専用Repositoryは
  同じ`ReadoutPreferencesRepository`への薄いラッパーに過ぎない完全な重複だった」と判明し、
  削除した（PR #557）。`LmuWindowsMyBestLapEnabledRepository`も同一パターンの可能性が高いが未確認。
  **改善案**: `ReadoutItemKey.LmuWindows.MyBestLap.Root`経由で同等のON/OFFが既に実現されていないか
  `LmuWindowsNarratorViewModel`を確認し、重複であれば同様に削除する。
