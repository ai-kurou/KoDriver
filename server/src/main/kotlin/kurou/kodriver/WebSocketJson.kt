package kurou.kodriver

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal val serverJson =
    Json {
        encodeDefaults = true
    }

internal suspend inline fun <reified T> DefaultWebSocketServerSession.sendJsonMessages(messages: Flow<T>) =
    coroutineScope {
        val outgoingJob =
            launch {
                messages.collect { message ->
                    send(Frame.Text(serverJson.encodeToString(message)))
                }
            }
        val incomingJob =
            launch {
                for (ignored in incoming) {
                    // Consume close/control frames so client initiated close cancels the sender promptly.
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
