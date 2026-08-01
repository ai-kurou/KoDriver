package kurou.kodriver.data.model

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogEntityTest {
    @Test
    fun `toDomainはEntityをDomainに変換する`() {
        val entity =
            TelemetryLogEntity(
            id = 1L,
            createdAt = 1000L,
            simulatorId = Simulator.Gt7Ps5.id,
            readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
            telemetryJson = """{"lapCount":1}""",
        )

        assertEquals(
            TelemetryLog(
                id = 1L,
                createdAt = 1000L,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                telemetryJson = """{"lapCount":1}""",
            ),
            entity.toDomain(),
        )
    }
}
