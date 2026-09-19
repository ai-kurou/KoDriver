package kurou.kodriver.data.telemetrylog

import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TelemetryLogEntityTest {
    @Test
    fun `toDomainはEntityをDomainに変換する`() {
        val entity =
            TelemetryLogEntity(
                id = 1L,
                createdAt = 1000L,
                simulatorId = Simulator.Gt7Ps5.id,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
                narratedText = "燃料は残り約1周",
                narrationOutcome = NarrationOutcome.QUEUED.id,
                telemetryJson = """{"lapCount":1}""",
            )

        assertEquals(
            TelemetryLog(
                id = 1L,
                createdAt = 1000L,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                narratedText = "燃料は残り約1周",
                narrationOutcome = NarrationOutcome.QUEUED,
                telemetryJson = """{"lapCount":1}""",
            ),
            entity.toDomain(),
        )
    }

    @Test
    fun `toDomainは未知のnarrationOutcomeにnullを返す`() {
        val entity =
            TelemetryLogEntity(
                id = 1L,
                createdAt = 1000L,
                simulatorId = Simulator.Gt7Ps5.id,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
                narratedText = "燃料は残り約1周",
                narrationOutcome = "unknown",
                telemetryJson = """{"lapCount":1}""",
            )

        assertNull(entity.toDomain())
    }
}
