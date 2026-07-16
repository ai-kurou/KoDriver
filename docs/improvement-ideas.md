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

## CI

- **対象**: `.github/workflows/on-pull-request.yml` / `on-main-merge.yml` の `desktop-screenshot-test` / `android-screenshot-test` ジョブ
  **課題**: `recordRoborazziXxxTests` と `verifyRoborazziXxxTests` を同一ジョブ内で連続実行しており、両者とも各モジュールの `testJvmTest` / `testAndroidHostTest`（スクリーンショット以外も含む単体テスト全体）に依存するため、`unit-test` ジョブ（`koverXmlReport` 経由）と合わせて実質同じテストスイートがCI全体で3回実行されている。さらに、CIが自動生成する `chore: update golden images` コミット（スナップショットPNGの一括更新）が同一ジョブ内の `verify` ステップ実行前に発生すると、多くのモジュールでGradleの `UP-TO-DATE` 判定が広範囲に無効化され、本来スキップできるテストが軒並み再実行されて実行時間が数分〜15分超まで跳ね上がることがある（PR #561で timeout-minutes を 10→15→25 に順次引き上げる事態が発生）。
  **改善案（一部対応済み）**: record用ジョブとverify用ジョブを分離した後、さらに record を通常のPR push / main mergeから外し、`workflow_dispatch` で必要なときだけ起動する方式へ変更した。通常のPR pushでは `verify` のみを他ジョブと並列実行し、golden画像が古ければ通常どおり失敗させる。開発者は失敗を確認した上で明示的に `Record Golden Images` workflow を起動し、CI(Linux)上で生成した画像を自動コミットさせる。これにより、(1) 生成環境はLinux CIのまま維持でき、(2) 通常のPRでは record→verify の直列待ちが発生せず実行時間を短縮でき、(3) 意図しない見た目変化はverify失敗として検知でき自動コミットで握りつぶされない。ただし `unit-test` ジョブとの重複実行（同じテストスイートがCI全体で複数回走る点）は未解消であり、Roborazziタスクの入力宣言を見直してスナップショット全体ではなく変更されたファイル単位で正しく `UP-TO-DATE` 判定できるようにするなど、引き続き重複実行の削減を検討する。
  **対応見送り**: 上記の重複実行削減（Gradleリモートキャッシュのジョブ間共有、Roborazziタスクの依存関係切り離し等）は、ワークフローファイルの複雑化・保守コスト増に見合うほどの実害（現状は数分〜十数分の時間ロス）ではないと判断し、対応しない。

## mDNS（server / feature:other-server-ip-detail）

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`
  **課題**: `start()` は既存の `JmDNS` インスタンスを close せずに新規生成して上書きする。現状 `KoDriverServer.start()` は起動時に1回しか呼ばれないため実害はないが、将来サーバー再起動フローができた場合に前のマルチキャストソケットがリークする。
  **改善案**: `start()` 冒頭で既存インスタンスがあれば `stop()` を呼ぶ、または多重起動を防ぐガードを入れる。

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`、`feature/other-server-ip-detail/src/jvmMain/kotlin/kurou/kodriver/feature/otherserveripdetail/PlatformWindowsServerDiscovery.jvm.kt`
  **課題**: mDNSサービスタイプ文字列 `_kodriver._tcp.local.` がサーバー側・クライアント側それぞれに private 定数として個別定義されており、共有定数化されていない。将来どちらかだけ値を変更すると検出できなくなる。
  **改善案**: 両モジュールから参照できる共通定数（例: `core:domain` 等）へ抽出する。

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`
  **課題**: mDNSのインスタンス名に `InetAddress.getLocalHost().hostName` を使用しているが、環境によってはFQDN（ドット区切り）が返ることがあり、mDNSサービス名にドットが含まれると `ServiceInfo.create` での名前解釈が崩れる可能性がある。
  **改善案**: ホスト名のサニタイズ（ドット除去など）を検討する。

- **対象**: `feature/other-server-ip-detail/src/androidMain/kotlin/kurou/kodriver/feature/otherserveripdetail/PlatformWindowsServerDiscovery.android.kt`
  **課題**: `NsdManager` ベースの検出実装で、実機のDoze/Wi-Fiスリープ設定次第では検出が不安定になる可能性がある既知の事例がある。また、プラットフォーム固有の外部APIを直接呼ぶためユニットテストの対象外（`CLAUDE.md`のテスト方針に基づく除外）となっており、実機確認でしか動作を担保できない。
  **改善案**: Doze/Wi-Fiスリープ中の実機動作確認を行う。必要であれば結合テスト（instrumented test）の追加を検討する。

## ViewModel / UseCase 責務分離

- **対象**: `feature/readout-list/.../ReadoutListViewModel.kt`
  **課題**: 保存済み読み上げ順序と現在のデフォルト順序を突き合わせ、削除済み項目を除外し、新規項目を末尾に補完するロジックが ViewModel 内にある。これは UI 表示都合だけでなく、読み上げ項目順序の整合性を保つドメインルールに近い。
  **改善案**: `ResolveReadoutOrderUseCase` などへ切り出し、`ObserveReadoutOrderUseCase` の結果と `ReadoutListItemType.defaultOrder(simulator)` から有効な順序を生成する責務を domain 側へ寄せる。

- **対象**: `feature/telemetry-log-list/.../TelemetryLogListViewModel.kt`
  **課題**: テレメトリログの新しい順ソートと、削除後などに存在しなくなった選択IDを無効化する処理が ViewModel 内にある。現状は小さいが、ログ検索・フィルタ・ページングを追加すると ViewModel の表示整形責務が膨らみやすい。
  **改善案**: 必要になった段階で `ObserveSortedTelemetryLogsUseCase` やログ一覧用の query UseCase へ切り出し、ViewModel は選択状態とダイアログ状態だけを扱う。

- **対象**: `feature/other-server-ip-detail/.../OtherServerIpDetailViewModel.kt`
  **課題**: IPv4 形式チェック、接続確認付き保存、接続警告後の強制保存が ViewModel 内にある。画面専用の入力処理としては許容範囲だが、接続先設定の保存ルールとして再利用される場合は責務が重くなる。
  **改善案**: 他画面や自動設定で再利用する段階で `ValidateIpAddressUseCase` や `SaveServerIpWithConnectivityCheckUseCase` へ切り出す。

- **対象**: `feature/gt7-ps5-narrator/.../Gt7Ps5NarratorViewModel.kt`
  **課題**: 読み上げ判定自体は `DetermineGt7Ps5NarratorReadoutUseCase` に切れているが、優先度に基づく読み上げ中断判定、前回テレメトリとのログJSON生成、機能ごとの前回値保持が ViewModel に残っている。GT7の読み上げ項目が増えると LMU Narrator と同様に肥大化しやすい。
  **改善案**: 読み上げ優先度制御やログ保存を担う小さな UseCase / service へ段階的に切り出し、ViewModel は Flow の接続とライフサイクル管理に寄せる。

## core:gt7-ps5-data

- **対象**: `core/gt7-ps5-data/src/jvmAndroidMain/kotlin/kurou/kodriver/core/gt7ps5data/datasource/Gt7Ps5UdpSource.kt`
  **課題（バグ候補・優先度高）**: `udpPacketFlow` の受信ループで `DatagramPacket` の length をループごとにリセットしていない。`DatagramSocket.receive` は受信したバイト数に `length` を縮めるため、一度でも `PACKET_MIN_SIZE`（0x170）より短いパケット（LAN 内の迷子パケットや将来のパケットフォーマット差異など）を受信すると、以降のすべての受信がその短い length に切り詰められる。切り詰められたパケットは `decrypt` のサイズチェックで捨てられ続けるため、ソケットを開き直すまで GT7 テレメトリが恒久的に止まる。
  **改善案**: `socket.receive(dgram)` の直前に `dgram.setLength(buf.size)` を入れてループごとに受信バッファ長をリセットする。短いパケット受信後も正常パケットを受信できることを `FakeUdpSocket` で再現するテストを追加する。

- **対象**: `core/gt7-ps5-data/src/jvmAndroidMain/kotlin/kurou/kodriver/core/gt7ps5data/datasource/Gt7Ps5UdpSource.kt`
  **課題**: `retryWhen` がリトライ対象とするのは `BindException` のみで、それ以外の `IOException`（Wi-Fi 切替・スリープ復帰時の `send` 失敗、`SocketException: Network is unreachable` 等）が発生すると flow が例外終了する。この flow は `shareIn(scope, WhileSubscribed)` の上流なので、例外は共有コルーチン側で発生し、購読者にエラーとして伝わらないまま受信が復帰不能になる（アプリ再起動か consoleAddress 変更まで回復しない）。また初回の `socket.send(HEARTBEAT_PAYLOAD, ...)` は try-catch の外にあり、宛先不達で即例外になる経路がある。
  **改善案**: `retryWhen` を `IOException` 全般（またはリトライ可能な例外の集合）に広げ、遅延付きで再接続する。初回 heartbeat 送信も受信ループと同じ例外処理の中へ移す。

## server（WebSocket 配信）

- **対象**: `server/src/main/kotlin/kurou/kodriver/FlagWebSocket.kt` ほか各 `*WebSocket.kt`
  **課題**: WebSocket ハンドラが `incoming` を一切読まず、Flow の `collect` → `send` だけで構成されている。クライアントが正常に Close フレームを送って切断しても、サーバー側は次に `send` が失敗するまで切断を検知できない。`distinctUntilChanged` によって送信頻度が低い（フラッグが変わらない・LMU 未起動で flow が emit しない）状況では、切断済みクライアントのセッションとコルーチンが長時間残留する。
  **改善案**: ハンドラ内で `incoming` を読み捨てるコルーチンを併走させる（`launch { for (frame in incoming) { } }` など）か、`closeReason` の完了で `collect` をキャンセルする構成にする。

- **対象**: `server/src/main/kotlin/kurou/kodriver/FlagWebSocket.kt` / `TimingWebSocket.kt` ほか
  **課題**: `Json { encodeDefaults = true }` のインスタンスが WebSocket ルートのファイルごとに private 定義されており、設定が分散している。将来 `ignoreUnknownKeys` 等の設定を足すときに漏れが生じやすい。
  **改善案**: server モジュール内の共通 `Json` インスタンスに集約する。

## core:data（WebSocket クライアント）

- **対象**: `core/data/src/androidMain/kotlin/kurou/kodriver/data/WebSocketLmuWindows*Repository.kt`（5 ファイル）
  **課題**: 「serverIp を `flatMapLatest` → `client.webSocket` 接続 → Text フレームを JSON デコードして emit → 失敗時 `delay` 後リトライ」という構造が 5 リポジトリでほぼ同一のまま重複している。また接続失敗の `catch (_: Exception) {}` が完全に握りつぶしで、接続できない原因（ポート違い・ファイアウォール等）の調査手段がない。各リポジトリが `HttpClient` を個別生成しており、`close()` も呼ばれない。
  **改善案**: 「path とデシリアライザを渡すと再接続付き Flow を返す」共通ヘルパー（例: `WebSocketFlowFactory`）に集約する。接続失敗時は少なくともデバッグログを残す。`HttpClient` は DI で単一インスタンスを共有する。

## core:lmu-windows-data

- **対象**: `core/lmu-windows-data/src/main/kotlin/kurou/kodriver/core/lmuwindowsdata/datasource/LmuWindowsSharedMemorySource.kt`
  **課題**: `bufferFlow` は 16ms ごとに共有メモリ全体（約 324KB）を heap の `ByteBuffer` へコピーしており、購読中は約 20MB/s のアロケーションが発生する。ネイティブバッファを下流に渡さない設計自体は安全のため妥当だが、GC 負荷としては大きい。
  **改善案**: 実測で GC 負荷が問題になった場合に、ダブルバッファの再利用や、下流が必要とするセグメント（Scoring / Telemetry の一部）だけを構造体に読み出してから emit する方式を検討する。現状は「計測してから」の課題として記録に留める。

## feature:server-connection

- **対象**: `feature/server-connection/src/commonMain/kotlin/kurou/kodriver/feature/serverconnection/ServerConnectionViewModel.kt`
  **課題**: バージョン不一致警告の表示判定が `map { }` 内の副作用（`versionMismatchWarningShown` フラグ更新と `_showVersionMismatchBottomSheet.update`）で行われており、CLAUDE.md の「宣言的に状態を組み立てる」規則から外れている。`WhileSubscribed` のため画面の再購読で upstream が再実行される点は `versionMismatchWarningShown` で守られているが、collect 中の副作用は挙動が追いにくい。
  **改善案**: 「初回の不一致検知で一度だけ表示する」ロジックを `distinctUntilChanged` + `runningFold` 等の演算子、または UseCase 側の状態として宣言的に表現する。

## デザイン（UI/UX・designsystem）

- **対象**: `core/designsystem/.../Theme.kt`
  **課題**: `MaterialTheme` に `colorScheme` のみ渡しており、`typography` / `shapes` が Material3 デフォルトのまま。画面ごとに `fontSize` や `FontWeight` を直接指定し始めるとスタイルが分散し、後からアプリ全体の文字スケールを調整できなくなる。
  **改善案**: designsystem に `KoDriverTypography`（必要なら `Shapes` も）を定義して `MaterialTheme` へ渡し、feature 側は `MaterialTheme.typography.*` だけを参照する運用にする。

- **対象**: `core/designsystem/.../Color.kt` / `Theme.kt`（ライトテーマ）
  **課題**: ライトテーマの `primary = Yellow40` に対して `onPrimary = Neutral99`（ほぼ白）を組み合わせている。黄色系 primary × 白文字は WCAG のコントラスト比 4.5:1 を満たさないことが多く、ボタンラベル等の可読性が低い恐れがある。secondary（Lime）・tertiary（Neon）も同様の懸念がある。
  **改善案**: 主要な色ペア（primary/onPrimary など）のコントラスト比を実測し、不足していれば `onPrimary` を暗色（Yellow10 等）へ変更する。スクリーンショットテストとは別に、色定義だけのコントラスト検証ユニットテストを designsystem に置くことも検討する。

- **対象**: Android アプリ全体のテーマ
  **課題**: Android 12+ の Dynamic Color（Material You）に対応しておらず、常に固定のブランドカラーで表示される。レース用アプリとしてブランド色固定は妥当な判断でもあるため、対応しない場合でも「意図的に非対応」であることがどこにも記録されていない。
  **改善案**: Dynamic Color を採用するか検討し、採用しない場合はその方針を designsystem の README に明記する。

## 作業改善（開発体験）

- **対象**: CLAUDE.md「コード変更時の必須確認」と日常の検証コマンド
  **課題**: 完了報告前に必要なコマンドが 6 種類以上（ユニットテスト・detekt・assertModuleGraph・Android ビルド・desktop jar・desktop 統合テスト）あり、人も AI エージェントも打ち漏らしやすい。実際に CLAUDE.md には「常に実行すること」の注意書きが繰り返し追記されており、手順の多さ自体が抜け漏れの温床になっている。
  **改善案**: ルート `build.gradle.kts` に集約タスク（例: `./gradlew preMergeCheck`）を定義し、必須チェック一式を 1 コマンドに束ねる。CLAUDE.md のチェックリストも「`preMergeCheck` を実行する」に簡素化できる。

- **対象**: `.github/`（PR テンプレート）
  **課題**: `PULL_REQUEST_TEMPLATE.md` がなく、PR 説明の構成（概要・変更点・確認事項）が作成者ごとにばらつく。CLAUDE.md の完了前チェックリストとも連動していない。
  **改善案**: 日本語の PR テンプレートを追加し、「実行した検証コマンド」「スクリーンショットテスト要否」「ドキュメント更新要否」のチェックボックスを設ける。

- **対象**: `docs/improvement-ideas.md` の運用
  **課題**: 記録は蓄積される一方で、着手判断・優先度付けの仕組みがない。項目が増えるほど「書いたが誰も読まない」状態になりやすい。
  **改善案**: 定期的（リリース前など）に棚卸しし、着手するものは GitHub Issue 化して本ファイルからは Issue 番号を添えて削除する運用を README に明記する。

## CI（GitHub Actions）

- **対象**: `.github/workflows/on-pull-request.yml` の `update-module-graph` ジョブ
  **課題**: PR のたびに `GH_PAT` で PR ブランチへ `chore: update module graph images` をコミット・プッシュする構成のため、モジュール構成に変更がない PR でも毎回ジョブが走り、変更があった場合は push が新たな workflow run を誘発して CI が二重に実行される。また fork からの PR では secrets が使えず失敗する。
  **改善案**: モジュール構成ファイル（`settings.gradle.kts` / 各 `build.gradle.kts`）に変更がある場合のみ実行する paths フィルタ（`dorny/paths-filter` 等）を入れる。あるいは main マージ時のみ画像を更新し、PR 中は `assertModuleGraph` の検証だけにする。

- **対象**: `.github/workflows/on-pull-request.yml` 全ジョブ
  **課題**: checkout / setup-java / setup-gradle の 3 ステップが 9 ジョブすべてに重複しており、actions のバージョン更新時に 9 箇所（on-main-merge 等も含めるとさらに多く）を書き換える必要がある。
  **改善案**: `.github/actions/setup`（composite action）に共通セットアップを切り出し、各ジョブは 1 ステップで呼び出す。

- **対象**: `.github/workflows/on-pull-request.yml` の `concurrency`
  **課題**: `cancel-in-progress: false` のため、同一 PR に連続プッシュすると古いコミットの run が完走するまで新しい run が待たされる。PR の CI は最新コミットの結果だけが意味を持つため、古い run の完走は Actions 時間の浪費になる。
  **改善案**: PR トリガーでは `cancel-in-progress: true` にする（`update-module-graph` の push と干渉しないよう、ジョブ分割や group 名の工夫と合わせて検討する）。

- **対象**: `.github/workflows/on-pull-request.yml` の `android-test` ジョブ
  **課題**: ドキュメントのみの変更でも Android エミュレータを起動して `connectedDebugAndroidTest` を実行しており、PR あたり数分〜十数分の Actions 時間を消費する。
  **改善案**: `docs/**`・`*.md` のみの変更ではエミュレータテスト等の重いジョブをスキップする paths フィルタを導入する（branch protection の必須チェックと両立させるため、スキップ時に成功を返すゲートジョブ方式にする）。

- **対象**: `.github/`（依存自動更新）
  **課題**: GitHub Actions は SHA ピン留めされているが `dependabot.yml` / Renovate 設定がなく、actions・Gradle ライブラリの更新が手動任せになっている。CLAUDE.md は「ライブラリは最新安定版を使う」方針だが、それを支える自動化がない。
  **改善案**: Dependabot（`github-actions` + `gradle` エコシステム）または Renovate を導入し、更新 PR を自動作成させる。

## feature:gt7-ps5-narrator

- **対象**: `feature/gt7-ps5-narrator/src/androidMain/kotlin/kurou/kodriver/feature/gt7ps5narrator/AndroidSoundPlayer.kt`
  **課題**: `MediaPlayer` に `setOnErrorListener` を設定していないため、prepare / 再生中にエラーが起きると `onCompletion` が呼ばれず `suspendCancellableCoroutine` が永久に resume されない恐れがある。LMU 側の `AndroidSoundPlayer`（SoundPool 版）で修正した「再生ジョブの永久サスペンド」と同型の潜在バグ。
  **改善案**: `setOnErrorListener` でエラー時にも `cont.resume` する。あわせて `withTimeoutOrNull` による保険を検討する。
