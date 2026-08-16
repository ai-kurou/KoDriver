# Nightly TODO List

毎晩 JST 午前3時ごろ、Claude Code がこのファイルを読み取り、内容を `docs/improvement-ideas.md` への追記案としてまとめ、下書き PR を作成する運用メモです。

## 使い方

- ここに、`improvement-ideas.md` へ追記したい改善案・気になっていることを箇条書きで書いておく。
- 粒度は `improvement-ideas.md` と同じく「対象・課題・改善案」がわかる程度でよい。
- 「毎晩実行する項目」「曜日ローテーション項目」は毎晩・毎週繰り返しチェックする恒常的な項目のため、処理後もこのファイルから削除しない。
- 夜間バッチは `improvement-ideas.md` を直接 main に書き換えず、必ず下書き PR を作成する（`docs/improvement-ideas.md` の変更は事前承認が必要という CLAUDE.md のルールを守るため）。
- 1晩に実施するのは「毎晩実行する項目」＋「その曜日のローテーション項目」までとし、リスト全体を一度に処理しようとしない（コンテキスト・使用量超過を防ぐため）。
- Webサイトを元にした改善案を improvement-ideas.md に追記する場合は、参考にしたURLも併記する。
- improvement-ideas.md へ追記する前に、既存の改善案リストと重複していないか、`docs/resolved-improvement-ideas.md`（対応済み改善案の1行ログ）に同趣旨の記録が既にないか、また対象のコードを実際に読んで既に修正済みでないかを確認する（夜間バッチは Bash・git・gh を使えないため、Read/Grep で確認できる範囲に限る）。
- 改善案は「〜が気になる」のような抽象的な記述ではなく、対象のファイルパス・関数名・クラス名など具体的な箇所を必ず含める。
- 夜間バッチが作成する下書き PR は、他の作業と競合していないか（同じ箇所を対象にした未マージの PR が既にないか）を、マージする人間側でレビュー時に確認する。

## 毎晩実行する項目

- Zennで直近の記事を確認する場合、`WebSearch`はZennの新着記事のインデックス反映が遅く投稿直後の記事を拾えないことが分かっているため使わない。代わりに `WebFetch` で `https://zenn.dev/api/articles?order=latest&count=100`（Zennが公開している新着記事一覧のJSON API）を取得する。`count=100` は投稿数の多い日でも数時間分にしかならないため、`published_at` が24時間以上前に達するまで `page=2`, `page=3`, ... とページを進めて取得を続ける（レスポンスの `next_page` を参照する）。また `order=latest` でも一部トレンド記事等が時系列を乱して混在することがあるため、必ず各記事の `published_at` を確認して24時間以内かどうかを判定する（順序だけで打ち切らない）。集めた記事の中からタイトルがAIコーディング・Compose Multiplatform・Android・Claude・Codex等に関連するものを絞り込み、KoDriverに導入する価値があるとClaudeが判断したものがあれば improvement-ideas.md に追記する。
- Qiita（[https://qiita.com](https://qiita.com)）でも同様に、AIコーディング・Compose Multiplatform・Android・Claude・Codex等に関する過去24時間以内の記事をWebで確認し、KoDriverに導入する価値があるとClaudeが判断したものがあれば improvement-ideas.md に追記する。

## 曜日ローテーション項目

- 月: プロジェクト全体を見て、バグ・不具合・配線漏れ（例: ReadoutItemKeyがlistPaneにはあるがNarrator側で未参照など）・仕様不備があれば improvement-ideas.md に追記する。 / アニメーション付与、識別しにくい配色の変更、GUI配置の見直し、マテリアルデザインからの逸脱など、UI/UXの改善提案があれば improvement-ideas.md に追記する。 / `AndroidDataModule.kt`（`core:data`）・`DesktopDataModule.kt`（`core:data`）等のKoin DIモジュールを確認し、新設したRepository/UseCaseの`single { }`バインディングが漏れていないか（片方だけ実装してもう片方への配線を忘れる、ReadoutItemKeyの配線漏れと同種のバグパターン）があれば improvement-ideas.md に追記する。
- 火: `libs.versions.toml` に記載の各ライブラリについて最新安定版をWebで確認し、致命的な不具合がない範囲で更新余地があれば improvement-ideas.md に追記する（CLAUDE.mdの「ライブラリバージョン管理」方針に沿う）。 / `./gradlew koverXmlReport` の結果を確認し、カバレッジ100%方針から外れているのに除外理由が妥当でない箇所があれば improvement-ideas.md に追記する。 / Androidの最新動向（[https://android-developers.googleblog.com](https://android-developers.googleblog.com) を含む）をWebで調査し、KoDriverが取り入れていない・遅れている技術やプラクティスで採用価値があると判断したものがあれば improvement-ideas.md に追記する。
- 水: detekt・ktlintを実行し、エラーには至っていないが閾値（LongMethod・CyclomaticComplexMethod等）に近づいている箇所があれば improvement-ideas.md に追記する。 / `rg` 等で未使用のクラス・関数や、複数モジュールに散らばった重複実装がないか横断的に確認し、あれば improvement-ideas.md に追記する。 / `@Composable` 関数で不要な再コンポジションを招きやすい実装（`remember`/`derivedStateOf` の使い所、`Stable`/`Immutable` の付与漏れ、ラムダ・オブジェクトの再生成など）がないか確認し、あれば improvement-ideas.md に追記する。
- 木: 認証・暗号化なしで稼働しているKtorサーバー（`0.0.0.0:8080`）まわりなど、既知の制約以外に新たなセキュリティリスクが増えていないか確認し、あれば improvement-ideas.md に追記する。 / displayNameやUI文言（表示名・ラベル・エラーメッセージ等）に表記揺れ（全角/半角、送り仮名の違い、用語の不統一など）がないか横断的に確認し、あれば improvement-ideas.md に追記する。
- 金: `assertModuleGraph` は通っていても、本来分離すべき責務が同一モジュールに混在していないかなど、依存関係グラフの設計面での気になる点があれば improvement-ideas.md に追記する。 / ルートの `CLAUDE.md`（モジュール構成表など）や各モジュールの `README.md` が実際の実装から乖離していないか（モジュール追加・分割・パッケージ再編後の記述漏れ等）を確認し、あれば improvement-ideas.md に追記する。
- 土: GitHub Actionsの実行履歴を確認し、実行時間が長すぎるジョブや、再実行で通ることが多い不安定なテスト（flaky test）があれば improvement-ideas.md に追記する。
- 日: 例外の握りつぶし、ログレベルの不適切な使用（本来warn/errorであるべきものがinfo等）など、エラーハンドリング・ログ出力の一貫性に問題があれば improvement-ideas.md に追記する。
