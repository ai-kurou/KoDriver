package kurou.kodriver.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import java.util.concurrent.TimeUnit

// Wi-Fi 瞬断などで TCP がサイレントに切れた（half-open になった）接続を検知するための
// WebSocket ping 間隔。pong がこの間隔内に返らないと OkHttp が接続を失敗として扱い、
// 各リポジトリの connectWithRetry が再接続できるようになる。
internal const val WEB_SOCKET_PING_INTERVAL_MS = 15_000L

/**
 * WebSocket 用の [HttpClient] を生成する。
 *
 * OkHttp エンジンでは Ktor プラグイン側の pingIntervalMillis は ping 送信に使われないため、
 * OkHttpClient 自体の pingInterval を設定する必要がある。
 */
internal fun createWebSocketHttpClient(
    pingIntervalMs: Long = WEB_SOCKET_PING_INTERVAL_MS,
): HttpClient =
    HttpClient(OkHttp) {
    engine {
        config {
            pingInterval(pingIntervalMs, TimeUnit.MILLISECONDS)
        }
    }
    install(WebSockets)
}
