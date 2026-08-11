# Kotlin Flow: stateとイベントのモデリング

## 基本原則

**リプレイ・ファンアウト・同期読み取りの要件に合ったプリミティブを選ぶこと。** `StateFlow`、`SharedFlow`、`Channel`裏付けのFlow、cold `Flow` はバッファリング、各emissionを誰が見るか、`.value` が存在するかという点で異なる。選択を誤ると、イベントの消失、共有コルーチンのリーク、stateへの偽のドメインsentinelの混入を引き起こす。

## このskillを使う場面

以下を含むKotlinコードを書く・レビューする際。

- `MutableStateFlow<T>(SomeSentinel)` — `NoUser`、`Empty`、`Loading` など — 実際の値が非同期であるため
- 関数内で `.stateIn(...)` を呼び、プロパティに代入していない
- `.value` を同期的に読み取り常に最新である必要があるFlowに `SharingStarted.WhileSubscribed(...)` を使っている
- ナビゲーションやスナックバーなど、消失がバグになる一度限りのemissionに `MutableSharedFlow` を使っている
- コンシューマーがまだ同期的な `.value` を必要としているのに `StateFlow` に `.map { }` している
- `MutableStateFlow.value = _state.value.copy(...)`、または `update { ... }` の中で高コストなオブジェクトを構築している

## 単一コンシューマー・一度限りのイベントには `SharedFlow`

`SharedFlow` のデフォルトにはリプレイバッファがない。emissionの瞬間に誰も収集していなければ、そのイベントは失われる。ナビゲーションやスナックバーのように**単一のUIコンシューマー**が厳密に一度だけ処理すべきイベントには、Flowとして公開されるバッファ付きの `Channel` の方がセマンティクスに合うことが多い。

```kotlin
// ❌ NG
private val _navEvents = MutableSharedFlow<NavigationEvent>()
val navEvents: SharedFlow<NavigationEvent> = _navEvents.asSharedFlow()

// ✅ OK
private val _navEvents = Channel<NavigationEvent>(Channel.BUFFERED)
val navEvents: Flow<NavigationEvent> = _navEvents.receiveAsFlow()
```

`Channel.receiveAsFlow()` は**ファンアウトであり、ブロードキャストではない**: 複数のコレクターがいる場合、各イベントは**1つ**のコレクターにのみ配信される。`Channel.BUFFERED` は有界であるため、sendがsuspendすることがあり、`trySend` が失敗することもある。複数のオブザーバー全員が同じイベントを見る必要がある場合は、明示的なstate・永続ストレージ・意図的に構成された `SharedFlow` を代わりに使うこと。

## 無効なsentinelデフォルト値で汚染された `StateFlow`

`StateFlow` は初期値を強制する。実際の値が非同期である場合、開発者は時に `NoUser`、`EmptyUser`、プレースホルダーIDのような偽のドメイン値を発明し、すべてのコンシューマーがそのsentinelを実データとして扱うことを強いられる。

```kotlin
// ❌ NG — sentinelが型に漏れ出している
class UserSession(private val db: Db) {
    private val _user = MutableStateFlow<User>(NoUser)
    val user: StateFlow<User> = _user.asStateFlow()
    init { scope.launch { _user.value = db.load() } }
}
```

1つの解決策は**フェーズ分け**である: 実際の値が存在するまで `StateFlow` を公開しない。

```kotlin
// ✅ OK — bootstrapがsuspendし、オブザーバーは実在するユーザーしか見ない
class UserSession(private val db: Db) {
    private var _user: MutableStateFlow<User>? = null
    val user: StateFlow<User>
        get() = checkNotNull(_user) { "Call login() first" }

    suspend fun login() {
        _user = MutableStateFlow(db.load())
    }
}
```

不在・ローディング・エラーが実際のstateであるなら、明示的にモデリングすること（`User?`、`sealed interface UserUiState`、`Result` など）。バグは「あらゆる初期値」ではなく、実データを装った偽のドメイン値そのものである。

## `MutableStateFlow` は `update { ... }` で変更する

`.value` を読み書きするより、`MutableStateFlow.update { current -> ... }` を優先する。`update` は最新のstateに対してアトミックに変換を適用するため、複数のコルーチンが同じstateを変更する際の更新ロストを避けられる。

```kotlin
// NG — 読み取り/変更/書き込みは並行更新をロストしうる
_state.value = _state.value.copy(
    selectedId = id,
    details = details,
)

// OK — 変換は最新のstateから開始する
_state.update { current ->
    current.copy(
        selectedId = id,
        details = details,
    )
}
```

現在のstateを必要としない限り、オブジェクトの生成は `update` ブロックの外で行うこと。updateのラムダはリトライされることがあるため、その中の高コストな処理や副作用は複数回実行される可能性がある。

```kotlin
// OK — detailsは現在のstateに依存しないので一度だけ構築する
val details = Details.from(response)
_state.update { current ->
    current.copy(details = details)
}

// OK — 派生値が現在のstateに依存するので内部で計算する
_state.update { current ->
    val nextItems = current.items.replaceById(updatedItem)
    current.copy(items = nextItems)
}
```

このブロックは純粋で高速なstate変換であるべきで、ブロック実行前にキャプチャした値でない限り、ネットワーク呼び出し・データベース書き込み・ログ出力の副作用・ランダムID・時刻の読み取りを含めてはならない。

## 関数内での `stateIn()`

```kotlin
// ❌ NG — 呼び出しごとに新しい共有コルーチンが作られる
fun getPreferences(): StateFlow<Prefs> =
    repo.prefsFlow.stateIn(scope, SharingStarted.Eagerly, Prefs.Default)
```

`getPreferences()` を呼ぶたびに、`scope` 上で決して完了しない新しいコルーチンが起動される。繰り返し読み取ると性能はすぐに悪化する。

```kotlin
// ✅ OK — 単一の共有インスタンスを一度だけ計算する
val preferences: StateFlow<Prefs> =
    repo.prefsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Prefs.Default)
```

## `WhileSubscribed` と同期的な `.value`

`SharingStarted.WhileSubscribed(timeout)` は、アクティブなコレクターがいなくなると上流を切断する。切断中は `.value` が最後のキャッシュ値を返すが、これは古いか初期値のままかもしれない。

**ルール:** `.value` がアクティブなコレクターなしに常に最新・初期化済みである必要があるなら、`SharingStarted.Eagerly` または明示的な初期化を使うこと。古い/キャッシュされた値が許容でき、コンシューマーが主に非同期に収集するなら `WhileSubscribed` で問題ない。

## `StateFlow` への `.map` は `.value` を失う

```kotlin
// ❌ NG — `name.value`はコンパイルできない。これは単なるFlowになっている
val name: Flow<String> = userState.map { it.name }
```

同期的な `.value` が必要なら、チェーンを `.stateIn(...)` で終端すること。

```kotlin
// ✅ OK
val name: StateFlow<String> = userState
    .map { it.name }
    .stateIn(viewModelScope, SharingStarted.Eagerly, userState.value.name)
```

コミュニティの「derived state flow」ユーティリティは `.value` を読むたびに変換を実行する — 高速で冪等な変換にのみ許容できる。デフォルトは `.stateIn(...)` にすること。

## 判断: どのFlow型を使うか

| 必要なこと | プリミティブ |
|------|-----------|
| 常に値を持ち、非同期のコレクターと同期コードの**両方**から読まれるstate | `StateFlow`（`.value`が重要なら`SharingStarted.Eagerly`が多い） |
| ホットストリーム、複数のsubscriber、同期的な`.value`が**不要** | `SharedFlow` |
| **1つの**コンシューマー向けの個別イベント、厳密に一度だけの受け渡し | `Channel(BUFFERED).receiveAsFlow()` を検討 |
| coldストリーム、収集ごとに1コンシューマー | 通常の `Flow` |

`SharedFlow` に手が伸びそうになったら、こう問うこと: emissionを落とすことはバグか、何人のコンシューマーが見る必要があるか。1つのコンシューマーが厳密に一度処理する必要があるなら `Channel` が合うかもしれない。すべてのオブザーバーが見る必要があるなら、永続的なstateをモデリングするか、ブロードキャストストリームを意図的に構成すること。

## クイックリファレンス

| 症状 | 問題 | 修正 |
|---------|---------|-----|
| `MutableStateFlow<X>(FakeDomainValue)` | 無効なプレースホルダーのデフォルト | 不在を明示的にモデリングするか、フェーズ初期化を使う |
| 単一コンシューマーのnav/スナックバーに `MutableSharedFlow<Event>` | 消失しうるデフォルトのイベントストリーム | `Channel(BUFFERED).receiveAsFlow()` を検討 |
| `fun foo() = flow.stateIn(...)` | 呼び出しごとの共有コルーチン | `val` / 共有インスタンスにする |
| `WhileSubscribed` + `.value` が常に最新/初期化済みである必要 | 古い、または初期のデータ | `SharingStarted.Eagerly` または明示的な初期化 |
| stateとして消費される `stateFlow.map { ... }` | `.value` の消失 | `.stateIn(...)` で終端する |
| `_state.value = _state.value.copy(...)` | 非アトミックな読み取り/変更/書き込み | `_state.update { it.copy(...) }` |
| 現在のstateを使わない高コストなオブジェクト生成を `update { ... }` の中で行う | updateがリトライすると処理が繰り返される可能性 | `update` の前に構築し、内部には現在のstateに依存する変換だけを残す |

## レビュー時の危険信号

| 思考 | 実際 |
|---------|---------|
| 「複数のsubscriberがいるから`SharedFlow`が必要」 | 複数subscriberはセマンティクスを変える。`Channel.receiveAsFlow()`はブロードキャストではない。イベントモデルを意図的に選ぶこと。 |
| 「リソース節約のために`WhileSubscribed`を使う」 | 古い/初期の`.value`読み取りが許容できる場合のみ。適用前に検証すること。 |
| 「実データがロードされるまでsentinelを使う」 | コンシューマーはそれを実際のドメインとして扱う。明示的なUI/stateモデリングかフェーズ分けを優先する。 |
| 「都合がいいので`update`の中で新しいオブジェクトを構築する」 | ラムダはリトライされる可能性がある。現在のstateに依存しない限り外で構築する。 |

## 関連

- [Kotlin control flow](../../kotlin-control-flow/SKILL.md) — stateとイベントをモデリングする際の `when`、ガード条件、網羅性、スマートキャスト、早期returnの選択。
- [Structured concurrency](structured-concurrency.md) — スコープの所有権、init内のlaunch、fire-and-forget境界、キャンセル、`runBlocking`
- [Compose state and effects](../../compose-state-and-effects/SKILL.md) — イベントFlowの収集と、純粋なstate駆動のUIへのstate holderの配線
