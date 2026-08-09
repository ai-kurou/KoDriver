package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

/**
 * [feature] / [simulator] から求まる WebSocket パスへ、[flow] が返す Flow を JSON として配信する。
 *
 * 接続ごとに独立した Flow を生成する（cold flow）ため、[flow] は毎回呼び出す関数として受け取る。
 * ここで固定の `Flow<T>` を受け取ると、複数クライアントが同一の Flow インスタンスを共有してしまう。
 */
internal inline fun <reified T> Route.telemetryWebSocket(
    feature: KoDriverServerFeature,
    simulator: Simulator,
    crossinline flow: () -> Flow<T>,
) {
    webSocket(feature.webSocketPath(simulator)) {
        flow()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}
