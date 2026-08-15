# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  **課題**: <現状の問題・気になっている点>
  **改善案**: <どう変えたいか>
```

---

## 設計・アーキテクチャ

- **対象**: `ReadoutNavigationState.kt`（`feature:readout-list`）・`OtherNavigationState.kt`（`app:shared`）・`TelemetryLogNavigationState.kt`（`feature:telemetry-log-list`）
  **課題**: list/detailペインの切り替え状態を`NavBackStack<NavKey>`で保持しているが、`clear()`→`add()`による「1要素の置き換え」としてのみ使っており、Navigation3本来の想定（`NavDisplay`によるレンダリング、pushによる複数エントリの積み上げ、戻る操作での自動pop）は利用していない。実際の画面遷移制御はMaterial3 Adaptiveの`rememberListDetailPaneScaffoldNavigator`/`ListDetailPaneScaffoldRole`が担っており、`NavBackStack`はそれと並行して「現在どちらのペインを表示しているか」を表す状態変数として存在するのみ。
  **改善案**: Navigation3のサンプル・公式ドキュメントにあるMaterial3 AdaptiveとNavDisplayの統合パターン（両者で単一のバックスタックを共有する設計）への寄せ替えを検討する。ただし現状の実装（PR #1069, #1075, #1077, #1078）で機能的な不具合は出ていないため、優先度は低め。
  **調査結果（2026-08-14）**: 統合用ライブラリ`org.jetbrains.compose.material3.adaptive:adaptive-navigation3`（AndroidX本家の`ListDetailSceneStrategy`に相当、`rememberListDetailSceneStrategy()`をNavDisplayに渡す構成）はJetBrains公式ドキュメント（https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html）に記載されており存在する。ただし現時点のバージョンは`1.3.0-beta02`で、プロジェクトが依存している`adaptive-layout`/`adaptive-navigation`の安定版`1.2.0`系とは異なるベータ系列。CLAUDE.mdの「致命的なバグや互換性問題がない限り最新安定版を使用する」方針とも相性が悪いため、この統合ライブラリが安定版としてリリースされてから改めて移行を検討する。

## パフォーマンス

- **対象**: `app/androidApp`（Android アプリ）
  **課題**: Baseline Profile（`androidx.benchmark.baseline-profile` プラグイン + Macrobenchmark）が未導入で、ART の AOT コンパイル対象を事前指定できていない。導入により初回起動時間の短縮が見込める。
  **改善案**: `app/androidApp` 向けに Macrobenchmark モジュールを追加し、起動〜読み上げ一覧表示までのクリティカルユーザージャーニーをプロファイル対象として Baseline Profile（`baseline-prof.txt`）を生成・同梱する。
  参考: https://developer.android.com/topic/performance/baselineprofiles/overview

## CI/CD

- **対象**: `.github/workflows/on-pull-request.yml` の `android-screenshot-test-verify` ジョブ（実体は `./gradlew verifyRoborazziAndroidHostTests`）
  **課題**: 他ジョブ（ktlint 約3分、unit-test 約10分、detekt 約6分など）に比べて所要時間が突出して長い（約22〜23分、例: run 31778741798, 31809382366）。
  **調査結果（2026-08-15）**:
  - CI実行（run 31883058239）のステップ別タイミングを確認したところ、`checkout`（3秒）・`setup-screenshot`（26秒）に対し `Verify screenshot tests` ステップだけで10分44秒を占めており、ジョブ全体（約11分19秒）のほぼ全てがこの1ステップに集中していた。
  - `androidHostTest` は Roborazzi + Robolectric による **JVM上でのホスト側テスト**であり、実機/仮想デバイスのエミュレータは一切登場しない（`reactivecircus/android-emulator-runner` 等は未使用）。「エミュレータ起動が時間を占めている」という当初の推測は誤りだった。
  - ローカルで `./gradlew verifyRoborazziAndroidHostTests --profile` を実行し `build/reports/profile/*.html` のタスク別内訳を確認したところ、`app:shared:testAndroidHostTest` 単体で **2分56秒**（ビルド全体3分6秒の大部分）を占めており、突出したボトルネックだった。他の6モジュール（`feature:other-list` 等、実際に `captureRoboImage` を呼ぶスクリーンショットテストを持つモジュール）は22〜27秒程度で、並列実行によりこちらは全体時間にほぼ影響していなかった。
  - `app:shared` の `androidHostTest` にはテストファイル5個・`@Test` 17個（`AppThemeAndroidTest`・`AppThemeModeAndroidTest`・`OtherContentScreenshotTest` 等）があり、1テストあたり平均10秒程度かかっている。Gradleの `Test` タスクはデフォルトで `maxParallelForks=1` のため、同一モジュール内のテストは単一JVM上で直列実行される。
  **改善案**: `app:shared` の `androidHostTest` を細分化する（テスト対象を複数モジュールに分割する）か、各テスト（特にテーマ・サイズの組み合わせが多いもの）のレンダリング回数・Robolectricの初期化コストをさらに深掘りして削減できないか検討する。
  **試行結果（2026-08-15、効果なし）**: ルート `build.gradle.kts` の `subprojects { tasks.withType<Test>() }` に `maxParallelForks = (availableProcessors / 2).coerceAtLeast(1)` を追加してローカルで再計測したところ、`app:shared:testAndroidHostTest` は2分56秒→4分2秒、他の6モジュールも22〜27秒→30〜39秒と、全体的にむしろ悪化した。`org.gradle.parallel=true` によるモジュール単位の並列実行と `maxParallelForks` によるテストクラス単位の並列forkが同時に働き、ローカル環境（10コア）でCPUを奪い合ったことが原因と考えられる。この設定変更は採用しなかった（コミットせず破棄）。次に試す場合は、`org.gradle.workers.max` との兼ね合いやCIランナーの実コア数を踏まえて再検討すること。
  **追加調査（2026-08-15）**: `./gradlew :app:shared:testAndroidHostTest` を単体実行すると37秒（JUnit XMLレポート上の実テスト実行時間の合計は約9.8秒）で完了し、`verifyRoborazziAndroidHostTests`（全13モジュール同時実行）内で計測した2分56秒とは大きな差があった。これは前述の`maxParallelForks`実験と整合しており、**ボトルネックの実体は`app:shared`のテスト自体の遅さではなく、13モジュールが同時にビルド・テストされる際のCPU競合（`org.gradle.parallel=true`によるタスク並列実行）だった可能性が高い**。
  JUnit XMLのテストケース別タイムを見ると、`AppThemeAndroidTest`（`@Config(sdk=[30])`と`[36]`が同一クラス内で混在）はSDK切替直後のテストだけ突出して遅い（例: 4.274秒・1.606秒 vs 同一SDK内の他テストは0.03〜0.05秒）。クラス全体6.258秒のうち約5.9秒（94%）がSDK切替コストで占められていた。同様に`OtherContentScreenshotTest`（`GraphicsMode.NATIVE`使用）も最初の1テストのみ2.123秒、残り2テストは各0.009秒と、ネイティブグラフィック初期化の一度きりのコストが支配的だった。
  いずれも**1回限りの初期化コスト**であり、テストケース数の多さそのものが遅さの原因ではない。この結果は「`app:shared`をモジュール分割する」という改善案の前提と矛盾する。モジュールを分割すると分割後の各モジュールで初期化コストが再発生し、`maxParallelForks`実験と同様に悪化する可能性が高い。分割ではなく、CI環境（`ubuntu-latest`）での実コア数・並列度を踏まえた上で、Gradleタスクの並列度そのもの（`org.gradle.workers.max`等）を調整する方向で再検討すべき。
  **3方向の再検証（2026-08-15、いずれも有効な打ち手なし）**:
  - **CIの実コア数を確認**: CI実行（run 31883058239）のログから `Runner Image`（`ubuntu24`）を確認したところ、GitHub Actions `ubuntu-latest` は標準4vCPUであり、ローカル検証環境（10コア）より大幅に少ない。ローカルで`maxParallelForks`が悪化した（コア競合）ことを踏まえると、コア数がさらに少ないCIでは並列度を上げる方向の改善は成功する見込みが薄い。
  - **リモートキャッシュのヒット率を確認**: 同ジョブのログで `gradle/actions/setup-gradle` によるキャッシュ復元がGradle依存関係・ビルドキャッシュ・toolchainsすべて `Cache hit` になっていることを確認した。既にキャッシュは有効に機能しており、ここに改善余地はない。
  - **SDK切替コストの削減を試行**: `AppThemeAndroidTest`内の`@Config(sdk=[30])`/`[36]`のテストメソッドをsdk値ごとに連続するよう並び替えて実測したが、クラス全体の所要時間は6.258秒→6.435秒とほぼ変化しなかった（誤差の範囲）。原因を調べたところ、JUnitのテスト実行順序はソースの宣言順ではなくハッシュベースの順序で決まっており、ソース上の並び替えは実行順序を制御できないことが判明した。さらに、コストは「切替回数」ではなく「そのJVMプロセス内で特定のsdk値を初めて使用する瞬間」に発生する固定コストであるため、`AppThemeAndroidTest`がsdk=30とsdk=36の両方を検証する仕様である以上、両方の初回コスト（合計約6秒）は順序に関わらず必ず発生する。この変更はコミットせず破棄した。
  以上により、これまで検討した打ち手（モジュール分割・並列フォーク追加・並列度調整・キャッシュ改善・SDK切替順序の最適化）はいずれも有効な改善につながらないことを確認した。次に検討する場合は、テスト内容そのものの削減（SDK=31境界のテストを本当に両方のSDKで検証する必要があるか等、テストカバレッジとのトレードオフを伴う）や、CI側のジョブ構成変更（`android-screenshot-test-verify`を待たずに他ジョブの完了を先に報告する等、体感時間の短縮）といった、より踏み込んだ検討が必要。

