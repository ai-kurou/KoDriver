package kurou.kodriver

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class KoDriverServerStartTest {
    private val server: KoDriverServer = mockk()

    @Test
    fun `起動に成功した場合は停止しない`() {
        every { server.start() } returns Unit

        server.startOrStop()

        verify(exactly = 1) { server.start() }
        verify(exactly = 0) { server.stop() }
        confirmVerified(server)
    }

    @Test
    fun `起動に失敗した場合は停止して起動時の例外を投げる`() {
        val startError = IllegalStateException("start")
        every { server.start() } throws startError
        every { server.stop() } returns Unit

        val thrown = assertFailsWith<IllegalStateException> { server.startOrStop() }

        assertSame(startError, thrown)
        verify(exactly = 1) { server.start() }
        verify(exactly = 1) { server.stop() }
        confirmVerified(server)
    }

    @Test
    fun `停止にも失敗した場合は起動時の例外に停止の例外を抑制例外として付ける`() {
        val startError = IllegalStateException("start")
        val stopError = IllegalArgumentException("stop")
        every { server.start() } throws startError
        every { server.stop() } throws stopError

        val thrown = assertFailsWith<IllegalStateException> { server.startOrStop() }

        assertSame(startError, thrown)
        assertContentEquals(arrayOf<Throwable>(stopError), thrown.suppressedExceptions.toTypedArray())
        verify(exactly = 1) { server.start() }
        verify(exactly = 1) { server.stop() }
        confirmVerified(server)
    }

    @Test
    fun `キャンセルはそのまま投げる`() {
        every { server.start() } throws CancellationException("cancel")

        assertFailsWith<CancellationException> { server.startOrStop() }

        verify(exactly = 1) { server.start() }
        verify(exactly = 0) { server.stop() }
        confirmVerified(server)
    }
}
