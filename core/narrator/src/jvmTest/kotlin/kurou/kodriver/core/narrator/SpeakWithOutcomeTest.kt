package kurou.kodriver.core.narrator

import kotlin.test.Test
import kotlin.test.assertEquals

class SpeakWithOutcomeTest {
    @Test
    fun `キュー再生が有効なら queued を返す`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithOutcome(
                eventKey = "low",
                currentKey = { "high" },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = true,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                outcomes =
                    SpeakOutcomes(
                        skipped = "skipped",
                        queued = "queued",
                        interrupted = "interrupted",
                        spoken = "spoken",
                    ),
            )

        assertEquals("queued", result)
        assertEquals(listOf("speak:true"), calls)
    }

    @Test
    fun `優先度に負けた場合は skipped を返す`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithOutcome(
                eventKey = "low",
                currentKey = { "high" },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                outcomes =
                    SpeakOutcomes(
                        skipped = "skipped",
                        queued = "queued",
                        interrupted = "interrupted",
                        spoken = "spoken",
                    ),
            )

        assertEquals("skipped", result)
        assertEquals(emptyList(), calls)
    }

    @Test
    fun `優先度に勝った場合は停止して interrupted を返す`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithOutcome(
                eventKey = "high",
                currentKey = { "low" },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                outcomes =
                    SpeakOutcomes(
                        skipped = "skipped",
                        queued = "queued",
                        interrupted = "interrupted",
                        spoken = "spoken",
                    ),
            )

        assertEquals("interrupted", result)
        assertEquals(listOf("stop", "speak:false"), calls)
    }

    @Test
    fun `再生中のイベントがなければ停止せず spoken を返す`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithOutcome(
                eventKey = "high",
                currentKey = { null },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                outcomes =
                    SpeakOutcomes(
                        skipped = "skipped",
                        queued = "queued",
                        interrupted = "interrupted",
                        spoken = "spoken",
                    ),
            )

        assertEquals("spoken", result)
        assertEquals(listOf("speak:false"), calls)
    }
}
