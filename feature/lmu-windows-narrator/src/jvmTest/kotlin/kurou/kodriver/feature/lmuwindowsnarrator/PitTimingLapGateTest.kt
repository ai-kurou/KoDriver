package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.SpeechEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class PitTimingLapGateTest {
    private val gate = PitTimingLapGate()

    private fun warning(laps: Int) = listOf<SpeechEvent>(SpeechEvent.PitTimingWarning(laps))

    @Test
    fun `イベントが無ければ空を返す`() {
        assertEquals(emptyList<SpeechEvent>(), gate.filter(currentLap = 1, events = emptyList()))
    }

    @Test
    fun `PitTimingWarning以外のイベントは通さない`() {
        assertEquals(
            emptyList<SpeechEvent>(),
            gate.filter(currentLap = 1, events = listOf(SpeechEvent.RedFlag)),
        )
    }

    @Test
    fun `同一ラップの別tickで後から来たより低い周回数の警告は読み上げる`() {
        assertEquals(warning(3), gate.filter(currentLap = 1, events = warning(3)))
        assertEquals(warning(2), gate.filter(currentLap = 1, events = warning(2)))
    }

    @Test
    fun `同一ラップの別tickで後から来たより高い周回数の警告は読み上げない`() {
        assertEquals(warning(2), gate.filter(currentLap = 1, events = warning(2)))
        assertEquals(emptyList<SpeechEvent>(), gate.filter(currentLap = 1, events = warning(3)))
    }

    @Test
    fun `同一ラップの別tickで同じ周回数の警告は読み上げない`() {
        assertEquals(warning(2), gate.filter(currentLap = 1, events = warning(2)))
        assertEquals(emptyList<SpeechEvent>(), gate.filter(currentLap = 1, events = warning(2)))
    }

    @Test
    fun `ラップが変わると高い周回数でも読み上げる`() {
        assertEquals(warning(2), gate.filter(currentLap = 1, events = warning(2)))
        assertEquals(warning(3), gate.filter(currentLap = 2, events = warning(3)))
    }
}
