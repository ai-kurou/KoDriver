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
                autoReadoutOrder = listOf("new", "current"),
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
                autoReadoutOrder = listOf("new"),
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
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { spoken = true },
                stop = { stopped = true },
            )

        assertFalse(result)
        assertFalse(spoken)
        assertFalse(stopped)
    }

    @Test
    fun `再生中と同じキーのイベントは優先度が等しいため読み上げない`() {
        var spoken = false
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "same",
                currentKey = { "same" },
                autoReadoutOrder = listOf("same", "other"),
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
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(false, spokenQueue)
        assertTrue(stopped)
    }

    @Test
    fun `autoReadoutOrderに含まれないキーは最も優先度が低いものとして扱う`() {
        var spoken = false
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "unknown",
                currentKey = { "current" },
                autoReadoutOrder = listOf("current"),
                queueEnabled = false,
                speak = { spoken = true },
                stop = { stopped = true },
            )

        assertFalse(result)
        assertFalse(spoken)
        assertFalse(stopped)
    }

    @Test
    fun `再生中のキーがautoReadoutOrderに含まれない場合は新しいイベントを優先して読み上げる`() {
        var spokenQueue: Boolean? = null
        var stopped = false

        val result =
            speakWithPriority(
                eventKey = "new",
                currentKey = { "unknown" },
                autoReadoutOrder = listOf("new"),
                queueEnabled = false,
                speak = { queue -> spokenQueue = queue },
                stop = { stopped = true },
            )

        assertTrue(result)
        assertEquals(false, spokenQueue)
        assertTrue(stopped)
    }

    @Test
    fun `手動は再生中が自動なら優先度に関係なく停止して割り込む`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithPriority(
                eventKey = "manual",
                currentKey = { "auto" },
                autoReadoutOrder = listOf("auto", "manual"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                isManual = true,
                currentIsManual = { false },
            )

        assertTrue(result)
        assertEquals(listOf("stop", "speak:false"), calls)
    }

    @Test
    fun `手動は再生中が手動ならキュー末尾へ追加する`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithPriority(
                eventKey = "manual",
                currentKey = { "manual2" },
                autoReadoutOrder = emptyList(),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                isManual = true,
                currentIsManual = { true },
            )

        assertTrue(result)
        assertEquals(listOf("speak:true"), calls)
    }

    @Test
    fun `手動は再生中がなければ停止せずそのまま読み上げる`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithPriority(
                eventKey = "manual",
                currentKey = { null },
                autoReadoutOrder = emptyList(),
                queueEnabled = true,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                isManual = true,
            )

        assertTrue(result)
        assertEquals(listOf("speak:false"), calls)
    }

    @Test
    fun `手動の再生中にキュー無効の自動イベントが来たら優先度が上でも読み上げない`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithPriority(
                eventKey = "high",
                currentKey = { "low" },
                autoReadoutOrder = listOf("high", "low"),
                queueEnabled = false,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                currentIsManual = { true },
            )

        assertFalse(result)
        assertEquals(emptyList(), calls)
    }

    @Test
    fun `手動の再生中でもキュー有効の自動イベントはキュー末尾へ追加する`() {
        val calls = mutableListOf<String>()

        val result =
            speakWithPriority(
                eventKey = "auto",
                currentKey = { "manual" },
                autoReadoutOrder = listOf("auto", "manual"),
                queueEnabled = true,
                speak = { queue -> calls += "speak:$queue" },
                stop = { calls += "stop" },
                currentIsManual = { true },
            )

        assertTrue(result)
        assertEquals(listOf("speak:true"), calls)
    }
}
