# Kotlin コルーチン: structured concurrency

## 基本原則

適切に構造化されたコルーチンとは、単一の入口・単一の出口を持ち、呼び出し側で分かるライフサイクルにスコープされた、自己完結的な非同期処理の単位である。

**スコープは通常、呼び出し元のライフサイクルに紐づくべきであり、呼び出される側のプロパティとして保持すべきではない。** 保持された `CoroutineScope` は強いレビュー上のシグナルである — そのクラスは、キャンセル・エラー報告・再起動の挙動・ライフサイクルを自ら所有していることを証明しなければならない。ほとんどのリポジトリ・マネージャー・ユースケース・データソースはそれを証明できないため、代わりに `suspend` APIを公開すべきである。

修正はほぼ常に同じである: **APIを`suspend`にし、呼び出し元にスコープを所有させる。**

## このskillを使う場面

Kotlinのコードを書く・レビューする際に、以下のいずれかを見かけたとき。

- `private val scope: CoroutineScope` を持つクラス（コンストラクタ引数がプロパティとして保持されている）
- `init { scope.launch { ... } }` ブロック
- bodyが `scope.launch { ... }` である非suspendの公開関数
- suspend可能なアプリケーションコード内、または `runTest` を使うべきテスト内の `runBlocking { ... }`
- `CancellationException` を再スローしない、suspend呼び出しを囲む `runCatching { suspendCall() }` や `Exception` / `Throwable` に対する `catch`
- suspendを囲みつつ再スローしない `catch (e: CancellationException)`（またはそれに相当するもの）

## 静かなキャンセルバグ

所有者不明の `CoroutineScope` プロパティが危険な理由: **一度スコープがキャンセルされると、それ以降そのスコープに対するすべての`launch`は、例外もログもなく静かにキャンセル済みとして完了する。** 処理は単に実行されない。これはコルーチンのバグの中でも診断が特に難しいものの一つであり、あるクラスが自ら所有していないライフサイクルへの長生きの参照を保持している時に発生する。

APIが`suspend`であれば、これは起こりえない: 呼び出し元のスコープは生きている（処理が実行される）か、呼び出し箇所がキャンセルする（呼び出し元がそれを知る）かのいずれかになる。

## アンチパターンと修正

### 1. プロパティとして保持される CoroutineScope

```kotlin
// ❌ NG
@Inject
class UserRepository(
    private val scope: CoroutineScope,
    private val api: UserApi,
) {
    fun refresh() {
        scope.launch { _state.value = api.fetchUser() }
    }
}

// ✅ OK
@Inject
class UserRepository(
    private val api: UserApi,
) {
    suspend fun refresh(): User = api.fetchUser()
}
```

リポジトリはもはやコルーチンについて一切知る必要がない。呼び出し元（ViewModel、ユースケース）が、どのスコープで、どんなエラーハンドリングで、どんなキャンセルセマンティクスで実行するかを決める。

### 2. initブロックでのlaunch

```kotlin
// ❌ NG: コンストラクション時の副作用、無制限の処理
class UserSession(private val scope: CoroutineScope, private val api: Api) {
    init { scope.launch { _user.value = api.load() } }
}
```

コンストラクタは即座に返る。呼び出し元はロードを`await`できず、エラーも見えず、キャンセルもできない。クラスは「生きている」が、そのstateは未定義である。

```kotlin
// ✅ OK: 明示的なbootstrap、呼び出し元がsuspendを所有する
class UserSession(private val api: Api) {
    private var _user: User? = null
    val user: User get() = checkNotNull(_user) { "Call init() first" }

    suspend fun init() { _user = api.load() }
}
```

### 3. 非UIクラスからのfire-and-forget

**非UIクラス**（リポジトリ、マネージャー、ユースケース、データソース）にある、クラス所有のスコープへlaunchする非suspendの公開関数。呼び出し元は結果もエラーもキャンセルも得られず、処理が実行された保証もない。

```kotlin
// ❌ NG — スコープを保持しfire-and-forgetな公開APIを持つリポジトリ
class AnalyticsClient(private val scope: CoroutineScope, private val api: Api) {
    fun track(event: Event) {
        scope.launch { api.send(event) }      // 呼び出し元は何が起きるか分からない
    }
    fun signOut() {
        scope.launch { api.signOut() }        // スコープがキャンセルされていたら静かに失敗
    }
}
```

```kotlin
// ✅ OK
class AnalyticsClient(private val api: Api) {
    suspend fun track(event: Event) = api.send(event)
    suspend fun signOut() = api.signOut()
}
```

#### 例外: UI ↔ state holder の境界

UIフレームワークは非suspendである。ComposableのonClick、Fragmentの`onKeyEvent`、Activityの`onNewIntent` — いずれも`suspend`にはできない。state holder（ViewModel、Decompose Component、feature modelなど、UIイベントを吸収しUI stateを保持する役割を持つもの）こそが、非suspendの一度限りのUIイベントをUIライフサイクルに紐づいた非同期処理へ変換する境界**である**。それがその役目である。

```kotlin
// ✅ OK — state holderが非suspendのUIイベントを自身のスコープへ吸収する
class FavouritesViewModel(private val repo: FavouritesRepository) : ViewModel() {
    fun onToggleFavourite(item: Item) {
        viewModelScope.launch { repo.toggleFavourite(item) }
    }
}

// Compose側:
ListItem(onClick = { viewModel.onToggleFavourite(item) })
```

これはfire-and-forgetのアンチパターンでは**ない**。以下の3条件すべてが成り立つ必要がある。

1. **UIサーフェスのstate holder** — ViewModel、Decompose Component、feature model、またはそれに相当するUI state holder。リポジトリ・マネージャー・ユースケース・データソースではない。
2. **ライフサイクルに紐づいたスコープ** — `viewModelScope`、破棄時にキャンセルされるComponentの`coroutineScope`、Composableの`rememberCoroutineScope()`。`AppScope`ではなく、DIされた長生きのスコープでもなく、その場限りの`CoroutineScope(...)`でもない。
3. **呼び出し元が本当にUIイベントである** — Composableのコールバック、キーハンドラー、ライフサイクルフック。state holder経由で呼ぶ別のビジネスロジッククラスではない。

その下にあるリポジトリ/ユースケース/データソース層は依然として`suspend` APIを公開する。非suspend→suspendの変換が属するのは、state holderという層のみである。

「state holderっぽい」だけでは十分ではない。問うべきは「UIがこれに直接バインドしているか？」であり、そうでなければこの例外は適用されない。

### 4. インジェクトされていない保持スコープ

同じアンチパターンだが、スコープがインジェクトされていない場合。

```kotlin
// ❌ NG — 同じ問題。スコープがインジェクトではなくクラス内で構築されている
class FooManager {
    private val scope = MainScope()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}
```

ライフサイクルは今や誰にも所有されず、永遠に生き続ける。`suspend` APIへ置き換えること。

インスタンス化が関数のbody内にネストされている場合も同様である — `fun foo() { CoroutineScope(...).launch { … } }` は、余分な手順を踏んだだけの保持スコープに過ぎない。呼び出しごとにキャンセル不能な新しいスコープがリークし、`by lazy` プロパティにまとめても根本的な問題（そのスコープはそもそも存在すべきでない）は解決しない。

### 5. launchするDIバインドのシングルトン/イニシャライザ

見落としやすい特定のパターン: DIバインドのクラス（`@SingleIn(AppScope)`、`@Singleton`、`Initializer.initialize()`）が、コンストラクタ/`init`ブロック/`initialize()`からコルーチンをlaunchする。launchされた処理には以下が伴う。

- **非決定的な開始タイミング** — グラフがバインディングを実現するタイミング次第。コールドスタートの順序は不可視。
- **観測可能なライフサイクルがない。** それが実行中か、クラッシュしたかを、コードベースの他のどこからも確認できない。
- **`stop()`/再起動の経路がない。** 上流が異常な状態に入っても、ループはキャンセル不能。
- **grepできる呼び出しコードがない。** 読み手は「誰がこれをいつ起動するか」を見つけられない。

§1は「スコープは呼び出し元のライフサイクルに紐づくべき」としている。DIバインド版はこれを間接的に破っている: *スコープ*はインジェクトされているかもしれないが、*launch*はコンストラクション内に隠されている — 効果は同じで、見えにくいだけである。

```kotlin
// ❌ NG — シングルトンが構築の副作用として処理を起動する
@SingleIn(AppScope::class)
@Inject
class TokenRefresher(
    @ForScope(AppScope::class) private val scope: CoroutineScope,
    private val auth: AuthService,
) {
    init {
        scope.launch {
            while (isActive) {
                delay(5.minutes)
                auth.refreshIfNeeded()
            }
        }
    }
}

// ❌ これもNG — 登録するだけでなく*launch*する Initializer.initialize()
class TokenInvalidatorInitializer @Inject constructor(
    @ForScope(AppScope::class) private val scope: CoroutineScope,
    private val store: AuthStore,
    private val invalidator: TokenInvalidator,
) : Initializer {
    override fun initialize() {
        scope.launch { store.tokenChanges.collect { invalidator.invalidate() } }
    }
}
```

どちらも「アプリスコープのシングルトン」に見えるが、**適用しない場合**の例外は`init`/`initialize()`からのlaunchを許可するものでは*ない*。それは、APIがsuspendである場合にシングルトンがスコープを所有することを許可するものである。

#### まず問うべきこと: このバックグラウンドループクラスはそもそも存在する必要があるか

ほとんどのバックグラウンドループクラスは、単に観測の向きを反転させていないために存在する。優先順にした3つの答え:

**パターン1 — コンシューマー側へ反転する。** このクラスは変化に反応するためにstateを永遠に監視する。しかし*誰か*がそのstateを変更している — サインアウトフロー、プロフィール切り替え、フラグ更新ハンドラーなど。その変更箇所は既にコルーチンコンテキスト内にあり、その処理を直接行うのに自然な場所である。

```kotlin
// ✅ OK — バックグラウンドループもスコープもクラスも不要。変更箇所が処理を直接行う
class Authenticator(
    private val authStore: AuthStore,
    private val tokenInvalidator: TokenInvalidator,
) {
    suspend fun signOut() {
        authStore.clearTokens()
        tokenInvalidator.invalidate()   // 変更箇所での直接呼び出し
    }
}
```

バックグラウンドループクラスは**削除**される。処理はstateが変化する場所で行われる。

これが適用される場合: stateのコンシューマーが明確なライフサイクル（ユースケース、Authenticator、サービスハンドラー）を持ち、反応をインラインで実行できる場合。

**パターン2 — スケジュールされた処理。** 真に周期的・遅延的な処理であれば、WorkManager / BGTaskSchedulerを使う。enqueueは一度限りにし、起動時に一度だけ実行するオーケストレーターからsuspendとして呼ぶ。

**パターン3 — 明示的な名前付きlaunch箇所。** コンシューマーが観測可能なライフサイクルを持たない同期APIである場合がある（例: OpenTelemetryの`Sampler.shouldSample(...)`、AIDLスタブのファンアウト、ブロードキャストレシーバーのブリッジ）。観測はコルーチンを意識した場所に存在する必要があるが、それはクラス自身の`init`ではなく、*明示的な名前付き呼び出し箇所*に存在しなければならない。

```kotlin
// ✅ OK — 処理に名前が付き、明示的な呼び出し箇所がlaunchを所有する
@SingleIn(AppScope::class)
class OtelConfigurableSampler(...) : Sampler {
    @Volatile private var delegate: Sampler = ...

    suspend fun observeRate(featureFlags: FeatureFlags) {
        featureFlags.observe(OTEL_SAMPLING_RATE).collect { rate ->
            delegate = Sampler.traceIdRatioBased(rate.coerceIn(0.0, 1.0))
        }
    }

    override fun shouldSample(...) = delegate.shouldSample(...)
}

// OTel SDKの初期化モジュールで明示的に配線する:
applicationScope.launch { otelSampler.observeRate(featureFlags) }
```

これが適用される場合: コンシューマーが、観測可能なライフサイクルなしに自分に呼び出してくる同期APIである場合。launchを反転させることはできないが、それでも名前付きの箇所で可視化されていなければならない。

#### どのパターンが合うかのテスト

「そのコンシューマーのライフサイクルは自分から観測可能か？」

- **Yesで、既にコルーチンコンテキスト内にいる** → パターン1。購読をコンシューマー側に押し込み、バックグラウンドループクラスを削除する。
- **処理が周期的/遅延的** → パターン2。一度だけ呼ばれるsuspendのenqueue。
- **No、観測可能なライフサイクルを持たない同期APIである** → パターン3。`init`ではなく明示的なlaunch箇所。

「自分の代わりにすべてをlaunchしてくれる`Bootable`インターフェースが欲しい」のような第4の答えが浮かんだ場合、それは抽象化の層が1つ増えただけの同じアンチパターンである。要点はlaunchが*可視*であることであり、インターフェースによる自動発見はそれを台無しにする。

#### イニシャライザは*登録するだけ*なら問題ない

`Initializer`パターンは、`initialize()`がリスナーやフックを*登録する*場合には正しい。バグは`initialize()`がコルーチンを*launch*する場合である。

```kotlin
// ✅ OK な Initializer — コントリビューターを登録するだけで、launchしない
class FavouritesContributorInitializer @Inject constructor(
    private val registry: ContributorRegistry,
    private val favouritesContributor: FavouritesContributor,
) : Initializer {
    override fun initialize() {
        registry.register(favouritesContributor)
    }
}
```

**`Initializer.initialize()` はコルーチンを`launch`してはならない。** もしそうしているなら、パターン1/2/3の候補である。

#### レビュー時の診断

- 開始のタイミングはどこで定義されているか？「DIが自分を実現した時」なら悪い兆候。
- その処理が実行中かどうかを誰が観測できるか？「誰も」なら悪い兆候。
- 誰がそれを止めたり再起動したりできるか？「誰も」なら悪い兆候。
- 読み手はlaunch箇所をgrepできるか？できないなら悪い兆候。

答えが「コンシューマー/オーケストレーター/名前付き呼び出し箇所」であれば問題ない。

### 6. `CancellationException` の握りつぶし

`suspend`呼び出しを囲む`catch`節が、直接または`Exception`/`Throwable`経由で`CancellationException`にマッチし、再スローしない場合、通常はキャンセルが静かな成功に変わってしまう。親コルーチンは子が完了したと思い込むが、子（あるいはその副作用）は動き続ける。キャンセルの契約が破られる。

これは§1の保持スコープのバグと同じ失敗の形を、逆の端から見たものである: §1は処理を呼び出し元のライフサイクルから隠すが、これはキャンセルを処理から隠す。

```kotlin
// ❌ NG — CancellationExceptionをキャッチし、決して再スローしない
suspend fun fetch() {
    try {
        api.load()
    } catch (e: Exception) {           // CancellationExceptionにもマッチしてしまう
        logger.warn("load failed", e)
    }
}

// ❌ これもNG — runCatchingも同じ問題を持つ
suspend fun fetch() {
    runCatching { api.load() }
        .onFailure { logger.warn("load failed", it) }
}
```

許容される形:

```kotlin
// ✅ 最初に別のcatchを設ける
try { api.load() }
catch (e: CancellationException) { throw e }
catch (e: Exception) { logger.warn("load failed", e) }

// ✅ 広いcatchの中で条件付き再スロー
try { api.load() }
catch (e: Exception) {
    if (e is CancellationException) throw e
    logger.warn("load failed", e)
}

// ✅ ensureActive() — catchが通常の失敗を処理し、現在のコルーチンが
// キャンセルされている場合にのみ再スローすればよい場合に適する
try { api.load() }
catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    logger.warn("load failed", e)
}

// ✅ 明示的なガード付きのrunCatching
runCatching { api.load() }
    .onFailure {
        if (it is CancellationException) throw it
        logger.warn("load failed", it)
    }

// ✅ getOrThrowで終端するrunCatching（キャンセルは外へ伝播する）
runCatching { api.load() }.getOrThrow()
```

トリガーは「`try`の中にsuspend呼び出しがある」ことであり、「囲む関数が`suspend`と宣言されている」ことではない。これはあらゆるsuspendするbody内に適用される — `suspend fun`、`launch { … }`のラムダ、Flowの`collect { … }`など。

よくある例外は、意図的にローカルなタイムアウトである: 自身の`withTimeout`から`TimeoutCancellationException`をキャッチし、ドメイン結果に変換することは正しい場合がある。そのcatchは狭く、タイムアウトの近くに留めること。任意のキャンセルを握りつぶす許可としては使わないこと。

キャンセル以外のサブタイプ（`IOException`、独自の例外型）をキャッチするのは問題ない — これらは`CancellationException`を継承していない。

### 7. `runBlocking`

`runBlocking`は、ラムダが完了するまで現在のスレッドをブロックする。suspend可能またはライフサイクルにスコープされたアプリケーションのパス内では誤りである: 非同期であるはずのスレッドがブロックされ、structured concurrencyが破壊され、上流のキャンセルは何の効果も持たなくなる。「呼び出される側が呼び出し元の代わりに構造的な決定を下す」アンチパターンの最も直接的な形である。

```kotlin
// ❌ NG — 呼び出し側のスレッドをブロックしてsuspendへブリッジする
fun saveUser(user: User) {
    runBlocking { repository.save(user) }
}
```

文脈に応じた3つの修正:

**suspend可能なアプリケーションコード** — 関数を`suspend`にする:

```kotlin
// ✅ OK
suspend fun saveUser(user: User) = repository.save(user)
```

直接の呼び出し元もsuspendできない場合（非suspendのUIコールバック、`BroadcastReceiver`フックなど）は、境界にある既存のライフサイクルに紐づいたスコープを使う — §3のUI↔state holderの例外を参照。修正は境界にあるべきで、`saveUser`の内部ではない。

正当なブロッキング境界も存在する: CLIツールの`main`、同期的に返さなければならないJava相互運用API、suspendの代替がないフレームワークコールバック、移行用のシムなど。`runBlocking`はその外側の境界に留め、bodyは小さく保ち、すぐにsuspendコードを呼ぶこと。

**テスト** — `runTest`を使う:

```kotlin
// ❌ NG — 実時間、遅いテスト、仮想delayなし
@Test fun loadsUser() = runBlocking {
    assertThat(repository.load().name).isEqualTo("Alice")
}

// ✅ OK
@Test fun loadsUser() = runTest {
    assertThat(repository.load().name).isEqualTo("Alice")
}
```

`runTest`は仮想時間（`delay()`が即座に返る）、`TestDispatcher`との統合、適切なコルーチンのクリーンアップを提供する。テストでの実時間の`runBlocking`は、テストを遅く不安定にする。

**`ContentProvider`の例外** — AndroidのContentProviderメソッド（`query`、`insert`、`update`、`delete`、`onCreate`、`call`）はプロセス外から見て同期的である。これらをsuspendにする方法はない。`ContentProvider`サブクラスの*メンバー関数*内（直接・間接を問わず、コンパニオンオブジェクトは除く）では、`runBlocking`は避けられないブリッジである。bodyはできるだけ短くし、すぐにsuspendコードを呼ぶこと。

```kotlin
// ✅ ContentProviderのメンバーでのみ許容される
class MyProvider : ContentProvider() {
    override fun query(...): Cursor? = runBlocking { dao.query(...) }
}
```

この例外は`android.content.ContentProvider`サブクラス*のみ*に適用される。「ContentProviderに似ている」は適用されず、`ContentProvider`のコンパニオンオブジェクト内の`runBlocking`も依然として通常の違反である — そのヘルパーはフレームワークの同期サーフェスの一部ではない。

## クイックリファレンス

| 症状 | アンチパターン | 修正 |
|---|---|---|
| クラスが`private val scope: CoroutineScope`を持つ | 呼び出される側での保持スコープ | 削除する。公開APIを`suspend`にする。 |
| `init { scope.launch { ... } }` | コンストラクション時のlaunch | `suspend fun init()` / `login()` へ移す |
| リポジトリ/マネージャー/ユースケースの`fun foo() { scope.launch { ... } }` | 非UIクラスからのfire-and-forget | `suspend fun foo()`にし、UI state holderにスコープを選ばせる |
| state holderの`fun onClick() { viewModelScope.launch { ... } }`（UIから呼ばれる） | UI↔state holder境界 — 問題ない | そのまま維持（§3の例外を参照） |
| `private val scope = MainScope()` | 内部で構築された保持スコープ | 同様に削除し、APIを`suspend`にする |
| `@SingleIn(AppScope) class X(scope) { init { scope.launch { … } } }` | DIバインドの不透明なlaunch（§5） | `suspend fun run()`を公開し、起動時オーケストレーターからlaunchする |
| `class Y : Initializer { override fun initialize() { scope.launch { … } } }` | 登録ではなくlaunchするInitializer（§5） | 同様に`suspend fun run()`にし、オーケストレーターがライフサイクルを所有する |
| 再スローなしの`try { suspendCall() } catch (e: Exception\|Throwable\|CancellationException) { … }` | キャンセルの握りつぶし（§6） | `catch (e: CancellationException) { throw e }`を優先。意図に合う場合のみ`ensureActive()`を使う |
| キャンセルガードなしの`runCatching { suspendCall() }.onFailure { … }` | 上と同じ形（§6） | `if (it is CancellationException) throw it`を追加するか、`.getOrThrow()`で終端する |
| suspend可能なアプリコード内の`runBlocking { … }` | スレッドをブロックするブリッジ（§7） | 呼び出し元を`suspend`にするか、境界でライフサイクルスコープを使う |
| テスト内の`runBlocking { … }` | 同様 — 実時間ブリッジ（§7） | `runTest { … }`を使う |
| `ContentProvider.query`/`insert`/…のメンバー内の`runBlocking { … }` | 例外（§7） | 許容される。bodyは最小限に保つ |

## リファクタリングの進め方

既存の違反を取り除く手順:

1. **末端から始める。** UIから最も遠いクラス（通常はリポジトリやデータソース）を選ぶ。その公開サーフェスが最も変換しやすい。
2. **公開関数を1つずつ`suspend`に変換する。** コンパイラがすべての呼び出し元を表面化させる。
3. **各呼び出し元で、スコープを意図的に選ぶ:** `viewModelScope`、`lifecycleScope`、`coroutineScope { }`、または明示的なJob。これが以前は欠けていた選択である。
4. **何も使わなくなったら`CoroutineScope`のコンストラクタ引数を削除する。** インジェクションのバインディングも削除する。

1つのMRですべてのクラスを直そうとしないこと。アンチパターンの除去は段階的な作業である。

## 適用しない場合

- **UIイベントを吸収するUI state holder。** `fun onClick(...) { viewModelScope.launch { ... } }`を持つViewModel/Component/feature modelは正しい — それはフレームワークが必要とする境界である。§3の例外を参照。
- **明示的なキャンセル・エラーポリシーを持つライフサイクルオーナー。** Actor/サービス、アプリインフラ、アプリスコープのシングルトンは、明確な`close`/`cancel`/再起動の挙動を公開するか、アプリケーションのライフサイクルに直接対応する場合にスコープを所有してよい。その場で作るのではなく、`Application.applicationScope`を明示的にインジェクトすること。**これは`init`/`initialize()`からのlaunchを許可するものではない** — §5を参照。
- **既にsuspendなAPI** はこの作業を必要としない。
- **テスト** では、意図的なアンビエントスコープとして`TestScope`を使うことがある — これは仮想時間の明示的な制御を伴う別のパターンである。

## レビュー時の危険信号

以下の思考はアンチパターンが戻ってきたことを意味する。

| 思考 | 実際 |
|---|---|
| 「スコープに`CoroutineExceptionHandler`を追加すればいい」 | 問題はエラーハンドリングではない。問題はそのスコープがそもそも存在すべきでないことである。 |
| 「コンシューマーが来た時にデータが準備できているよう、`init`からlaunchする必要がある」 | 準備できていないstateをコンシューマーが読むこと自体がバグである。フェーズ分けを使うこと。 |
| 「呼び出し元は`suspend`を扱いたくない」 | それなら呼び出し元が自分のスコープでfire-and-forgetを選べばよい。代わりに決めてはならない。 |
| 「ただの小さなfire-and-forget呼び出しだ」 | 静かなキャンセルにより、あらゆるfire-and-forgetが潜在的な静かな失敗になりうる。 |
| 「キャッチしてログに出したから大丈夫」 | そのcatchは`CancellationException`を再スローしたか？していなければ、そのコルーチンは静かにキャンセル不能になっている（§6）。 |
| 「重要でないパスの`runBlocking`が1つだけだ」 | すべての`runBlocking`は、呼び出し元に非同期の選択肢がないと主張している。選択肢があるなら、それは誤ったプリミティブである（§7）。 |
| 「テストは`runBlocking`の方が簡単」 | 実時間で動き、`delay`を早送りできず、`TestDispatcher`のセマンティクスを失う。`runTest`を使うこと（§7）。 |

## 関連

- [Flow state and events](flow-state-events.md) — `StateFlow`、`SharedFlow`、`Channel`、`stateIn`、一度限りのイベント、および関連するモデリング。
