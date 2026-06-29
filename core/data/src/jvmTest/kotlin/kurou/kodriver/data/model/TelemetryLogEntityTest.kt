package kurou.kodriver.data.model

import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogEntityTest {
    @Test
    fun `toDomainはEntityをDomainに変換する`() {
        val entity = TelemetryLogEntity(
            id = 1L,
            createdAt = 1000L,
            simulatorId = "gt7_ps5",
            readoutItemKey = "remaining_fuel_laps",
            telemetryJson = """{"lapCount":1}""",
        )

        assertEquals(
            TelemetryLog(
                id = 1L,
                createdAt = 1000L,
                simulatorId = "gt7_ps5",
                readoutItemKey = "remaining_fuel_laps",
                telemetryJson = """{"lapCount":1}""",
            ),
            entity.toDomain(),
        )
    }

    @Test
    fun `toEntityはDomainをEntityに変換する`() {
        val log = TelemetryLog(
            id = 2L,
            createdAt = 2000L,
            simulatorId = "lmu_windows",
            readoutItemKey = "flag",
            telemetryJson = """{"currentLap":2}""",
        )

        assertEquals(
            TelemetryLogEntity(
                id = 2L,
                createdAt = 2000L,
                simulatorId = "lmu_windows",
                readoutItemKey = "flag",
                telemetryJson = """{"currentLap":2}""",
            ),
            log.toEntity(),
        )
    }
}
