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

## UI/UX

- **対象**: `feature/readout-list/src/commonMain/kotlin/kurou/kodriver/feature/readoutlist/ReadoutListPane.kt`（`itemsIndexed` によるドラッグ並び替え）、`feature/telemetry-log-list/src/commonMain/kotlin/kurou/kodriver/feature/telemetryloglist/TelemetryLogListPane.kt`（リアルタイムに先頭追加される `items`）
  **課題**: 選択状態の色変化には `animateColorAsState` を使っているが、リスト内でのアイテムの挿入・削除・並び替え自体には `Modifier.animateItem()` が使われておらず（リポジトリ全体で使用箇所0件）、位置の変化が瞬時に切り替わる。特にテレメトリログ一覧は走行中に先頭へ継続的に追加される性質上、視認性が低い。
  **改善案**: `LazyColumn`/`LazyRow` の `items`/`itemsIndexed` で返す各アイテムの `Modifier` に `Modifier.animateItem()` を付与し、挿入・削除・並び替え時に位置がアニメーションするようにする。

- **対象**: `app/shared/src/commonMain/kotlin/kurou/kodriver/presentation/AppTheme.kt` と `core/designsystem` の `Color.kt`/`Theme.kt`（`KoDriverTheme`）
  **課題**: 実行時の画面は `AppTheme` を使用する一方、`KoDriverTheme` は主に Preview・スクリーンショットテスト専用になっており、両者が同種の配色値をそれぞれ独自に定義している（意図的な分離である可能性はあるが要確認）。今後 `core/designsystem` 側で配色を修正しても本番アプリ（`AppTheme`）に反映されない構造で、`KoDriverTypography` のような一元管理の設計方針から外れる。
  **改善案**: `AppTheme` が `core/designsystem` の配色定義を参照する構成に統一するか、意図的な分離であればその理由をコメント等に明記する。

## バグ・仕様調査

- **対象**: `core/domain/src/commonMain/kotlin/kurou/kodriver/domain/usecase/DetermineLmuWindowsNarratorReadoutUseCase.kt` の `calculatePitTimingRemainingLaps`
  **課題**: `remainingLapsFloor == lastAnnouncedLaps` や `!enabled` で早期returnする際に `PitTimingRemainingLapsEvaluation(lastEvaluationLap, null)`（`evaluatedLap` を進めない）を返しており、同一ラップ内で以降のtickも `trackingState.currentLap == lastEvaluationLap` の早期returnに到達できず、計算済みのラップでも毎tick再計算され続ける。GT7版の `DetermineGt7Ps5NarratorReadoutUseCase.calculateRemainingFuelLaps` は同等の分岐で `fuelState.currentLap`（今回のラップ）を返して以降のtickをスキップしており、実装が非対称になっている。読み上げ結果自体への影響はない想定だが、無駄な再計算が発生する。
  **改善案**: LMU版も、平均消費量の算出に成功した以降の早期return（`remainingLapsFloor == lastAnnouncedLaps` / `!enabled`）では `trackingState.currentLap` を返すようにし、GT7版と同じくラップ内の再計算を1回に抑える。

- **対象**: `feature/ace-windows-narrator/src/commonMain/kotlin/kurou/kodriver/feature/acewindowsnarrator/AceWindowsNarratorViewModel.kt` の `fuelJob`/`flagJob`（`isOnTrack` ガード）
  **課題**: `isOnTrack`（LIVEかつTRACK上）が `false` の間は `fuelFlow`/`flagFlow` の `onEach` が `return@onEach` するため、`determineFlag`/`determineRemainingFuel` が呼ばれず `narratorState`（`previousFlag` 等）が更新されない。ピットレーン滞在中に旗状態が変化した場合、コース復帰後の最初のtickで「コース外にいる間に古い状態のまま止まっていた previous」と「現在の状態」を比較することになり、意図しないアナウンスが即座に発火する可能性がある（要確認）。
  **改善案**: コース外にいる間も状態（`previousFlag` 等）の更新自体は行い、アナウンスの発火（`eventProcessor` 呼び出し）だけを `isOnTrack` でガードする形に分離できないか検討する。LMU側の同等実装との差異も含めて仕様として意図的か確認する。

## ライブラリ・技術動向

- **対象**: `app:desktopApp` の `hotRun --auto`（ホットリロード）設定、Compose Multiplatform Gradle プラグイン
  **課題**: Compose Hot Reload が 1.0 で stable 化し、Compose Multiplatform Gradle プラグインにバンドル・デフォルト有効化されている（JetBrains Blog「The Journey to Compose Hot Reload 1.0.0」「Compose Multiplatform 1.10.0」）。KoDriverは CMP 1.11.1 を使用しているが、`hotRun` 周りに個別設定が残っている場合、stable化に伴い不要になっていないか未確認。
  **改善案**: `app/desktopApp/build.gradle.kts` の `hotRun` 関連設定を、最新の Compose Hot Reload stable版のデフォルト挙動と照らし合わせて簡素化できないか確認する。

- **対象**: `app:shared` の画面遷移・ナビゲーション実装
  **課題**: Jetpack Navigation 3 が stable化し（Android Developers Blog, 2025年11月）、Compose Multiplatform 1.10.0 以降で Android/Desktop/iOS/Web 含め非Androidターゲットでも使用可能になった。KoDriverの `app:shared` が既存の Navigation Compose ベースの実装であれば、Android/Desktop で一貫した設計に揃えられる可能性がある。
  **改善案**: 移行コストと Navigation 3 の成熟度（高度なデバイス依存パターンはまだ発展途上）を踏まえたうえで、Navigation 3 への移行余地を調査する。優先度は低め。

