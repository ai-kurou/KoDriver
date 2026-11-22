# コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`AutoReadoutListViewModel` を参照）。
- `LmuWindowsRepository.telemetryStream()` は `Flow<LmuWindowsTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（`LmuWindowsSharedMemorySource` の `pollingIntervalMs`、≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- LMU Windows共有メモリ固有の実装はJVM専用の `core:lmu-windows-data` に置き、`core:data` へ依存させない。
- モジュール間の依存（`projects.*`）は `implementation(projects.xxx)` で宣言し、`api(projects.xxx)` は使わない。`api` は依存先を利用側へ推移的に公開するため、`moduleGraphAssert` が禁止している依存（例: `app:*App` → `core:domain`）が見かけ上の経路で成立したり、不要な依存が利用側のコンパイルクラスパスへ広がったりする。依存先の型が必要なモジュールは、自身の `build.gradle.kts` で明示的に宣言する。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- `@Preview` 関数名は末尾を `Preview` で終える（例: `XxxScreenPreview`）こと。detekt の `UnusedPrivateMember`（`config/detekt/detekt.yml`）は `@Preview` にアノテーション除外設定が無いため、`allowedNames: '.*Preview'` という命名規則ベースの除外で対応している。この命名から外れると、IDEプレビュー/スクリーンショットテストからのみ呼び出される `@Preview` 関数が未使用コードとして detekt に検出される。
- 文字スタイルは `MaterialTheme.typography.*` を参照し、`fontSize` / `FontWeight` を Composable 内で直接指定しない。アプリ全体のタイポグラフィは `:core:designsystem` の `KoDriverTypography` で一元管理する。等幅・数値表示は `:core:designsystem` の `koDriverMonospaceTextStyle()` / `koDriverNumericTextStyle()` を使い、`FontFamily` を feature で直接指定しない。
- 角丸は `MaterialTheme.shapes.*` を参照し、`RoundedCornerShape` を Composable 内で直接指定しない。アプリ全体の角丸は `:core:designsystem` の `KoDriverShapes` で一元管理する。
- 余白（padding・Spacer・`Arrangement.spacedBy`）のうち 4/8/12/16/24dp の値は、Composable 内で dp を直接指定せず `:core:designsystem` の `KoDriverSpacing`（`app:shared` では `AppSpacing`）を参照する。アイコンサイズ等「余白ではない」寸法は対象外。
- list/detail 構成の画面の一覧ペインの初期幅は `:core:designsystem` の `KoDriverListPaneWidth`（400.dp）を参照する。`app:shared` は `:core:*` に依存できないため `AppPaneWidth` に同値を別定義しており、変更する場合は両方を同期させる。デバッグ状態詳細画面の `NARROW_WIDTH_UPPER_BOUND` は別の意味の境界値なので共用しない。
- `MaterialTheme.colorScheme` にロールがない拡張カラー（警告色等）は、色を直接 `Color(0x...)` で書かず `:core:designsystem` の `KoDriverExtendedColors.current`（`app:shared` では `AppExtendedColors.current`）を参照する。ライト/ダークの値は `ExtendedColors.kt`（`app:shared` では `AppExtendedColors.kt`）に定義し、`KoDriverTheme`/`AppTheme` が `CompositionLocalProvider` で配布する。ただし、ゲーム画面に重ねるオーバーレイ（`NarratorOverlayContent` 等）のようにアプリテーマ（ライト/ダーク）から独立させる必要のある固定色は、その理由を KDoc に書いたうえで `private val` として直接定義してよい。テーマのカラーパレット定義（`AppTheme.kt` 等）も対象外。

## 見出しのタイポグラフィ階層

ListPane・DetailPane の見出しは、画面の階層（見出し → セクション → 項目）に応じて以下のスタイル・色を使い分けること。

| 階層 | スタイル | 色 |
|---|---|---|
| ListPane のページ見出し（例: `AutoReadoutListPane` の「読み上げ優先度」） | `titleMedium` | `onSurface` |
| DetailPane のセクション見出し（`DetailPaneSubtitle`） | `titleSmall` | `primary` |
| カード・項目のタイトル（`DetailPaneCard` 等） | `bodyLarge` | `onSurface` |

ページ見出しを `titleLarge` / `headlineSmall` にすると ListPane 上部の余白が大きくなり、スマホでの一覧表示件数が減るため `titleMedium` にとどめる。

- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は分離し、`ReadoutItemKey` の値から表示名への変換は `:core:designsystem` の `readoutItemDisplayName(value)` に集約する。

## コメント・KDoc の言語

コメントと KDoc は**日本語で書く**こと。言語が混在すると読み手の負担が増え、新しくコードを書くときにどちらへ合わせるべきか判断しにくくなる。

ただし、次のものは英語のままでよい。

- 共有メモリの構造体フィールド名・オフセット表（`mCurrentET`、`mWheels[4]` 等）や、`OpenFileMappingA` のような API 名といった固有名詞。
- 外部仕様・公式ドキュメントからの引用や、そこで定義された用語（Salsa20 の `column round` など）。必要なら `列ラウンド（column round）` のように訳語と併記する。
- 識別子だけを見出しとして書くコメント（テストの `// bufferFlow`、Koin モジュールの `// ViewModel` など）。

説明文（散文）として書く部分は日本語にする。

レースの旗（Yellow Flag 等）は「フラッグ」と表記し、真偽値・ビット（bit flag、有効/無効フラグ）は「フラグ」と表記する。

Energy の日本語表記は「エネルギー」に統一する。LMU の Virtual Energy は「バーチャルエネルギー」と表記する。

カタカナ語の長音は、`strings.xml` に合わせて「シミュレーター」「ドライバー」と末尾の長音を付け、「テレメトリ」のみ長音を付けない。

ピットの出入りは「ピットイン」「ピットアウト」と表記する（「ピット進入」「ピット退出」は使わない）。

コード・UI 文言・ドキュメントで次の表記に統一する（表記揺れの調査で多数派だったものに合わせている）。

| 統一する表記 | 使わない表記 |
|---|---|
| リポジトリ | レポジトリ |
| ℃（温度の単位） | °C |
| すべて | 全て |
| ON/OFF、有効/無効 | オン/オフ、オン・オフ、有効・無効 |
| ユニットテスト | 単体テスト |
| エミュレーター | エミュレータ |
| 切り替え・割り当て・読み込み・引き継ぎ（送り仮名を省略しない） | 切替・割当・読込・引継ぎ |
| IPアドレス・IPv4アドレス（日本語と半角英数字の間に空白を入れない） | IP アドレス・IPv4 アドレス |
| Gran Turismo 7（GT7 は略称として可） | グランツーリスモ7 |

- 識別子（クラス名・関数名・リソース ID）、外部仕様の引用、固有名（`Auto Hide` など、上の表に載っていない固定された外部名称）、翻訳文書 `docs/tinypedal-guide.md` の体裁は対象外とする。表に載っている表記（`Gran Turismo 7` など）は固有名であっても表を優先する。
- 音声コマンドの語彙（ウェイクワードの「ドライバ」「ドライヴァー」、手動読み上げの「ベストタイム」など）は、認識のための意図的な言い換えなので統一しない。

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

### 例外を記録して握りつぶす場合のヘルパー

失敗しても処理を続けたく、例外を `captureError` へ記録するだけでよい場合は、`try-catch` を手書きせず `core:domain` のヘルパーを使う。いずれも `CancellationException` は再スローし、それ以外の例外を `captureError` へ記録する。

| ヘルパー | 使う場面 |
| --- | --- |
| `runCapturingErrors { ... }` | 結果が不要な処理（保存・送信など）。戻り値は `Unit` |
| `runCapturingErrorsOrNull { ... }` | 成功可否や値が必要な処理。失敗時は `null` を返す（`T : Any`） |

```kotlin
// 成功可否が必要: 非null値を返して `!= null` で判定する
val succeeded =
    runCapturingErrorsOrNull {
        deleteLog(id)
        true
    } != null

// 値が必要: 失敗時の既定値は `?:` で補う
val voices = runCapturingErrorsOrNull { getAvailableVoices() } ?: emptyList()
```

次の場合はヘルパーを使わず、従来どおり `try-catch` を手書きする。

- 例外の種類で処理を分ける（例: `IOException` は記録せず、それ以外を記録する）。
- 記録先が別にある（`core:narrator` の `runCatchingNarratorError` など）。
- ブロック内で `return@launch` や `return@withContext` のように非ローカルで抜ける（ヘルパーは非 inline のため書けない）。
- 例外をそのまま呼び出し側へ伝えたい。

テストにおける mockk の `any()` 使用ルールは [`docs/testing-guidelines.md`](testing-guidelines.md) を参照。

## Kotlin コンパイラの警告

ルートの `build.gradle.kts` で、全サブプロジェクトの Kotlin コンパイルに `allWarningsAsErrors` を設定している。警告が出るとコンパイルが失敗するため、`@Suppress` で隠さず原因を解消する。非推奨 API の移行で検証方式やリソース読み込みの挙動が変わるなど、そのPRで対応できない場合に限り、理由をコメントに書いて対象の宣言・式に限定した `@Suppress("DEPRECATION")` を付ける。

## Android Lint の重大度

`kmp-library` プラグイン（`build-logic`）と `app:androidApp` の `lint` ブロックで、`NewApi` と `InlinedApi` を `error` に固定している。minSdk 未満の API を `SDK_INT` ガードなしで呼ぶと古い端末でのみクラッシュするため、既定の重大度や suppress の変更で検出が弱まらないようにするため。

- Lint baseline（`lint-baseline.xml`）は使わない。既存の違反は baseline で隠さず、コードを直すか、理由を書いた個別の `@SuppressLint` で対応する。
- `@SuppressLint("InlinedApi")` は、対象が `SDK_INT` ガード済み、またはインライン展開される定数で実行時に安全な場合に限り、理由コメントを付けて対象の宣言に限定する。
- 違反の検出確認は、ガードなしの新 API 呼び出しを一時的に入れて `lint` が失敗することを確かめ、確認後に戻す（コミットしない）。

## Kotlin の公開範囲（explicitApi）

`explicitApi()`（strict モード）は、`build-logic` の `kmp-library` プラグインを使う KMP ライブラリモジュールのほか、`server` や `app:androidApp` など個別に有効にしたモジュールでも有効になっている。対象モジュールの main 系ソースセットでは、宣言の可視性と、public な関数の戻り値型・public なプロパティの型を明示する。

- 他モジュールや AndroidManifest から参照される宣言だけを `public` にし、モジュール内でのみ使う宣言は `internal` / `private` にする。参照の有無は `rg` で確認する。
- public 宣言のシグネチャに `internal` 型を出せないため、型ごと `public` にするか、宣言側を `internal` にする。
- `expect` / `actual` は両方の可視性を揃える。interface のメンバーは `internal` にできないため、契約として公開するものは `public` のままとする。実装本体を持つ補助メンバーは `private` にできる。
- Koin モジュールは `public val xxxModule: Module`（`org.koin.core.module.Module`）の形で型を明示する。Unit を返す public 関数も `: Unit` を明示する。
- `override`・enum entry・data class のコンストラクタ引数には可視性の明示は不要。
- テストソースセットは対象外。Kotlin プラグインを使わない `com.android.test` モジュール（`app:androidBenchmark`）も対象外。

## ペイン・タブ切り替え時の初期フォーカス

一覧ペイン⇔詳細ペイン、タブの切り替えでは、キーボード利用者が Tab を何度も押さなくて済むよう、共通部品（`core/designsystem`）で初期フォーカスを決める。各 feature では共通部品を呼ぶ 1 か所だけを変更し、フォーカス用の独自処理は書かない。

### 方針

- フォーカスを動かすのは `InputMode.Keyboard` のときだけ。タッチ操作では要求しない（テキスト入力に当たると IME が開くため）。詳細ペイン（`detailPaneInitialFocus()`）は入力モードをペインに入った時点で一度だけ判定し、表示中の切り替えではフォーカスを奪わない。一覧側の `RestoreRowFocusOnReturn` / `FocusFirstRowOnEntry` は、フォーカスを要求する時点の入力モードを見る（先頭行がまだ無く後から決まる場合は、決まった時点のモードで判定する）。
- 一覧と詳細が並んで表示される 2 ペイン表示では、一覧の行フォーカスを奪わない。詳細へ初期フォーカスを置くのは、詳細が新しく開いた瞬間だけ。
- `Dialog` で開く詳細は対象外。フォーカスは OS 側の仕組みに任せる。
- 合成直後は `requestFocus` が失敗しうるため、`withFrameNanos { }` で 1 フレーム待ち、`runCatching { requestFocus() }` で失敗を無視する。画面外の行へ移す場合は、一覧側の部品に `listState` と `lazyIndexOf` を渡すと、内部（`core/designsystem` の `internal` な `focusItemWhenComposed`）でスクロールしてから移す。feature から `focusItemWhenComposed` を直接呼ばない。
- フォーカスできる部品が無いときは何も起きない（例外にしない）。

### 共通部品の使い分け

| 場面 | 部品 | フォーカス先 |
|------|------|--------------|
| 詳細を開いたとき | `Modifier.detailPaneInitialFocus()`（`DetailPaneScaffold` と `AutoReadoutDetailPane` の本文に適用済み） | 本文先頭の操作部品 |
| 詳細から一覧へ戻ったとき | `RestoreRowFocusOnReturn` | 直前に開いた行 |
| タブを切り替えたとき | `FocusFirstRowOnEntry` | 一覧の先頭の行 |

- 一覧は、タブ切り替えと詳細からの復帰の両方に対応するため、`FocusFirstRowOnEntry` と `RestoreRowFocusOnReturn` の両方を組み込む（呼び出しを 1 つの Composable にまとめてもよい）。
- 新しい詳細ペインが `DetailPaneScaffold` を使うなら追加の対応は不要。使わない場合は本文を囲む要素に `detailPaneInitialFocus()` を付ける。
- 一覧は `ListRowFocusState` の `requesterFor(key)` で行ごとの `FocusRequester` を受け取る。`RestoreRowFocusOnReturn` へ渡す復元キーは、詳細から戻った（選択が解除された）ときだけ非 null にする。
- `FocusFirstRowOnEntry` の `enabled` は、詳細が選択済みのときや復元キーがあるときに false にする。表示時点で false なら、その表示では先頭行へフォーカスを置かない。先頭行へのフォーカスは 1 回のコンポジション（タブ内容が新しく表示されるたび）につき一度だけで、同じコンポジションのまま非表示から戻った場合は再度置かない。
- 手動読み上げの一覧は現状 `listState` / `lazyIndexOf` を渡していないため、画面外の行へはスクロールして移せない（画面内の行のみ対象）。
- 音声認識の一覧やログの一覧など `ListRowFocusState` を持たない一覧は、現状は対象外。
- テストでは `LocalInputModeManager` を `InputModeManager` のフェイクで差し替え、`Keyboard` と `Touch` の両方を確認する。
