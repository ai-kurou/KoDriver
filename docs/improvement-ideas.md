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

## ViewModel / UseCase 責務分離

- **対象**: `feature/readout-list/.../ReadoutListViewModel.kt`
  **課題**: 保存済み読み上げ順序と現在のデフォルト順序を突き合わせ、削除済み項目を除外し、新規項目を末尾に補完するロジックが ViewModel 内にある。これは UI 表示都合だけでなく、読み上げ項目順序の整合性を保つドメインルールに近い。
  **改善案**: `ResolveReadoutOrderUseCase` などへ切り出し、`ObserveReadoutOrderUseCase` の結果と `ReadoutListItemType.defaultOrder(simulator)` から有効な順序を生成する責務を domain 側へ寄せる。

- **対象**: `feature/telemetry-log-list/.../TelemetryLogListViewModel.kt`
  **課題**: テレメトリログの新しい順ソートと、削除後などに存在しなくなった選択IDを無効化する処理が ViewModel 内にある。現状は小さいが、ログ検索・フィルタ・ページングを追加すると ViewModel の表示整形責務が膨らみやすい。
  **改善案**: 必要になった段階で `ObserveSortedTelemetryLogsUseCase` やログ一覧用の query UseCase へ切り出し、ViewModel は選択状態とダイアログ状態だけを扱う。

- **対象**: `feature/other-server-ip-detail/.../OtherServerIpDetailViewModel.kt`
  **課題**: IPv4 形式チェック、接続確認付き保存、接続警告後の強制保存が ViewModel 内にある。画面専用の入力処理としては許容範囲だが、接続先設定の保存ルールとして再利用される場合は責務が重くなる。
  **改善案**: 他画面や自動設定で再利用する段階で `ValidateIpAddressUseCase` や `SaveServerIpWithConnectivityCheckUseCase` へ切り出す。

- **対象**: `feature/gt7-ps5-narrator/.../Gt7Ps5NarratorViewModel.kt`
  **課題**: 読み上げ判定自体は `DetermineGt7Ps5NarratorReadoutUseCase` に切れているが、優先度に基づく読み上げ中断判定、前回テレメトリとのログJSON生成、機能ごとの前回値保持が ViewModel に残っている。GT7の読み上げ項目が増えると LMU Narrator と同様に肥大化しやすい。
  **改善案**: 読み上げ優先度制御やログ保存を担う小さな UseCase / service へ段階的に切り出し、ViewModel は Flow の接続とライフサイクル管理に寄せる。

## デザイン（UI/UX・designsystem）

- **対象**: `core/designsystem/.../Color.kt` / `Theme.kt`（ライトテーマ）
  **課題**: ライトテーマの `primary = Yellow40` に対して `onPrimary = Neutral99`（ほぼ白）を組み合わせている。黄色系 primary × 白文字は WCAG のコントラスト比 4.5:1 を満たさないことが多く、ボタンラベル等の可読性が低い恐れがある。secondary（Lime）・tertiary（Neon）も同様の懸念がある。
  **改善案**: 主要な色ペア（primary/onPrimary など）のコントラスト比を実測し、不足していれば `onPrimary` を暗色（Yellow10 等）へ変更する。スクリーンショットテストとは別に、色定義だけのコントラスト検証ユニットテストを designsystem に置くことも検討する。

- **対象**: Android アプリ全体のテーマ
  **課題**: Android 12+ の Dynamic Color（Material You）に対応しておらず、常に固定のブランドカラーで表示される。レース用アプリとしてブランド色固定は妥当な判断でもあるため、対応しない場合でも「意図的に非対応」であることがどこにも記録されていない。
  **改善案**: Dynamic Color を採用するか検討し、採用しない場合はその方針を designsystem の README に明記する。

## 作業改善（開発体験）

- **対象**: CLAUDE.md「コード変更時の必須確認」と日常の検証コマンド
  **課題**: 完了報告前に必要なコマンドが 6 種類以上（ユニットテスト・detekt・assertModuleGraph・Android ビルド・desktop jar・desktop 統合テスト）あり、人も AI エージェントも打ち漏らしやすい。実際に CLAUDE.md には「常に実行すること」の注意書きが繰り返し追記されており、手順の多さ自体が抜け漏れの温床になっている。
  **改善案**: ルート `build.gradle.kts` に集約タスク（例: `./gradlew preMergeCheck`）を定義し、必須チェック一式を 1 コマンドに束ねる。CLAUDE.md のチェックリストも「`preMergeCheck` を実行する」に簡素化できる。

- **対象**: `.github/`（PR テンプレート）
  **課題**: `PULL_REQUEST_TEMPLATE.md` がなく、PR 説明の構成（概要・変更点・確認事項）が作成者ごとにばらつく。CLAUDE.md の完了前チェックリストとも連動していない。
  **改善案**: 日本語の PR テンプレートを追加し、「実行した検証コマンド」「スクリーンショットテスト要否」「ドキュメント更新要否」のチェックボックスを設ける。

- **対象**: `docs/improvement-ideas.md` の運用
  **課題**: 記録は蓄積される一方で、着手判断・優先度付けの仕組みがない。項目が増えるほど「書いたが誰も読まない」状態になりやすい。
  **改善案**: 定期的（リリース前など）に棚卸しし、着手するものは GitHub Issue 化して本ファイルからは Issue 番号を添えて削除する運用を README に明記する。

## CI（GitHub Actions）

- **対象**: `.github/workflows/on-pull-request.yml` の `update-module-graph` ジョブ
  **課題**: PR のたびに `GH_PAT` で PR ブランチへ `chore: update module graph images` をコミット・プッシュする構成のため、モジュール構成に変更がない PR でも毎回ジョブが走り、変更があった場合は push が新たな workflow run を誘発して CI が二重に実行される。また fork からの PR では secrets が使えず失敗する。
  **改善案**: モジュール構成ファイル（`settings.gradle.kts` / 各 `build.gradle.kts`）に変更がある場合のみ実行する paths フィルタ（`dorny/paths-filter` 等）を入れる。あるいは main マージ時のみ画像を更新し、PR 中は `assertModuleGraph` の検証だけにする。

- **対象**: `.github/workflows/on-pull-request.yml` の `concurrency`
  **課題**: `cancel-in-progress: false` のため、同一 PR に連続プッシュすると古いコミットの run が完走するまで新しい run が待たされる。PR の CI は最新コミットの結果だけが意味を持つため、古い run の完走は Actions 時間の浪費になる。
  **改善案**: PR トリガーでは `cancel-in-progress: true` にする（`update-module-graph` の push と干渉しないよう、ジョブ分割や group 名の工夫と合わせて検討する）。

- **対象**: `.github/`（依存自動更新）
  **課題**: GitHub Actions は SHA ピン留めされているが `dependabot.yml` / Renovate 設定がなく、actions・Gradle ライブラリの更新が手動任せになっている。CLAUDE.md は「ライブラリは最新安定版を使う」方針だが、それを支える自動化がない。
  **改善案**: Dependabot（`github-actions` + `gradle` エコシステム）または Renovate を導入し、更新 PR を自動作成させる。
