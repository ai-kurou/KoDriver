package kurou.kodriver.core.narrator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NarratorPriorityTest {
    @Test
    fun `キュー再生が有効なら優先度判定をせずキュー再生する`() {
        var spokenQueue: Boolean? = null
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "new",
                currentKey = { "current" },
                readoutOrder = listOf("new", "current"),
                queueEnabled = true,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(true, spokenQueue)
        assertFalse(stopped)
    }

    @Test
    fun `再生中のイベントがない場合はそのまま読み上げる`() {
        var spokenQueue: Boolean? = null
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "new",
                currentKey = { null },
                readoutOrder = listOf("new"),
                queueEnabled = false,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(false, spokenQueue)
        assertFalse(stopped)
    }

    @Test
    fun `再生中より優先度の低いイベントは読み上げない`() {
        var spoken = false
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "low",
                currentKey = { "high" },
                readoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { spoken = true },
                stop = { stopped = true },
            )

        assertFalse(result)
        assertFalse(spoken)
        assertFalse(stopped)
    }

    @Test
    fun `再生中より優先度の高いイベントは停止してから読み上げる`() {
        var spokenQueue: Boolean? = null
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "high",
                currentKey = { "low" },
                readoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(false, spokenQueue)
        assertTrue(stopped)
    }

    @Test
    fun `readoutOrderに含まれないキーは最も優先度が低いものとして扱う`() {
        var spoken = false
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "unknown",
                currentKey = { "current" },
                readoutOrder = listOf("current"),
                queueEnabled = false,
                speak = { spoken = true },
                stop = { stopped = true },
            )

        assertFalse(result)
        assertFalse(spoken)
        assertFalse(stopped)
    }

    @Test
    fun `再生中のキーがreadoutOrderに含まれない場合は新しいイベントを優先して読み上げる`() {
        var spokenQueue: Boolean? = null
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "new",
                currentKey = { "unknown" },
                readoutOrder = listOf("new"),
                queueEnabled = false,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(false, spokenQueue)
        assertTrue(stopped)
    }
}
