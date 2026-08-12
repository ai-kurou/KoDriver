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

## 開発フロー・CI

- **対象**: `.github/workflows/on-pull-request.yml`
  **課題**: 現状 detekt/ktlint による静的解析はCIにあるが、PR差分に対するAIレビューコメントの仕組みはない。Qiita記事「GitHub ActionsのPR自動レビューを公式claude-code-actionで組む（APIキー課金なし）」（https://qiita.com/itaraiguma/items/3a723688a2fe571c33ec , 2026-07-31）で、`nightly-todo.yml` と同じ `CLAUDE_CODE_OAUTH_TOKEN`（Pro/Maxサブスク枠）を使い、`pull_request` トリガー・Bot生成PR除外（`github.event.sender.type == 'User'`）・`concurrency: cancel-in-progress: true` によるインラインPRレビュー構成が紹介されていた。
  **改善案**: 既存のdetekt/ktlint/Codacyとの指摘重複やAPIコスト（サブスク枠消費）を踏まえたうえで、`claude_args` で許可ツールを絞ったインラインレビューjobの追加余地を検討する。優先度は低め。

## 重複実装・未使用コード

- **対象**: `feature/gt7-ps5-readout-remaining-fuel-detail/.../Gt7Ps5ReadoutRemainingFuelDetailViewModel.kt` と `feature/ace-windows-readout-remaining-fuel-detail/.../AceWindowsReadoutRemainingFuelDetailViewModel.kt`
  **課題**: 両ViewModel（各41行）は、UseCase名・デフォルト定数名・`SpeechEvent`種別以外がほぼ同一実装（`observeThresholdPercentage().map{...}.stateIn(...)` の配線、`onThresholdChanged`/`onThresholdReset`/`onPreviewClicked` の構造）。対応する `Pane` 側も同様のパターンと思われる。
  **改善案**: 「残量閾値を保存・プレビュー再生する詳細画面」共通の抽象（例: UseCase群とSpeechEventを引数に取る共通ViewModel基底・共通Pane Composable）を `core:domain`/`core:designsystem` に切り出せないか調査する。ただし各featureモジュールの独立性（CLAUDE.mdのモジュール構成方針）とのトレードオフを踏まえて検討する。
