package kurou.kodriver.core.acewindowsdata.websocket

import kotlinx.serialization.json.Json

/**
 * KoDriver サーバーから受信するフレームのデコード設定。
 * テレメトリには NaN・無限大が含まれうるため、サーバーの設定に合わせて受け付ける。
 */
internal val webSocketJson =
    Json {
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
    }
