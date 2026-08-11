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

- **対象**: `gradle/libs.versions.toml`
  **課題**: 2026-08-11時点のWeb調査で、以下のライブラリに現行バージョンより新しい安定版が存在することを確認した。
  - AGP: 9.2.1 → 9.3.1
  - Ktor: 3.5.0 → 3.5.2
  - KSP: 2.3.9 → 2.3.11
  - androidx-sqlite: 2.6.2 → 2.7.0
  - mockk: 1.14.3 → 1.14.11
  - kover: 0.9.8 → 0.9.9
  - roborazzi: 1.64.0 → 1.71.0
  - sentry: 8.44.0 → 8.52.0
  - jmdns: 3.6.1 → 3.6.3

  なお `material3`（`org.jetbrains.compose.material3:material3`）は現行の `1.11.0-alpha07` が Compose Multiplatform 1.11.1 時点でも最新に近いalpha版であり、stable版は未リリースのため更新対象外。`aboutLibraries` は 14.2.1 → 15.0.4 へ対応済み（PR #1046）。
  **改善案**: 「致命的な不具合がない限り最新安定版を使用する」方針（CLAUDE.md参照）に沿って、上記ライブラリを個別に最新安定版へ更新する。`androidx-sqlite` は `androidx-room`（2.8.4）との組み合わせ互換性を確認すること。`roborazzi` は差分が大きいため、更新後にスクリーンショットテストのgolden画像再記録が必要になる可能性がある（CIの `record-golden-images` ワークフローで対応）。

- **対象**: `.claude/skills/`（Claude Code運用）
  **課題**: Android Developers Blog（2026年8月6日「Inside Android Skills - Built for deprecation」）によると、AGP 9系・Navigation 3・Perfetto SQL・Wear Compose M3など「SOTAモデルが弱い領域」の知識をAIコーディングエージェントに注入するAndroid Skills（コミュニティ製として `chrisbanes/skills`（Compose向け）、`skydoves/compose-performance-skills` 等）が公開されている。KoDriverはAGP 9系・Compose Multiplatformを使用しており対象領域と重なるが、現状こうした外部知識源をClaude Codeの運用に組み込んでいない。
  **改善案**: Compose/AGP9向けのコミュニティSkillsやAndroid Knowledge Base相当の情報源を、KoDriverのClaude Code運用（`.claude/skills/` 等）に組み込む価値があるか調査する。優先度は低め。

