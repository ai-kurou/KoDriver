# Compose の state hoisting

## 基本原則

stateはロジックが必要とする範囲までしかhoist（引き上げ）しない。単純なUI要素のstateはローカルに留め、共有されるUI要素のstateは最も低い共通の親composableへ移動し、UI専用の挙動が1つの概念になった時点で純粋なstate holderを抽出し、ビジネスロジックやアプリデータが関わる場合は画面のstate holderを使う。画面境界では、state holderの配線と、純粋なstate駆動のUIレンダリングを分離しておく。

## レビュー手順

1. 関係するstate、操作、アプリ依存、イベントストリーム、命令的なエフェクトを列挙する。
2. 下記の判断基準を使い、読み取り・変更を必要とする最小の所有者に各項目を割り当てる。
3. 協調するUI専用の挙動が1つの概念になった場合のみ、純粋なstate holderを抽出する。
4. 画面がアプリの配線とレイアウトを混在させている場合は、小さなstate-holder composableを残しつつ、レンダリングを純粋なstate駆動のcomposableへ移す。
5. immutableなUI stateと明示的なイベントコールバックをその境界を越えて渡す。ビジネスロジックがその値を必要としない限り、UIの仕組みはcomposition側に留める。
6. これらの関心事がより深い扱いを必要とする場合は、焦点を絞ったエフェクト・テスト・フォーカス・遅延読み取りのskillを読み込む。
7. UIがアプリ依存なしにプレビュー・テスト可能になり、ビジネス処理が画面のstate holderに残り、必要以上にstateがhoistされていない状態になったら完了とする。

## 判断ガイド

| 状況 | 所有者 |
|---|---|
| 1つのcomposableだけが単純なstateを読み書きする | `remember` / `rememberSaveable` でローカルに保つ |
| 兄弟または親のcomposableが読み書きする必要がある | stateとイベントを最も低い共通の親composableへhoistする |
| 関連するUI要素のstateとUIロジックによりcomposableが読みにくく、プレビュー・テストしにくくなっている | compositionでrememberされる純粋なstate holderクラスを抽出する |
| リポジトリ呼び出し、永続化、ビジネスルール、画面UI stateの生成が関わる | `ViewModel` やコンポーネントなどの画面レベルのstate holderを使う |
| 画面composableがアプリstate/エフェクトを収集し、かつレイアウトの大半も所有している | 小さな配線用composableを残し、immutableなstateとコールバックを受け取る純粋なUI composableを抽出する |

UI要素のstateには、展開状態・シート表示・スクロール位置・フォーカス・テキストフィールドの編集状態・選択・アニメーション/インタラクションstateなどが含まれる。画面UI stateは、表示のために準備されたアプリデータである。

UI要素のstateがビジネスロジックの入力になる場合、画面のstate holderにも置く必要があるかもしれない。例えば、リポジトリ由来のサジェストをクエリするために使われるテキストは、そのサジェストを生成するstate holderと一緒に置くべきである。

## 純粋なstate holderを抽出するトリガー

以下のうち複数が当てはまる場合に、純粋なstate holderを抽出する。

- 複数の関連する `remember` 値が同じコールバックによって協調している。
- スクロール・フォーカス・テキスト・選択・シートのstateに `clear`、`submit`、`jumpToTop`、`openFilters` といった名前付き操作が必要。
- 派生UIフラグがcomposable内に散在している。
- 子composableが概念的に所有していない仕組みを受け取っている。
- 1つの挙動を確認するために、プレビューやテストが長いUI詳細のシーケンスを駆動しなければならない。
- ヘルパー関数がcomposableを読みやすく保つためだけに多数のstateパラメータを必要とする。

1つのBoolean、1つのテキストフィールド、些細な表示/非表示ロジックのために抽出しないこと。儀式的な分離は関心の分離ではない。

## パターン

UI要素のstateとUIロジックには純粋なクラスを、composition所有のオブジェクトには `remember...State` 関数を使う。

```kotlin
@Stable
class ProductSearchState(
    query: String,
    private val listState: LazyListState,
    private val focusRequester: FocusRequester,
) {
    var query by mutableStateOf(query)
        private set

    var filtersOpen by mutableStateOf(false)
        private set

    val canClear: Boolean
        get() = query.isNotEmpty()

    fun updateQuery(value: String) {
        query = value
    }

    fun clear() {
        query = ""
        focusRequester.requestFocus()
    }

    suspend fun jumpToTop() {
        listState.animateScrollToItem(0)
    }
}

@Composable
fun rememberProductSearchState(
    initialQuery: String = "",
    listState: LazyListState = rememberLazyListState(),
    focusRequester: FocusRequester = remember { FocusRequester() },
): ProductSearchState {
    return remember(listState, focusRequester) {
        ProductSearchState(initialQuery, listState, focusRequester)
    }
}
```

composableはstate holderからレンダリングし、intentスタイルのメソッドを呼ぶ。親が同じUIの挙動を協調させる必要がある場合は、デフォルト値付きのパラメータとしてstate holderを受け取る。

```kotlin
@Composable
fun ProductSearchPanel(
    state: ProductSearchState = rememberProductSearchState(),
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    SearchField(
        query = state.query,
        onQueryChange = state::updateQuery,
        onClear = state::clear,
    )

    JumpToTopButton(onClick = {
        scope.launch { state.jumpToTop() }
    })
}
```

## Compositionの所有権

`remember` で作られた純粋なstate holderはcomposableのライフサイクルに従う。これにより、`LazyListState`、`FocusRequester`、`PagerState`、`DrawerState`、`TextFieldState` といったCompose UIオブジェクトの置き場所として適している。

スクロールやドロワーのアニメーションなど、フレームクロックを必要とするsuspendのUI操作は、composition所有のコルーチン（`rememberCoroutineScope`、`LaunchedEffect`、またはその他のcomposition所有のスコープ）内に留めること。これらの呼び出しを `viewModelScope` へ移してはならない。

## stateの保存

`rememberSaveable` やカスタムの `Saver` は、Activityやプロセスの再作成を生き延びるべき値（クエリ文字列、選択されたフィルターID、現在のタブキーなど）にのみ使うこと。

`LazyListState`、`FocusRequester`、コルーチンスコープ、コールバックなどのランタイムオブジェクトをそのまま保存しようとしないこと。挙動を再構築するために必要な最小限のシリアライズ可能な値を保存する。

## 画面の配線とUIレンダリングの分離

画面が `ViewModel`、コンポーネント、コントローラー、ナビゲーター、リポジトリ、サービスを受け取る場合は、その依存を小さなstate-holder composableに留める。アプリstateとエフェクトはそこで収集し、immutableなUI stateと明示的なイベントコールバックを純粋なUI composableに渡す。

```kotlin
@Composable
fun ProfileScreen(component: ProfileComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onNameChange = component::onNameChange,
        onSaveClick = component::save,
        onBackClick = component::back,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // レイアウトのみ。
}
```

この境界は意図的に使うこと。

| 関心事 | state-holder composable | 純粋なUI composable |
|---|---|---|
| アプリ/ビジネスstateと一度限りのエフェクトを収集する | Yes | No |
| DIされたオブジェクトを保持する | Yes | No |
| immutableなUI stateとイベントコールバックを受け取る | 通常は素通しする | Yes |
| レイアウト・modifier・セマンティクス・テストタグを所有する | No または最小限 | Yes |
| `LazyListState`や`FocusRequester`などのCompose runtimeオブジェクトを所有する | No | Yes（直接、または純粋なUI state holder内） |
| UIの仕組みから派生したビジネス関連の値やintentを受け取る | Yes | ランタイムオブジェクトを公開せずにそれらを供給する |

最小限の有用なUI契約を渡すこと。

- 画面がまとまりのあるstateを持つ場合は、専用のimmutableな `UiState` を優先する。
- state holder全体をツリーに渡すより、明示的なイベントコールバックを優先する。
- ナビゲーションは、ユーザーのintentを記述するコールバックとして扱う。
- 直接使うとビジネスルールがレンダリングに紛れ込む場合は、ドメインモデルをUIモデルへマッピングする。
- layoutやdraw内で読むべきフレームレートの値には、[Compose performance](../../compose-performance/SKILL.md) に従いprovideラムダを渡す。

ナビゲーション・スナックバー・アナリティクス・イベント収集は、発信源と命令的な処理先が揃っているstate holderの近くで処理すること。エフェクト処理が肥大化する場合は、state holderをUI composableに渡すのではなく、小さな兄弟のエフェクトハンドラーを抽出する。エフェクトAPI・キー・クリーンアップ・古いキャプチャについては [Side effects](side-effects.md) を使う。

すべての小さなcomposableにstate-holder/UIの分割を作らないこと。アプリ依存を、プレビュー・テスト・再利用すべき意味のあるUIから取り除ける場合に限り、画面やまとまりのあるセクションの境界で分割する。

既に純粋な値とコールバックを受け取っている一度限りの小さなcomposable、スロットとmodifierを公開すべきデザインシステムのプリミティブ、アプリ依存を分離せず1つのプリミティブを素通しするだけのラッパーには、この分割を適用しないこと。

## RED/GREENエージェントシナリオ

各シナリオでは、該当する規則を省略・元に戻すことでREDを確立し、その後skillを復元してGREENの結果を求めること。

1. 画面がコンポーネントを受け取り、`StateFlow`を収集し、ナビゲーションイベントを処理し、レイアウトの大半を所有している。GREEN: コンポーネント・収集・エフェクト処理を小さな配線用composableに留め、immutableなstateとコールバックを持つ純粋なUI composableを抽出する。
2. 新規ケース: 検索クエリがリポジトリ由来のサジェストを駆動し、`LazyListState` と `FocusRequester` がUIを協調させる。GREEN: クエリとサジェストのロジックは画面のstate holderへ移すが、Compose runtimeオブジェクトは純粋なUIまたは純粋なUI state holderに残す。
3. 過剰適用の反例: ステートレスなデザインシステムのバッジが純粋な値・スロット・modifierを受け取る。GREEN: 構造的な対称性のためだけにstate-holder/UIの分割や `ViewModel` を導入しない。

## よくある間違い

| 間違い | 修正 |
|---|---|
| 「念のため」すべてのローカルstateを親へhoistする | 実際に読み書きする最も低い所有者へhoistする |
| 1つのBooleanのために純粋なstate holderを抽出する | 単純なprivate UI stateはローカルに留める |
| Compose state holderにリポジトリ呼び出しや製品ルールを置く | そのロジックを `ViewModel` やコンポーネントなどの画面state holderへ移す |
| リポジトリ由来の画面stateを駆動するテキストや選択をローカルに留める | その入力をビジネスロジックと共に画面のstate holderへ移す |
| state holderを無関係な子に深く渡す | 子が本当にholderの挙動を協調させる場合を除き、純粋な値とコールバックを渡す |
| holderを画面全体のゴミ捨て場として扱う | 検索入力・シート協調・リストコントロールなど、まとまりのあるUIの挙動で分割する |
| `viewModelScope` からアニメーションのsuspend関数を呼ぶ | composition所有のコルーチンを使う |
| 画面composableがコンポーネントを受け取りレイアウトすべてをレンダリングする | stateとコールバックを受け取る純粋なUIオーバーロードを抽出する |
| 子composableが `ViewModel` やコンポーネントを受け取る | 各子が必要とする値とコールバックのみを渡す |
| UIレンダリングがナビゲーションを行ったりアプリのイベントFlowを収集したりする | 画面のstate holderのそばでエフェクトを処理する |
| すべての小さなcomposableにstate-holderオーバーロードを持たせる | 画面またはまとまりのあるセクション境界でのみ分割する |

## 関連

- [Local state](local-state.md) — 正しいローカルの `remember` と可変stateの記述。
- [Side effects](side-effects.md) — エフェクトAPIとcomposition所有のコルーチン境界の選択。
- [Compose focus navigation](../../compose-focus-navigation/SKILL.md) — フォーカスstate、requester、キーボード/D-padの挙動。
- [Compose UI testing patterns](../../compose-ui-testing-patterns/SKILL.md) — アプリグラフ全体を構築せずに純粋なstate駆動のUIをテストする。
- [Kotlin API design](../../kotlin-api-design/SKILL.md) — プラットフォームサービスを意味的な境界の裏に保ちつつ、共有UIを純粋に保つ。
