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

- **対象**: `AceWindowsReadoutTyreTemperatureDetailPane`・`Gt7Ps5ReadoutTyreTemperatureDetailPane`・`LmuWindowsReadoutTyreTemperatureDetailPane`（各featureモジュールの `HIGH_THRESHOLD_MIN` / `HIGH_THRESHOLD_MAX`）
  **課題**: PR #1158（Sourcery指摘）で判明。高温しきい値スライダーの範囲定数（`90f`〜`110f`）が3つのdetail画面それぞれに `private const val` として重複定義されている。デフォルト値自体は既に `core:domain` の `*Defaults.kt` に集約済みだが、スライダーの上下限は各UI層に個別定義されたまま。
  **改善案**: スライダーの上下限もドメイン層の仕様値（`XXX_DEFAULT` と同様の命名規則）として `core:domain` に集約し、シミュレーター間で一貫させる。ただし3機能は意図的に独立実装を維持する方針（本ファイル「NarratorViewModelは共通化しない」）もあるため、UIコンポーネント自体を共通化するのではなく定数の参照元だけを揃える方向で検討する。

## UI/UX

- **対象**: `ReadoutContent.kt`（`feature:readout-list`）・`OtherContent.kt`（`app:shared`）・`TelemetryLogContent.kt`（`feature:telemetry-log-list`）の `ListDetailPaneScaffold`／画面幅判定まわり
- **課題**: Jetpack Compose 2026年4月リリース（Compose 1.11.0系）で追加された宣言的な `MediaQuery` API（`WindowSizeClass` の手動購読・分岐に代わり、ウィンドウ状態に応じた宣言的なクエリ記述が可能）をまだ利用していない。現状は `rememberListDetailPaneScaffoldNavigator` 等の既存の分岐ロジックで賄っている。
- **改善案**: プロジェクトが依存する Compose Multiplatform / Material3 Adaptive のバージョンで `MediaQuery` API が利用可能になった際、list/detailペインの表示切り替え判定を簡潔化できないか調査する。参考: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html

- **対象**: `feature/*-readout-*-detail` 配下の各 `strings.xml`（しきい値・設定値のリセットボタン文言）
  **課題**: デフォルト値に戻すボタンの文言が「デフォルト値にリセット」（`ace-windows-readout-remaining-fuel-detail`, `gt7-ps5-readout-remaining-fuel-detail`, `gt7-ps5-readout-remaining-fuel-laps-detail`, `lmu-windows-readout-pit-timing-detail`, `lmu-windows-readout-vehicle-approach-detail`）と「デフォルトに戻す」（`ace-windows-readout-tyre-temperature-detail`, `gt7-ps5-readout-tyre-temperature-detail`, `lmu-windows-readout-tyre-temperature-detail`, `lmu-windows-readout-tyre-wear-detail`, `lmu-windows-readout-remaining-virtual-energy-detail`）の2系統に分裂しており、燃料系・タイヤ系といった項目の種類に関係なく画面ごとにばらついている。
  **改善案**: いずれか一方の表現（例:「デフォルトに戻す」）に統一する。

## CI/CD

- **対象**: `app/desktopApp/build.gradle.kts` の `windows { }` ブロック(PR #1142)
  **課題**: `shortcut = true` / `menu = true` / `perUserInstall = true` はjpackageの仕様上いずれもサイレントフラグであり、インストール実行時に自動でその挙動が固定されるだけで、ユーザーに選択させるダイアログは表示されない。ショートカット作成可否を選ばせるには別途 `--win-shortcut-prompt` が必要だが、Compose MultiplatformのGradle DSL(`org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask`)には対応するプロパティが存在せず、現状のDSLの範囲では実現できない。インストール範囲(全ユーザー/個人用)を選ばせる標準ダイアログもjpackage自体に用意されていない。
  **改善案**: Compose Multiplatformが `winShortcutPrompt` 等のDSLプロパティを将来追加した場合、または freeform引数差し込み等の代替手段が判明した場合に、MSIインストーラー上でショートカット作成可否をユーザーに選択させる機能の追加を検討する。

## ライブラリ

- **対象**: `gradle/libs.versions.toml` の `androidx-lifecycle`（現在 `2.10.0`）
- **課題**: 2026-08-18時点のWeb調査で、AndroidX Lifecycle `2.11.0` が安定版としてリリース済みの可能性が高いことを確認した（`2.11.0-beta02` の変更点として `rememberViewModelStoreNavEntryDecorator` の新オーバーロード追加等が確認できる）。ただし検索結果だけでは正式リリース日・stable channel反映の断定はできなかった。
- **改善案**: https://developer.android.com/jetpack/androidx/releases/lifecycle で `2.11.0` が stable channel に載っているかを確認し、致命的な互換性問題がなければ `androidx-lifecycle` を更新する（CLAUDE.mdの「ライブラリバージョン管理」方針）。あわせて `2.11.0` は Compose UI 1.7.0+ を要求し、AGPも `9.2.0` 以上が前提とされる点（現在のAGPは `9.3.1` なので条件は満たす）を確認する。

