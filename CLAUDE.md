# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由でテレメトリデータを取得し、Compose Multiplatform デスクトップアプリで表示し、Windows TTS でアナウンスする。

---

## モジュール構成

```
KoDriver/
├── core/
│   ├── domain/        ドメインモデル・リポジトリ抽象・ユースケース
│   ├── data/          共有メモリ読み取り・DataStore・リポジトリ実装
│   └── designsystem/  共通 Composable コンポーネント
├── feature/
│   ├── lmu-connection/           LMU 接続状態の監視
│   ├── narrator/                 WAV 音声再生とアナウンス制御
│   ├── other-detail/             その他画面の詳細表示
│   ├── other-list/               その他画面の一覧表示・選択状態管理
│   ├── readout/                  アナウンス設定の一覧 UI・状態管理
│   ├── lmu-readout-flag-detail/          フラグアナウンスの詳細設定
│   ├── readout-vehicle-approach/        車両接近アナウンスの詳細設定
│   └── lmu-readout-vehicle-damage-detail/   車両故障アナウンスの詳細設定
├── app/
│   ├── androidApp/ Android アプリのエントリーポイント
│   ├── desktopApp/ JVM デスクトップアプリのエントリーポイント
│   ├── shared/     Compose Multiplatform 共通 UI・ナビゲーション
│   └── webApp/     Web アプリ（未実装）
└── server/         Ktor サーバー（未実装）
```

各モジュールの詳細は、対象モジュール配下の `README.md` と実装を参照すること。

---

## 重要な制約・注意事項

### 共有メモリ読み取りは Windows 専用
`SharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。

### TimingData のラップタイムは未実装
`LmuMapper` のラップタイム系フィールド（`currentLapTimeMs`, `lastLapTimeMs`, `bestLapTimeMs`, `sector1Ms`, `sector2Ms`）は `0L` で固定。Scoring セグメントの実装が必要。

### オフセット情報
`LmuMapper.kt` のコメントに pyLMUSharedMemory の ctypes レイアウト（`_pack_=4`）を記載済み。

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

`:app:webApp` は未実装のため、現在はテスト・ビルド確認の対象外。

GitHub Actions ワークフロー `build-windows.yml` は `workflow_dispatch` でのみ起動し、MSI を Artifact として 30 日間保存する。

---

## 主要ライブラリバージョン（libs.versions.toml）

| ライブラリ | バージョン |
|---|---|
| Kotlin | 2.3.21 |
| Compose Multiplatform | 1.11.0 |
| JNA | 5.17.0 |
| kotlinx-coroutines | 1.11.0 |
| androidx-lifecycle | 2.11.0-beta01 |
| Ktor | 3.4.3 |

---

## Git 操作ルール

- **コミット・プッシュ・PR の作成はユーザーが明示的に指示した場合のみ実行すること。** 自発的に行うことは禁止。
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。** feature ブランチへの操作は確認不要。
- **作業用ワークツリーは、必ずこのリポジトリの `.claude/worktrees/` 配下に作成すること。** リポジトリ外やその他のディレクトリに作成してはならない。
  - 例: `git worktree add .claude/worktrees/<worktree-name> -b <branch-name>`
- **ワークツリーの削除は、自分のセッションで作成したものだけに限定すること。** 複数の Claude セッションが並行してワークツリーを使用している場合があるため、他のワークツリーは削除してはならない。

---

## 作業完了前のルール

コードを変更・追加したら、**完了報告の前に必ず以下をすべて実行すること**。

1. 該当モジュールのユニットテスト
2. detekt（**常に全体で実行すること**。変更が 1 モジュールでも `./gradlew detekt` を使う。モジュール単位の `:xxx:detekt` だけでは `app:shared` 等の連鎖的な問題を見落とす）
3. モジュールグラフの検証
4. Android アプリ・デスクトップアプリのビルド確認
5. デスクトップアプリの統合テスト（**常に実行すること**。Koin モジュール構成の変更は `AppTest` に影響するため）

```bash
# 変更したモジュールのテストを実行（例: feature:readout を変更した場合）
./gradlew :feature:readout:jvmTest

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

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は `XxxViewModel` 内の `xxxDisplayNames: Map<String, String>` で分離する。

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
