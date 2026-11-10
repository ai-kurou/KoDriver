# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）・Assetto Corsa EVO（ACE）から Windows 共有メモリ経由で、Gran Turismo 7（GT7 PS5）から UDP 経由でテレメトリデータを取得し、Compose Multiplatform アプリで表示・OS標準TTSによる自由文言の読み上げと開始音のWAV再生によるアナウンスを行う。マイクからの音声認識はオフラインの Vosk（Android・Desktop 共通、日本語モデルはビルド時に取得して同梱）で行う（詳細は `core/speech-recognition-data/README.md`）。デスクトップアプリ内で Ktor サーバーも起動し、LMU / ACE 由来の走行情報を WebSocket で配信する。

---

## モジュール構成

モジュール一覧・役割・依存関係図は [`docs/architecture.md`](docs/architecture.md) を参照すること。モジュール構成は同ファイルのみを正とし、他の場所（README等）に重複した一覧を作らない。モジュールを追加・削除した場合は `settings.gradle.kts` の変更と同じ PR で `docs/architecture.md` を更新する。

各モジュールの詳細は、対象モジュール配下の `README.md` と実装を参照すること。

---

## 重要な制約・注意事項

### 改善案は作業の最後にIssue化するか確認する

改善案の記録方法・起票ルールの詳細は「[改善案の記録](#改善案の記録)」を参照。

### app:androidBenchmark の targetProjectPath は app 間依存の例外

`app:androidBenchmark` の `targetProjectPath` 参照のみ、「app モジュール同士は依存しない」原則の例外として容認する。詳細は [`docs/project-notes.md`](docs/project-notes.md) を参照。

### 共有メモリ読み取りは Windows 専用

共有メモリ読み取り（`:core:windows-shared-memory`）は Windows のみ動作し、他OSでは `open()` が `false` を返し続ける（クラッシュしない）。詳細は [`docs/project-notes.md`](docs/project-notes.md) を参照。

### Ktor サーバー

`:server` はデスクトップアプリと同一プロセスで `0.0.0.0:8080` で待ち受ける。認証・暗号化は未実装のため信頼できる LAN 内でのみ使用する。エンドポイント仕様は `server/README.md`、概要は [`docs/project-notes.md`](docs/project-notes.md) を参照。

### LMU Windows共有メモリのパース詳細

LMU のパース詳細は `core/lmu-windows-data/README.md`、REST API 調査メモは [`docs/lmu-windows-rest-api.md`](docs/lmu-windows-rest-api.md)、補足は [`docs/project-notes.md`](docs/project-notes.md) を参照。

### ReadoutItemKey の配線（listPane / detailPane とNarratorの読み上げ判定の一致）
`ReadoutItemKey` を新設・変更する際は、listPane/detailPaneのスイッチの保存先 `DataStore` と、Narrator側の実際の読み上げ判定ロジックへの配線が一致しているかを必ず確認すること。片方だけ実装すると、スイッチが存在するのに効果がない死んだ実装になる（過去に #464, #472 のバグが発生）。詳細・確認手順は [`docs/readout-item-key-wiring.md`](docs/readout-item-key-wiring.md) を参照。

### 実装前の類似コード確認

新規実装・修正の前に、必ず同一モジュールまたは近い責務の既存コードを確認すること。確認項目・進め方の詳細は [`docs/pr-workflow-checklist.md`](docs/pr-workflow-checklist.md) の「実装前」を参照。

### NarratorViewModel は共通化しない

`Gt7Ps5NarratorViewModel`・`LmuWindowsNarratorViewModel`・`AceWindowsNarratorViewModel` は共通の骨格を持つが、シミュレーターごとに `ReadoutItemKey` の種類・判定対象のテレメトリ項目・購読するUseCase群が異なるため、共通ViewModel基底やシミュレーター横断の共通購読ロジックへは切り出さない。理由・詳細は [`docs/narrator-viewmodel-design.md`](docs/narrator-viewmodel-design.md) を参照。新しいシミュレーター向けのNarratorViewModelを実装する際は、既存の類似ViewModel（「実装前の類似コード確認」を参照）の構成をそのままコピーして書いてよい。

### ユーザー設定のデフォルト値

デフォルト値・読み上げキーの置き場所と命名規則（`XXX_DEFAULT` など）は [`docs/project-notes.md`](docs/project-notes.md) を参照。シミュレーター固有値は各シミュレーターのドメインモジュールに置き、feature には仕様値を置かない。`_DEFAULT` は既定値にのみ付け、`_MIN` / `_MAX` / `_UNSPECIFIED` / `_UNKNOWN_KEY` / `_ALTERNATE` などの仕様値には付けない。新しい読み上げキーを追加する際は、そのシミュレーターの `*ReadoutItemKeys.kt` のキー一覧にも追加すること。

---

## テスト方針

**実装コードを書いたら、同時にユニットテストを書くこと。** テストは完了報告前に書くのではなく、実装と並行して書く。変更したモジュールのカバレッジは原則100%にする。

テストの配置先（commonTest / jvmTest）・スクリーンショットテストの配置先や画面サイズ・テストパターン（命名規則、MockKの使い方、any()禁止等）・カバレッジ計測の詳細は [`docs/testing-guidelines.md`](docs/testing-guidelines.md) を参照。

モックへの呼び出し自体を検証する場合、通常関数は `every` と `verify`、suspend 関数は `coEvery` と `coVerify` を対応させ、メソッド・引数・回数を一致させる。戻り値や状態を作るだけのスタブには呼び出し検証を要求しない。`verify`/`coVerify` には必ず `exactly = N` を指定し、検証対象の全モックを各テストケース末尾の `confirmVerified(...)` に渡す。詳細は [`docs/testing-guidelines.md`](docs/testing-guidelines.md) を参照。

---

## ライブラリバージョン管理

`libs.versions.toml` にライブラリを追加するときは、**致命的なバグや互換性問題がない限り、その時点の最新安定版を使用すること**。追加前に必ず公式リリースページで最新バージョンを確認する。

---

## ビルド・実行コマンド

主要コマンド一覧は [`docs/build-commands.md`](docs/build-commands.md) を参照。特に完了報告・PR 作成前は `./gradlew preSubmitChecks` を実行すること。GitHub Actions ワークフローの一覧・詳細な挙動・権限設計は [`docs/ci-workflows.md`](docs/ci-workflows.md) を参照。

**同一セッション内で実行中の `preSubmitChecks` は最大1つとする（ワークツリー・バックグラウンド実行の有無を問わない）。** 実行中なら完了と終了コードを確認してから次を開始する。出力待機のタイムアウトを終了扱いにせず、既存プロセスの状態・出力を確認する。終了コードを確実に取得するため、起動方法は [`docs/build-commands.md`](docs/build-commands.md) の「preSubmitChecks を終了が確定できる形で起動する」に従う。別セッションの同時実行は許容する。

---

## Git 操作ルール

ワークツリー運用・PR のタイトル/説明・署名禁止・取り込み済みPRセクション・モジュール図/スクリーンショット画像のステージング禁止など、詳細なルールは [`docs/git-workflow.md`](docs/git-workflow.md) を参照。特に次の2点は必ず守ること。

- **feature ブランチでのコミット・プッシュ・PR の作成は、ユーザーの明示的な指示を待たずに自発的に実行してよい。**
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。**

`build.gradle.kts` の `moduleGraphAssert { ... }` ブロックは、ユーザーが明示的に指示した場合を除き変更してはならない（詳細は [`docs/git-workflow.md`](docs/git-workflow.md) を参照）。

---

## 改善案の記録

作業中に気づいた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など、種類を問わない）は、依頼されたタスクの範囲外であっても内容を記録しておく。**作業の最後に、記録した改善案を GitHub Issue として起票するかユーザーに確認する。確認前に起票してはならない。**

- 起票するとユーザーが回答した場合は、`gh issue list --search "<キーワード>"` 等で同趣旨のIssueが既に存在しないか確認し、重複を避ける。既にあれば新規起票せず、そのIssueをユーザーに伝える。
- 起票する場合はラベルを付けない。タイトルは「対象」がわかる簡潔なもの、本文は「対象・課題・改善案」が後から読んで分かる粒度で書く。関連するファイルやモジュール名があれば添える。
- ユーザーが起票しないと回答した場合は起票しない。該当する改善案がない場合、確認は不要。
- Issueとして起票することは、対象コードの変更やテスト追加を意味しない。実際に着手する場合は通常どおり別作業として扱う。
- 対応済み・見送りと判断したIssueは close する。過去の対応履歴はIssueの検索・履歴で追える（旧 `docs/resolved-improvement-ideas.md` のような手動ログは不要）。

---

## 作業完了前のルール

### 作業単位・PR 単位の時系列チェックリスト

作業開始時からPRマージ後の片付けまでの時系列チェックリストは [`docs/pr-workflow-checklist.md`](docs/pr-workflow-checklist.md) を参照。ユーザーが「コミットしない」「PR だけ作る」「ベースブランチは main 以外」などの条件を指定している場合は、その条件を優先する。

### コード変更時の必須確認

コードを変更・追加した場合は、変更範囲・変更規模・対象モジュールに関係なく、**完了報告の前に必ず以下を実行すること**。

1. **ユニットテストの追加・更新**（→「[テスト方針](#テスト方針)」を参照。テストは実装と同時に書くこと）
2. `./gradlew preSubmitChecks`（detekt・ktlint・モジュールグラフ検証・全ユニットテスト・Android lint・各アプリのビルド確認などの集約タスク。含まれるチェックの一覧は [`docs/build-commands.md`](docs/build-commands.md) の「preSubmitChecks に含まれるチェック」を参照）
3. `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメントに変更が必要かを確認し、必要であれば更新する

`preSubmitChecks` は Codacy や CI で検出される基本的な問題を作業者側で事前に検出するための最低必須チェックであり、モジュール単位の detekt や個別テストだけで代替してはならない。実行できなかった場合は、完了報告で理由を明記すること。

作業中に個別のチェックを素早く回したい場合のコマンド例、detekt の閾値設定については [`docs/build-commands.md`](docs/build-commands.md) を参照（完了報告前の `preSubmitChecks` 実行は省略不可）。

テストが失敗・detekt で指摘がある・assertModuleGraph で違反がある・ビルドエラーがある場合は修正してからレポートする。

---

## コーディング規約

状態管理・共有メモリ・モジュール構成・デザインシステム（typography/shapes/spacing/color）・DataStore キー・Repository / ViewModel の設計規則・Coroutines のエラーハンドリングなどの詳細は [`docs/coding-conventions.md`](docs/coding-conventions.md) を参照。テストパターン（mockkの`any()`禁止等含む）は [`docs/testing-guidelines.md`](docs/testing-guidelines.md) を参照。
