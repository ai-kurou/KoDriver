package kurou.kodriver

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketJsonTest {
    private val session: DefaultWebSocketServerSession = mockk(relaxed = true)

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

            session.sendJsonMessages(messages, String.serializer())

            assertTrue(messageFlowCancelled)
        }

    @Test
    fun `クライアントからのフレームを読み捨てたうえでincomingが閉じると送信中のFlowをキャンセルする`() =
        runTest {
            val incoming = Channel<Frame>()
            every { session.incoming } returns incoming
            every { session.closeReason } returns CompletableDeferred()
            coEvery { session.send(any()) } returns Unit

            var messageFlowCancelled = false
            val messages = cancellableFlow { messageFlowCancelled = true }

            // クライアントから制御フレームが届いても送信側は継続し、その後のクローズで終了する。
            launch {
                // ランデブーチャネルのため、send は受信側が読み捨てるまで中断する。
                incoming.send(Frame.Ping(ByteArray(0)))
                incoming.close()
            }

            session.sendJsonMessages(messages, String.serializer())

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

            session.sendJsonMessages(messages, String.serializer())

            assertTrue(messageFlowCancelled)
        }

    @Test
    fun `非有限値を含むメッセージもエンコードして送信を続ける`() =
        runTest {
            every { session.incoming } returns Channel()
            every { session.closeReason } returns CompletableDeferred()
            val sent = mutableListOf<String>()
            coEvery { session.send(any()) } answers { sent += (firstArg<Frame>() as Frame.Text).readText() }

            session.sendJsonMessages(
                flowOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.5),
                Double.serializer(),
            )

            assertEquals(listOf("NaN", "Infinity", "-Infinity", "1.5"), sent)
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
