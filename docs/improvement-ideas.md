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

- **対象**: `Gt7Ps5RemainingFuelLapsEnabledRepository`（`core/domain`）と
  `gt7-ps5-readout-remaining-fuel-laps-detail` feature、`LmuWindowsMyBestLapEnabledRepository`
  （`core/domain`）と `lmu-windows-readout-my-best-lap-detail` feature
  **課題**: 両Repository・対応する `Observe*EnabledUseCase` / `Save*EnabledUseCase` は
  `core/domain` に定義され `desktopDataModule` にもバインドされているが、feature 側のどの Koin
  モジュールからも `get()` されていない（実装・UI側で一切参照されていないことをgrepで確認済み）。
  CLAUDE.md が警告する「ReadoutItemKeyの配線漏れ」と類似した、死んだ実装の可能性がある。
  **改善案**: 本来「燃料残り周回数」「自己ベストラップ」アナウンスのON/OFFスイッチとして
  UIに表示・配線される意図だったのか仕様を確認し、必要なら detail 画面のUseCase呼び出しに
  正しく組み込むか、不要であれば削除する。
