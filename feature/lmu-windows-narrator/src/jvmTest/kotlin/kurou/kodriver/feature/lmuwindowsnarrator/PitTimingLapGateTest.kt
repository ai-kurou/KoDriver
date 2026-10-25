package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.PitTimingSource
import kotlin.test.Test
import kotlin.test.assertEquals

class PitTimingLapGateTest {
    private val gate = PitTimingLapGate()

    private fun warning(laps: Int) =
        listOf<SpeechEvent>(LmuWindowsPitTimingWarning(laps, source = PitTimingSource.TyreWear))

    @Test
    fun `イベントが無ければ空を返す`() {
        assertEquals(emptyList<SpeechEvent>(), gate.filter(currentLap = 1, events = emptyList()))
    }

    @Test
    fun `PitTimingWarning以外のイベントは通さない`() {
        assertEquals(
            emptyList<SpeechEvent>(),
            gate.filter(currentLap = 1, events = listOf(LmuWindowsRedFlag())),
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

    @Test
    fun `通過したイベントは算出元を維持する`() {
        val virtualEnergy =
            listOf<SpeechEvent>(
                LmuWindowsPitTimingWarning(3, PitTimingSource.VirtualEnergy),
            )
        val tyreWear = warning(2)

        assertEquals(virtualEnergy, gate.filter(currentLap = 1, events = virtualEnergy))
        assertEquals(tyreWear, gate.filter(currentLap = 1, events = tyreWear))
    }

    @Test
    fun `3ソースの候補から最小の周回数を選び同値では既存ソース順を保つ`() {
        val energy = LmuWindowsPitTimingWarning(3, PitTimingSource.VirtualEnergy)
        val tyre = LmuWindowsPitTimingWarning(2, PitTimingSource.TyreWear)
        val fuel = LmuWindowsPitTimingWarning(1, PitTimingSource.Fuel)
        assertEquals(listOf(fuel), selectLowerPitTimingEvent(listOf(energy), listOf(tyre), listOf(fuel)))
        assertEquals(listOf(tyre), selectLowerPitTimingEvent(emptyList(), listOf(tyre), listOf(fuel.copy(laps = 2))))
        assertEquals(listOf(energy), selectLowerPitTimingEvent(listOf(energy), emptyList(), emptyList()))
        assertEquals(emptyList(), selectLowerPitTimingEvent(emptyList(), emptyList(), listOf(LmuWindowsRedFlag())))
    }

    @Test
    fun `燃料候補にもソースをまたぐ周回ゲートを適用する`() {
        val fuel = listOf<SpeechEvent>(LmuWindowsPitTimingWarning(1, PitTimingSource.Fuel))
        assertEquals(warning(2), gate.filter(1, warning(2)))
        assertEquals(fuel, gate.filter(1, fuel))
        assertEquals(emptyList(), gate.filter(1, warning(1)))
        assertEquals(emptyList(), gate.filter(1, fuel))
        assertEquals(fuel, gate.filter(2, fuel))
    }
}
