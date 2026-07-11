# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由で、GranTurismo 7（GT7 PS5）から UDP 経由でテレメトリデータを取得し、Compose Multiplatform アプリで表示・WAV 音声再生によるアナウンスを行う。デスクトップアプリ内で Ktor サーバーも起動し、LMU 由来の走行情報を WebSocket で配信する。

---

## モジュール構成

```
KoDriver/
├── core/
│   ├── domain/        ドメインモデル・リポジトリ抽象・ユースケース
│   ├── data/          DataStore・HTTP/WebSocketクライアント・リポジトリ実装
│   ├── lmu-windows-data/ LMU Windows共有メモリ読み取り・リポジトリ実装
│   ├── gt7-ps5-data/  GT7 PS5 UDP テレメトリ読み取り・リポジトリ実装
│   └── designsystem/  共通 Composable コンポーネント
├── feature/
│   ├── desktop-splash/           デスクトップ起動中スプラッシュの初期化進捗管理・画面表示
│   ├── lmu-windows-connection/   LMU 接続状態の監視
│   ├── main/                     アプリ全体のメイン画面状態管理
│   ├── server-connection/        KoDriver サーバーへの接続状態確認
│   ├── lmu-windows-narrator/     WAV 音声再生とアナウンス制御
│   ├── other-license-detail/     その他画面のライセンス詳細表示
│   ├── other-list/               その他画面の一覧表示・選択状態管理
│   ├── other-readout-start-sound-detail/ その他画面の読み上げ開始音設定詳細
│   ├── other-server-ip-detail/   その他画面の接続先サーバーIP設定ダイアログ
│   ├── other-console-ip-detail/  その他画面のゲーム機 IP 設定ダイアログ
│   ├── other-theme-detail/       その他画面のテーマ設定詳細
│   ├── other-volume-detail/      その他画面の音量設定詳細
│   ├── readout-list/             アナウンス設定の一覧 UI・状態管理
│   ├── lmu-windows-readout-flag-detail/          フラグアナウンスの詳細設定
│   ├── lmu-windows-readout-my-best-lap-detail/   LMU自己ベストラップアナウンスの詳細設定
│   ├── lmu-windows-readout-vehicle-approach-detail/ 車両接近アナウンスの詳細設定
│   ├── lmu-windows-readout-vehicle-damage-detail/   車両故障アナウンスの詳細設定
│   ├── lmu-windows-readout-tyre-temperature-detail/ タイヤ温度アナウンスの詳細設定
│   ├── gt7-ps5-connection/              GT7 PS5 接続状態の監視
│   ├── gt7-ps5-narrator/                GT7 PS5 WAV 音声再生とアナウンス制御
│   ├── gt7-ps5-readout-my-best-lap-detail/      GT7自己ベストラップアナウンスの詳細設定
│   ├── gt7-ps5-readout-remaining-fuel-laps-detail/ GT7燃料残り周回数アナウンスの詳細設定
│   ├── telemetry-log-list/              テレメトリログの一覧表示
│   └── telemetry-log-detail/            テレメトリログの詳細表示
├── app/
│   ├── androidApp/ Android アプリのエントリーポイント
│   ├── desktopApp/ JVM デスクトップアプリのエントリーポイント
│   ├── shared/     Compose Multiplatform 共通 UI・ナビゲーション
│   └── webApp/     Web アプリ（Gradle ビルド設定のみ用意、独自機能は未実装）
└── server/         デスクトップアプリ内で起動する Ktor WebSocket サーバー
```

各モジュールの詳細は、対象モジュール配下の `README.md` と実装を参照すること。

---

## 重要な制約・注意事項

### 共有メモリ読み取りは Windows 専用
`:core:lmu-windows-data` の `SharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。

### Ktor サーバー
`:server` は Windows 版デスクトップアプリと同一プロセスで起動し、`0.0.0.0:8080` で待ち受ける。WebSocket エンドポイントは `/ws/<Simulator.id>/<feature>` のパターンに従う（例: `/ws/lmu_windows/flags`）。`/ws/<Simulator.id>/flags` は `ObserveLmuWindowsRaceFlagsUseCase` を通じて `LmuWindowsFlagRepository` を購読し、`LmuWindowsRaceFlagsData` を JSON として送信する。同一内容の連続値は送信しない。

LAN 内の Android 端末からは `ws://<Windows PC のローカル IP>:8080/ws/<Simulator.id>/flags` 等へ接続する。外部端末から接続するには Windows ファイアウォールで TCP 8080 番ポートの受信を許可する必要がある場合がある。現時点では認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用すること。

### TimingData のラップタイム
`LmuWindowsMapper` は Scoring セグメントのプレイヤー車両からラップタイム系フィールド（`currentLapTimeMs`, `lastLapTimeMs`, `bestLapTimeMs`, `sector1Ms`, `sector2Ms`）を取得する。Scoring のプレイヤー車両が見つからない場合は `0L` にフォールバックする。

### オフセット情報
`LmuMapper.kt` のコメントに pyLMUSharedMemory の ctypes レイアウト（`_pack_=4`）を記載済み。

### ReadoutItemKey の配線（listPane / detailPane とNarratorの読み上げ判定の一致）
`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目をNarratorで読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`ReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `DetermineLmuWindowsNarratorReadoutUseCase`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態がNarratorに未反映だった
- #472: listPane の `VehicleDamage` スイッチが `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage` から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する `Determine*NarratorReadoutUseCase` のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

---

## テスト方針

**実装コードを書いたら、同時にユニットテストを書くこと。** テストは完了報告前に書くのではなく、実装と並行して書く。

ユニットテストを書ける実装コードを変更・追加した場合は、**変更したモジュールのカバレッジが 100% になるようにすること**。ただし、以下のコードはテスト対象から除外してよい。

- Fake / Stub / Spy などのテストダブル
- Koin などの DI Module
- 単純な Preview・サンプルデータ・定数定義
- プラットフォーム固有の外部 API（JNA, UDP ソケット等）を直接呼び出すためモックが現実的でない箇所

---

## ライブラリバージョン管理

`libs.versions.toml` にライブラリを追加するときは、**致命的なバグや互換性問題がない限り、その時点の最新安定版を使用すること**。追加前に必ず公式リリースページで最新バージョンを確認する。

---

## ビルド・実行コマンド

```bash
# デスクトップアプリ起動（通常）
./gradlew :app:desktopApp:run

# デスクトップアプリ起動（ホットリロード）
./gradlew :app:desktopApp:hotRun --auto

# Ktor サーバー単体起動（共有メモリ由来のフラッグ情報は配信しない）
./gradlew :server:run

# Windows MSI パッケージビルド（CI: GitHub Actions / ローカル Windows 環境）
./gradlew :app:desktopApp:packageMsi

# Kover 対象モジュールのテストとカバレッジレポート生成
./gradlew koverXmlReport

# 静的解析とモジュール依存関係の検証
./gradlew detekt assertModuleGraph

# Android・デスクトップアプリのビルドと統合テスト
./gradlew :app:androidApp:assembleDebug
./gradlew :app:desktopApp:jar
./gradlew :app:desktopApp:test

# 特定モジュールだけを確認する場合
./gradlew :<module-path>:jvmTest
```

`:app:webApp` は Gradle ビルド設定のみで独自機能が未実装のため、現在はテスト・ビルド確認の対象外。

GitHub Actions ワークフロー:

- `on-pull-request.yml`: PR 作成・更新時に静的解析・テストを実行
- `on-main-merge.yml`: main へのマージ時に実行
- `build-android.yml`: Android アプリのビルド
- `build-windows.yml`: `workflow_dispatch` でのみ起動し、Windows MSI を Artifact として 30 日間保存する
- `on-release.yml`: リリース時に実行

---

## 主要ライブラリバージョン（libs.versions.toml）

| ライブラリ | バージョン |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| JNA | 5.19.1 |
| kotlinx-coroutines | 1.11.0 |
| androidx-lifecycle | 2.10.0 |
| Ktor | 3.5.0 |

---

## Git 操作ルール

- **コミット・プッシュ・PR の作成はユーザーが明示的に指示した場合のみ実行すること。** 自発的に行うことは禁止。
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。** feature ブランチへの操作は確認不要。
- **作業用ワークツリーは、必ずこのリポジトリの `.claude/worktrees/` 配下に作成すること。** リポジトリ外やその他のディレクトリに作成してはならない。
  - 例: `git worktree add .claude/worktrees/<worktree-name> -b <branch-name>`
- **ワークツリーの削除は、自分のセッションで作成したものだけに限定すること。** 複数の Claude セッションが並行してワークツリーを使用している場合があるため、他のワークツリーは削除してはならない。
- **PR のタイトルと説明は日本語で書くこと。**
- **モジュール図・スクリーンショットテストの画像は `git add` してはならない。** `assertModuleGraph` が生成するモジュール図（例: `docs/graphs/*.gv`, `docs/graphs/*.svg`）や、スクリーンショットテストが生成・更新するスクリーンショット画像（例: `**/snapshots/*.png`）は CI で自動更新される仕組みのため、手元での変更をコミットすると CI の更新と競合する。動作確認のために生成されることがあるが、**ステージングすること自体を禁止する**。ファイルを指定してステージングするときは、これらのファイルを絶対に含めないこと。また、動作確認でこれらのファイルが生成・変更された場合は、**報告前に必ず `git checkout -- <file>` または `git clean -f <file>` で変更を破棄すること**。

### moduleGraphAssert の変更禁止

`build.gradle.kts` の `moduleGraphAssert { ... }` ブロックは、ClaudeCode / Codex が自律的に変更してはならない。

- ユーザーが明示的に `moduleGraphAssert` の変更を指示した場合のみ変更してよい。
- モジュール追加・依存関係修正・CI 修正の一環であっても、事前確認なしに `allowed` / `restricted` / `configurations` を変更してはならない。
- `assertModuleGraph` が失敗した場合は、まず依存関係やモジュール構成側を修正し、`moduleGraphAssert` の緩和で解決しない。

---

## 作業完了前のルール

### 作業単位・PR 単位の時系列チェックリスト

作業を開始してから PR マージ後の片付けまで、実装内容に応じて以下を確認すること。

1. 作業開始時
   - 対象ブランチ、ベースブランチ、worktree の有無、作業範囲を確認する。
   - ユーザーが「コミットしない」「PR だけ作る」「ベースブランチは main 以外」などの条件を指定している場合は、その条件を優先する。
2. 実装前
   - 既存実装を読み、Repository / UseCase / ViewModel / UI の責務と命名が既存パターンに合うことを確認する。
   - 実装対象のモジュール・依存方向が妥当であることを確認する。`:core:designsystem` と `:core:domain` の相互依存や不要な依存追加は避ける。
   - 追加・変更するテストの対象を先に洗い出す。正常系・異常系・境界値・全項目の確認が必要な箇所を確認する。
3. 実装中
   - ユニットテストを書ける実装コードを変更・追加する場合は、実装と同時にテストを追加・更新する。
   - 画面項目・表示名・一覧項目を追加した場合は、listPane / detailPane のテスト、displayName 変換テスト、スクリーンショットテストの要否を確認する。
   - UI を変更した場合は、既存のスクリーンショットテスト対象か、新規スクリーンショットテストが必要かを確認する。
   - モジュールを追加した場合は、Kover 集計対象、Gradle 設定、GitHub Actions ワークフロー、ドキュメントの更新要否を確認する。
   - GitHub Actions のスクリーンショットテストは集約タスクを使い、モジュール追加のたびに workflow を変更しない構成を維持する。
4. 完了前
   - 変更範囲に応じたユニットテスト・スクリーンショットテストを実行する。
   - `./gradlew detekt` と `./gradlew assertModuleGraph` を必ず実行する。
   - Android アプリ・デスクトップアプリのビルド確認と、デスクトップアプリの統合テストを実行する。
   - `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメント更新要否を確認する。
5. PR 作成後
   - GitHub checks / Codacy / Actions の結果を確認し、指摘があれば修正する。
   - PR のタイトルと説明が実装内容を正しく表していることを確認する。
6. PR マージ後
   - ユーザーから指示があった場合は、自分が作成した worktree / branch だけを削除する。
   - 指定されたベースブランチを最新化し、作業ツリーが clean であることを確認する。

### コード変更時の必須確認

コードを変更・追加した場合は、変更範囲・変更規模・対象モジュールに関係なく、**完了報告の前に必ず以下を実行すること**。

- `./gradlew detekt`
- `./gradlew assertModuleGraph`

これらは Codacy や CI で検出される基本的な問題を作業者側で事前に検出するための最低必須チェックであり、モジュール単位の detekt や個別テストだけで代替してはならない。実行できなかった場合は、完了報告で理由を明記すること。

コードを変更・追加したら、**完了報告の前に必ず以下をすべて実行すること**。

1. **ユニットテストの追加・更新**（→「[テスト方針](#テスト方針)」を参照。テストは実装と同時に書くこと）
2. detekt（**常に全体で実行すること**。変更が 1 モジュールでも `./gradlew detekt` を使う。モジュール単位の `:xxx:detekt` だけでは `app:shared` 等の連鎖的な問題を見落とす）
3. モジュールグラフの検証
4. Android アプリ・デスクトップアプリのビルド確認
5. デスクトップアプリの統合テスト（**常に実行すること**。Koin モジュール構成の変更は `AppTest` に影響するため）
6. `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメントに変更が必要かを確認し、必要であれば更新する

```bash
# 変更したモジュールのテストを実行（例: feature:readout を変更した場合）
./gradlew :feature:readout-list:jvmTest

# server モジュールを変更した場合
./gradlew :server:test

# androidMain に変更がある場合は androidHostTest も実行（例: core:data を変更した場合）
./gradlew :core:data:testAndroidHostTest

# detekt は常に全体で実行（モジュール単位では不十分）
./gradlew detekt

# モジュールグラフの依存関係ルールを検証（常に実行）
./gradlew assertModuleGraph

# Android アプリのビルド確認（常に実行）
./gradlew :app:androidApp:assembleDebug

# デスクトップアプリのビルド確認（常に実行）
./gradlew :app:desktopApp:jar

# デスクトップアプリの統合テスト（常に実行。Koin モジュール構成の変更を検証する）
./gradlew :app:desktopApp:test
```

detekt の主な閾値（`config/detekt/detekt.yml`）:
- `MagicNumber`: 無効（数値リテラルは許容）
- `LongMethod`: 閾値 100 行（`@Composable` は除外）
- `LongParameterList`: 関数・コンストラクタともに 8 個
- `TooManyFunctions`: ファイル・クラス・オブジェクト 20 個
- `CyclomaticComplexMethod`: 閾値 15

テストが失敗・detekt で指摘がある・assertModuleGraph で違反がある・ビルドエラーがある場合は修正してからレポートする。

---

## コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutListViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- LMU Windows共有メモリ固有の実装はJVM専用の `core:lmu-windows-data` に置き、`core:data` へ依存させない。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は `XxxViewModel` 内の `xxxDisplayNames: Map<String, String>` で分離する。

### Repository の命名規則

`Repository` は責務に応じて接尾辞で区別すること。命名だけで「取得用」か「設定保存用」かが判別できる状態を保つ。

- **データ取得用**（テレメトリ・走行データなど外部ソースからの読み取り、Flow 配信、バージョン取得など）は接尾辞なしの素の `XxxRepository`（例: `LmuWindowsRepository`, `LmuWindowsFlagRepository`, `Gt7Ps5Repository`, `ServerVersionRepository`）。
- **設定保存用**（DataStore による永続化）は必ず `XxxPreferencesRepository`（複数値・任意型の設定）または `XxxEnabledRepository`（単一の有効/無効フラグ）の接尾辞を付ける（例: `ThemePreferencesRepository`, `ConsoleAddressPreferencesRepository`, `ServerIpPreferencesRepository`, `KeepScreenOnEnabledRepository`）。設定保存用を素の `XxxRepository` にしてはならない。

### ViewModel の設計規則

- **`uiState: StateFlow<XxxUiState>` を唯一の公開状態にすること。** 個別の `StateFlow`（例: `selectedSimulator`）を `public` で追加してはならない。UI は `uiState` だけを参照すれば済む設計にする。
- **`init {}` を使わず、宣言的に状態を組み立てること。** 外部ソース（Repository など）からの Flow は `stateIn` で StateFlow 化し、派生状態は `combine` で組み立てる。副作用のない読み取りは `private val` のカスタム getter（`get() { ... }`）で表現する。

```kotlin
// NG: public な個別 StateFlow
val selectedSimulator: StateFlow<String?> = ...

// OK: uiState に集約
val uiState: StateFlow<XxxUiState> = ...

// NG: init {} でコルーチンを起動して状態を同期
init {
    viewModelScope.launch { flow.collect { _state.value = it } }
}

// OK: stateIn で宣言的に StateFlow 化
private val _selected: StateFlow<String?> = repository.observe()
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)
```

### MutableStateFlow の更新

`MutableStateFlow` の値を更新するときは **必ず `update { }` を使うこと**。`.value = ...` の直接代入は競合状態を招く恐れがある。

```kotlin
// NG
_state.value = _state.value.copy(count = _state.value.count + 1)

// OK
_state.update { it.copy(count = it.count + 1) }
```

### Coroutines のエラーハンドリング

`runCatching` および `mapCatching` は `CancellationException` を捕捉するため、structured concurrency を破壊する恐れがある。**使用禁止**。

代わりに `try-catch` で `CancellationException` を明示的に再スローすること:

```kotlin
// NG
runCatching { suspendFun() }

// OK
try {
    Result.success(suspendFun())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
```

### テストパターン

- テスト名は日本語のバッククォート記法（`` `初期状態は Connecting を返す`() ``）
- ViewModel の `uiState` から流れてきた内容を検証するときは `first()` を使う
- テストケース数は最小限に絞ること。正常系・異常系・境界値の 3 軸を意識し、冗長なケースは省く

### カバレッジ

Kover でカバレッジを計測する。新しいモジュールを追加した場合、ルートの `build.gradle.kts` の `kover { }` ブロックに `kover(project(":module:name"))` を追加しないとカバレッジ集計から除外される。

```bash
# ローカルでカバレッジレポート生成
./gradlew koverXmlReport
```
