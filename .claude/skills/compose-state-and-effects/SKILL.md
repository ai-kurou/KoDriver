---
name: compose-state-and-effects
description: Jetpack Composeの状態所有権、remember state、state hoisting、画面のstate holder、LaunchedEffect、DisposableEffect、SideEffect、Flow収集、ナビゲーション、スナックバー、アナリティクス、フォーカス要求を記述・レビューする際に使用する。
---

# Compose の状態とエフェクト

## 基本原則

すべてのUI stateに「最も責務の小さい所有者」を1つ与え、その所有者のライフサイクルに従うエフェクトを通じて命令的な処理を実行すること。Compositionはレンダリングを担い、stateとエフェクトはレンダリングを安全に変化させる。

## 手順

1. 対象の画面・コンポーネントにおける、可変UI state・アプリstate・イベントストリーム・アプリ依存・命令的処理を洗い出す。
2. 各stateを、必要とする最小限の所有者（ローカルUI state、hoistされたstate、単純なUI state holder、または画面のstate holder）に配置する。
3. アプリの配線（wiring）とビジネスstateは画面境界に留め、プレビュー可能なレンダリングには純粋なUI stateと明示的なコールバックのみを渡す。
4. 実行される処理のライフサイクルに合ったエフェクトAPIを選び、そのエフェクトを再起動・破棄すべき意味的な入力でキー付けする。
5. 下記の各関心事について、該当するリファレンスのみを読み込む。トピックが近いというだけでリファレンスを読まないこと。
6. フレームレートの読み取り、フェーズをまたぐback-writing、`@ReadOnlyComposable`の契約は [Compose performance](../compose-performance/SKILL.md) に委ねる。
7. すべてのstateが単一の所有者を持ち、すべてのエフェクトが妥当なライフサイクルとキーを持ち、UIがアプリ依存なしにプレビュー・テスト可能になった時点で完了とする。

## トピックルーター

| シグナル | 参照先 |
|---|---|
| 素の `var`、`remember { mutableStateOf(...) }`、state list/map、stateのリセット | [Local state](references/local-state.md) |
| 兄弟間で共有されるstate、UI state holder、ViewModel/コンポーネントの配線、プレビュー可能な画面境界 | [State hoisting](references/state-hoisting.md) |
| `LaunchedEffect`、`DisposableEffect`、`SideEffect`、`snapshotFlow`、`rememberCoroutineScope`、`rememberUpdatedState`、`produceState`、命令的な `requestFocus`、コールバック、イベントFlowの収集、スナックバー、ナビゲーション、アナリティクス | [Side effects](references/side-effects.md) |
| フォーカスの所有権とキーボード/TV/D-padの挙動 | [Compose focus navigation](../compose-focus-navigation/SKILL.md) |
| 結果として得られるUI契約のテストやプレビュー | [Compose UI testing patterns](../compose-ui-testing-patterns/SKILL.md) |

## RED/GREENエージェントシナリオ

1. RED: コンポーネント、収集した `StateFlow`、ナビゲーションイベント、画面レイアウトを1つのcomposableにまとめてしまっている。GREEN: 配線とエフェクトを画面境界に残し、純粋なレンダリングにはimmutableなstateとコールバックのみを渡す。
2. 新規ケース: クエリがリポジトリのサジェストを駆動しつつ、list stateとfocus requesterがUIの挙動を協調させる。GREEN: クエリとサジェストは画面のstate holderに置くが、Compose runtimeオブジェクトは純粋なUI state側に保つ。
3. 反例: 一度限りの展開バッジは1つのprivate Booleanで十分。GREEN: state holderやエフェクトを導入せず、ローカルに留める。
