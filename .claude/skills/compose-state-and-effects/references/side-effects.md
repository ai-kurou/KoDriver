# Compose: 副作用（side effects）

## 基本原則

composableのbodyはUIを記述するものであり、recomposeされたり、スキップされたり、破棄されたりしうる。外の世界を変更する処理は、その処理のライフサイクルに合ったエフェクトAPIに置くこと。

## 最小のエフェクトを選ぶ

| 必要なこと | API |
|---|---|
| recomposeが成功するたびに、Compose stateをCompose外のコードへ公開する | `SideEffect` |
| リスナー・コールバック・オブザーバー・リソースの登録/解除 | `DisposableEffect(keys...)` |
| suspendする、遅延する、キー付けされた一度限りの処理を実行する | `LaunchedEffect(keys...)` |
| ユーザーイベントのコールバックからsuspend処理を起動する | `rememberCoroutineScope()` |
| コルーチン内でCompose snapshotの読み取りをFlowに変換する | `LaunchedEffect` 内の `snapshotFlow { ... }` |

## エフェクトのキー

キーは再起動の識別子を定義する。いずれかのキーが変わると、古いエフェクトはキャンセル/破棄され、新しいエフェクトが開始する。

```kotlin
// ✅ userIdが変わったら収集を再起動する
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}

// ❌ Unitが変化する入力を隠してしまい、収集は最初のuserIdを使い続ける
LaunchedEffect(Unit) {
    repository.events(userId).collect { event -> handle(event) }
}
```

安定した意味のあるキーを使うこと。

- エフェクトが従うべきライフサイクルを持つもの: `userId`、`screenId`、`lifecycleOwner`、`focusRequester`。
- 1つのプロパティしか関係しない場合に、広いオブジェクト（`state`、`viewModel`）を使わない。
- 変化するラムダをキーに追加しない。ただしラムダが変わるたびに本当に再起動したい場合を除く。

## 古いキャプチャ（stale capture）を避ける

再起動すべきではないが最新のコールバックや値を必要とする長生きのエフェクトには `rememberUpdatedState` を使う。

```kotlin
@Composable
fun Timeout(onTimeout: () -> Unit) {
    val latestOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        delay(1_000)
        latestOnTimeout()
    }
}
```

これは、ライフサイクルは「一度だけ開始」だが、呼び出されるラムダは常に最新であるべき場合に使う。よくあるケース:

- タイムアウトやスプラッシュのエフェクトは `onTimeout` が変わっても再起動すべきではないが、最新のコールバックを呼ぶべき。
- ライフサイクルオブザーバーは同じownerに登録され続けるべきだが、最新の `onStart` / `onStop` ラムダを呼ぶべき。
- 長生きのコレクターは収集のライフサイクルを維持すべきだが、最新のイベントハンドラーを呼ぶべき。

適切なキーを選ぶことを避けるために `rememberUpdatedState` を使わないこと。変化した値が処理を再起動すべきなら、代わりにそれをキーにする。

```kotlin
// NG: userIdの変化は収集を再起動すべきであり、キャプチャした値を更新するだけでは不十分
val latestUserId by rememberUpdatedState(userId)
LaunchedEffect(Unit) {
    repository.events(latestUserId).collect { event -> handle(event) }
}

// OK: 収集のライフサイクルがuserIdに従う
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}
```

### `remember {}` ブロック内では `rememberUpdatedState` の値は古いまま

`rememberUpdatedState` は、recomposeのたびに `.value` が更新される `State` オブジェクトを返す。「最新」という挙動は、そのStateが**遅延して読み取られる**場合 — エフェクトのbodyや後で実行されるラムダの中 — にのみ機能し、値が即座にキャプチャされる場合には機能しない。

`remember {}` ブロック内ではproducerラムダは一度だけ実行される。そこでdelegateを読み取ると、現在の `.value` がrememberされたオブジェクトにスナップショットされる — 以降のState更新はそこに届かない。

```kotlin
val latestChannelId by rememberUpdatedState(channelId)

// ❌ NG — channelIdはrememberのラムダが実行される時に一度だけ読まれる。
// destinationは初期値を永遠に保持し続ける
val destination = remember {
    Destination(channelId = latestChannelId)
}

// ✅ OK — rememberUpdatedStateを使わず、変化する値でrememberをキー付けする
val destination = remember(channelId) {
    Destination(channelId = channelId)
}

// ✅ これもOK — ラップしたラムダで各呼び出し時に読み取りを遅延させる
val destination = remember {
    Destination(channelId = { latestChannelId })
}
```

同じ罠は、`rememberUpdatedState` のdelegateがラムダやエフェクトのbodyの背後に遅延されず**即座に読み取られる**あらゆる場所で発生する: `remember` 内で構築されるdata class、`DisposableEffect` のセットアップブロックで一度だけ構築されるオブジェクト、作成時に評価される任意の式。

キャプチャした値がrememberされたオブジェクトの再作成を引き起こすべき場合は、それを `remember` のキーにし、`rememberUpdatedState` は使わないこと。`rememberUpdatedState` は、そのスコープを再起動**せずに**、長生きのスコープ（エフェクトのコルーチン、イベントコールバック）内で値を常に最新に保つ必要がある場合のために取っておく。

`rememberUpdatedState` はレンダリングstateを「recomposeしないもの」にするわけでもない。UIが変化する値を表示する必要があるなら、composition内で通常の `State` を読むか、フレームレートの値には [Compose performance](../../compose-performance/SKILL.md) を使うこと。

## Flowの収集

**副作用/イベントFlow**（スナックバー、ナビゲーションイベント、アナリティクスイベント、フォーカスコマンドなど、各emissionが命令的な処理を引き起こすストリーム）には `LaunchedEffect` を使う。

```kotlin
LaunchedEffect(events) {
    events.collect { event ->
        snackbarHostState.showSnackbar(event.message)
    }
}
```

ローカルstateを変更するためだけにレンダリングstateを命令的に収集しないこと。UI stateについては、state holderの近くで収集し、純粋な値をUI composableに渡す — **state holder と UI の分離**、`collectAsStateWithLifecycle()` / `collectAsState()`、プレビューしやすい配線については [State hoisting](state-hoisting.md) で扱う。ここでそのアーキテクチャを重複させないこと。

Android上では、可能であればlifecycle-awareな収集を優先する。lifecycle-aware APIがないターゲットでは `collectAsState()` を使う。

Compose stateの読み取りには `snapshotFlow` を使う。

```kotlin
LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .collect { index -> analytics.visibleIndex(index) }
}
```

終端の `collect` がない `snapshotFlow { ... }.map { ... }` は何も行わない。

## ユーザーイベント

クリックやジェスチャーがsuspend処理を開始する場合は `rememberCoroutineScope()` を使う。

```kotlin
@Composable
fun SaveButton(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                snackbarHostState.showSnackbar("Saved")
            }
        },
    ) {
        Text("Save")
    }
}
```

`LaunchedEffect` を発火させるためだけの「イベントフラグ」stateは避けること。クリック自体が既にイベントである。

## 登録とクリーンアップ

対になったセットアップ/ティアダウンには `DisposableEffect` を使う。

```kotlin
@Composable
fun ObserveLifecycle(owner: LifecycleOwner, observer: LifecycleObserver) {
    DisposableEffect(owner, observer) {
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }
}
```

すべての登録パスには対応する `onDispose` によるクリーンアップパスがあるべき。

## よくある間違い

| 間違い | 診断 | 修正 |
|---|---|---|
| composableのbody内で直接ネットワークリクエストを行う | composition内での副作用 | 通常はViewModel/state holderへ移動。UIが所有するキー付き処理にのみ `LaunchedEffect` を使う |
| composableのbodyからアナリティクスプロパティを書き込む | composition内での副作用 | recomposeが成功するたびに公開すべきなら `SideEffect` を使う |
| composableのbodyからインプレッション/イベントをログ出力する | composition内での副作用 | そのキーで一度だけ実行すべきなら `LaunchedEffect(key)` を使う |
| `LaunchedEffect(Unit)` が変化する `id` をキャプチャしている | キー漏れ | `id` でキー付けするか、再起動すべきでないなら `rememberUpdatedState` を使う |
| `id` 変更後も `LaunchedEffect(Unit)` を動かし続けるために `rememberUpdatedState(id)` を使っている | 隠れたライフサイクルバグ | エフェクトを `id` でキー付けする |
| 長生きのエフェクトがrecomposition後に古いコールバックを呼ぶ | 古いキャプチャ | コールバックを `rememberUpdatedState` でラップし、エフェクト内でラッパーを呼ぶ |
| `remember {}` 内で直接 `rememberUpdatedState` のdelegateを読む（例: `Destination(id = latestId)`） | 値が一度だけキャプチャされ更新されない | 値を `remember` のキーにする: `remember(id) { Destination(id = id) }` |
| `LaunchedEffect(state) { ... }` が頻繁に再起動しすぎる | キーが広すぎる | 特定のプロパティでキー付けする |
| `LaunchedEffect(...) { nonSuspendSetter() }` | エフェクトの種類が間違っている | 通常は `SideEffect`。`LaunchedEffect` はキー付きの一度限り/遅延処理にのみ使う |
| `LaunchedEffect` 内でリスナーを追加してクリーンアップがない | 破棄漏れ | `DisposableEffect` を使う |
| クリックで `shouldShowSnackbar = true` をセットして起動する | イベントフラグのアンチパターン | クリックコールバック内で `rememberCoroutineScope()` を使う |
| composableのbody内で副作用のために `if (isFocused) { … }` やフォーカス読み取りを行う | composition中の副作用 | `LaunchedEffect(focused) { … }` または `snapshotFlow` |
| measureされたcomposableで `onSizeChanged { heightState = it.height }` | 兄弟がcomposition内で`heightState`を読むと、layout→compositionのback-writeになる | 兄弟はcomposition内で `Modifier.height(state.dp)` するのではなく、measureフェーズで高さを消費すべき |

## フォーカスと計測

**フォーカス:** composableのbody内でフォーカスを読み取り**副作用**（プリロード、アナリティクス、トースト）を駆動すると、その処理はcomposition中に実行される。代わりにエフェクト内でフォーカスを監視すること。

```kotlin
// ❌ NG — `focused`がtrueになるたびcomposition中に副作用が実行され、
// 一時的なフォーカス通過も含まれる。`SideEffect`はrecomposeが成功するたびに再実行される
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    if (focused) {
        preloadImages()
    }
}

// ✅ OK — キー付きエフェクト内で副作用を実行する
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(focused) {
        if (focused) preloadImages()
    }
}
```

複数のsnapshot readをサンプリングしたり、すべての派生値でエフェクトをキー付けせずに急な変化をdebounceしたりする必要がある場合は、`LaunchedEffect` 内で `snapshotFlow { … }` を使う。TV/D-padのフォーカスナビゲーションのセマンティクスについては [Compose focus navigation](../../compose-focus-navigation/SKILL.md) を参照。

**計測:** `onSizeChanged` / `onGloballyPositioned` は有効な**コールバック**だが、layoutフェーズで発火する。そこでsnapshot stateに書き込むのは、それより前のフェーズが読まない場合にのみ安全。兄弟がcomposition内でそのstateを読む場合、layoutがcompositionへback-writeしていることになり、measureのたびに兄弟がrecomposeする。キャプチャした寸法は `Modifier.layout` に適用すること（[Compose component design](../../compose-component-design/SKILL.md) と [Compose performance](../../compose-performance/SKILL.md) を参照）。

## レビュー時の危険信号

- composableのbody内のコードについて「これは一度しか実行されない」という思い込み
- パラメータが変化する関数内の `LaunchedEffect(Unit)`
- 終端の収集がないエフェクト内のFlowチェーン
- ライフサイクルをモデリングするためではなくlintを黙らせるために選ばれたキー
- キーも `rememberUpdatedState` もなしに、長生きのエフェクトから使われるコールバックラムダ
- `remember {}` ブロックやオブジェクトのコンストラクタ内で即座に読み取られる `rememberUpdatedState` のdelegate — 値が一度だけキャプチャされ二度と更新されない
