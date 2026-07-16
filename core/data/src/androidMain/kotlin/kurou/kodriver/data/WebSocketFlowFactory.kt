package kurou.kodriver.data

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

/**
 * KoDriver サーバーの WebSocket エンドポイントへ接続し、テキストフレームを [decode] で
 * デシリアライズして emit する再接続付き [Flow] を生成する。
 *
 * デコード失敗（不正な JSON）はフレーム単位でスキップして次のフレームを待つ。接続失敗・切断は
 * [retryDelayMs] 待機後にリトライする。いずれの失敗も Sentry へ送信し、原因調査を可能にする。
 */
internal fun <T> HttpClient.webSocketFlow(
    host: String,
    port: Int,
    path: String,
    retryDelayMs: Long,
    decode: (String) -> T,
): Flow<T> = flow {
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
                            Sentry.captureException(e)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
        delay(retryDelayMs)
    }
}
