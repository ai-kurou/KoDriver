# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由で、GranTurismo 7（GT7 PS5）から UDP 経由でテレメトリデータを取得し、Compose Multiplatform アプリで表示・WAV 音声再生によるアナウンスを行う。デスクトップアプリ内で Ktor サーバーも起動し、LMU 由来の走行情報を WebSocket で配信する。

---

## モジュール構成

```
KoDriver/
├── core/
│   ├── domain/        ドメインモデル・リポジトリ抽象・ユースケース
│   ├── data/          DataStore・HTTP/WebSocketクライアント・リポジトリ実装
│   ├── lmu-windows-data/ LMU Windows共有メモリ読み取り・リポジトリ実装
│   ├── gt7-ps5-data/  GT7 PS5 UDP テレメトリ読み取り・リポジトリ実装
│   └── designsystem/  共通 Composable コンポーネント
├── feature/
│   ├── desktop-splash/           デスクトップ起動中スプラッシュの初期化進捗管理・画面表示
│   ├── lmu-windows-connection/   LMU 接続状態の監視
│   ├── main/                     アプリ全体のメイン画面状態管理
│   ├── server-connection/        KoDriver サーバーへの接続状態確認
│   ├── lmu-windows-narrator/     WAV 音声再生とアナウンス制御
│   ├── other-license-detail/     その他画面のライセンス詳細表示
│   ├── other-list/               その他画面の一覧表示・選択状態管理
│   ├── other-readout-start-sound-detail/ その他画面の読み上げ開始音設定詳細
│   ├── other-server-ip-detail/   その他画面の接続先サーバーIP設定ダイアログ
│   ├── other-console-ip-detail/  その他画面のゲーム機 IP 設定ダイアログ
│   ├── other-theme-detail/       その他画面のテーマ設定詳細
│   ├── other-volume-detail/      その他画面の音量設定詳細
│   ├── readout-list/             アナウンス設定の一覧 UI・状態管理
│   ├── lmu-windows-readout-flag-detail/          フラグアナウンスの詳細設定
│   ├── lmu-windows-readout-my-best-lap-detail/   LMU自己ベストラップアナウンスの詳細設定
│   ├── lmu-windows-readout-vehicle-approach-detail/ 車両接近アナウンスの詳細設定
│   ├── lmu-windows-readout-vehicle-damage-detail/   車両故障アナウンスの詳細設定
│   ├── lmu-windows-readout-tyre-temperature-detail/ タイヤ温度アナウンスの詳細設定
│   ├── lmu-windows-readout-remaining-virtual-energy-laps-detail/ バーチャルエナジー残量による残り周回数アナウンスの詳細設定
│   ├── gt7-ps5-connection/              GT7 PS5 接続状態の監視
│   ├── gt7-ps5-narrator/                GT7 PS5 WAV 音声再生とアナウンス制御
│   ├── gt7-ps5-readout-my-best-lap-detail/      GT7自己ベストラップアナウンスの詳細設定
│   ├── gt7-ps5-readout-remaining-fuel-laps-detail/ GT7燃料残り周回数アナウンスの詳細設定
│   ├── telemetry-log-list/              テレメトリログの一覧表示
│   └── telemetry-log-detail/            テレメトリログの詳細表示
├── app/
│   ├── androidApp/ Android アプリのエントリーポイント
│   ├── desktopApp/ JVM デスクトップアプリのエントリーポイント
│   ├── shared/     Compose Multiplatform 共通 UI・ナビゲーション
│   └── webApp/     Web アプリ（Gradle ビルド設定のみ用意、独自機能は未実装）
└── server/         デスクトップアプリ内で起動する Ktor WebSocket サーバー
```

各モジュールの詳細は、対象モジュール配下の `README.md` と実装を参照すること。

---

## 重要な制約・注意事項

### 共有メモリ読み取りは Windows 専用
`:core:lmu-windows-data` の `SharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。

### Ktor サーバー
`:server` は Windows 版デスクトップアプリと同一プロセスで起動し、`0.0.0.0:8080` で待ち受ける。WebSocket エンドポイントは `/ws/<Simulator.id>/<feature>` のパターンに従う（例: `/ws/lmu_windows/flags`）。`/ws/<Simulator.id>/flags` は `ObserveLmuWindowsRaceFlagsUseCase` を通じて `LmuWindowsFlagRepository` を購読し、`LmuWindowsRaceFlagsData` を JSON として送信する。同一内容の連続値は送信しない。

LAN 内の Android 端末からは `ws://<Windows PC のローカル IP>:8080/ws/<Simulator.id>/flags` 等へ接続する。外部端末から接続するには Windows ファイアウォールで TCP 8080 番ポートの受信を許可する必要がある場合がある。現時点では認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用すること。

`KoDriverServer.start()` は Ktor サーバー起動と同時に `KoDriverServiceAdvertiser`（`javax.jmdns.JmDNS` によるラッパー）でサービスタイプ `_kodriver._tcp.local.`（`core:domain` の `MdnsConstants.KO_DRIVER_SERVICE_TYPE` として `:server` と `:feature:other-server-ip-detail`（JVM 実装）から共有）を LAN 内へ mDNS 広告する。インスタンス名にはホスト名を使用し、複数台の Windows PC が同一 LAN 上で起動している場合でも Android 側がホスト名で区別できるようにしている。ホスト名が FQDN（ドット区切り）で返る環境向けに、ドット以降を除去してから使用する。`start()` は呼び出しごとに既存の `JmDNS` インスタンスを `stop()` してから新規生成するため、多重起動してもソケットはリークしない。mDNS の登録・解除に失敗しても（`IOException`）ログ出力のみで Ktor サーバー自体の起動・停止は妨げない。

`:feature:other-server-ip-detail` の接続先 IP 入力画面（detailPane）は、画面が表示されている間だけ `WindowsServerDiscovery`（プラットフォーム実装: JVM は JmDNS、Android は `NsdManager`）で上記の mDNS 広告を検出する。`OtherServerIpDetailViewModel` は検出結果を `SharingStarted.WhileSubscribed` で `uiState` の購読に連動させており、アプリ起動時ではなく detailPane 表示中のみ検出が動作する。検出できた場合はホスト名・IP アドレスを選べるダイアログを自動表示し、「選択する」で選択した IP アドレスを入力欄へ自動入力する。

### TimingData のラップタイム
`LmuWindowsMapper` は Scoring セグメントのプレイヤー車両からラップタイム系フィールド（`currentLapTimeMs`, `lastLapTimeMs`, `bestLapTimeMs`, `sector1Ms`, `sector1And2Ms`）を取得する。Scoring のプレイヤー車両が見つからない場合は `0L` にフォールバックする。

### オフセット情報
`LmuWindowsMapper.kt` のコメントに pyLMUSharedMemory の ctypes レイアウト（`_pack_=4`）を記載済み。

### ReadoutItemKey の配線（listPane / detailPane とNarratorの読み上げ判定の一致）
`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目をNarratorで読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`ReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `DetermineLmuWindowsNarratorReadoutUseCase`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態がNarratorに未反映だった
- #472: listPane の `VehicleDamage` スイッチが `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage` から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する `Determine*NarratorReadoutUseCase` のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

---

## テスト方針

**実装コードを書いたら、同時にユニットテストを書くこと。** テストは完了報告前に書くのではなく、実装と並行して書く。

ユニットテストを書ける実装コードを変更・追加した場合は、**変更したモジュールのカバレッジが 100% になるようにすること**。ただし、以下のコードはテスト対象から除外してよい。

- Fake / Stub / Spy などのテストダブル
- Koin などの DI Module
- 単純な Preview・サンプルデータ・定数定義
- プラットフォーム固有の外部 API（JNA, UDP ソケット等）を直接呼び出すためモックが現実的でない箇所

### テストの配置先（commonTest / jvmTest）

`expect` / `actual` を使わない ViewModel・UiState・純粋ロジックのテストは、原則 `jvmTest` に置くこと。`commonTest` は js / wasmJs / android を含む全ターゲットでコンパイル・実行されるため、モック等の JVM/Android 専用ライブラリを使えない。このプロジェクトの配布対象は実質 JVM（デスクトップ）と Android のみで、js / wasmJs（`:app:webApp`）はビルド設定のみのため、`commonTest` に置く意味があるのは実際にマルチプラットフォームで分岐する実装（`expect` / `actual` を持つコードなど）をテストする場合に限る。

### スクリーンショットテストの配置先

スクリーンショットテストは、原則として Desktop/JVM 向けの `src/jvmTest` のみに実装すること。Android と Desktop で見た目・レイアウト・表示内容が異なる場合に限り、差分を確認するための Android 向けスクリーンショットテストを `src/androidHostTest` に追加する。

スクリーンショットテストの画面サイズは、目的別にできるだけ以下へ揃えること。

- listPane などの一覧単体: `360.dp x 640.dp`
- detailPane などの詳細単体: `480.dp x 640.dp`
- 項目が多く縦方向の収まりを確認したい detailPane: `480.dp x 800.dp`
- list/detail などアプリ全体・2ペイン構成: `720.dp x 640.dp` または `840.dp x 640.dp`
- ダイアログ表示用のホスト: `480.dp x 320.dp`
- デスクトップ Splash など独立したウィンドウ: 既存の専用サイズ

新規追加・移動したスクリーンショットテストのゴールデン画像は、手元で生成してコミットしてはならない。ゴールデン画像の追加・更新は CI の `record-golden-images` ワークフローで行う。動作確認などで手元に `**/snapshots/*.png` が生成・更新された場合は、PR 作成や報告の前に必ず破棄すること。Android 向けスクリーンショットテストを追加する場合は、PR 説明に Desktop/JVM 版と見た目が異なる理由を書くこと。

---

## ライブラリバージョン管理

`libs.versions.toml` にライブラリを追加するときは、**致命的なバグや互換性問題がない限り、その時点の最新安定版を使用すること**。追加前に必ず公式リリースページで最新バージョンを確認する。

---

## ビルド・実行コマンド

```bash
# デスクトップアプリ起動（通常）
./gradlew :app:desktopApp:run

# デスクトップアプリ起動（ホットリロード）
./gradlew :app:desktopApp:hotRun --auto

# Ktor サーバー単体起動（共有メモリ由来のフラッグ情報は配信しない）
./gradlew :server:run

# Windows MSI パッケージビルド（CI: GitHub Actions / ローカル Windows 環境）
./gradlew :app:desktopApp:packageMsi

# Kover 対象モジュールのテストとカバレッジレポート生成
./gradlew koverXmlReport

# 完了報告・PR 作成前の必須チェック一式（detekt・モジュールグラフ検証・
# 全ユニットテスト（カバレッジ付き）・両アプリのビルド・デスクトップ統合テスト）
./gradlew preMergeCheck

# 静的解析とモジュール依存関係の検証
./gradlew detekt assertModuleGraph

# Android・デスクトップアプリのビルドと統合テスト
./gradlew :app:androidApp:assembleDebug
./gradlew :app:desktopApp:jar
./gradlew :app:desktopApp:test

# 特定モジュールだけを確認する場合
./gradlew :<module-path>:jvmTest
```

`:app:webApp` は Gradle ビルド設定のみで独自機能が未実装のため、現在はテスト・ビルド確認の対象外。

GitHub Actions ワークフロー:

- `on-pull-request.yml`: PR 作成・更新時に静的解析・テストを実行。同一 PR に新しいコミットが追加された場合は、実行中の古い CI をキャンセルする
- `on-main-merge.yml`: main へのマージ時に実行
- `_build-android-release.yml`: 署名付き Android APK をビルドする再利用可能ワークフロー（`workflow_call` 専用、単体では実行不可）。ファイル名・表示名を `_` で始め、Actions の実行一覧では手動起動対象として表示されないようにしている。`ref` 入力でビルド対象のブランチ・タグ・コミットを指定する。`build-apps.yml` と `release-apps.yml` の両方から呼び出される
- `build-apps.yml`: `workflow_dispatch` で起動し、Android APK と Windows MSI を並列にビルドする。Android APK のビルドは `_build-android-release.yml` を呼び出す
- `release-apps.yml`: 手動でリリースする際に実行。まず `_e2e-android-maestro.yml`（`ref: main`）を実行し、成功した場合のみバージョンバンプ・MSI/APK ビルド・リリース作成に進む。Android APK のビルドは `_build-android-release.yml` を呼び出す
- `_e2e-android-maestro.yml`: `_build-android-release.yml` で署名付き APK をビルドし、Android エミュレータ上で Maestro（`.maestro/tap-bottom-tabs.yaml`）を実行してボトムナビゲーションの各タブ（読み上げ・ログ・その他）をタップする E2E テスト。`release-apps.yml` から呼び出されるほか、Actions の画面から `ref` を指定して手動実行できる
- `record-golden-images.yml`: `workflow_dispatch` に加え、PR に `record-golden-images` ラベルが付与された時にも起動し、PR のブランチに対して golden 画像（Roborazzi スクリーンショット）を再記録してコミットする。同一ブランチで新しい実行が開始された場合は、実行中の古い記録処理をキャンセルする

---

## 主要ライブラリバージョン（libs.versions.toml）

| ライブラリ | バージョン |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| JNA | 5.19.1 |
| kotlinx-coroutines | 1.11.0 |
| androidx-lifecycle | 2.10.0 |
| Ktor | 3.5.0 |

---

## Git 操作ルール

- **コミット・プッシュ・PR の作成はユーザーが明示的に指示した場合のみ実行すること。** 自発的に行うことは禁止。
- **`main` ブランチへの直接コミット・プッシュは、実行前に必ずユーザーに確認すること。** feature ブランチへの操作は確認不要。
- **作業用ワークツリーは、必ずこのリポジトリの `.claude/worktrees/` 配下に作成すること。** リポジトリ外やその他のディレクトリに作成してはならない。
  - 例: `git worktree add .claude/worktrees/<worktree-name> -b <branch-name>`
- **ワークツリーの削除は、自分のセッションで作成したものだけに限定すること。** 複数の Claude セッションが並行してワークツリーを使用している場合があるため、他のワークツリーは削除してはならない。
- **PR のタイトルと説明は日本語で書くこと。**
- **モジュール図・スクリーンショットテストの画像は `git add` してはならない。** `assertModuleGraph` が生成するモジュール図（例: `docs/graphs/*.gv`, `docs/graphs/*.svg`）や、スクリーンショットテストが生成・更新するスクリーンショット画像（例: `**/snapshots/*.png`）は CI で自動更新される仕組みのため、手元での変更をコミットすると CI の更新と競合する。動作確認のために生成されることがあるが、**ステージングすること自体を禁止する**。ファイルを指定してステージングするときは、これらのファイルを絶対に含めないこと。また、動作確認でこれらのファイルが生成・変更された場合は、**報告前に必ず `git checkout -- <file>` または `git clean -f <file>` で変更を破棄すること**。ただし、ユーザーから古いスクリーンショットテストや不要になったゴールデン画像の削除を明示的に指示された場合に限り、既存の `**/snapshots/*.png` の削除はステージングしてよい。

### moduleGraphAssert の変更禁止

`build.gradle.kts` の `moduleGraphAssert { ... }` ブロックは、ClaudeCode / Codex が自律的に変更してはならない。

- ユーザーが明示的に `moduleGraphAssert` の変更を指示した場合のみ変更してよい。
- モジュール追加・依存関係修正・CI 修正の一環であっても、事前確認なしに `allowed` / `restricted` / `configurations` を変更してはならない。
- `assertModuleGraph` が失敗した場合は、まず依存関係やモジュール構成側を修正し、`moduleGraphAssert` の緩和で解決しない。

---

## 改善案の記録

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など、種類を問わない）は、**その場で `docs/improvement-ideas.md` に書き残すこと**。

- 依頼されたタスクの範囲外であっても、気づいた時点で追記する。今回のタスクで実装する必要はなく、あくまで記録に留める。
- 記録は箇条書きで、「対象・課題・改善案」が後から読んで分かる粒度で書く。関連するファイルやモジュール名があれば添える。
- 既に同趣旨の項目がある場合は重複させず、必要なら追記・更新する。
- ここに記録することは、対象コードの変更やテスト追加を意味しない。実際に着手する場合は通常どおり別作業として扱う。

---

## 作業完了前のルール

### 作業単位・PR 単位の時系列チェックリスト

作業を開始してから PR マージ後の片付けまで、実装内容に応じて以下を確認すること。

1. 作業開始時
   - 対象ブランチ、ベースブランチ、worktree の有無、作業範囲を確認する。
   - ユーザーが「コミットしない」「PR だけ作る」「ベースブランチは main 以外」などの条件を指定している場合は、その条件を優先する。
2. 実装前
   - 既存実装を読み、Repository / UseCase / ViewModel / UI の責務と命名が既存パターンに合うことを確認する。
   - 実装対象のモジュール・依存方向が妥当であることを確認する。`:core:designsystem` と `:core:domain` の相互依存や不要な依存追加は避ける。
   - 追加・変更するテストの対象を先に洗い出す。正常系・異常系・境界値・全項目の確認が必要な箇所を確認する。
3. 実装中
   - ユニットテストを書ける実装コードを変更・追加する場合は、実装と同時にテストを追加・更新する。
   - 画面項目・表示名・一覧項目を追加した場合は、listPane / detailPane のテスト、displayName 変換テスト、スクリーンショットテストの要否を確認する。
   - UI を変更した場合は、既存のスクリーンショットテスト対象か、新規スクリーンショットテストが必要かを確認する。
   - モジュールを追加した場合は、Kover 集計対象、Gradle 設定、GitHub Actions ワークフロー、ドキュメントの更新要否を確認する。
   - GitHub Actions のスクリーンショットテストは集約タスクを使い、モジュール追加のたびに workflow を変更しない構成を維持する。
4. 完了前
   - 変更範囲に応じたスクリーンショットテストを実行する。
   - `./gradlew preMergeCheck` を必ず実行する（detekt・assertModuleGraph・全ユニットテスト・両アプリのビルド・デスクトップ統合テストを含む）。
   - `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメント更新要否を確認する。
5. PR 作成後
   - GitHub checks / Codacy / Actions の結果を確認し、指摘があれば修正する。
   - PR のタイトルと説明が実装内容を正しく表していることを確認する。
6. PR マージ後
   - ユーザーから指示があった場合は、自分が作成した worktree / branch だけを削除する。
   - 指定されたベースブランチを最新化し、作業ツリーが clean であることを確認する。

### コード変更時の必須確認

コードを変更・追加した場合は、変更範囲・変更規模・対象モジュールに関係なく、**完了報告の前に必ず以下を実行すること**。

1. **ユニットテストの追加・更新**（→「[テスト方針](#テスト方針)」を参照。テストは実装と同時に書くこと）
2. `./gradlew preMergeCheck`（必須チェックの集約タスク。以下をすべて含む）
   - 全モジュールの detekt（モジュール単位の `:xxx:detekt` だけでは `app:shared` 等の連鎖的な問題を見落とすため、全体で実行される）
   - モジュールグラフの検証（`assertModuleGraph`）
   - 全ユニットテスト＋カバレッジレポート生成（`koverXmlReport`）
   - Android アプリ・デスクトップアプリのビルド確認
   - デスクトップアプリの統合テスト（Koin モジュール構成の変更は `AppTest` に影響するため）
3. `CLAUDE.md`・`README.md`・`docs/` 以下のドキュメントに変更が必要かを確認し、必要であれば更新する

`preMergeCheck` は Codacy や CI で検出される基本的な問題を作業者側で事前に検出するための最低必須チェックであり、モジュール単位の detekt や個別テストだけで代替してはならない。実行できなかった場合は、完了報告で理由を明記すること。

作業中に個別のチェックを素早く回したい場合は、以下を利用できる（完了報告前の `preMergeCheck` 実行は省略不可）。

```bash
# 完了報告・PR 作成前の必須チェック一式
./gradlew preMergeCheck

# 変更したモジュールのテストだけを実行（例: feature:readout-list を変更した場合）
./gradlew :feature:readout-list:jvmTest

# server モジュールを変更した場合
./gradlew :server:test

# androidMain に変更がある場合は androidHostTest も実行（例: core:data を変更した場合）
./gradlew :core:data:testAndroidHostTest
```

detekt の主な閾値（`config/detekt/detekt.yml`）:
- `MagicNumber`: 無効（数値リテラルは許容）
- `LongMethod`: 閾値 100 行（`@Composable` は除外）
- `LongParameterList`: 関数・コンストラクタともに 8 個
- `TooManyFunctions`: ファイル・クラス・オブジェクト 20 個
- `CyclomaticComplexMethod`: 閾値 15

テストが失敗・detekt で指摘がある・assertModuleGraph で違反がある・ビルドエラーがある場合は修正してからレポートする。

---

## コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`ReadoutListViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `core:data` は `kotlinMultiplatform` プラグイン（JVM + Android ターゲット）を使用する。JVM 実装は `src/jvmMain/kotlin`、Android 実装は `src/androidMain/kotlin` に置く。
- LMU Windows共有メモリ固有の実装はJVM専用の `core:lmu-windows-data` に置き、`core:data` へ依存させない。
- `@Preview` 関数は実体の `@Composable` と同一ファイルに記述する。`@Preview` のインポートは `androidx.compose.ui.tooling.preview.Preview` を使う（`org.jetbrains.compose.ui.tooling.preview.Preview` は commonMain で解決されないため使用不可）。
- 文字スタイルは `MaterialTheme.typography.*` を参照し、`fontSize` / `FontWeight` を Composable 内で直接指定しない。アプリ全体のタイポグラフィは `:core:designsystem` の `KoDriverTypography` で一元管理する。
- DataStore のキーには **ASCII の内部 ID を使うこと**。日本語などのマルチバイト文字をキーに使うと、表示名の変更でデータが孤立する。内部 ID（例: `"vehicle_approach"`）と表示名（例: `"車両接近"`）は `XxxViewModel` 内の `xxxDisplayNames: Map<String, String>` で分離する。

### Repository の命名規則

`Repository` は責務に応じて接尾辞で区別すること。命名だけで「取得用」か「設定保存用」かが判別できる状態を保つ。

- **データ取得用**（テレメトリ・走行データなど外部ソースからの読み取り、Flow 配信、バージョン取得など）は接尾辞なしの素の `XxxRepository`（例: `LmuWindowsRepository`, `LmuWindowsFlagRepository`, `Gt7Ps5Repository`, `ServerVersionRepository`）。
- **設定保存用**（DataStore による永続化）は必ず `XxxPreferencesRepository`（複数値・任意型の設定）または `XxxEnabledRepository`（単一の有効/無効フラグ）の接尾辞を付ける（例: `ThemePreferencesRepository`, `ConsoleAddressPreferencesRepository`, `ServerIpPreferencesRepository`, `KeepScreenOnEnabledRepository`）。設定保存用を素の `XxxRepository` にしてはならない。

### ViewModel の設計規則

- **`uiState: StateFlow<XxxUiState>` を唯一の公開状態にすること。** 個別の `StateFlow`（例: `selectedSimulator`）を `public` で追加してはならない。UI は `uiState` だけを参照すれば済む設計にする。
- **`init {}` を使わず、宣言的に状態を組み立てること。** 外部ソース（Repository など）からの Flow は `stateIn` で StateFlow 化し、派生状態は `combine` で組み立てる。副作用のない読み取りは `private val` のカスタム getter（`get() { ... }`）で表現する。

```kotlin
// NG: public な個別 StateFlow
val selectedSimulator: StateFlow<String?> = ...

// OK: uiState に集約
val uiState: StateFlow<XxxUiState> = ...

// NG: init {} でコルーチンを起動して状態を同期
init {
    viewModelScope.launch { flow.collect { _state.value = it } }
}

// OK: stateIn で宣言的に StateFlow 化
private val _selected: StateFlow<String?> = repository.observe()
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)
```

### MutableStateFlow の更新

`MutableStateFlow` の値を更新するときは **必ず `update { }` を使うこと**。`.value = ...` の直接代入は競合状態を招く恐れがある。

```kotlin
// NG
_state.value = _state.value.copy(count = _state.value.count + 1)

// OK
_state.update { it.copy(count = it.count + 1) }
```

### mockk テストでの any() 使用

mockk の `every`/`coEvery`/`verify`/`coVerify` では、**`any()` でないとテストコードが書けない場合を除き `any()` を使わないこと**。引数の実値を検証できず、意図しない値でもテストが通ってしまうため。

- 引数が固定値（`ReadoutItemKey`・`Simulator.id` など）なら、その具体値を直接指定する。
- `verify`/`coVerify` で引数の中身を確認したい場合は `withArg<T> { assert(...) }` を使う（`server/src/test/kotlin/kurou/kodriver/KoDriverServiceAdvertiserTest.kt` を参照）。
- 呼び出しごとに値が変わり検証が現実的でない場合（例: `saveTelemetryLog` の `createdAt` など）に限り `any()` を残してよい。

```kotlin
// NG: 具体値がわかっているのに any()
verify { jmdns.registerService(any()) }

// OK: withArg で実値を検証
verify {
    jmdns.registerService(
        withArg<ServiceInfo> {
            assert(it.name == "my-pc")
            assert(it.port == 8080)
        },
    )
}
```

### Coroutines のエラーハンドリング

`runCatching` および `mapCatching` は `CancellationException` を捕捉するため、structured concurrency を破壊する恐れがある。**使用禁止**。

代わりに `try-catch` で `CancellationException` を明示的に再スローすること:

```kotlin
// NG
runCatching { suspendFun() }

// OK
try {
    Result.success(suspendFun())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
```

### テストパターン

- テスト名は日本語のバッククォート記法（`` `初期状態は Connecting を返す`() ``）
- ViewModel の `uiState` から流れてきた内容を検証するときは `first()` を使う
- テストケース数は最小限に絞ること。正常系・異常系・境界値の 3 軸を意識し、冗長なケースは省く
- モックはテストクラスのプロパティとして `@MockK lateinit var` で宣言し、`setUp()`（`@BeforeTest` 関数）の `MockKAnnotations.init(this)` で初期化する。テストケース内やプロパティ初期化時に `mockk()` で生成しない。
- `every`/`coEvery` によるスタブ設定は **各テストケース内で行うこと**。`setUp()` でスタブまで済ませると、そのテストケースが何を前提にしているかがテスト本体だけを読んでも分からなくなり、他のテストケースの前提を変更した際に気づかず壊す原因になる。
- `verify`/`coVerify` では `exactly = N` を必ず指定し、期待する呼び出し回数を明示する。
- `verify`/`coVerify` を使用した各テストケースの最後で、検証対象のモックに対して `confirmVerified(...)` を呼び、検証していない呼び出しが残っていないことを確認する。
- MockK API は import して短い名前で呼び出し、テストコード内に `io.mockk.` の完全修飾名を書かない。
- 通常の `@MockK` / `@RelaxedMockK` は各テストの `MockKAnnotations.init(this)` で再初期化するため、`unmockkAll()` や `clearAllMocks()` を追加しない。
- `mockkObject` / `mockkStatic` / `mockkConstructor` でグローバルな差し替えを行う場合に限り、`finally` または `@AfterTest` で対応する `unmockkObject` / `unmockkStatic` / `unmockkConstructor` を必ず呼ぶ。対象を限定せず全グローバルモックを解除する `unmockkAll()` は原則として使わない。

### カバレッジ

Kover でカバレッジを計測する。新しいモジュールを追加した場合、ルートの `build.gradle.kts` の `kover { }` ブロックに `kover(project(":module:name"))` を追加しないとカバレッジ集計から除外される。

```bash
# ローカルでカバレッジレポート生成
./gradlew koverXmlReport
```
