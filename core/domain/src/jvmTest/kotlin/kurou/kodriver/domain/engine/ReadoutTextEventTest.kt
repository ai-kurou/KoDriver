package kurou.kodriver.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutTextEventTest {
    @Test
    fun `全GT7自由文言イベントは本文だけを解決して既定文言とキーを維持する`() {
        val events: List<ReadoutTextEvent> =
            listOf(
                Gt7Ps5MyBestLap(83_456),
                Gt7Ps5RemainingFuelLapsWarning(3),
                Gt7Ps5RemainingFuelLapsWarning(0),
                Gt7Ps5RemainingFuelWarning(30),
                Gt7Ps5TyreOverheat(120),
            )
        events.forEach { event ->
            assertEquals(null, event.resolvedText)
            val resolved = event.withResolvedText("確定した本文")
            assertEquals("確定した本文", resolved.resolvedText)
            assertEquals(event.readoutItemKey, resolved.readoutItemKey)
            assertEquals(event.narratedText, resolved.narratedText)
            assertEquals(resolved, resolved.withResolvedText("確定した本文"))
            assertEquals("", resolved.withResolvedText("").resolvedText)
        }
    }
}
