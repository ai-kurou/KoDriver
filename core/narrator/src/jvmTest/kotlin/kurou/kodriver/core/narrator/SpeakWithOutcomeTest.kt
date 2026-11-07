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

    @Test
    fun `手動が自動を止めて割り込んだ場合は interrupted を返す`() {
        val calls = mutableListOf<String>()

        val result = manualOutcome(calls, currentKey = "auto", currentIsManual = false)

        assertEquals("interrupted", result)
        assertEquals(listOf("stop", "speak:false"), calls)
    }

    @Test
    fun `手動が手動の再生中に追加された場合は queued を返す`() {
        val calls = mutableListOf<String>()

        val result = manualOutcome(calls, currentKey = "manual2", currentIsManual = true)

        assertEquals("queued", result)
        assertEquals(listOf("speak:true"), calls)
    }

    @Test
    fun `手動が再生中なしで読み上げた場合は spoken を返す`() {
        val calls = mutableListOf<String>()

        val result = manualOutcome(calls, currentKey = null, currentIsManual = false)

        assertEquals("spoken", result)
        assertEquals(listOf("speak:false"), calls)
    }

    @Test
    fun `手動の再生中に自動イベントが来てキュー無効なら skipped を返す`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithOutcome(
                eventKey = "high",
                currentKey = { "low" },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                outcomes = OUTCOMES,
                currentIsManual = { true },
            )

        assertEquals("skipped", result)
        assertEquals(emptyList(), calls)
    }

    private fun manualOutcome(
        calls: MutableList<String>,
        currentKey: String?,
        currentIsManual: Boolean,
    ): String =
        speakWithOutcome(
            eventKey = "manual",
            currentKey = { currentKey },
            autoReadoutOrder = emptyList(),
            queueEnabled = false,
            speak = { queue -> calls += "speak:$queue" },
            stop = { calls += "stop" },
            outcomes = OUTCOMES,
            isManual = true,
            currentIsManual = { currentIsManual },
        )

    private companion object {
        val OUTCOMES =
            SpeakOutcomes(
                skipped = "skipped",
                queued = "queued",
                interrupted = "interrupted",
                spoken = "spoken",
            )
    }
}
