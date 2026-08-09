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

## 設計・重複

- **対象**: `core:data`（`*RepositoryFactory.kt`）
  **課題**: Serializer / DataStoreFactory（`datasource/PreferencesSerializerFactory.kt`, `datasource/PreferencesDataStoreFactory.kt`）に続き、RepositoryImpl も `DataStore<T>` を薄くラップする共通拡張関数（`repository/DataStorePropertyExtensions.kt` の `observeProperty` / `saveProperty`）へ共通化済み。Repository インターフェース自体はプロパティ単位のメソッド（`observeThresholdPercentage()` / `saveThresholdPercentage()` 等）のまま変更していないため、feature 側への影響はない。一方 RepositoryFactory（25ファイル、`create*Repository(directory) = *RepositoryImpl(create*DataStore(directory))` という定型コード）は、Koin モジュール（`DesktopDataModule.kt` / `AndroidDataModule.kt`）から関数名で直接参照されるため、単純に1つの汎用関数へ置き換えると呼び出し側の書き方が変わる。
  **改善案**: RepositoryFactory を共通化する場合、関数名は現状維持しつつ内部実装だけを薄い共通ヘルパー呼び出しに変える形（Koin 側の呼び出しコードは変更しない）が現実的か検討する。

- **対象**: `core:domain`（`Simulator.kt`）, `feature:readout-list`（`ReadoutListViewModel.kt:27-28`）
  **課題**: `Simulator.entries` が private なため、シミュレータ一覧が必要な `ReadoutListViewModel` が `listOf(Simulator.LmuWindows, Simulator.Gt7Ps5, Simulator.AceWindows)` を独自に再定義している。同じ `core:domain` の `ReadoutItemKey.entries` は public で、扱いが割れている。新しいシミュレータを追加したときに追加漏れがコンパイルエラーにならない。
  **改善案**: `Simulator.entries` を public にし、`ReadoutListViewModel` はそれを参照する。

- **対象**: `core:ace-windows-data`（`AceWindowsMapper.kt`）
  **課題**: 燃料の変換だけ `map()` で、他は `mapFlag()` / `mapStatus()`。何を map するのか関数名から分からない。
  **改善案**: `mapFuel()` に改名して他と揃える。

- **対象**: `feature:lmu-windows-narrator` / `feature:gt7-ps5-narrator` / `feature:ace-windows-narrator` の Koin モジュール
  **課題**: DI 修飾子が `named("lmu_windows")` などの文字列リテラルで、`Simulator.id` と同じ値を別々に書いている。値が一致していることがコンパイル時に保証されない。
  **改善案**: `named(Simulator.LmuWindows.id)` のように `Simulator` 側の定義を参照する。

- **対象**: `feature:ace-windows-narrator`（`AceWindowsNarratorViewModel`）と LMU / GT7 の Narrator
  **課題**: ACE だけ `isLive`（`AceWindowsStatusType.LIVE`）でセッション状態をゲートして、メニュー・リプレイ・ポーズ中は読み上げない仕様になっている（#888）。LMU / GT7 には同等のゲートがない。意図的な差なのか、単に ACE から先に入れただけなのかがコードから読み取れない。
  **改善案**: LMU / GT7 にも同等のセッション状態ゲートが必要かを判断し、必要なら実装、不要ならその理由をコメントか CLAUDE.md に残す。

- **対象**: `core:gt7-ps5-data`（`Gt7Ps5Mapper.kt`）, `core:ace-windows-data`（`AceWindowsMapper.kt`）
  **課題**: LMU の車両クラス取得（上記）に続き、GT7 / ACE でも同様のデータが取れるか調査した。
  - GT7: `docs/gt7-ps5-telemetry.md` によると `carCategory[4]`（オフセット `0x16C`、char[4]、"GR3"/"GRX" 等）と `carCode`（オフセット `0x124`、int32の車両ID）が存在するが、`Gt7Ps5Mapper` はいずれも未実装。`carCategory` はLMUの `mVehicleClass` に近い性質だが4文字と短く、GT7独自の車格コードでありレースクラス名としての粒度はLMUと異なる。
  - ACE: `docs/ace-windows-telemetry.md` を確認したが、Physics/Graphics/Static いずれのブロックにもクラス・カテゴリに相当するフィールドは見当たらない。`car_model`（車種の内部ID文字列）はあるが、レースクラス名の代替にはならない。
  **改善案**: GT7 は `carCategory`/`carCode` を `Gt7Ps5Mapper` に追加すれば LMU と同様の仕組みが作れる可能性がある（要実機での値の実測確認）。ACE は現状取得手段がないため対応不可。

---

## テスト

- **対象**: `core:designsystem`
  **課題**: 実装13ファイルに対しテストは3ファイルのみで、`ListPaneCard` はカバレッジ 0%。`DetailPane` / `DetailPaneCard` / `DetailPaneScaffold` / `DetailPaneTopAppBar` / `ThresholdSlider` などアプリ全体で使い回している Composable にスクリーンショットテストが1つもない。共通コンポーネントの見た目が変わっても、各 feature の golden 画像が全部更新されるまで気づけない。
  **改善案**: designsystem 側に主要コンポーネントのスクリーンショットテストを追加する。

---

## 規約・ドキュメントの追従

- **対象**: `core:data`（`repository/SentryFeedbackRepository.kt:16`）
  **課題**: CLAUDE.md で使用禁止の `runCatching` が残っている。`CancellationException` を捕捉してしまい structured concurrency を壊す。
  **改善案**: `try-catch` で `CancellationException` を明示的に再スローする形に置き換える。

- **対象**: 各 `suspend` 関数・`viewModelScope.launch` 内の `try-catch`
  **課題**: `runCatching` は禁止されている一方、`catch (e: Exception)` で `CancellationException` を再スローしていない箇所が多数ある（`SaveServerIpWithConnectivityCheckUseCase.kt:33` / `OtherFeedbackDetailViewModel.kt:89` / `TelemetryLogListViewModel.kt:63` / `OtherConsoleIpDetailViewModel.kt:79` / `HttpServerVersionRepository.kt:34` / `GitHubAppReleaseRepository.kt:33,50` / GT7・ACE の SoundPlayer など）。`WavNarratorEngine` や Narrator の `EventProcessor` は再スロー済みで、同じ問題への対処が箇所によって割れている。
  **改善案**: `CancellationException` を先に再スローする形へ統一する。可能であれば detekt のカスタムルールか `SwallowedException` 系ルールの有効化で機械的に検出したい。

