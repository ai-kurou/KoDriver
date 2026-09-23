# server

デスクトップアプリ内で Windows 版と同一プロセスで起動する Ktor WebSocket サーバー。`0.0.0.0:8080` で待ち受ける。

## WebSocket エンドポイント

WebSocket エンドポイントは `/ws/<Simulator.id>/<feature>` のパターンに従う（例: `/ws/lmu_windows/flags`）。パスは `KoDriverServerFeature.webSocketPath(simulator)` で組み立てられ、サーバー側の登録（`Application.kt` の `telemetryWebSocket(...)`）とクライアント側の `WebSocket*Repository` で共有される。各エンドポイントは UseCase 経由で Repository を購読し、送信型を JSON として送信する。

### LMU（`/ws/lmu_windows/...`）

| パス | データソース（UseCase） | 送信型 |
| --- | --- | --- |
| `/ws/lmu_windows/flags` | `ObserveLmuWindowsRaceFlagsUseCase`（`LmuWindowsFlagRepository`） | `LmuWindowsRaceFlagsData` |
| `/ws/lmu_windows/vehicle_approach` | `ObserveLmuWindowsVehicleApproachUseCase`（`LmuWindowsVehicleApproachRepository`） | `LmuWindowsVehicleApproachData` |
| `/ws/lmu_windows/damage` | `ObserveLmuWindowsVehicleDamageUseCase`（`LmuWindowsVehicleDamageRepository`） | `LmuWindowsVehicleDamageData` |
| `/ws/lmu_windows/tyre_carcass_temperature` | `ObserveLmuWindowsTyreCarcassTemperatureUseCase`（`LmuWindowsTyreCarcassTemperatureRepository`） | `LmuWindowsTyreCarcassTemperatureData` |
| `/ws/lmu_windows/vehicle_class` | `ObserveLmuWindowsVehicleClassUseCase`（`LmuWindowsVehicleClassRepository`） | `LmuWindowsVehicleClassData` |
| `/ws/lmu_windows/tyre_wear` | `ObserveLmuWindowsTyreWearUseCase`（`LmuWindowsTyreWearRepository`） | `LmuWindowsTyreWearData` |
| `/ws/lmu_windows/my_best_lap` | `ObserveLmuWindowsUseCase`（`LmuWindowsRepository`）の `timing` | `LmuWindowsTimingData` |
| `/ws/lmu_windows/virtual_energy` | `ObserveLmuWindowsVirtualEnergyUseCase`（`LmuWindowsVirtualEnergyRepository`） | `LmuWindowsVirtualEnergyData` |
| `/ws/lmu_windows/pit_status` | `ObserveLmuWindowsPitStatusUseCase`（`LmuWindowsPitStatusRepository`） | `LmuWindowsPitStatusData` |
| `/ws/lmu_windows/tyre_detached` | `ObserveLmuWindowsTyreDetachedUseCase`（`LmuWindowsTyreDetachedRepository`） | `LmuWindowsTyreDetachedData` |

### ACE（`/ws/ace_windows/...`）

| パス | データソース（UseCase） | 送信型 |
| --- | --- | --- |
| `/ws/ace_windows/fuel` | `ObserveAceWindowsFuelUseCase`（`AceWindowsFuelRepository`） | `AceWindowsFuelData` |
| `/ws/ace_windows/flags` | `ObserveAceWindowsFlagUseCase`（`AceWindowsFlagRepository`） | `AceWindowsFlagData` |
| `/ws/ace_windows/status` | `ObserveAceWindowsStatusUseCase`（`AceWindowsStatusRepository`） | `AceWindowsStatusData` |
| `/ws/ace_windows/tyre_carcass_temperature` | `ObserveAceWindowsTyreCarcassTemperatureUseCase`（`AceWindowsTyreCarcassTemperatureRepository`） | `AceWindowsTyreCarcassTemperatureData` |
| `/ws/ace_windows/vehicle_approach` | `ObserveAceWindowsVehicleApproachUseCase`（`AceWindowsVehicleApproachRepository`） | `AceWindowsVehicleApproachData` |
| `/ws/ace_windows/my_best_lap` | `ObserveAceWindowsBestLapTimeUseCase`（`AceWindowsBestLapTimeRepository`） | `AceWindowsBestLapTimeData` |
| `/ws/ace_windows/remaining_fuel_laps` | `ObserveAceWindowsRemainingFuelLapsUseCase`（`AceWindowsRemainingFuelLapsRepository`） | `AceWindowsRemainingFuelLapsData` |

全エンドポイント共通で、同一内容の連続値は送信しない。エンドポイントを追加・変更したときは、この表も同じ PR で更新すること。

LAN 内の Android 端末からは `ws://<Windows PC のローカル IP>:8080/ws/<Simulator.id>/flags` 等へ接続する。外部端末から接続するには Windows ファイアウォールで TCP 8080 番ポートの受信を許可する必要がある場合がある。現時点では認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用すること。

`src/main/kotlin/kurou/kodriver/TelemetryWebSocket.kt` の `telemetryWebSocket` は、WebSocket 接続に `Origin` ヘッダが含まれる場合は `CloseReason.Codes.VIOLATED_POLICY` で切断する。WebSocket はブラウザの CORS（Same-Origin Policy）の対象外のため、LAN 内の別端末で開かれた悪意あるページの JavaScript から接続されてテレメトリ情報を読み取られる恐れがある（CSWSH: Cross-Site WebSocket Hijacking）。Android アプリ等のネイティブクライアントは通常 `Origin` ヘッダを送らないため接続に影響しない。

## mDNS 広告（サーバー自動検出）

`KoDriverServer.start()` は Ktor サーバー起動と同時に `KoDriverServiceAdvertiser`（`javax.jmdns.JmDNS` によるラッパー）でサービスタイプ `_kodriver._tcp.local.`（`core:domain` の `MdnsConstants.KO_DRIVER_SERVICE_TYPE` として `:server` と `:feature:other-server-ip-detail`（JVM 実装）から共有）を LAN 内へ mDNS 広告する。インスタンス名には OS のホスト名（PC所有者名などの個人情報を含みうる）を使わず、`KoDriver-<ランダムな英数字4桁>`（例: `KoDriver-A1B2`）という固定プレフィックス＋ランダムサフィックスの識別子を使用する。ランダムサフィックスは `KoDriverServiceAdvertiser` のインスタンス生成時に一度だけ生成し、サーバーが再起動するまで同一の値を維持するため、複数台の Windows PC が同一 LAN 上で起動している場合でも Android 側が識別子で区別できる。`start()` は呼び出しごとに既存の `JmDNS` インスタンスを `stop()` してから新規生成するため、多重起動してもソケットはリークしない。mDNS の登録・解除に失敗しても（`IOException`）ログ出力のみで Ktor サーバー自体の起動・停止は妨げない。

`:feature:other-server-ip-detail` の接続先 IP 入力画面（detailPane）は、画面が表示されている間だけ `WindowsServerDiscovery`（プラットフォーム実装: JVM は JmDNS、Android は `NsdManager`）で上記の mDNS 広告を検出する。`OtherServerIpDetailViewModel` は検出結果を `SharingStarted.WhileSubscribed` で `uiState` の購読に連動させており、アプリ起動時ではなく detailPane 表示中のみ検出が動作する。検出できた場合は広告の識別子・IP アドレスを選べるダイアログを自動表示し、「選択する」で選択した IP アドレスを入力欄へ自動入力する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../docs/graphs/server.svg)
<!-- MODULE-GRAPH-END -->
