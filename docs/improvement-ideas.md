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

## 設計・アーキテクチャ

- **対象**: `ReadoutNavigationState.kt`（`feature:readout-list`）・`OtherNavigationState.kt`（`app:shared`）・`TelemetryLogNavigationState.kt`（`feature:telemetry-log-list`）
  **課題**: list/detailペインの切り替え状態を`NavBackStack<NavKey>`で保持しているが、`clear()`→`add()`による「1要素の置き換え」としてのみ使っており、Navigation3本来の想定（`NavDisplay`によるレンダリング、pushによる複数エントリの積み上げ、戻る操作での自動pop）は利用していない。実際の画面遷移制御はMaterial3 Adaptiveの`rememberListDetailPaneScaffoldNavigator`/`ListDetailPaneScaffoldRole`が担っており、`NavBackStack`はそれと並行して「現在どちらのペインを表示しているか」を表す状態変数として存在するのみ。
  **改善案**: Navigation3のサンプル・公式ドキュメントにあるMaterial3 AdaptiveとNavDisplayの統合パターン（両者で単一のバックスタックを共有する設計）への寄せ替えを検討する。ただし現状の実装（PR #1069, #1075, #1077, #1078）で機能的な不具合は出ていないため、優先度は低め。

## セキュリティ

- **対象**: `server/src/main/kotlin/kurou/kodriver/Application.kt`（`Application.module`）・`server/src/main/kotlin/kurou/kodriver/TelemetryWebSocket.kt`
  **課題**: `install(WebSockets) { ... }` および各 `webSocket(...)` ルートで `Origin` ヘッダの検証（Ktorの`WebSockets`プラグインが持つ`CheckOrigin`相当の仕組み）を一切行っていない。ブラウザはWebSocket接続に対してXHR/fetchのようなCORS（Same-Origin Policy）を強制しないため、信頼できないLAN内の別端末で開かれた悪意あるWebページのJavaScriptから `ws://<Windows PC のローカルIP>:8080/ws/<Simulator.id>/<feature>` へ接続され、走行中のテレメトリ情報（車両接近・タイヤ状態等）を外部にWebSocket越しに読み取られる恐れがある（CSWSH: Cross-Site WebSocket Hijacking）。CLAUDE.mdの「現時点では認証・暗号化を実装していないため、信頼できるLAN内でのみ使用すること」はネットワークレベルの信頼を前提にした記述だが、CSWSHはLAN内の（利用者本人が操作する）ブラウザがLAN外の悪意あるページを閲覧しただけでも成立し得る点で別種のリスク。
  **改善案**: `install(WebSockets)`に`Origin`検証（許可するOriginが存在しない/不明な場合の扱いを含む）を追加するか、少なくとも既知の制約として本ファイル・READMEに明記する。認証・暗号化の実装（別途検討中）とは独立して対応可能。

## UI/UX・表記

- **対象**: `feature/ace-windows-readout-remaining-fuel-detail`・`feature/gt7-ps5-readout-remaining-fuel-detail` の `strings.xml`（`remaining_fuel_title`, `remaining_fuel_preview_label`）と `core/designsystem/.../ReadoutItemDisplay.kt`（`readout_item_remaining_fuel`）
  **課題**: 読み上げ項目一覧（`core/designsystem`の`readout_item_remaining_fuel` = 「燃料残量」）およびGT7版detail画面のタイトル・プレビュー文言は「燃料残量」で統一されているのに対し、ACE版detail画面（`feature/ace-windows-readout-remaining-fuel-detail/src/commonMain/composeResources/values/strings.xml`）のみ「残り燃料」「残り燃料警告」という別表記になっている。`ReadoutItemDisplay.kt`では`"gt7_ps5_remaining_fuel"`と`"ace_windows_remaining_fuel"`が同じ`readout_item_remaining_fuel`（「燃料残量」）にマッピングされているため、一覧画面とACEのdetail画面とで同一機能の呼び方が食い違って見える。
  **改善案**: ACE版detail画面の文言を「燃料残量」「燃料残量警告」に統一する（GT7版detail画面の文言をそのまま踏襲する）。
- **対象**: `core/designsystem/.../strings.xml`（`readout_item_my_best_lap` = 「自己ベストラップ」）と `feature/debug-state-detail/src/commonMain/composeResources/values/strings.xml`（`debug_state_best_lap_title` = 「ベストラップ」）
  **課題**: 読み上げ項目一覧および`feature/lmu-windows-readout-my-best-lap-detail`・`feature/gt7-ps5-readout-my-best-lap-detail`では「自己ベストラップ」表記だが、`feature/debug-state-detail`のデバッグ表示ラベルのみ「自己」が付かない「ベストラップ」表記になっている。
  **改善案**: `debug_state_best_lap_title`を「自己ベストラップ」に揃えるか、意図的に短縮している場合はその理由をコメントで残す。

