# テスト方針

**実装コードを書いたら、同時にユニットテストを書くこと。** テストは完了報告前に書くのではなく、実装と並行して書く。

ユニットテストを書ける実装コードを変更・追加した場合は、**変更したモジュールのカバレッジが 100% になるようにすること**。ただし、以下のコードはテスト対象から除外してよい。

- Fake / Stub / Spy などのテストダブル
- Koin などの DI Module
- 単純な Preview・サンプルデータ・定数定義
- プラットフォーム固有の外部 API（JNA, UDP ソケット等）を直接呼び出すためモックが現実的でない箇所

## テストの配置先（commonTest / jvmTest）

`expect` / `actual` を使わない ViewModel・UiState・純粋ロジックのテストは、原則 `jvmTest` に置くこと。`commonTest` は js / wasmJs / android を含む全ターゲットでコンパイル・実行されるため、モック等の JVM/Android 専用ライブラリを使えない。このプロジェクトの配布対象は実質 JVM（デスクトップ）と Android のみで、js / wasmJs（`:app:webApp`）はビルド設定のみのため、`commonTest` に置く意味があるのは実際にマルチプラットフォームで分岐する実装（`expect` / `actual` を持つコードなど）をテストする場合に限る。

## スクリーンショットテストの配置先

スクリーンショットテストは、原則として Desktop/JVM 向けの `src/jvmTest` のみに実装すること。Android と Desktop で見た目・レイアウト・表示内容が異なる場合に限り、差分を確認するための Android 向けスクリーンショットテストを `src/androidHostTest` に追加する。

スクリーンショットテストの画面サイズは、目的別にできるだけ以下へ揃えること。

- listPane などの一覧単体: `360.dp x 1080.dp`
- detailPane などの詳細単体: `1560.dp x 1080.dp`
- list/detail などアプリ全体・2ペイン構成: `720.dp x 640.dp` または `840.dp x 640.dp`
- ダイアログ表示用のホスト: `480.dp x 320.dp`
- デスクトップ Splash など独立したウィンドウ: 既存の専用サイズ

新規追加・移動したスクリーンショットテストのゴールデン画像は、手元で生成してコミットしてはならない。ゴールデン画像の追加・更新は CI（`on-pull-request.yml` の verify → 失敗時の自動再記録）で行われる。動作確認などで手元に `**/snapshots/*.png` が生成・更新された場合は、PR 作成や報告の前に必ず破棄すること。Android 向けスクリーンショットテストを追加する場合は、PR 説明に Desktop/JVM 版と見た目が異なる理由を書くこと。

## テストパターン

- テスト名は日本語のバッククォート記法（`` `初期状態は Connecting を返す`() ``）
- ViewModel の `uiState` から流れてきた内容を検証するときは `first()` を使う
- テストケース数は最小限に絞ること。正常系・異常系・境界値の 3 軸を意識し、冗長なケースは省く
- モックはテストクラスのプロパティとして `@MockK lateinit var` で宣言し、`setUp()`（`@BeforeTest` 関数）の `MockKAnnotations.init(this)` で初期化する。テストケース内やプロパティ初期化時に `mockk()` で生成しない。
- `every`/`coEvery` によるスタブ設定は **各テストケース内で行うこと**。`setUp()` でスタブまで済ませると、そのテストケースが何を前提にしているかがテスト本体だけを読んでも分からなくなり、他のテストケースの前提を変更した際に気づかず壊す原因になる。
- `verify`/`coVerify` では `exactly = N` を必ず指定し、期待する呼び出し回数を明示する。
- `verify`/`coVerify` を使用した各テストケースの最後で、検証対象のモックに対して `confirmVerified(...)` を呼び、検証していない呼び出しが残っていないことを確認する。
- MockK API は import して短い名前で呼び出し、テストコード内に `io.mockk.` の完全修飾名を書かない。
- 通常の `@MockK` / `@RelaxedMockK` は各テストの `MockKAnnotations.init(this)` で再初期化するため、`unmockkAll()` や `clearAllMocks()` を追加しない。
- `mockkObject` / `mockkStatic` / `mockkConstructor` でグローバルな差し替えを行う場合に限り、`finally` または `@AfterTest` で対応する `unmockkObject` / `unmockkStatic` / `unmockkConstructor` を必ず呼ぶ。対象を限定せず全グローバルモックを解除する `unmockkAll()` は原則として使わない。

### mockk テストでの any() 使用

mockk の `every`/`coEvery`/`verify`/`coVerify` では、**`any()` でないとテストコードが書けない場合を除き `any()` を使わないこと**。引数の実値を検証できず、意図しない値でもテストが通ってしまうため。

- 引数が固定値（`ReadoutItemKey`・`Simulator.id` など）なら、その具体値を直接指定する。
- `verify`/`coVerify` で引数の中身を確認したい場合は `withArg<T> { assert(...) }` を使う（`server/src/test/kotlin/kurou/kodriver/KoDriverServiceAdvertiserTest.kt` を参照）。
- 呼び出しごとに値が変わり検証が現実的でない場合（例: `saveTelemetryLog` の `createdAt` など）に限り `any()` を残してよい。

```kotlin
// NG: 具体値がわかっているのに any()
verify { jmdns.registerService(any()) }

// OK: withArg で実値を検証
verify {
    jmdns.registerService(
        withArg<ServiceInfo> {
            assert(it.name == "my-pc")
            assert(it.port == 8080)
        },
    )
}
```

## カバレッジ

Kover でカバレッジを計測する。新しいモジュールを追加した場合、ルートの `build.gradle.kts` の `kover { }` ブロックに `kover(project(":module:name"))` を追加しないとカバレッジ集計から除外される。

```bash
# ローカルでカバレッジレポート生成
./gradlew koverXmlReport
```
