package kurou.kodriver

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import org.koin.core.Koin

/**
 * `:server` が配信する購読用 UseCase をシミュレーター別にまとめたバンドル。
 *
 * [KoDriverServer] / [Application.module] のコンストラクタ・関数パラメータ数を detekt の
 * LongParameterList 閾値内に収めるため、シミュレーター別の UseCase バンドルをこのデータクラスで受け渡す。
 */
data class KoDriverServerUseCases(
    val lmuWindows: LmuWindowsServerUseCases,
    val aceWindows: AceWindowsServerUseCases,
)

fun main() {
    KoDriverServer(
        useCases =
            KoDriverServerUseCases(
                lmuWindows = emptyLmuWindowsServerUseCases(),
                aceWindows = emptyAceWindowsServerUseCases(),
            ),
    ).start(wait = true)
}

/**
 * KoDriver の WebSocket サーバー。
 *
 * Desktop アプリ内で起動し、LMU / ACE など Windows 側でしか読めない走行情報を
 * LAN 内の Android アプリへ配信する。既定では `0.0.0.0:8080` で待ち受け、
 * 起動時に mDNS で KoDriver サービスを広告する。
 */
class KoDriverServer(
    useCases: KoDriverServerUseCases,
    private val port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    internal var serviceAdvertiser: KoDriverServiceAdvertiser = KoDriverServiceAdvertiser()
    private val server =
        embeddedServer(
            factory = Netty,
            port = port,
            host = host,
            module = {
                module(useCases)
            },
        )

    /**
     * Ktor サーバーを起動し、同じポートで mDNS 広告を開始する。
     *
     * [wait] を true にすると Ktor のサーバースレッドでブロックするため、
     * CLI エントリーポイントから単体起動する場合に使う。
     */
    fun start(wait: Boolean = false) {
        server.start(wait = wait)
        serviceAdvertiser.start(port)
    }

    /** mDNS 広告を停止し、Ktor サーバーを即時停止する。 */
    fun stop() {
        serviceAdvertiser.stop()
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }

    private companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOST = "0.0.0.0"
    }
}

/**
 * アプリ本体の Koin コンテナから Repository を解決して [KoDriverServer] を生成する。
 *
 * Desktop アプリ内でサーバーを起動する通常経路。単体起動用の [main] は空 Repository を使うため、
 * 実走行データを配信したい場合はこちらを使う。
 */
fun createKoDriverServer(koin: Koin): KoDriverServer =
    KoDriverServer(
        useCases =
            KoDriverServerUseCases(
                lmuWindows = lmuWindowsServerUseCasesFrom(koin),
                aceWindows = aceWindowsServerUseCasesFrom(koin),
            ),
    )

private const val WEB_SOCKET_PING_PERIOD_MS = 15_000L
private const val WEB_SOCKET_TIMEOUT_MS = 15_000L
private const val WEB_SOCKET_MAX_FRAME_SIZE_BYTES = 64L * 1024

/**
 * KoDriver サーバーの Ktor module。
 *
 * `/version` はアプリバージョンを JSON で返し、`/ws/{simulator}/{feature}` 系の
 * WebSocket エンドポイントは [KoDriverServerUseCases] の Flow を JSON メッセージとして配信する。
 */
fun Application.module(useCases: KoDriverServerUseCases) {
    install(WebSockets) {
        // クライアントがサイレントに消えた（half-open になった）接続を検知して
        // セッションを解放するため、ping/pong を有効にする。
        pingPeriodMillis = WEB_SOCKET_PING_PERIOD_MS
        timeoutMillis = WEB_SOCKET_TIMEOUT_MS
        // LAN内の端末から巨大なフレームを送られた場合の受信バッファ肥大化を防ぐ。
        maxFrameSize = WEB_SOCKET_MAX_FRAME_SIZE_BYTES
    }
    routing {
        get("/") {
            call.respondText(
                """{"status":"ok"}""",
                ContentType.Application.Json,
            )
        }
        get("/version") {
            call.respondText(
                """{"version":"${BuildConfig.APP_VERSION}"}""",
                ContentType.Application.Json,
            )
        }
        lmuWindowsRoutes(useCases)
        aceWindowsRoutes(useCases)
    }
}
