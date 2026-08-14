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

- **対象**: `.github/workflows/on-pull-request.yml` の `android-screenshot-test-verify` ジョブ
  **課題**: 他ジョブ（ktlint 約3分、unit-test 約10分、detekt 約6分など）に比べて所要時間が突出して長い（約22〜23分、例: run 31778741798, 31809382366）。エミュレータ起動とRoborazzi比較のどちらが時間を占めているか内訳が不明で、PR全体の待ち時間のボトルネックになっている可能性がある。
  **改善案**: ジョブ内のステップ別所要時間を計測し、エミュレータ起動のキャッシュ・スナップショット活用等で短縮できないか検討する。

- **対象**: `.github/workflows/_e2e-android-maestro.yml` の `maestro-test` ジョブ
  **課題**: 複数の異なるPR・日付（例: run 31066805808, 31611469221, 31725790690, 31771247476）で `actions/checkout` ステップが30〜40秒程度の短時間で失敗するflaky挙動が繰り返し観測されている。同時刻の他ジョブのcheckoutは成功しており、コード側の問題ではなく一過性のインフラ要因と見られる。
  **改善案**: `actions/checkout` ステップへのリトライ設定追加、またはジョブ全体への自動リトライ（例: `nick-fields/retry` 等のaction）導入を検討し、再実行の手間を減らす。

