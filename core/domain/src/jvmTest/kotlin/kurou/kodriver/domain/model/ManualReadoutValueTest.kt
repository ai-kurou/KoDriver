package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutValueTest {
    @Test
    fun `種別ごとに整数値を保持し種別が違えば等しくない`() {
        val values =
            listOf(
                ManualReadoutValue.Percent(42),
                ManualReadoutValue.Celsius(42),
                ManualReadoutValue.LapTimeMs(42),
                ManualReadoutValue.RemainingLaps(42),
            )
        values.forEach { assertEquals(42, it.value) }
        assertEquals(values.size, values.toSet().size)
        assertEquals(ManualReadoutValue.Percent(42), ManualReadoutValue.Percent(42))
    }
}
