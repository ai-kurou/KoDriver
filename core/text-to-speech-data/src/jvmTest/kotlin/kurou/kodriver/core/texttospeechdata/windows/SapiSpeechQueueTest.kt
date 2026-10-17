package kurou.kodriver.core.texttospeechdata.windows

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class SapiSpeechQueueTest {
    @Test
    fun `キュー発話は前の完了後に送信する`() {
        SapiSpeechFixture().use { fixture ->
            val first = fixture.speak("先行")
            val firstRequest = fixture.process.awaitRequest()
            val firstFuture = fixture.awaitCompletion()
            val queued = fixture.speak("後続", queue = true)
            assertSame(firstFuture, fixture.awaitCompletion())
            assertEquals(firstRequest + "\n", fixture.process.requests)
            fixture.process.respond("DONE")
            first.awaitSuccess()
            assertEquals(
                buildSpeakRequest("後続", 80, "voice", speedToSapiRate(1.5f), 1.2f),
                fixture.process.awaitRequest(),
            )
            fixture.awaitCompletion()
            fixture.process.respond("DONE")
            queued.awaitSuccess()
        }
    }

    @Test
    fun `キュー待機中でも新しい非キュー発話が割り込める`() {
        SapiSpeechFixture().use { fixture ->
            val first = fixture.speak("先行")
            val firstRequest = fixture.process.awaitRequest()
            val firstFuture = fixture.awaitCompletion()
            val queued = fixture.speak("取り消される後続", queue = true)
            assertSame(firstFuture, fixture.awaitCompletion())
            val replacement = fixture.speak("割り込み")
            assertEquals("STOP", fixture.process.awaitRequest())
            val replacementRequest = fixture.process.awaitRequest()
            assertEquals(buildSpeakRequest("割り込み", 80, "voice", speedToSapiRate(1.5f), 1.2f), replacementRequest)
            fixture.awaitCompletion()
            fixture.process.respond("DONE")
            fixture.process.respond("DONE")
            first.awaitSuccess()
            queued.awaitSuccess()
            replacement.awaitSuccess()
            assertEquals(firstRequest + "\nSTOP\n" + replacementRequest + "\n", fixture.process.requests)
        }
    }

    @Test
    fun `現在の発話スレッドの割り込みは停止要求を送り例外を再送出する`() {
        SapiSpeechFixture().use { fixture ->
            val call = fixture.speak("読み上げ")
            fixture.process.awaitRequest()
            fixture.awaitCompletion()
            call.interrupt()
            assertIs<InterruptedException>(call.awaitFailure())
            assertEquals("STOP", fixture.process.awaitRequest())
        }
    }

    @Test
    fun `古い発話のスレッド割り込みでは新しい発話を停止しない`() {
        SapiSpeechFixture().use { fixture ->
            val first = fixture.speak("先行")
            fixture.process.awaitRequest()
            fixture.awaitCompletion()
            val replacement = fixture.speak("新しい発話")
            assertEquals("STOP", fixture.process.awaitRequest())
            fixture.process.awaitRequest()
            fixture.awaitCompletion()
            val requests = fixture.process.requests
            first.interrupt()
            assertIs<InterruptedException>(first.awaitFailure())
            assertEquals(requests, fixture.process.requests)
            fixture.process.respond("DONE")
            fixture.process.respond("DONE")
            replacement.awaitSuccess()
        }
    }
}
