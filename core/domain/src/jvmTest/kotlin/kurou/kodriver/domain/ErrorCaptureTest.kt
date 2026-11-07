package kurou.kodriver.domain

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorCaptureTest {
    @Test
    fun `captureErrorは例外をSentryへ送信する`() {
        val exception = RuntimeException("test")
        mockkStatic(Sentry::class)
        every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
        try {
            captureError(exception)

            verify(exactly = 1) { Sentry.captureException(exception) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }

    @Test
    fun `captureErrorTypeOnlyは例外メッセージを含めず種類だけをSentryへ送信する`() {
        val captured = slot<Throwable>()
        mockkStatic(Sentry::class)
        every { Sentry.captureException(capture(captured)) } returns SentryId.EMPTY_ID
        try {
            captureErrorTypeOnly(RuntimeException("USB Microphone (端末名)"))

            assertEquals("RuntimeException", captured.captured.message)
            verify(exactly = 1) { Sentry.captureException(captured.captured) }
            confirmVerified(Sentry::class)
        } finally {
            unmockkStatic(Sentry::class)
        }
    }
}
