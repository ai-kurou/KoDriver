# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由で、Gran Turismo 7（GT7 PS5）から UDP 経由でテレメトリデータを取得し、Compose Multiplatform アプリで表示・WAV 音声再生によるアナウンスを行う。デスクトップアプリ内で Ktor サーバーも起動し、LMU 由来の走行情報を WebSocket で配信する。

---

## モジュール構成

モジュール一覧・役割・依存関係図は [`docs/architecture.md`](docs/architecture.md) を参照すること。モジュール構成は同ファイルのみを正とし、他の場所（README等）に重複した一覧を作らない。モジュールを追加・削除した場合は `settings.gradle.kts` の変更と同じ PR で `docs/architecture.md` を更新する。

各モジュールの詳細は、対象モジュール配下の `README.md` と実装を参照すること。

---

## 重要な制約・注意事項

### 改善案はGitHub Issueとして起票する

改善案の記録方法・起票ルールの詳細は「[改善案の記録](#改善案の記録)」を参照。

### app:androidBenchmark の targetProjectPath は app 間依存の例外

`app:androidBenchmark`（`com.android.test` モジュール）は `targetProjectPath = ":app:androidApp"` で `app:androidApp` を計装対象として参照する。これは Gradle の `implementation`/`api` 依存ではなく AGP 固有のテスト対象指定であり、`moduleGraphAssert` の対象外（`app:.*App` を含む `allowed` パターンにも含まれない）。「app モジュール同士は依存しない」という原則の例外として、この参照のみ容認する（PR #1126）。技術的な背景は `app/androidBenchmark/README.md` を参照。

### 共有メモリ読み取りは Windows 専用
`:core:windows-shared-memory` の `SharedMemoryReader` / `WindowsSharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。`:core:lmu-windows-data` と `:core:ace-windows-data` はこの共通基盤に依存し、それぞれのシム固有の構造体パースのみを実装する。

### Ktor サーバー
`:server` は Windows 版デスクトップアプリと同一プロセスで起動し、`0.0.0.0:8080` で待ち受ける。WebSocket エンドポイントは `/ws/<Simulator.id>/<feature>` のパターンに従う（例: `/ws/lmu_windows/flags`）。現時点では認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用すること。`KoDriverServer.start()` は mDNS（`_kodriver._tcp.local.`）でサーバーを LAN 内へ広告し、`:feature:other-server-ip-detail` がそれを検出して接続先 IP の自動入力に使う。エンドポイント仕様・CSWSH対策・mDNS広告の詳細は `server/README.md` を参照。

### LMU Windows共有メモリのパース詳細
`LmuWindowsMapper` のラップタイム系フィールドの扱い（Scoringのプレイヤー車両フォールバック等）、車両クラス名、共有メモリのオフセット情報は `core/lmu-windows-data/README.md` を参照。共有メモリに含まれない情報（天候予報、Virtual Energy消費履歴等）をLMU内蔵のローカルREST APIから補えないかの調査メモは [`docs/lmu-windows-rest-api.md`](docs/lmu-windows-rest-api.md) を参照（未統合）。

### ReadoutItemKey の配線（listPane / detailPane とNarratorの読み上げ判定の一致）
`ReadoutItemKey` を新設・変更する際は、listPane/detailPaneのスイッチの保存先 `DataStore` と、Narrator側の実際の読み上げ判定ロジックへの配線が一致しているかを必ず確認すること。片方だけ実装すると、スイッチが存在するのに効果がない死んだ実装になる（過去に #464, #472 のバグが発生）。詳細・確認手順は [`docs/readout-item-key-wiring.md`](docs/readout-item-key-wiring.md) を参照。

### list/detail ペイン切り替えの Navigation 3 パターン

`ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）は、`Material3 Adaptive` の `ListDetailPaneScaffold` と並行して、Navigation 3 の `NavBackStack<NavKey>` を「現在どちらのペインを表示しているか」の状態として保持する共通パターンを使う。新しく list/detail 構成の画面を追加する場合は、これらの実装（`XxxNavigationState.kt` とその `XxxNavigationStateTest.kt`）を参照すること。パターンの詳細・`rememberSaveable` を使わない理由は [`docs/list-detail-navigation-pattern.md`](docs/list-detail-navigation-pattern.md) を参照。

### 実装前の類似コード確認

新規実装・修正の前に、必ず同一モジュールまたは近い責務の既存コードを確認すること。UI、UseCase、Repository、DataSource、Test、ScreenshotTest などは、既存の命名・粒度・責務分割・テストスタイルに合わせる。

実装前に `rg` などで最低限以下を確認すること。

- 同じ種類の画面・Pane・Content・ListItem
- 同じ種類の UseCase / Repository / DataSource
- 同じファイル内または同一モジュールのテスト
- 既存の MockK の `verify` / `coVerify` / `confirmVerified` の使い方
- ScreenshotTest / AndroidTest / DesktopTest の粒度

類似コードが存在する場合は、その構成・依存関係・テスト方針を優先し、独自の実装スタイルを持ち込まないこと。既存パターンから外れる設計にする場合は、理由を説明してから実装すること。

### NarratorViewModel は共通化しない

`Gt7Ps5NarratorViewModel`・`LmuWindowsNarratorViewModel`・`AceWindowsNarratorViewModel` は共通の骨格を持つが、シミュレーターごとに `ReadoutItemKey` の種類・判定対象のテレメトリ項目・購読するUseCase群が異なるため、共通ViewModel基底やシミュレーター横断の共通購読ロジックへは切り出さない。理由・詳細は [`docs/narrator-viewmodel-design.md`](docs/narrator-viewmodel-design.md) を参照。新しいシミュレーター向けのNarratorViewModelを実装する際は、既存の類似ViewModel（「実装前の類似コード確認」を参照）の構成をそのままコピーして書いてよい。

### ユーザー設定のデフォルト値

ユーザー設定として永続化され、DataStore の初期値・詳細設定画面のリセット値・UiState の初期値・Narrator / UseCase の初期値で共有されるデフォルト値は、仕様値として `:core:domain` の `domain/model/*Defaults.kt` に定義すること。`:core:data` の `*Preferences` や feature の ViewModel / Pane / UiState は、その定数を参照する。

feature の `companion object` や `Pane.kt` に仕様値を置くと、`:core:data` から参照できず同じ値を重複定義することになるため避ける。Preview やテストデータだけで完結する表示用の値は feature 内に置いてよい。

定数名は `XXX_DEFAULT` のように末尾に `DEFAULT` を付けて統一すること（例: `LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT`）。

---

## テスト方針

**実装コードを書いたら、同時にユニットテストを書くこと。** テストは完了報告前に書くのではなく、実装と並行して書く。変更したモジュールのカバレッジは原則100%にする。

テストの配置先（commonTest / jvmTest）・スクリーンショットテストの配置先や画面サイズ・テストパターン（命名規則、MockKの使い方、any()禁止等）・カバレッジ計測の詳細は [`docs/testing-guidelines.md`](docs/testing-guidelines.md) を参照。

---

## ライブラリバージョン管理

`libs.versions.toml` にライブラリを追加するときは、**致命的なバグや互換性問題がない限り、その時点の最新安定版を使用すること**。追加前に必ず公式リリースページで最新バージョンを確認する。

---

## ビルド・実行コマンド

主要コマンド一覧は [`docs/build-commands.md`](docs/build-commands.md) を参照。特に完了報告・PR 作成前は `./gradlew preSubmitChecks` を実行すること。GitHub Actions ワークフローの一覧・詳細な挙動・権限設計は [`docs/ci-workflows.md`](docs/ci-workflows.md) を参照。

---

## Git 操作ルール

ワークツリー運用・PR のタイトル/説明・署名禁止・取り込み済みPRセクション・モジュール図/スクリーンショット画像のステージング禁止など、詳細なルールは [`docs/git-workflow.md`](docs/git-workflow.md) を参照。特に次の2点は必ず守ること。

- **feature ブランチでのコミット・プッシュ・PR の作成は、ユーザーの明示的な指示を待たずに自発的に実行してよい。**
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。**

`build.gradle.kts` の `moduleGraphAssert { ... }` ブロックは、ユーザーが明示的に指示した場合を除き変更してはならない（詳細は [`docs/git-workflow.md`](docs/git-workflow.md) を参照）。

---

## 改善案の記録

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など、種類を問わない）は、**その場で GitHub Issue として起票すること**（`gh issue create`）。ラベルは付けない。

- 依頼されたタスクの範囲外であっても、気づいた時点で起票する。今回のタスクで実装する必要はなく、あくまで記録に留める。
- タイトルは「対象」がわかる簡潔なもの、本文は「対象・課題・改善案」が後から読んで分かる粒度で書く。関連するファイルやモジュール名があれば添える。
- 起票前に `gh issue list --search "<キーワード>"` 等で同趣旨のIssueが既に存在しないか確認し、重複させない。既にあれば新規起票せず、必要ならコメントを追記する。
- Issueとして起票することは、対象コードの変更やテスト追加を意味しない。実際に着手する場合は通常どおり別作業として扱う。
- 対応済み・見送りと判断したIssueは close する。過去の対応履歴はIssueの検索・履歴で追える（旧 `docs/resolved-improvement-ideas.md` のような手動ログは不要）。

---

## 作業完了前のルール

### 作業単位・PR 単位の時系列チェックリスト

作業開始時からPRマージ後の片付けまでの時系列チェックリストは [`docs/pr-workflow-checklist.md`](docs/pr-workflow-checklist.md) を参照。ユーザーが「コミットしない」「PR だけ作る」「ベースブランチは main 以外」などの条件を指定している場合は、その条件を優先する。

### コード変更時の必須確認

コードを変更・追加した場合は、変更範囲・変更規模・対象モジュールに関係なく、**完了報告の前に必ず以下を実行すること**。

1. **ユニットテストの追加・更新**（→「[テスト方針](#テスト方針)」を参照。テストは実装と同時に書くこと）
2. `./gradlew preSubmitChecks`（必須チェックの集約タスク。以下をすべて含む）
   - 全モジュールの detekt（モジュール単位の `:xxx:detekt` だけでは `app:shared` 等の連鎖的な問題を見落とすため、全体で実行される）
   - モジュールグラフの検証（`assertModuleGraph`）
   - 全ユニットテスト＋カバレッジレポート生成（`koverXmlReport`）
   - Android アプリ・デスクトップアプリのビルド確認
   - デスクトップアプリの統合テスト（Koin モジュール構成の変更は `AppTest` に影響するため）
3. `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメントに変更が必要かを確認し、必要であれば更新する

`preSubmitChecks` は Codacy や CI で検出される基本的な問題を作業者側で事前に検出するための最低必須チェックであり、モジュール単位の detekt や個別テストだけで代替してはならない。実行できなかった場合は、完了報告で理由を明記すること。

作業中に個別のチェックを素早く回したい場合のコマンド例、detekt の閾値設定については [`docs/build-commands.md`](docs/build-commands.md) を参照（完了報告前の `preSubmitChecks` 実行は省略不可）。

テストが失敗・detekt で指摘がある・assertModuleGraph で違反がある・ビルドエラーがある場合は修正してからレポートする。

---

## コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutListViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- LMU Windows共有メモリ固有の実装はJVM専用の `core:lmu-windows-data` に置き、`core:data` へ依存させない。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- 文字スタイルは `MaterialTheme.typography.*` を参照し、`fontSize` / `FontWeight` を Composable 内で直接指定しない。アプリ全体のタイポグラフィは `:core:designsystem` の `KoDriverTypography` で一元管理する。
- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は `XxxViewModel` 内の `xxxDisplayNames: Map<String, String>` で分離する。

Repository の命名規則・ViewModel の設計規則・`MutableStateFlow` の更新・Coroutines のエラーハンドリングの詳細は [`docs/coding-conventions.md`](docs/coding-conventions.md) を参照。テストパターン（mockkの`any()`禁止等含む）は [`docs/testing-guidelines.md`](docs/testing-guidelines.md) を参照。
