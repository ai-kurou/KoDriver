package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createTelemetryLogRepository(initialLogs: List<TelemetryLog> = emptyList()): TelemetryLogRepository {
    val repository = mockk<TelemetryLogRepository>()
    val logs = MutableStateFlow(initialLogs)
    every { repository.observeTelemetryLogs() } returns logs
    listOf(
        TelemetryLog(
            id = 0L,
            createdAt = 1000L,
            simulatorId = "gt7_ps5",
            readoutItemKey = "remaining_fuel_laps",
            telemetryJson = """{"lapCount":1}""",
        ),
        TelemetryLog(
            id = 0L,
            createdAt = 2000L,
            simulatorId = "lmu_windows",
            readoutItemKey = "flag",
            telemetryJson = """{"currentLap":2}""",
        ),
    ).forEach { log ->
        coEvery {
            repository.saveTelemetryLog(log.createdAt, log.simulatorId, log.readoutItemKey, log.telemetryJson)
        } answers {
            val nextId = (logs.value.maxOfOrNull { it.id } ?: 0) + 1
            logs.update { it + log.copy(id = nextId) }
        }
    }
    coEvery { repository.deleteAllTelemetryLogs() } answers {
        logs.update { emptyList() }
    }
    return repository
}

class ResetTelemetryLogDatabaseUseCaseTest {
    @Test
    fun `全てのログを削除する`() = runBlocking {
        val repository = createTelemetryLogRepository(
            initialLogs = listOf(
                TelemetryLog(
                    id = 1L,
                    createdAt = 1000L,
                    simulatorId = "gt7_ps5",
                    readoutItemKey = "remaining_fuel_laps",
                    telemetryJson = """{"lapCount":1}""",
                ),
            ),
        )
        val resetUseCase = ResetTelemetryLogDatabaseUseCase(repository)
        val observeUseCase = ObserveTelemetryLogsUseCase(repository)

        resetUseCase()

        assertEquals(emptyList(), observeUseCase().first())
        io.mockk.coVerify(exactly = 1) { repository.deleteAllTelemetryLogs() }
        io.mockk.verify(exactly = 1) { repository.observeTelemetryLogs() }
        io.mockk.confirmVerified(repository)
    }
}
