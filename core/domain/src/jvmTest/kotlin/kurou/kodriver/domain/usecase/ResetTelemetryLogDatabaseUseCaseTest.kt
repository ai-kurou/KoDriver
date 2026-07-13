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
    coEvery { repository.saveTelemetryLog(any(), any(), any(), any()) } answers {
        val nextId = (logs.value.maxOfOrNull { it.id } ?: 0) + 1
        logs.update {
            it + TelemetryLog(
                id = nextId,
                createdAt = firstArg(),
                simulatorId = secondArg(),
                readoutItemKey = thirdArg(),
                telemetryJson = arg(3),
            )
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
    }
}
