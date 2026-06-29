package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.TelemetryLogDao
import kurou.kodriver.data.model.TelemetryLogEntity
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogRepositoryImplTest {
    @Test
    fun `saveTelemetryLogはEntityへ変換して保存する`() = runTest {
        val dao = FakeTelemetryLogDao()
        val repository = TelemetryLogRepositoryImpl(dao)

        repository.saveTelemetryLog(
            TelemetryLog(
                createdAt = 1000L,
                simulatorId = "gt7_ps5",
                readoutItemKey = "remaining_fuel_laps",
                telemetryJson = """{"lapCount":1}""",
            ),
        )

        assertEquals(
            listOf(
                TelemetryLogEntity(
                    createdAt = 1000L,
                    simulatorId = "gt7_ps5",
                    readoutItemKey = "remaining_fuel_laps",
                    telemetryJson = """{"lapCount":1}""",
                ),
            ),
            dao.logs.first(),
        )
    }

    @Test
    fun `observeTelemetryLogsはDomainへ変換して観測する`() = runTest {
        val dao = FakeTelemetryLogDao(
            initialLogs = listOf(
                TelemetryLogEntity(
                    id = 1L,
                    createdAt = 2000L,
                    simulatorId = "lmu_windows",
                    readoutItemKey = "flag",
                    telemetryJson = """{"currentLap":2}""",
                ),
            ),
        )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 1L,
                    createdAt = 2000L,
                    simulatorId = "lmu_windows",
                    readoutItemKey = "flag",
                    telemetryJson = """{"currentLap":2}""",
                ),
            ),
            repository.observeTelemetryLogs().first(),
        )
    }
}

private class FakeTelemetryLogDao(
    initialLogs: List<TelemetryLogEntity> = emptyList(),
) : TelemetryLogDao {
    val logs = MutableStateFlow(initialLogs)

    override fun observeTelemetryLogs(): Flow<List<TelemetryLogEntity>> = logs

    override suspend fun insert(log: TelemetryLogEntity) {
        logs.update { it + log }
    }
}
