# ace-windows-connection

ACE (Assetto Corsa EVO) Windows版の接続状態監視機能。`feature:lmu-windows-connection` /
`feature:gt7-ps5-connection` に相当する ACE 版。

`AceWindowsConnectionViewModel` は `ObserveSelectedSimulatorUseCase` で選択中シミュレーターを監視し、
`Simulator.AceWindows` が選択されている間だけ `ObserveAceWindowsConnectionUseCase`
（`CheckAceWindowsConnectionUseCase` による1秒間隔の接続確認と `ObserveAceWindowsFuelUseCase` の
燃料テレメトリを合成）を購読して `AceWindowsConnectionUiState`（接続状態・燃料残量%）を公開する。

接続判定は `:core:ace-windows-data` の `AceWindowsGraphicsSharedMemorySource.isConnected()` が
共有メモリの `packetId`（フレームごとに増加するカウンタ）の変化を監視し、一定時間変化がなければ
未接続とみなす（LMU の `mCurrentET` によるstale検知と同じ方式）。Android では共有メモリを
直接読めないため、`AceWindowsFuelRepository` の Android 実装（`WebSocketAceWindowsFuelRepository`）は
KoDriver サーバー経由の WebSocket で `fuelStream()` を配信する一方、`isConnected()` は常に
`false` を返す（接続確認バナーは別途 `AceServerBannerConnectionChecker` で疎通確認するため。
LMU の Android 実装と同じ扱い）。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-ace-windows-connection.svg)
<!-- MODULE-GRAPH-END -->
