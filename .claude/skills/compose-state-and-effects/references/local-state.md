# Compose stateの記述

すべての `remember { … }` がここに該当するわけではない。このリファレンスは**ローカルUI state**（`remember { mutableStateOf(…) }`、`mutableStateListOf` / `mutableStateMapOf`）を扱う。その他のremember系APIは別のリファレンスで扱う。

- **`rememberCoroutineScope` / `rememberUpdatedState`** → [Side effects](side-effects.md)
- **フレームレート読み取りに使う `rememberLazyListState` / `rememberScrollState`** → [Compose performance](../../compose-performance/SKILL.md)
- **フォーカスナビゲーション、フォーカスstate、`FocusRequester`の所有権・挙動** → [Compose focus navigation](../../compose-focus-navigation/SKILL.md)

## 基本原則

`@Composable` はランタイムが入力の変化ごとに再実行する関数である。ローカルstateを正しく書くには1つの問いに答えればよい。

1. **可変なローカルstate** — この `var` はrecompositionを生き延び、かつrecompositionを引き起こすか？そうでなければ、recomposeのたびに黙って初期化され、書き込みも見えなくなる。

これを誤ると、stateが消えたり書き込みが反映されなくなったりする。

## このskillを使う場面

Composeのコードを書く・レビューする際に、以下のいずれかを見かけたとき。

- `@Composable fun` 内や任意のcomposableラムダ（`Column { var x = … }`）内の `var x = …`
- 回転・テーマ変更・recompositionのたびに表示上のstateが謎にリセットされるcomposable

## 1. composable内の `var` はStateに裏打ちされている必要がある

recompositionはcomposableをトップから再実行する。ローカルな `var` はパスごとに*再初期化*される — 直前のrecomposeの値は失われ、書き込んでもランタイムに再合成を伝えない。

```kotlin
// ❌ NG — カウンターがrecomposeのたびにリセットされ、クリックがUIに反映されない
@Composable
fun Counter() {
    var count = 0
    Button(onClick = { count++ }) { Text("$count") }
}

// ❌ これもNG — composableのコンテンツラムダ内でも同じ規則が適用される
@Composable
fun Wrapper() {
    Row {
        var count = 0         // Rowのcontentラムダも@Composableである
        // …
    }
}
```

```kotlin
// ✅ OK — `remember`がrecompositionを生き延び、`mutableStateOf`が再合成を引き起こす
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("$count") }
}
```

重要な要素は2つ。

- `remember { … }` — *recompositionを生き延びる*。これがなければ値は毎回作り直される。
- `mutableStateOf(…)` — *recompositionを引き起こす*。これがなければ変更はランタイムから見えない。

コレクションには `mutableStateListOf` / `mutableStateMapOf`（これらも`remember`される）を優先する。これらは読み取りのたびにSnapshot readを、変更のたびにSnapshot writeを発行する。`remember { mutableStateOf(mutableListOf<X>()) }` に続けて `list.add(x)` してもrecomposeは*起きない*。`MutableList.add` はStateのsetterを経由しないためである — 値そのものを置き換える必要がある（`state = state + x`）。

### composition中のsnapshot stateへのback-writing

**Back-writing** とは、以前の（または現在の）フェーズの無効化を引き起こすフェーズでobservable stateを書き込むことを指す。composableのbody内で `mutableState*` を変更すると、同じcompositionパスへback-writeされ、別のパスがスケジュールされる。派生データをこの方法で再構築してはならない。

```kotlin
// ❌ NG — composeのたびにclear + putAll
val merged = remember { mutableStateMapOf<Key, ViewState>() }
merged.clear()
merged.putAll(parent)
merged.putAll(overlay)

// ✅ OK — 入力からimmutableなsnapshotをrememberする
val merged = remember(parent, overlay) {
    if (overlay.isEmpty()) parent else parent + overlay
}
```

現在の入力に対して結果が読み取り専用であれば `remember(keys) { … }` で十分。行をまたぐ計測やmeasureフェーズでの修正については [Compose performance](../../compose-performance/SKILL.md) を参照。

### このルールが適用されない場合

- **`remember { … }` のproducerブロック内。** これはキーが変わるたびに一度だけ実行され、recomposeのたびには実行されない。ここでのローカル `var` は問題ない: `val builder = remember { mutableListOf<X>().apply { var n = 0; … } }`。
- **composableから*外に*渡される非`@Composable`ラムダ内。** `onClick = { var a = 0; … }` は単なる `() -> Unit`。ここでのローカル変数は通常のKotlinと同じ。
- **通常の（非`@Composable`な）ヘルパー関数内。** composableスコープのみが対象。

## 関連

composableに `LaunchedEffect`、`DisposableEffect`、`SideEffect`、`rememberCoroutineScope`、`rememberUpdatedState`、`snapshotFlow`、スナックバー/ナビゲーション処理、アナリティクス、Flow収集が必要な場合は [Side effects](side-effects.md) を使うこと。

フォーカスは問いによって分かれる: **ナビゲーション、フォーカスstate、`FocusRequester`の所有権・挙動** → [Compose focus navigation](../../compose-focus-navigation/SKILL.md)、命令的な `requestFocus` を呼ぶ**タイミング**（エフェクトのタイミング、ライフサイクル、キー、API選択） → [Side effects](side-effects.md)。

このskillはCompose stateを正しく記述することが目的である。`rememberUpdatedState` はエフェクトのキャプチャstateであり、`remember { mutableStateOf(...) }` の一般的な代替ではない。副作用には別のライフサイクル・キー付け規則があり、それを1つの焦点を絞ったskillにまとめることで、情報源が2つに分かれることを避けている。

## クイックリファレンス

| 症状 | 診断 | 修正 |
|---|---|---|
| `@Composable fun` のbody内の `var x = …` | recomposition-safeでない（§1） | `var x by remember { mutableStateOf(…) }` |
| `Column { … }` / `Row { … }` のcontentラムダ内の `var x = …` | 同上 — contentラムダも `@Composable`（§1） | 同じ修正 |
| `remember { mutableStateOf(list) }` の後 `.add(x)` してもrecomposeしない | 変更がStateのsetterを迂回している | `mutableStateListOf` を使うか、値を置き換える: `state = state + x` |
| composableのbody内の `stateMap.clear(); stateMap.putAll(...)` | composition → compositionへのback-writing | `remember(keys) { derivedSnapshot }` |

## 適用しない場合

- **`composeTestRule.setContent { … }` を使うテスト** も同じ規則に従う — これらは本番相当のcomposableである。
- **`produceState`** はコルーチン内で動く独自のproducerブロックを持つため、内部で `LaunchedEffect` は不要。
- **`derivedStateOf`** は安定性・等価性に関する独自の関心事があり、ここでは対象外。state記述ではなくrecompositionの*抑制*が目的。

## レビュー時の危険信号

| 思考 | 実際 |
|---|---|
| 「小さいcomposableだから素の`var`でいい」 | recompositionはいつでも発生しうる。リセットは設計上非決定的であり、後日バグ報告が来る。 |
| 「いつも知っている`LaunchedEffect`を使う」 | [Side effects](side-effects.md) を使うこと。エフェクトAPIの選択はライフサイクルとキーに依存する。 |
| 「rememberしたリストに`.add()`すればいい」 | `mutableStateOf(List)` は内部の変更を観測しない — `mutableStateListOf` を使うか値を置き換える。 |
