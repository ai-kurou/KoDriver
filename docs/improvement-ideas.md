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

- **対象**: `core/data`（`core/data/src/androidMain/kotlin/kurou/kodriver/data/`, `core/data/src/jvmAndroidMain/kotlin/kurou/kodriver/data/repository/`, `.../datasource/`）
  **課題**: シミュレーター別のテレメトリ取得は `core:ace-windows-data` / `core:gt7-ps5-data` / `core:lmu-windows-data` のようにモジュール単位で分離されているが、`core:data` だけはこの粒度方針から外れ、性質の異なる複数の責務を1モジュールに同居させている。
  - Android版でのWebSocket経由テレメトリ受信（`WebSocketLmuWindowsRepository.kt`, `WebSocketLmuWindowsFlagRepository.kt`, `WebSocketLmuWindowsPitStatusRepository.kt`, `WebSocketLmuWindowsTyreCarcassTemperatureRepository.kt`, `WebSocketLmuWindowsTyreWearRepository.kt`, `WebSocketLmuWindowsVehicleApproachRepository.kt`, `WebSocketLmuWindowsVehicleClassRepository.kt`, `WebSocketLmuWindowsVehicleDamageRepository.kt`, `WebSocketLmuWindowsVirtualEnergyRepository.kt`, `WebSocketAceWindowsFlagRepository.kt`, `WebSocketAceWindowsFuelRepository.kt`, `WebSocketAceWindowsStatusRepository.kt`。いずれも `androidMain/kotlin/kurou/kodriver/data/` 直下でサブパッケージなし）
  - 各機能のDataStore設定永続化（`repository/` 配下の `*PreferencesRepositoryImpl.kt` 十数種、`datasource/` 配下の `*Serializer.kt` / `*DataStoreFactory.kt` 約49ファイル）
  - Roomによるテレメトリログ保存（`TelemetryLogRepositoryImpl.kt`）
  - GitHub Releases APIによるアプリ更新確認（`GitHubAppReleaseRepository.kt`）とサーババージョン取得（`HttpServerVersionRepository.kt`）

  `moduleGraphAssert` はモジュール間の依存方向のみを検証するため、この種の「1モジュール内での責務混在」は検知されない。`repository/`・`datasource/` はファイル数が多い割にサブパッケージが切られておらず、`androidMain` 直下のWebSocket系リポジトリ群に至ってはサブパッケージ自体が存在しない。
  **改善案**: 他のsimulator別dataモジュールと粒度をそろえるなら、Android版WebSocketリポジトリ群を機能別（例: `websocket` パッケージ、あるいは将来的に別モジュール）に切り出す、設定永続化用DataStore実装を `preferences` パッケージにまとめる、Room/GitHub API/サーババージョン取得のような単発の外部連携を用途別パッケージに分けるなど、パッケージ単位での責務分離を検討する。モジュール分割まで踏み込むかはコンパイル時間・依存関係の複雑化とのトレードオフのため、まずはパッケージ整理から着手するのが現実的。

