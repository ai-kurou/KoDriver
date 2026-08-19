package kurou.kodriver

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class WebSocketJsonTest {
    @RelaxedMockK
    private lateinit var session: DefaultWebSocketServerSession

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `incomingチャネルが閉じるとクライアント切断とみなし送信中のFlowをキャンセルする`() =
        runTest {
            val incoming = Channel<Frame>()
            every { session.incoming } returns incoming
            every { session.closeReason } returns CompletableDeferred()
            coEvery { session.send(any()) } returns Unit

            var messageFlowCancelled = false
            val messages = cancellableFlow { messageFlowCancelled = true }

            // sendJsonMessages が送信中Flowの購読を開始した後にクライアント切断が起きる実際の
            // タイミングに近づけるため、並行するコルーチンからincomingを閉じる。
            launch {
                yield()
                incoming.close()
            }

            session.sendJsonMessages(messages)

            assertTrue(messageFlowCancelled)
        }

    @Test
    fun `closeReasonが完了すると送信中のFlowをキャンセルする`() =
        runTest {
            val closeReason = CompletableDeferred<CloseReason?>()
            every { session.incoming } returns Channel()
            every { session.closeReason } returns closeReason
            coEvery { session.send(any()) } returns Unit

            var messageFlowCancelled = false
            val messages = cancellableFlow { messageFlowCancelled = true }

            // sendJsonMessages が送信中Flowの購読を開始した後にサーバー側クローズが起きる実際の
            // タイミングに近づけるため、並行するコルーチンからcloseReasonを完了させる。
            launch {
                yield()
                closeReason.complete(CloseReason(CloseReason.Codes.NORMAL, ""))
            }

            session.sendJsonMessages(messages)

            assertTrue(messageFlowCancelled)
        }

    private fun cancellableFlow(onCancelled: () -> Unit): Flow<String> =
        flow {
            try {
                awaitCancellation()
            } finally {
                onCancelled()
            }
        }
}
