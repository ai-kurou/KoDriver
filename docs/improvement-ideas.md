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

## UI/UX

- **対象**: `ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）の `ListDetailPaneScaffold`／画面幅判定まわり
- **課題**: Jetpack Compose 2026年4月リリース（Compose 1.11.0系）で追加された宣言的な `MediaQuery` API（`WindowSizeClass` の手動購読・分岐に代わり、ウィンドウ状態に応じた宣言的なクエリ記述が可能）をまだ利用していない。現状は `rememberListDetailPaneScaffoldNavigator` 等の既存の分岐ロジックで賄っている。
- **改善案**: プロジェクトが依存する Compose Multiplatform / Material3 Adaptive のバージョンで `MediaQuery` API が利用可能になった際、list/detailペインの表示切り替え判定を簡潔化できないか調査する。参考: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
- **調査結果（2026-08-23）**: プロジェクトが依存する `org.jetbrains.compose.ui:ui` `1.11.1` には `MediaQuery` API（`androidx.compose.ui.MediaQueryKt`、`derivedMediaQuery`、`UiMediaScope`、`LocalUiMediaScope`）が `commonMain` として同梱されており、`feature:readout-list` 等でのコンパイル自体は成功する（JVM・Androidいずれのターゲットでも確認済み）。しかし実際に3画面へ導入して `feature:readout-list` のユニットテストを実行したところ、`LocalUiMediaScope` を明示的に提供しなかったテストが軒並み `IllegalStateException`（`CompositionLocal LocalUiMediaScope not present`）で失敗した。原因を調査した結果、`androidx.compose.ui:ui-android`（AARの`classes.jar`）には `LocalUiMediaScope` へ実際の値を供給するプラットフォーム実装（`androidx/compose/ui/adaptive/MediaQuery_androidKt.obtainUiMediaScope` 等）が存在する一方、`org.jetbrains.compose.ui:ui-desktop:1.11.1` の jar にはこの配線が一切含まれていないことを確認した。**つまり `MediaQuery` API は現時点で Android ターゲットのみ実用可能で、KoDriver の主要配布形態である Desktop（Windows MSI）では `LocalUiMediaScope` が誰からも提供されず実行時にクラッシュする。** Compose Multiplatform がDesktopターゲット向けの `UiMediaScope` プロバイダ実装を追加するまでは導入を見送る。

## 開発プロセス

- **対象**: リポジトリの PR テンプレート（現状 `.github/PULL_REQUEST_TEMPLATE.md` は未整備で、PRの説明欄は都度自由記述している）
  **課題**: 3,400件のPR分析に基づく知見として、「これまでの仕様/実装後の仕様」のような対構造のテンプレートは、否定を肯定に変えただけの情報価値の低い記述を誘発しやすいことが指摘されている。KoDriverもCLAUDE.mdでPR説明の書式（タイトル・説明を日本語で、署名を含めない等）は定めているが、本文の構成についてのテンプレートは存在しない。
  **改善案**: PRテンプレートを新設する場合、変更理由（WHY）を最上部に配置する・反転記述（「〜だったが〜にした」のみの記述）を避ける・設計判断は人間側が明記する・リスク情報を早期に可視化する、という4原則を参考に構成を検討する。参考: https://zenn.dev/third_yagami/articles/590216d145007f

## CI/CD

- **対象**: `app/desktopApp/build.gradle.kts` の `windows { }` ブロック(PR #1142)
  **課題**: `shortcut = true` / `menu = true` / `perUserInstall = true` はjpackageの仕様上いずれもサイレントフラグであり、インストール実行時に自動でその挙動が固定されるだけで、ユーザーに選択させるダイアログは表示されない。ショートカット作成可否を選ばせるには別途 `--win-shortcut-prompt` が必要だが、Compose MultiplatformのGradle DSL(`org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask`)には対応するプロパティが存在せず、現状のDSLの範囲では実現できない。インストール範囲(全ユーザー/個人用)を選ばせる標準ダイアログもjpackage自体に用意されていない。
  **改善案**: Compose Multiplatformが `winShortcutPrompt` 等のDSLプロパティを将来追加した場合、または freeform引数差し込み等の代替手段が判明した場合に、MSIインストーラー上でショートカット作成可否をユーザーに選択させる機能の追加を検討する。

