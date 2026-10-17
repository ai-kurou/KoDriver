package kurou.kodriver.core.texttospeechdata.windows

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ResidentSpeechSessionTest {
    private val process = FakeResidentSpeechProcess()
    private val session = ResidentSpeechSession(process, Any())

    @AfterTest
    fun tearDown() {
        process.finishAndJoinReader()
    }

    @Test
    fun `開いているセッションの利用可否はプロセスの生存状態に従う`() {
        assertTrue(session.isUsable)
        assertNull(session.lastOutstanding())

        process.alive = false

        assertFalse(session.isUsable)
    }

    @Test
    fun `要求行をASCIIの改行付きで送信して毎回flushする`() {
        val first = session.send("SPEAK 80 0 0 dm9pY2U= dGV4dA==")
        val second = session.send("SPEAK 50 1 0 - bmV4dA==")

        assertEquals("SPEAK 80 0 0 dm9pY2U= dGV4dA==\nSPEAK 50 1 0 - bmV4dA==\n", process.requests)
        assertEquals(2, process.flushCount)
        assertFalse(first.isDone)
        assertFalse(second.isDone)
        assertSame(second, session.lastOutstanding())
    }

    @Test
    fun `DONEごとに先頭の要求だけを送信順に完了する`() {
        val first = session.send("first")
        val second = session.send("second")

        process.respond("DONE")

        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))
        assertFalse(second.isDone)
        assertSame(second, session.lastOutstanding())

        process.respond("DONE")

        assertEquals(Unit, second.get(5, TimeUnit.SECONDS))
        assertNull(session.lastOutstanding())
        assertTrue(session.isUsable)
    }

    @Test
    fun `DONEと一致しない応答行は要求を完了しない`() {
        val future = session.send("request")

        process.respond("ログ出力")
        process.respond("done")
        process.respond(" DONE")
        process.awaitResponses()

        assertFalse(future.isDone)
        assertSame(future, session.lastOutstanding())
    }

    @Test
    fun `要求がないときのDONEは後続の要求を完了しない`() {
        process.respond("DONE")
        process.awaitResponses()

        val future = session.send("request")

        assertFalse(future.isDone)
        assertSame(future, session.lastOutstanding())
        assertTrue(session.isUsable)
    }

    @Test
    fun `EOFですべての要求を完了し閉じた後は書き込まない`() {
        val first = session.send("first")
        val second = session.send("second")

        process.finish()
        process.awaitDestroyed()

        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))
        assertEquals(Unit, second.get(5, TimeUnit.SECONDS))
        assertNull(session.lastOutstanding())
        process.alive = true
        assertFalse(session.isUsable)
        assertEquals(Unit, session.send("ignored").get(5, TimeUnit.SECONDS))
        session.stop()
        session.stopIfCurrent(second)
        assertEquals("first\nsecond\n", process.requests)
        assertEquals(2, process.flushCount)
    }

    @Test
    fun `読み取りのIOExceptionでも未完了要求を正常完了して破棄する`() {
        val first = session.send("first")
        val second = session.send("second")

        process.failRead()
        process.awaitDestroyed()

        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))
        assertEquals(Unit, second.get(5, TimeUnit.SECONDS))
        assertFalse(session.isUsable)
        assertNull(session.lastOutstanding())
    }

    @Test
    fun `書き込み失敗で既存要求と送信中の要求を完了して閉じる`() {
        val first = session.send("first")
        process.failWrite = true

        val second = session.send("second")
        process.awaitDestroyed()

        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))
        assertEquals(Unit, second.get(5, TimeUnit.SECONDS))
        assertFalse(session.isUsable)
        assertNull(session.lastOutstanding())
        assertEquals("first\n", process.requests)
        assertEquals(1, process.flushCount)
    }

    @Test
    fun `flush失敗でも未完了要求を完了して閉じる`() {
        val first = session.send("first")
        process.failFlush = true

        val second = session.send("second")
        process.awaitDestroyed()

        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))
        assertEquals(Unit, second.get(5, TimeUnit.SECONDS))
        assertFalse(session.isUsable)
        assertNull(session.lastOutstanding())
        assertEquals("first\nsecond\n", process.requests)
        assertEquals(1, process.flushCount)
    }

    @Test
    fun `未完了要求がなければ停止要求を送らない`() {
        session.stop()
        session.stopIfCurrent(CompletableFuture())

        assertEquals("", process.requests)
        assertEquals(0, process.flushCount)
    }

    @Test
    fun `停止要求だけではFutureを完了せずDONEを待つ`() {
        val future = session.send("request")

        session.stop()

        assertEquals("request\nSTOP\n", process.requests)
        assertEquals(2, process.flushCount)
        assertFalse(future.isDone)

        process.respond("DONE")

        assertEquals(Unit, future.get(5, TimeUnit.SECONDS))
    }

    @Test
    fun `唯一の未完了要求を指定した場合だけ停止する`() {
        val future = session.send("request")

        session.stopIfCurrent(CompletableFuture())
        assertEquals("request\n", process.requests)

        session.stopIfCurrent(future)

        assertEquals("request\nSTOP\n", process.requests)
        assertFalse(future.isDone)
    }

    @Test
    fun `複数要求の先頭と末尾を指定しても停止せず最後の一件になってから停止する`() {
        val first = session.send("first")
        val second = session.send("second")

        session.stopIfCurrent(first)
        session.stopIfCurrent(second)

        assertEquals("first\nsecond\n", process.requests)
        process.respond("DONE")
        assertEquals(Unit, first.get(5, TimeUnit.SECONDS))

        session.stopIfCurrent(first)
        assertEquals("first\nsecond\n", process.requests)

        session.stopIfCurrent(second)

        assertEquals("first\nsecond\nSTOP\n", process.requests)
        assertFalse(second.isDone)
    }
}
