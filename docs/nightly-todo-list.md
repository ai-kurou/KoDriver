# Nightly TODO List

毎晩 JST 午前3時ごろ、Claude Code がこのファイルを読み取り、内容を `docs/improvement-ideas.md` への追記案としてまとめ、下書き PR を作成する運用メモです。

## 使い方

- ここに、`improvement-ideas.md` へ追記したい改善案・気になっていることを箇条書きで書いておく。
- 粒度は `improvement-ideas.md` と同じく「対象・課題・改善案」がわかる程度でよい。
- 夜間バッチが処理した項目は、処理後にこのファイルから削除される（`improvement-ideas.md` 側に転記済みのため）。
- 夜間バッチは `improvement-ideas.md` を直接 main に書き換えず、必ず下書き PR を作成する（`docs/improvement-ideas.md` の変更は事前承認が必要という CLAUDE.md のルールを守るため）。

## リスト

- プロジェクト全体を見て、バグ・不具合・配線漏れ（例: ReadoutItemKeyがlistPaneにはあるがNarrator側で未参照など）・仕様不備があれば improvement-ideas.md に追記する。
- アニメーション付与、識別しにくい配色の変更、GUI配置の見直し、マテリアルデザインからの逸脱など、UI/UXの改善提案があれば improvement-ideas.md に追記する。
- Androidの最新動向をWebで調査し、KoDriverが取り入れていない・遅れている技術やプラクティスで採用価値があると判断したものがあれば improvement-ideas.md に追記する。
- `libs.versions.toml` に記載の各ライブラリについて最新安定版をWebで確認し、致命的な不具合がない範囲で更新余地があれば improvement-ideas.md に追記する（CLAUDE.mdの「ライブラリバージョン管理」方針に沿う）。
- `./gradlew koverXmlReport` の結果を確認し、カバレッジ100%方針から外れているのに除外理由が妥当でない箇所があれば improvement-ideas.md に追記する。
