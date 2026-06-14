# server

Windows 版デスクトップアプリと同一プロセスで起動する Ktor サーバー。デスクトップアプリの起動時に `0.0.0.0:8080` で待ち受けを開始し、終了時に停止する。

## Flag WebSocket

`ws://<Windows PC のローカル IP>:8080/ws/flags` へ接続すると、共有メモリから取得したフラッグ情報を JSON で受信できる。同一内容の連続値は送信しない。

配信形式には独自 DTO を設けず、`:core:domain` の `RaceFlagsData` をそのままシリアライズする。

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

LAN 内の別端末から接続できない場合は、Windows ファイアウォールで TCP 8080 番ポートの受信が許可されているか確認すること。現時点では認証と TLS に対応していないため、信頼できる LAN 内でのみ使用する。

## 単体起動

```bash
./gradlew :server:run
```

単体起動では `FlagRepository` に空の実装を使用するため、フラッグ情報は配信されない。共有メモリのデータを配信する場合は Windows 版デスクトップアプリを起動する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../docs/graphs/server.svg)
<!-- MODULE-GRAPH-END -->
