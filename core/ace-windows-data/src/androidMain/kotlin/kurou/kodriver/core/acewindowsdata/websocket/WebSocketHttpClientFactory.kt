package kurou.kodriver.core.acewindowsdata.websocket

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import kurou.kodriver.domain.model.WEB_SOCKET_PING_INTERVAL_MS
import java.util.concurrent.TimeUnit

/**
 * WebSocket 用の [HttpClient] を生成する。
 *
 * OkHttp エンジンでは Ktor プラグイン側の pingIntervalMillis は ping 送信に使われないため、
 * OkHttpClient 自体の pingInterval を設定する必要がある。
 */
internal fun createWebSocketHttpClient(pingIntervalMs: Long = WEB_SOCKET_PING_INTERVAL_MS): HttpClient =
    HttpClient(OkHttp) {
        engine {
            config {
                pingInterval(pingIntervalMs, TimeUnit.MILLISECONDS)
            }
        }
        install(WebSockets)
    }
