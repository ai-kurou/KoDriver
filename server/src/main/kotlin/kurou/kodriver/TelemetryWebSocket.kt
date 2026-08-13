package kurou.kodriver

import io.ktor.http.HttpHeaders
import io.ktor.server.request.header
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

/**
 * [feature] / [simulator] から求まる WebSocket パスへ、[flow] が返す Flow を JSON として配信する。
 *
 * 接続ごとに独立した Flow を生成する（cold flow）ため、[flow] は毎回呼び出す関数として受け取る。
 * ここで固定の `Flow<T>` を受け取ると、複数クライアントが同一の Flow インスタンスを共有してしまう。
 *
 * WebSocket はブラウザの CORS（Same-Origin Policy）の対象外のため、`Origin` ヘッダを検証しないと
 * LAN 内の別端末で開かれた悪意あるページの JavaScript から接続され、走行中のテレメトリ情報を
 * 読み取られる恐れがある（CSWSH: Cross-Site WebSocket Hijacking）。ブラウザ以外のクライアント
 * （Android アプリ等）は通常 `Origin` ヘッダを送らないため、`Origin` ヘッダを持つ接続のみ拒否する。
 */
internal inline fun <reified T> Route.telemetryWebSocket(
    feature: KoDriverServerFeature,
    simulator: Simulator,
    crossinline flow: () -> Flow<T>,
) {
    webSocket(feature.webSocketPath(simulator)) {
        if (call.request.header(HttpHeaders.Origin) != null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Origin header is not allowed"))
            return@webSocket
        }
        flow()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}
