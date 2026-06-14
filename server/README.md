# server

Windows版デスクトップアプリと同一プロセスで起動するKtorサーバー。

## Flag WebSocket

`ws://<Windows PCのローカルIP>:8080/ws/flags` へ接続すると、共有メモリから取得したフラッグ情報をJSONで受信できる。同一内容の連続値は送信しない。

```json
{
  "gamePhase": "GREEN_FLAG",
  "yellowFlagState": "NONE",
  "sectorFlags": ["CLEAR", "YELLOW", "CLEAR"],
  "startLight": 4,
  "numRedLights": 2,
  "playerFlag": "BLUE",
  "playerUnderYellow": true,
  "playerCountLapFlag": "COUNT_LAP_AND_TIME"
}
```

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../docs/graphs/server.svg)
<!-- MODULE-GRAPH-END -->
