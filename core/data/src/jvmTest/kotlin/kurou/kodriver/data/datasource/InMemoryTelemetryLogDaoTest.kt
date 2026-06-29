package kurou.kodriver.data.datasource

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.TelemetryLogEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryTelemetryLogDaoTest {
    @Test
    fun `insertはIDを採番して新しいログを先頭に追加する`() = runTest {
        val dao = InMemoryTelemetryLogDao()

        dao.insert(log(createdAt = 1L, readoutItemKey = "my_best_lap"))
        dao.insert(log(createdAt = 2L, readoutItemKey = "remaining_fuel_laps"))

        assertEquals(
            listOf(
                log(id = 2L, createdAt = 2L, readoutItemKey = "remaining_fuel_laps"),
                log(id = 1L, createdAt = 1L, readoutItemKey = "my_best_lap"),
            ),
            dao.observeTelemetryLogs().first(),
        )
    }

    @Test
    fun `insertは指定済みIDを維持する`() = runTest {
        val dao = InMemoryTelemetryLogDao()

        dao.insert(log(id = 10L, createdAt = 1L, readoutItemKey = "my_best_lap"))

        assertEquals(
            listOf(log(id = 10L, createdAt = 1L, readoutItemKey = "my_best_lap")),
            dao.observeTelemetryLogs().first(),
        )
    }

    private fun log(
        id: Long = 0L,
        createdAt: Long,
        readoutItemKey: String,
    ) = TelemetryLogEntity(
        id = id,
        createdAt = createdAt,
        simulatorId = "gt7_ps5",
        readoutItemKey = readoutItemKey,
        telemetryJson = """{"current":{}}""",
    )
}
