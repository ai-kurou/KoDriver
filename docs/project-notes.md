# プロジェクト補足ルール

CLAUDE.md から分離した、該当領域を触るときだけ必要な詳細ルール。CLAUDE.md には要点と本ファイルへのリンクのみを残す。

### app:androidBenchmark の targetProjectPath は app 間依存の例外

`app:androidBenchmark`（`com.android.test` モジュール）は `targetProjectPath = ":app:androidApp"` で `app:androidApp` を計装対象として参照する。これは Gradle の `implementation`/`api` 依存ではなく AGP 固有のテスト対象指定であり、`moduleGraphAssert` の対象外（`app:.*App` を含む `allowed` パターンにも含まれない）。「app モジュール同士は依存しない」という原則の例外として、この参照のみ容認する（PR #1126）。技術的な背景は `app/androidBenchmark/README.md` を参照。

### 共有メモリ読み取りは Windows 専用
`:core:windows-shared-memory` の `SharedMemoryReader` / `WindowsSharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。`:core:lmu-windows-data` と `:core:ace-windows-data` は JVM source set でこの共通基盤に依存し、それぞれのシム固有の構造体パースを実装する。`:core:lmu-windows-data` と `:core:ace-windows-data` は Android 向け設定永続化と WebSocket クライアントも提供する。

### Ktor サーバー
`:server` は Windows 版デスクトップアプリと同一プロセスで起動し、`0.0.0.0:8080` で待ち受ける。WebSocket エンドポイントは `/ws/<Simulator.id>/<feature>` のパターンに従う（例: `/ws/lmu_windows/flags`）。現時点では認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用すること。`KoDriverServer.start()` は mDNS（`_kodriver._tcp.local.`）でサーバーを LAN 内へ広告し、`:feature:other-server-ip-detail` がそれを検出して接続先 IP の自動入力に使う。エンドポイント仕様・CSWSH対策・mDNS広告の詳細は `server/README.md` を参照。

### LMU Windows共有メモリのパース詳細
`LmuWindowsMapper` のラップタイム系フィールドの扱い（Scoringのプレイヤー車両フォールバック等）、車両クラス名、共有メモリのオフセット情報は `core/lmu-windows-data/README.md` を参照。共有メモリに含まれない情報（天候予報、Virtual Energy消費履歴等）をLMU内蔵のローカルREST APIから補えないかの調査メモは [`docs/lmu-windows-rest-api.md`](docs/lmu-windows-rest-api.md) を参照（ブレーキ摩耗は統合済みで、Android 版へは KoDriver サーバーの WebSocket で配信する）。

### ユーザー設定のデフォルト値

共通のデフォルト値は従来どおり `:core:domain` の `domain/model/*Defaults.kt` に定義する。シミュレーター固有のデフォルト値は、そのシミュレーターのドメインモジュール（`:core:lmu-windows-domain` / `:core:ace-windows-domain` / `:core:gt7-ps5-domain`）の `domain/<sim>/model/*Defaults.kt` に定義する（`:core:*-data` と feature はそのシミュレーターのドメインモジュールに依存して参照するため重複定義にならない）。

すべてのシミュレーター固有の読み上げキーと既定値（`*ReadoutItemKey.kt` / `*ReadoutDefaults.kt`）は各シミュレーターのドメインモジュールに置き、共通の `ReadoutItemKeyRegistry` が登録済み spec を通じて集約する。新しいキーを追加する際は、その simulator モジュールの `*ReadoutItemKeys.kt` のキー一覧にも追加すること。

feature の `companion object` や `Pane.kt` に仕様値を置かず、共通または該当するシミュレーターのドメインモジュールで定義した定数を参照する。Preview やテストデータだけで完結する表示用の値は feature 内に置いてよい。

定数名は `XXX_DEFAULT` のように末尾に `DEFAULT` を付けて統一すること（例: `LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT`）。

ただし `*Defaults.kt` には、デフォルト値と組で使う仕様値（範囲の上限・下限の `_MIN` / `_MAX`、番兵値 `_UNSPECIFIED` / `_UNKNOWN_KEY`、代替値 `_ALTERNATE` など）も置いてよい。`_DEFAULT` を付けるのはデフォルト値だけとし、これらの仕様値には付けない。
