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

## DI（Koin）

- **対象**: `core/data/src/jvmMain/kotlin/kurou/kodriver/data/DesktopDataModule.kt`
  **課題**: `ServerVersionRepository`（`core/data/src/androidMain/kotlin/.../release/HttpServerVersionRepository.kt`）・`ServerIpPreferencesRepository`（`core/data/src/androidMain/kotlin/.../preferences/AndroidServerIpPreferencesRepository.kt`）は `AndroidDataModule.kt`（195〜202行目）にのみ `single<...> { ... }` バインディングがあり、`DesktopDataModule.kt` には対応するバインディングが存在しない（`core/data` に `jvmMain`/`androidMain` 共通の `commonMain` ソースセットもない）。一方これらを利用する `feature:server-connection`（`ServerConnectionModule.kt`）・`feature:other-server-ip-detail`（`OtherServerIpDetailModule.kt`）はいずれも `app/shared/.../FeatureModules.kt` の `featureModules` に無条件で含まれており、Android/Desktop どちらのコンポジションルートからも読み込まれる。
  **改善案**: Desktop版でこれら2画面がどう扱われているか（実際に到達不能で問題が顕在化していないのか、既にDesktop向け実装が別途存在するのか）を確認したうえで、必要であれば `DesktopDataModule.kt` に対応する `single { }` バインディングを追加する。

## エラーハンドリング・ログ

- **対象**: `Gt7Ps5NarratorEventProcessor.process`（`feature:gt7-ps5-narrator`）の `saveTelemetryLog` 呼び出し
  **課題**: `LmuWindowsNarratorEventProcessor.saveTelemetryLogSafely`（`feature:lmu-windows-narrator`）・`AceWindowsNarratorEventProcessor.saveTelemetryLogSafely`（`feature:ace-windows-narrator`）は `saveTelemetryLog` の呼び出しを `try-catch` で囲み、`CancellationException` のみ再スローして他の例外（DB書き込み失敗等）は握りつぶし「ログ保存は読み上げの補助機能のため、保存失敗で以後の読み上げを止めない」というコメントを添えている。一方 GT7 版の `Gt7Ps5NarratorEventProcessor.process`（`Gt7Ps5NarratorEventProcessor.kt:55`）は `saveTelemetryLog` を素で呼んでおり、`TelemetryLogRepositoryImpl.saveTelemetryLog`（`core:data`）内の Room `dao.insert` が例外を投げた場合（ディスク容量不足等）にそのまま呼び出し元へ伝播し、GT7 の読み上げ処理自体を止めてしまう恐れがある。3機種で本来同じであるべき「ログ保存失敗時の扱い」が実装ごとに異なっている。
  **改善案**: GT7版にも LMU/ACE 同様の `saveTelemetryLogSafely`（`CancellationException` のみ再スロー、それ以外は握りつぶし）を導入し、3機種で挙動を揃える。

## CI/CD

- **対象**: `app/desktopApp/build.gradle.kts` の `windows { }` ブロック(PR #1142)
  **課題**: `shortcut = true` / `menu = true` / `perUserInstall = true` はjpackageの仕様上いずれもサイレントフラグであり、インストール実行時に自動でその挙動が固定されるだけで、ユーザーに選択させるダイアログは表示されない。ショートカット作成可否を選ばせるには別途 `--win-shortcut-prompt` が必要だが、Compose MultiplatformのGradle DSL(`org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask`)には対応するプロパティが存在せず、現状のDSLの範囲では実現できない。インストール範囲(全ユーザー/個人用)を選ばせる標準ダイアログもjpackage自体に用意されていない。
  **改善案**: Compose Multiplatformが `winShortcutPrompt` 等のDSLプロパティを将来追加した場合、または freeform引数差し込み等の代替手段が判明した場合に、MSIインストーラー上でショートカット作成可否をユーザーに選択させる機能の追加を検討する。

## UI/UX

- **対象**: `core/designsystem/src/commonMain/kotlin/kurou/kodriver/core/designsystem/DetailPaneCard.kt`
  **課題**: `titleAlpha`・`dividerAlpha`・`bottomContentAlpha`（42〜44行目）は `checked` に応じて `1f` / `DISABLED_CONTENT_ALPHA`（0.38f）を即座に切り替え、`.alpha(...)`（100〜118行目）でそのまま適用しているため、`Switch` のON/OFF操作時にカード全体の不透明度が瞬時に切り替わりアニメーションがない。このコンポーネントは各detail画面（タイヤ摩耗・車両故障・車両接近など）から共通利用されているため、影響範囲が広い。
  **改善案**: `animateFloatAsState` で `titleAlpha` 等をアニメーション化し、有効/無効切り替え時に滑らかに減光させる。

- **対象**: `feature/debug-state-detail/src/commonMain/kotlin/kurou/kodriver/feature/debugstatedetail/DebugStateYellowFlagStateCard.kt`（`YellowFlagStateContent`）・`DebugStateGamePhaseCard.kt`（`GamePhaseContent`）
  **課題**: いずれもテレメトリ由来のenum状態（`SessionYellowFlagState`・`SessionPhase`）をもとに `Text` の表示内容を直接切り替えており、`Crossfade`/`AnimatedContent` を使っていないため、走行中に状態が変化した際にテキストが瞬時に切り替わる。状態変化を監視するためのデバッグ画面という性質上、変化の視認性が低い。
  **改善案**: `Crossfade` または `AnimatedContent` で状態変化時にフェード等の遷移を付与する。

