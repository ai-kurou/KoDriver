# コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutListViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- LMU Windows共有メモリ固有の実装はJVM専用の `core:lmu-windows-data` に置き、`core:data` へ依存させない。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- `@Preview` 関数名は末尾を `Preview` で終える（例: `XxxScreenPreview`）こと。detekt の `UnusedPrivateMember`（`config/detekt/detekt.yml`）は `@Preview` にアノテーション除外設定が無いため、`allowedNames: '.*Preview'` という命名規則ベースの除外で対応している。この命名から外れると、IDEプレビュー/スクリーンショットテストからのみ呼び出される `@Preview` 関数が未使用コードとして detekt に検出される。
- 文字スタイルは `MaterialTheme.typography.*` を参照し、`fontSize` / `FontWeight` を Composable 内で直接指定しない。アプリ全体のタイポグラフィは `:core:designsystem` の `KoDriverTypography` で一元管理する。
- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は `XxxViewModel` 内の `xxxDisplayNames: Map<String, String>` で分離する。

## Repository の命名規則

`Repository` は責務に応じて接尾辞で区別すること。命名だけで「取得用」か「設定保存用」かが判別できる状態を保つ。

- **データ取得用**（テレメトリ・走行データなど外部ソースからの読み取り、Flow 配信、バージョン取得など）は接尾辞なしの素の `XxxRepository`（例: `LmuWindowsRepository`, `LmuWindowsFlagRepository`, `Gt7Ps5Repository`, `ServerVersionRepository`）。
- **設定保存用**（DataStore による永続化）は必ず `XxxPreferencesRepository`（複数値・任意型の設定）または `XxxEnabledRepository`（単一の有効/無効フラグ）の接尾辞を付ける（例: `ThemePreferencesRepository`, `ConsoleAddressPreferencesRepository`, `ServerIpPreferencesRepository`, `KeepScreenOnEnabledRepository`）。設定保存用を素の `XxxRepository` にしてはならない。
- **送信・実行用**（外部サービスへの送信など、取得・永続化を伴わない書き込み専用のアクション実行）は必ず `XxxSenderRepository` の接尾辞を付ける（例: `FeedbackSenderRepository`）。送信・実行用を素の `XxxRepository` にしてはならない。

## ViewModel の設計規則

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

## MutableStateFlow の更新

`MutableStateFlow` の値を更新するときは **必ず `update { }` を使うこと**。`.value = ...` の直接代入は競合状態を招く恐れがある。

```kotlin
// NG
_state.value = _state.value.copy(count = _state.value.count + 1)

// OK
_state.update { it.copy(count = it.count + 1) }
```

## Coroutines のエラーハンドリング

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

テストにおける mockk の `any()` 使用ルールは [`docs/testing-guidelines.md`](testing-guidelines.md) を参照。
