---
name: kotlin-concurrency-and-flow
description: Kotlinのコルーチンスコープの所有権、init内でのlaunch、非suspend関数からのlaunch API、runBlocking、キャンセル、StateFlow、SharedFlow、Channel、stateIn、SharingStarted、state更新、一度限りのイベントを記述・レビューする際に使用する。
---

# Kotlin のコルーチンとFlow

## 基本原則

非同期処理には明示的な所有者と生存期間を与え、永続的なstateと一時的なイベントは、製品契約に合った配信・リプレイセマンティクスを持つプリミティブでモデリングすること。

## 手順

1. コルーチンの所有者、キャンセル境界、プロデューサー、コンシューマー、永続的なstate、一時的なイベントをそれぞれ特定する。
2. 処理を所有するライフサイクルに合ったスコープを選ぶ。任意のスコープを保持したり、非suspendのAPIの裏に構造化されていないlaunchを隠したりしないこと。
3. レンダリング可能な最新データはstateとしてモデリングし、命令的な一度限りの処理は、消失・再送の挙動が明示的に許容できる場合に限りイベントとしてモデリングする。
4. Flowの共有・バッファリングのセマンティクスは、デフォルト値ではなくプロデューサーとコンシューマーのライフタイムから選ぶ。
5. 該当する関心事について、下記のリファレンスを読み込む。
6. キャンセル・再起動・リプレイ・失敗時の挙動がすべて公開APIから観測可能になり、呼び出し側が処理の所有者を推測する必要がなくなった時点で完了とする。

## トピックルーター

| シグナル | 参照先 |
|---|---|
| 保持された `CoroutineScope`、`init { launch }`、fire-and-forget API、`runBlocking`、広範なcatch、キャンセル境界 | [Structured concurrency](references/structured-concurrency.md) |
| `StateFlow`、`SharedFlow`、`Channel`、`stateIn`、`SharingStarted`、`.value`、state更新、sentinel値、一度限りのイベント | [Flow state and events](references/flow-state-events.md) |
| Composeでの収集やUIエフェクト処理 | [Compose state and effects](../compose-state-and-effects/SKILL.md) |

## RED/GREENエージェントシナリオ

1. RED: サービスに長生きする `CoroutineScope` を保持し、任意の呼び出し元からlaunchしている。GREEN: 所有権とキャンセルが明確に定義されたライフサイクル境界に従うようにする。
2. 新規ケース: 画面がリプレイ可能なローディングstateと、リプレイ不要なナビゲーションを必要とする。GREEN: stateとイベントで別々の契約を使い、配信セマンティクスを明文化する。
3. 反例: すでに呼び出し元がスコープを所有しているsuspend関数。GREEN: APIを非同期に見せかけるためだけに内部スコープを追加しない。
