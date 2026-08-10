# Nightly TODO List

毎晩 JST 午前3時ごろ、Claude Code がこのファイルを読み取り、内容を `docs/improvement-ideas.md` への追記案としてまとめ、下書き PR を作成する運用メモです。

## 使い方

- ここに、`improvement-ideas.md` へ追記したい改善案・気になっていることを箇条書きで書いておく。
- 粒度は `improvement-ideas.md` と同じく「対象・課題・改善案」がわかる程度でよい。
- 夜間バッチが処理した項目は、処理後にこのファイルから削除される（`improvement-ideas.md` 側に転記済みのため）。
- 夜間バッチは `improvement-ideas.md` を直接 main に書き換えず、必ず下書き PR を作成する（`docs/improvement-ideas.md` の変更は事前承認が必要という CLAUDE.md のルールを守るため）。
- 1晩に実施するのは「毎晩実行する項目」＋「その曜日のローテーション項目」までとし、リスト全体を一度に処理しようとしない（コンテキスト・使用量超過を防ぐため）。

## 毎晩実行する項目

- Zenn（https://zenn.dev）でAIコーディング・Compose Multiplatform・Android・Claude・Codex等に関する過去24時間以内の記事をWebで確認し、KoDriverに導入する価値があるとClaudeが判断したものがあれば improvement-ideas.md に追記する。
- Androidの最新動向（https://android-developers.googleblog.com を含む）をWebで調査し、KoDriverが取り入れていない・遅れている技術やプラクティスで採用価値があると判断したものがあれば improvement-ideas.md に追記する。

## 曜日ローテーション項目

- 月: プロジェクト全体を見て、バグ・不具合・配線漏れ（例: ReadoutItemKeyがlistPaneにはあるがNarrator側で未参照など）・仕様不備があれば improvement-ideas.md に追記する。 / アニメーション付与、識別しにくい配色の変更、GUI配置の見直し、マテリアルデザインからの逸脱など、UI/UXの改善提案があれば improvement-ideas.md に追記する。
- 火: `libs.versions.toml` に記載の各ライブラリについて最新安定版をWebで確認し、致命的な不具合がない範囲で更新余地があれば improvement-ideas.md に追記する（CLAUDE.mdの「ライブラリバージョン管理」方針に沿う）。 / `./gradlew koverXmlReport` の結果を確認し、カバレッジ100%方針から外れているのに除外理由が妥当でない箇所があれば improvement-ideas.md に追記する。
- 水: detekt・ktlintを実行し、エラーには至っていないが閾値（LongMethod・CyclomaticComplexMethod等）に近づいている箇所があれば improvement-ideas.md に追記する。 / `rg` 等で未使用のクラス・関数や、複数モジュールに散らばった重複実装がないか横断的に確認し、あれば improvement-ideas.md に追記する。
- 木: 認証・暗号化なしで稼働しているKtorサーバー（`0.0.0.0:8080`）まわりなど、既知の制約以外に新たなセキュリティリスクが増えていないか確認し、あれば improvement-ideas.md に追記する。 / displayNameやUI文言（表示名・ラベル・エラーメッセージ等）に表記揺れ（全角/半角、送り仮名の違い、用語の不統一など）がないか横断的に確認し、あれば improvement-ideas.md に追記する。
- 金: `assertModuleGraph` は通っていても、本来分離すべき責務が同一モジュールに混在していないかなど、依存関係グラフの設計面での気になる点があれば improvement-ideas.md に追記する。
- 土: GitHub Actionsの実行履歴を確認し、実行時間が長すぎるジョブや、再実行で通ることが多い不安定なテスト（flaky test）があれば improvement-ideas.md に追記する。
- 日: 例外の握りつぶし、ログレベルの不適切な使用（本来warn/errorであるべきものがinfo等）など、エラーハンドリング・ログ出力の一貫性に問題があれば improvement-ideas.md に追記する。
