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

## バグ

各項目の末尾に、実測で再現を確認したものか、コードからの推定かを明記している。

- **対象**: `feature:*-narrator` の3つの `*NarratorViewModel` と `core:domain` の `Determine*NarratorReadoutUseCase`
  **課題**: 読み上げ判定は `enabledStates.getValue(key)` でユーザー設定を参照している（本番コードに29箇所）。`Map.getValue` はキーが無いと `NoSuchElementException` を投げるが、ViewModel 側は `stateIn(..., SharingStarted.Eagerly, emptyMap())` で初期化しているため、DataStore の初回読み込みが完了する前にテレメトリが届くと例外になる。特に `Gt7Ps5NarratorViewModel.kt:211,215` の `currentSettings` getter、`LmuWindowsNarratorViewModel.kt:385,446`（`determinePitTiming*` の `enabled=` 引数）、`DetermineLmuWindowsNarratorReadoutUseCase.kt:562,578`（車両接近）は毎 tick 無条件に評価される。一時テストで再現し、`java.util.NoSuchElementException: Key Root is missing in the map.` が発生して当該ジョブが恒久停止することを確認済み（Android では未捕捉例外としてクラッシュに至る）。`SharedMemoryPollingSource` は待機なしで最初のバッファを emit するため、LMU 起動済みの状態でデスクトップアプリを起動するケースが最も現実的な発生条件。
  **改善案**: `getValue` を `getOrDefault` / `?: default` に置き換えるか、`enabledStates` が空の間は判定自体をスキップするガードを入れる。デフォルト値は `READOUT_ENABLED_STATE_DEFAULT` など既存の Defaults に揃える。あわせて「設定未ロード中にテレメトリが届いても例外にならない」テストを各 Narrator ViewModel に追加する。

---

## 設計・重複

- **対象**: `feature:lmu-windows-narrator` / `feature:gt7-ps5-narrator` / `feature:ace-windows-narrator`
  **課題**: `SoundPlayer` / `NarratorErrorCapture`（commonMain + jvm/android/js/wasmJs の actual）/ `PlatformSoundModule` / `JsSoundPlayer` / `WasmJsSoundPlayer` は3モジュールで完全に同一（パッケージ名を正規化して diff 済み）。`*WavNarratorEngine` と `*NarratorEventProcessor` も構造が同じで、実質の差はイベント→WAV ファイルのマップだけ。上記のバグ（SoundPlayer の改善が LMU にしか入っていない、割り込みバグが3箇所に同時に存在する）は、この重複が直接の原因になっている。`SoundPlayer` を型ごと分けているのは Koin の型解決を分離するためと見えるが、`named()` 修飾子で代替できる。
  **改善案**: 共通の narrator 基盤モジュール（例: `feature:narrator-core` もしくは `core:designsystem` 配下）へ `SoundPlayer` / `NarratorErrorCapture` / `PlatformSoundModule` と、イベント→ファイルのマップを差し替え可能にした `WavNarratorEngine` を切り出し、3モジュールはマップと DI 登録のみを持つ形にする。
  **付随する課題**: 3つの `TextToSpeechEngine` はいずれも `single` で、`init` ブロックで自モジュールの WAV を全て ByteArray としてロードする。そのため選択中でないシミュレータの音声（3モジュール合計で 46 ファイル・約 2.8MB）も常時メモリに載る。共通化のタイミングで、選択中のシミュレータ分だけ遅延ロードする方式も検討したい。

- **対象**: `feature:readout-list`（`ReadoutItemDisplayName.kt`）, `feature:telemetry-log-list`（`ReadoutItemDisplayName.kt`）
  **課題**: `ReadoutItemKey` を表示名に変換する `when` 式が、関数名（`itemDisplayName` / `readoutItemDisplayName`）とリソースキーの接頭辞（`item_` / `readout_item_`）以外はほぼ逐語で重複しており、約190行ある。文字列リソースも接頭辞を除けばキー名・文言ともに30項目すべて一致している（diff で確認）。先に PR #908 で解決した Simulator 表示名・アイコンの重複とまったく同じ構図。カバレッジも 51.9%（telemetry-log-list） / 62.0%（readout-list）と低く、旗の分岐がほとんど未検証。
  **改善案**: PR #908 と同じ方針で `core:designsystem` に `readoutItemDisplayName(readoutItemKeyValue: String)` を集約し、文字列リソースも designsystem 側の1箇所に寄せる。`ReadoutItemKey` は `core:domain` の型なので、#908 と同様に `ReadoutItemKey.value`（文字列 ID）を引数に取れば `core:designsystem -> core:domain` の依存を増やさずに済む。集約後は全 `ReadoutItemKey` を網羅する表示名テストを1箇所に置けば済むため、カバレッジの穴も同時に埋まる。

- **対象**: `core:domain`（`DetermineGt7Ps5NarratorReadoutUseCase.kt:44-49`）, `feature:gt7-ps5-narrator`（`Gt7Ps5NarratorViewModel.kt:210-215`）
  **課題**: GT7 だけ読み上げのゲート機構が二重になっている。`Gt7Ps5NarratorReadoutSettings` は `enabledStates: Map<ReadoutItemKey, Boolean>` を持ちながら、`remainingFuelLapsEnabled` / `remainingFuelEnabled` という専用の boolean も併存させており、自己ベストラップだけ `enabledStates` 経由・他2つは専用フラグ経由で判定している。LMU / ACE は `enabledStates` に統一されている。CLAUDE.md が警告している「ReadoutItemKey の配線」が壊れやすい形で、項目を増やすたびに二重管理が必要になる。
  **改善案**: GT7 も `enabledStates` 経由に統一し、専用 boolean を `Gt7Ps5NarratorReadoutSettings` から削除する。あわせて `Determine*NarratorReadoutUseCase` のテストに「その項目を無効にした場合は読み上げられない」ケースを揃える。

- **対象**: `:server`（`FlagWebSocket.kt` ほか WebSocket ルーティング10ファイル）
  **課題**: 各ファイルが `webSocket(path) { flow.distinctUntilChanged().let { sendJsonMessages(it) } }` の写経で、内容の差はパスと UseCase だけ。加えて命名が非対称で、ACE 側は `AceWindowsFlagWebSocket.kt` / `aceWindowsFlagWebSocket()` と接頭辞付きなのに、LMU 側は `FlagWebSocket.kt` / `flagWebSocket()` と無印のまま。シミュレータが3種になった今、無印のファイル名・関数名はどのシムのものか読み取れない。
  **改善案**: `Route.telemetryWebSocket(feature: KoDriverServerFeature, simulator: Simulator, flow: Flow<T>)` のような汎用関数1つに集約する。集約しない場合でも、LMU 側のファイル名・関数名に `lmuWindows` 接頭辞を付けて ACE 側と揃える。

- **対象**: `:server`（`Application.kt` のルート `get("/")`）
  **課題**: `get("/") { call.respondText("Hello, Ktor!") }` が Ktor 雛形の残骸のまま残っている。LAN 内に公開されるエンドポイントであり、実運用上の意味を持たない応答を返している。
  **改善案**: 削除するか、`/version` と同様にサーバーの状態が分かる意味のある応答（稼働確認用のヘルスチェックなど）へ変更する。

- **対象**: `core:data`（`datasource/*Serializer.kt`, `datasource/*DataStoreFactory.kt`, `*RepositoryFactory.kt`, `repository/*RepositoryImpl.kt`）
  **課題**: Preferences 1種類につき Serializer / DataStoreFactory / RepositoryFactory / RepositoryImpl の4点セットが必要で、現在それぞれ 22 / 23 / 25 / 24 ファイル、合計で約94ファイルの定型コードになっている。設定を1つ増やすたびに4ファイル追加と、それぞれのテスト追加が必要。
  **改善案**: `@Serializable` なデータクラスと DataStore ファイル名を渡せば Serializer と DataStore を組み立てられる汎用ファクトリ（例: `jsonPreferencesDataStore<T>(fileName, default)`）を用意し、個別ファイルは差分のみ持つ形にする。RepositoryImpl も `observe` / `save` の定型部分を共通化できる余地がある。

- **対象**: `core:domain`（`Simulator.kt`）, `feature:readout-list`（`ReadoutListViewModel.kt:27-28`）
  **課題**: `Simulator.entries` が private なため、シミュレータ一覧が必要な `ReadoutListViewModel` が `listOf(Simulator.LmuWindows, Simulator.Gt7Ps5, Simulator.AceWindows)` を独自に再定義している。同じ `core:domain` の `ReadoutItemKey.entries` は public で、扱いが割れている。新しいシミュレータを追加したときに追加漏れがコンパイルエラーにならない。
  **改善案**: `Simulator.entries` を public にし、`ReadoutListViewModel` はそれを参照する。

- **対象**: `feature:lmu-windows-narrator`（`jvmMain/JvmSoundPlayer.kt:60`）
  **課題**: 同一モジュール内に `captureNarratorError()` という expect/actual のエラー送出抽象があり `AndroidSoundPlayer` はそれを使っているのに、`JvmSoundPlayer` だけ `Sentry.captureException()` を直接呼んでいる。
  **改善案**: `captureNarratorError()` に統一する。

- **対象**: `core:ace-windows-data`（`AceWindowsMapper.kt`）
  **課題**: 燃料の変換だけ `map()` で、他は `mapFlag()` / `mapStatus()`。何を map するのか関数名から分からない。
  **改善案**: `mapFuel()` に改名して他と揃える。

- **対象**: `feature:lmu-windows-narrator` / `feature:gt7-ps5-narrator` / `feature:ace-windows-narrator` の Koin モジュール
  **課題**: DI 修飾子が `named("lmu_windows")` などの文字列リテラルで、`Simulator.id` と同じ値を別々に書いている。値が一致していることがコンパイル時に保証されない。
  **改善案**: `named(Simulator.LmuWindows.id)` のように `Simulator` 側の定義を参照する。

- **対象**: `feature:ace-windows-narrator`（`AceWindowsNarratorViewModel`）と LMU / GT7 の Narrator
  **課題**: ACE だけ `isLive`（`AceWindowsStatusType.LIVE`）でセッション状態をゲートして、メニュー・リプレイ・ポーズ中は読み上げない仕様になっている（#888）。LMU / GT7 には同等のゲートがない。意図的な差なのか、単に ACE から先に入れただけなのかがコードから読み取れない。
  **改善案**: LMU / GT7 にも同等のセッション状態ゲートが必要かを判断し、必要なら実装、不要ならその理由をコメントか CLAUDE.md に残す。

- **対象**: `feature:lmu-windows-readout-tyre-temperature-detail`
  **課題**: LMU のタイヤ温度アナウンス（高温閾値）は車両クラスによらず単一の閾値設定になっている。しかしクラスごとにタイヤ温度の実運用レンジが異なり、例えば GT3 は90℃に届かないままレースを終えることがある一方、GTE は90℃を超えた状態でレースを走り切ることがあり、同じ閾値では「GTEでは正常域なのに警告される／GT3では警告すべき水準に届く前に見逃す」というズレが起きうる（ユーザー報告、実測は未確認）。現在の LMU 車両クラスは Hypercar・LMP2・LMP3・GTE・LMGT3 の5クラスが確認できている。
  車両クラス自体の取得は `core:lmu-windows-data`（`LmuWindowsVehicleClassRepositoryImpl`, `LmuWindowsMapper.readVehicleClassName`）で実装済み（Scoring セグメント `rF2VehicleScoring.mVehicleClass[32]`、車両先頭 +200 の人間可読な文字列を使用。#916 / #917）。`feature:debug-state-detail` のデバッグカードとしても表示できる。ただし `feature:lmu-windows-readout-tyre-temperature-detail` の閾値設定側では未活用で、クラス別の閾値切り替えはまだ実装されていない。
  **改善案**: `feature:lmu-windows-readout-tyre-temperature-detail` の閾値設定をクラス文字列ごとに保持できるよう拡張する（未知のクラス文字列は現行の単一閾値にフォールバック）。`ReadoutItemKey` を追加する場合は CLAUDE.md の「ReadoutItemKey の配線」の手順に従い listPane/detailPane と Narrator 判定側の両方を確認すること。

- **対象**: `core:gt7-ps5-data`（`Gt7Ps5Mapper.kt`）, `core:ace-windows-data`（`AceWindowsMapper.kt`）
  **課題**: LMU の車両クラス取得（上記）に続き、GT7 / ACE でも同様のデータが取れるか調査した。
  - GT7: `docs/gt7-ps5-telemetry.md` によると `carCategory[4]`（オフセット `0x16C`、char[4]、"GR3"/"GRX" 等）と `carCode`（オフセット `0x124`、int32の車両ID）が存在するが、`Gt7Ps5Mapper` はいずれも未実装。`carCategory` はLMUの `mVehicleClass` に近い性質だが4文字と短く、GT7独自の車格コードでありレースクラス名としての粒度はLMUと異なる。
  - ACE: `docs/ace-windows-telemetry.md` を確認したが、Physics/Graphics/Static いずれのブロックにもクラス・カテゴリに相当するフィールドは見当たらない。`car_model`（車種の内部ID文字列）はあるが、レースクラス名の代替にはならない。
  **改善案**: GT7 は `carCategory`/`carCode` を `Gt7Ps5Mapper` に追加すれば LMU と同様の仕組みが作れる可能性がある（要実機での値の実測確認）。ACE は現状取得手段がないため対応不可。

---

## テスト

- **対象**: 各 feature モジュールの `jvmTest/.../RoborazziUtil.kt`
  **課題**: 34モジュールに複製されており、内容は `package` 行以外まったく同一（md5 と diff で確認）。スクリーンショットテストの設定を変えるときに34ファイルを触ることになる。
  **改善案**: 共有のテスト用モジュール（`testFixtures` もしくは `build-logic` の `feature-compose-screenshot` プラグイン側）に1箇所だけ置き、各モジュールから参照する。

- **対象**: `core:designsystem`
  **課題**: 実装13ファイルに対しテストは3ファイルのみで、`ListPaneCard` はカバレッジ 0%。`DetailPane` / `DetailPaneCard` / `DetailPaneScaffold` / `DetailPaneTopAppBar` / `ThresholdSlider` などアプリ全体で使い回している Composable にスクリーンショットテストが1つもない。共通コンポーネントの見た目が変わっても、各 feature の golden 画像が全部更新されるまで気づけない。
  **改善案**: designsystem 側に主要コンポーネントのスクリーンショットテストを追加する。

- **対象**: `feature:lmu-windows-narrator` / `feature:ace-windows-narrator` の `NarratorErrorCapture`
  **課題**: js / wasmJs の actual 実装は3モジュールで同一だが、テスト（`jsTest` / `wasmJsTest`）があるのは `feature:gt7-ps5-narrator` だけ。
  **改善案**: Narrator 基盤の共通化（上記）で1箇所に集約するのが本筋。集約前に対応する場合は、LMU / ACE にも同等のテストを追加する。

---

## 規約・ドキュメントの追従

- **対象**: `core:data`（`repository/SentryFeedbackRepository.kt:16`）
  **課題**: CLAUDE.md で使用禁止の `runCatching` が残っている。`CancellationException` を捕捉してしまい structured concurrency を壊す。
  **改善案**: `try-catch` で `CancellationException` を明示的に再スローする形に置き換える。

- **対象**: 各 `suspend` 関数・`viewModelScope.launch` 内の `try-catch`
  **課題**: `runCatching` は禁止されている一方、`catch (e: Exception)` で `CancellationException` を再スローしていない箇所が多数ある（`SaveServerIpWithConnectivityCheckUseCase.kt:33` / `OtherFeedbackDetailViewModel.kt:89` / `TelemetryLogListViewModel.kt:63` / `OtherConsoleIpDetailViewModel.kt:79` / `HttpServerVersionRepository.kt:34` / `GitHubAppReleaseRepository.kt:33,50` / GT7・ACE の SoundPlayer など）。`WavNarratorEngine` や Narrator の `EventProcessor` は再スロー済みで、同じ問題への対処が箇所によって割れている。
  **改善案**: `CancellationException` を先に再スローする形へ統一する。可能であれば detekt のカスタムルールか `SwallowedException` 系ルールの有効化で機械的に検出したい。

- **対象**: `core:domain`（`*Defaults.kt`）
  **課題**: CLAUDE.md は定数名の末尾を `_DEFAULT` に統一すると定めているが、`ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE` / `LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE` / `LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE` の3つは `DEFAULT` が途中に入っている。
  **改善案**: 末尾 `_DEFAULT` へ改名する。

- **対象**: `feature:lmu-windows-narrator`（`LmuWindowsNarratorViewModel.kt`）, `feature:gt7-ps5-narrator`（`Gt7Ps5NarratorViewModel.kt`）
  **課題**: `stateIn` の初期値に、対応する `*_DEFAULT` 定数があるのにリテラルを直書きしている箇所が8つある（LMU: `:207 FORMAL`, `:212 SESSION_STOP`, `:217 95`, `:251 true`, `:256 CAR_LEFT_RIGHT`, `:270 KEEP_LEFT_RIGHT` / GT7: `:89 FORMAL`, `:94 3`）。現状は値が一致しているため挙動上のバグはないが、Defaults 側だけを変更したときに Narrator の初期値が古いまま残る。CLAUDE.md の「デフォルト値は `:core:domain` の定数を参照する」に反する。
  **改善案**: すべて対応する定数参照に置き換える。同じ ViewModel 内でも `LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE` などは定数参照になっており、揃えるだけで済む。

