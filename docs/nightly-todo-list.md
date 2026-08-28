# Nightly TODO List

毎晩 JST 午前5時ごろ、Claude Code がこのファイルを読み取り、内容を `docs/improvement-ideas.md` への追記案としてまとめ、下書き PR を作成する運用メモです。

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

- 対象キーワードは共通で「Compose・Android・Claude・Codex・Kotlin」とする。以下のZenn・Qiita双方とも、まずキーワードで検索してから過去24時間以内かを判定する（新着記事を全件取得してからキーワードでフィルターする方式は使わない。取得件数・処理量を減らすため）。
- 記事をimprovement-ideas.mdへの追記対象として拾うかどうかは、以下の2つの軸のいずれかを満たすかで判断する（両方を満たす必要はない、OR条件）。
  - **KoDriverへの適用可能性**: 記事の内容をKoDriverのコード・設計・運用にそのまま、または応用して取り入れられるか。
  - **技術的な有用性**: KoDriverのコードへ直接適用できるわけではなくても、開発プロセス・ツール活用・設計思想として一般的に有用で、知見として残す価値があるか（例: `docs/improvement-ideas.md` 自体の運用の仕組みはZennで見つけた知見を参考にしている。このように、AIエージェント活用のプラクティスや開発体験の工夫など、KoDriverの実装そのものではなくプロジェクト運営に活かせるものも対象に含める）。
- Zenn: キーワードごとに `WebFetch` で `https://zenn.dev/api/articles?topicname=<topicname>&order=latest&count=100`（Zennが公開しているトピック別記事一覧のJSON API）を取得する。`topicname` は各キーワードに対応するZennのトピック名（`compose`, `android`, `claude`, `codex`, `kotlin`。いずれもキーワードを小文字化したもの。「Compose Multiplatform」は独立したトピックが存在しないため`compose`で代用する）を指定する。`count=100` で足りない場合は `next_page` を参照して `page=2`, `page=3`, ... と進める。トピックによる絞り込みのため無関係な記事が混じることがあるが、タイトルからの関連性判断は次のステップに委ねてよい。取得した記事の中から `published_at` が24時間以内のものだけを対象にし、上記2軸のいずれかを満たすとClaudeが判断したものがあれば improvement-ideas.md に追記する。`WebSearch`はZennの新着記事のインデックス反映が遅く投稿直後の記事を拾えないことが分かっているため使わない。
- Qiita（[https://qiita.com](https://qiita.com)）: キーワードごとに `WebFetch` で `https://qiita.com/api/v2/items?query=<キーワード>+created:%3E%3D<24時間前の日付、JST基準でYYYY-MM-DD形式に丸めたもの>&page=1&per_page=100`（Qiita API v2の記事検索、`query`パラメータでキーワード検索し`created:>=`で日付範囲を絞り込む）を取得する。`created:>=`は日付単位（時刻は含まない）で指定し、JST基準で「24時間前の日付」に丸めた日付を使う（境界日の記事が漏れなく含まれるよう、日付の切り上げは行わない）。1回のレスポンスが100件（`per_page`の上限）に達した場合は、対象キーワードで24時間以内の記事を取りこぼさないよう `page=2`, `page=3`, ... と進めて取得を続ける。取得した記事のうち `created_at` が実際に過去24時間以内のものだけを対象にし、上記2軸のいずれかを満たすとClaudeが判断したものがあれば improvement-ideas.md に追記する。
- Zenn・Qiitaいずれも、絞り込み対象になった記事・ならなかった記事を問わず、過去24時間以内に取得できた全記事のURLと1行程度の要約を、最終出力（PRの説明欄に転記される）に一覧として必ず含める。ただし全件網羅（個別記事の内容まで深掘りする）は不要で、タイトルから明らかに関連しそうな記事だけを深掘りすればよい。
- Android Developers Blog: `WebFetch` で `https://android-developers.googleblog.com/feeds/posts/default?alt=json&max-results=5`（Bloggerが公開しているフィードのJSON API）を取得し、`published` が過去24時間以内の記事だけを対象にする。更新頻度が不定期なため曜日ローテーションではなく毎晩確認する。対象記事のうち、KoDriverが取り入れていない・遅れている技術やプラクティスで採用価値があると上記2軸のいずれかで判断したものがあれば improvement-ideas.md に追記する。過去24時間以内に該当記事がなかった場合もその旨を最終出力に含める。

## 曜日ローテーション項目

- 月: プロジェクト全体を見て、バグ・不具合・配線漏れ（例: ReadoutItemKeyがlistPaneにはあるがNarrator側で未参照など）・仕様不備があれば improvement-ideas.md に追記する。 / アニメーション付与、識別しにくい配色の変更、GUI配置の見直し、マテリアルデザインからの逸脱など、UI/UXの改善提案があれば improvement-ideas.md に追記する。 / `AndroidDataModule.kt`（`core:data`）・`DesktopDataModule.kt`（`core:data`）等のKoin DIモジュールを確認し、新設したRepository/UseCaseの`single { }`バインディングが漏れていないか（片方だけ実装してもう片方への配線を忘れる、ReadoutItemKeyの配線漏れと同種のバグパターン）があれば improvement-ideas.md に追記する。 / Androidダガシ🌰（[https://androiddagashi.github.io/](https://androiddagashi.github.io/)）は毎週日曜午後に新しい記事が公開されるため、月曜のこの項目で最新記事をWebで確認する。取り上げられているトピックの中でKoDriverに関連しそうなもの（Compose Multiplatform、Kotlin、Jetpack、Android開発全般の新機能・プラクティス等）があれば、参考記事のURLとあわせて improvement-ideas.md に追記する。
- 火: `libs.versions.toml` に記載の各ライブラリについて最新安定版をWebで確認し、致命的な不具合がない範囲で更新余地があれば improvement-ideas.md に追記する（CLAUDE.mdの「ライブラリバージョン管理」方針に沿う）。 / `./gradlew koverXmlReport` の結果を確認し、カバレッジ100%方針から外れているのに除外理由が妥当でない箇所があれば improvement-ideas.md に追記する。
- 水: detekt・ktlintを実行し、エラーには至っていないが閾値（LongMethod・CyclomaticComplexMethod等）に近づいている箇所があれば improvement-ideas.md に追記する。 / `rg` 等で未使用のクラス・関数や、複数モジュールに散らばった重複実装がないか横断的に確認し、あれば improvement-ideas.md に追記する。 / `@Composable` 関数で不要な再コンポジションを招きやすい実装（`remember`/`derivedStateOf` の使い所、`Stable`/`Immutable` の付与漏れ、ラムダ・オブジェクトの再生成など）がないか確認し、あれば improvement-ideas.md に追記する。
- 木: 認証・暗号化なしで稼働しているKtorサーバー（`0.0.0.0:8080`）まわりなど、既知の制約以外に新たなセキュリティリスクが増えていないか確認し、あれば improvement-ideas.md に追記する。 / displayNameやUI文言（表示名・ラベル・エラーメッセージ等）に表記揺れ（全角/半角、送り仮名の違い、用語の不統一など）がないか横断的に確認し、あれば improvement-ideas.md に追記する。
- 金: `assertModuleGraph` は通っていても、本来分離すべき責務が同一モジュールに混在していないかなど、依存関係グラフの設計面での気になる点があれば improvement-ideas.md に追記する。 / ルートの `CLAUDE.md`（モジュール構成表など）や各モジュールの `README.md` が実際の実装から乖離していないか（モジュール追加・分割・パッケージ再編後の記述漏れ等）を確認し、あれば improvement-ideas.md に追記する。
- 土: GitHub Actionsの実行履歴を確認し、実行時間が長すぎるジョブや、再実行で通ることが多い不安定なテスト（flaky test）があれば improvement-ideas.md に追記する。
- 日: 例外の握りつぶし、ログレベルの不適切な使用（本来warn/errorであるべきものがinfo等）など、エラーハンドリング・ログ出力の一貫性に問題があれば improvement-ideas.md に追記する。
