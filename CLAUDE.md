# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由でテレメトリデータを取得し、Compose Multiplatform デスクトップアプリで表示し、Windows TTS でアナウンスする。

---

## モジュール構成

```
KoDriver/
├── core/
│   ├── domain/     ドメインモデル・リポジトリ抽象・ユースケース（KMP: JVM / JS / wasmJS / Android）
│   └── data/       JVM データ層（JNA 共有メモリ + DataStore）※ JVM 専用
├── feature/
│   ├── readout/    アナウンス設定 UI（ReadoutViewModel, ReadoutContent, ReadoutListPane, ReadoutDetailPane）
│   └── other/      その他画面（未実装）
├── app/
│   ├── shared/     Compose Multiplatform 共通 UI（ナビゲーション骨格）
│   ├── desktopApp/ JVM デスクトップアプリ（エントリーポイント）
│   ├── androidApp/ Android アプリ（基本実装済み）
│   └── webApp/     Web アプリ（未実装）
└── server/         Ktor サーバー（未実装）
```

### `core:domain` モジュール（KMP）

```
commonMain/domain/
  model/          LmuTelemetryData, EngineData, FuelData, TimingData, TyreData,
                  VehicleData, InputsData, WheelIndex
  repository/     LmuRepository, ReadoutPreferencesRepository, SimulatorPreferencesRepository（interface）
  usecase/        ObserveLmuUseCase, DisconnectLmuUseCase,
                  ObserveReadoutEnabledStatesUseCase, SaveReadoutEnabledStateUseCase,
                  ObserveSelectedSimulatorUseCase, SaveSelectedSimulatorUseCase
```

### `core:data` モジュール（JVM 専用 / `kotlinJvm` プラグイン）

```
src/main/kotlin/kurou/kodriver/data/
  datasource/     SharedMemoryReader（JNA で Windows File Mapping を open/read/close）
                  Kernel32Ext（JNA インターフェース）
                  ReadoutPreferencesSerializer, SimulatorPreferencesSerializer（DataStore ProtoBuf）
                  ReadoutPreferencesDataStoreFactory, SimulatorPreferencesDataStoreFactory
  mapper/         LmuMapper（LMU バイナリ → LmuTelemetryData）
  model/          ReadoutPreferences, SimulatorPreferences（ProtoBuf モデル）
  repository/     LmuRepositoryImpl, ReadoutPreferencesRepositoryImpl, SimulatorPreferencesRepositoryImpl

src/test/kotlin/   ← kotlinJvm プラグイン時のテストパス（kotlinMultiplatform の jvmTest とは異なる）
```

### `feature:readout` モジュール（KMP）

```
commonMain/
  ReadoutViewModel.kt     uiState: StateFlow<ReadoutListUiState> を公開
  ReadoutListUiState.kt   selectedSimulator, simulators, simulatorDisplayNames,
                          items, itemDisplayNames, readoutEnabledStates
  ReadoutContent.kt       ReadoutContent（ListPane / DetailPane を組み合わせたルート Composable）
  ReadoutListPane.kt      シミュレータ選択 + アイテム一覧
  ReadoutDetailPane.kt    詳細ペイン（未実装）
```

### `app/shared` の UI 構造

```
commonMain/presentation/
  AppScreen.kt            AppDestination（enum）, AppScreen（ナビゲーション骨格）
  component/
    PlaceholderContent.kt PlaceholderContent
```

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

# テスト
./gradlew :core:domain:jvmTest
./gradlew :core:data:test
./gradlew :feature:readout:jvmTest
./gradlew :app:shared:jvmTest
./gradlew :server:test
```

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
- **ワークツリーの削除は、自分のセッションで作成したものだけに限定すること。** 複数の Claude セッションが並行してワークツリーを使用している場合があるため、他のワークツリーは削除してはならない。

---

## 作業完了前のルール

コードを変更・追加したら、**完了報告の前に必ず以下をすべて実行すること**。

1. 該当モジュールのユニットテスト
2. 該当モジュールの detekt
3. モジュールグラフの検証
4. スクリーンショットテストの検証（差分がある場合はゴールデン画像を更新してから再度テストが通ることを確認）
5. Android アプリ・デスクトップアプリのビルド確認

```bash
# 変更したモジュールのテストを実行（例: feature:readout を変更した場合）
./gradlew :feature:readout:jvmTest

# 変更したモジュールの detekt を実行
./gradlew :feature:readout:detekt

# 複数モジュールを変更した場合は全体で実行
./gradlew detekt

# モジュールグラフの依存関係ルールを検証（常に実行）
./gradlew moduleGraphAssert

# スクリーンショットテストを検証（常に実行）
./gradlew :feature:readout:verifyRoborazziJvmTest
./gradlew :app:shared:verifyRoborazziJvmTest

# 差分がある場合: ゴールデン画像を更新してからテストを再実行
./gradlew :feature:readout:recordRoborazziJvmTest
./gradlew :feature:readout:verifyRoborazziJvmTest
# feature モジュールの UI を変更した場合は app:shared も合わせて更新すること
# （AppScreen が feature の画面を内包しているため連鎖して差分が出る）
./gradlew :app:shared:recordRoborazziJvmTest
./gradlew :app:shared:verifyRoborazziJvmTest

# Android アプリのビルド確認（常に実行）
./gradlew :app:androidApp:assembleDebug

# デスクトップアプリのビルド確認（常に実行）
./gradlew :app:desktopApp:jar
```

detekt の主な閾値（`config/detekt/detekt.yml`）:
- `MagicNumber`: 無効（数値リテラルは許容）
- `LongMethod`: 閾値 100 行（`@Composable` は除外）
- `LongParameterList`: 関数・コンストラクタともに 8 個
- `TooManyFunctions`: ファイル・クラス・オブジェクト 20 個
- `CyclomaticComplexMethod`: 閾値 15

テストが失敗・detekt で指摘がある・moduleGraphAssert で違反がある・スクリーンショットテストに差分がある・ビルドエラーがある場合は修正してからレポートする。

---

## コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinJvm` プラグインを使用するため、ソースパスは `src/main/kotlin`（`src/jvmMain/kotlin` は `kotlinMultiplatform` 専用）。
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
- テストケース数は最小限に絞ること。正常系・異常系・境界値の 3 軸を意識し、冗長なケースは省く

### カバレッジ

Kover でカバレッジを計測する。新しいモジュールを追加した場合、ルートの `build.gradle.kts` の `kover { }` ブロックに `kover(project(":module:name"))` を追加しないとカバレッジ集計から除外される。

```bash
# ローカルでカバレッジレポート生成
./gradlew koverXmlReport
```
