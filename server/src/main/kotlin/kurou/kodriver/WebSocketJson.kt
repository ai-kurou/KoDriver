package kurou.kodriver

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal val serverJson =
    Json {
        encodeDefaults = true
    }

internal suspend fun <T> DefaultWebSocketServerSession.sendJsonMessages(
    messages: Flow<T>,
    serializer: KSerializer<T>,
) = coroutineScope {
    val outgoingJob =
        launch {
            messages.collect { message ->
                send(Frame.Text(serverJson.encodeToString(serializer, message)))
            }
        }
    val incomingJob =
        launch {
            for (ignored in incoming) {
                // クローズ・制御フレームを読み捨てる。こうすることでクライアント側からのクローズが
                // 送信側へ速やかに伝わり、送信ジョブがキャンセルされる。
            }
        }
    val closeReasonJob =
        launch {
            closeReason.await()
        }

    select {
        outgoingJob.onJoin {}
        incomingJob.onJoin {}
        closeReasonJob.onJoin {}
    }
    outgoingJob.cancelAndJoin()
    incomingJob.cancelAndJoin()
    closeReasonJob.cancelAndJoin()
}
