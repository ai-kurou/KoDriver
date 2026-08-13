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

## 設計・アーキテクチャ

- **対象**: `ReadoutNavigationState.kt`（`feature:readout-list`）・`OtherNavigationState.kt`（`app:shared`）・`TelemetryLogNavigationState.kt`（`feature:telemetry-log-list`）
  **課題**: list/detailペインの切り替え状態を`NavBackStack<NavKey>`で保持しているが、`clear()`→`add()`による「1要素の置き換え」としてのみ使っており、Navigation3本来の想定（`NavDisplay`によるレンダリング、pushによる複数エントリの積み上げ、戻る操作での自動pop）は利用していない。実際の画面遷移制御はMaterial3 Adaptiveの`rememberListDetailPaneScaffoldNavigator`/`ListDetailPaneScaffoldRole`が担っており、`NavBackStack`はそれと並行して「現在どちらのペインを表示しているか」を表す状態変数として存在するのみ。
  **改善案**: Navigation3のサンプル・公式ドキュメントにあるMaterial3 AdaptiveとNavDisplayの統合パターン（両者で単一のバックスタックを共有する設計）への寄せ替えを検討する。ただし現状の実装（PR #1069, #1075, #1077, #1078）で機能的な不具合は出ていないため、優先度は低め。

