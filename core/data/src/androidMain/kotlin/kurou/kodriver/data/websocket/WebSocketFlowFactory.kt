package kurou.kodriver.data.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * KoDriver サーバーの WebSocket エンドポイントへ接続し、テキストフレームを [decode] で
 * デシリアライズして emit する再接続付き [Flow] を生成する。
 *
 * デコード失敗（不正な JSON）はフレーム単位でスキップして次のフレームを待つ。接続失敗・切断は
 * [retryDelayMs] 待機後にリトライする。接続拒否・タイムアウト・名前解決失敗・ソケット切断による
 * 想定内のネットワーク利用不可は Sentry へ送信せず、それ以外の失敗とデコード失敗のみ送信する。
 */
internal fun <T> HttpClient.webSocketFlow(
    host: String,
    port: Int,
    path: String,
    retryDelayMs: Long,
    decode: (String) -> T,
    captureException: (Throwable) -> Unit = { Sentry.captureException(it) },
): Flow<T> =
    flow {
        while (true) {
            try {
                webSocket(host = host, port = port, path = path) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            try {
                                emit(decode(frame.readText()))
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: SerializationException) {
                                captureException(e)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!e.isExpectedNetworkFailure()) {
                    captureException(e)
                }
            }
            delay(retryDelayMs)
        }
    }

internal fun Throwable.isExpectedNetworkFailure(): Boolean =
    this is SocketTimeoutException ||
        this is ConnectException ||
        this is UnknownHostException ||
        this is SocketException
