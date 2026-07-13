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

class ObserveTelemetryLogsUseCaseTest {
    @Test
    fun `初期値が空のとき空リストを返し・保存済みのログをそのまま返す`() = runBlocking {
        val repository = createTelemetryLogRepository()
        val useCase = ObserveTelemetryLogsUseCase(repository)

        assertEquals(emptyList(), useCase().first())

        repository.saveTelemetryLog(
            createdAt = 2000L,
            simulatorId = "lmu_windows",
            readoutItemKey = "flag",
            telemetryJson = """{"currentLap":2}""",
        )
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
            useCase().first(),
        )
    }
}
