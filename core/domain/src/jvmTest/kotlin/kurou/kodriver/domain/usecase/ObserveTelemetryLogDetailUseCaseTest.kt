package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun createTelemetryLogRepository(initialLogs: List<TelemetryLog> = emptyList()): TelemetryLogRepository {
    val repository = mockk<TelemetryLogRepository>()
    val logs = MutableStateFlow(initialLogs)
    every { repository.observeTelemetryLogs() } returns logs
    every { repository.observeTelemetryLogDetail(any()) } answers {
        val id = firstArg<Long>()
        logs.map { currentLogs ->
            val sortedLogs = currentLogs.sortedWith(
                compareByDescending<TelemetryLog> { it.createdAt }.thenByDescending { it.id },
            )
            val index = sortedLogs.indexOfFirst { it.id == id }
            if (index == -1) {
                null
            } else {
                TelemetryLogDetail(
                    current = sortedLogs[index],
                    previous = sortedLogs.getOrNull(index + 1),
                )
            }
        }
    }
    coEvery { repository.saveTelemetryLog(any()) } answers {
        logs.update { it + firstArg<TelemetryLog>() }
    }
    coEvery { repository.deleteAllTelemetryLogs() } answers {
        logs.update { emptyList() }
    }
    return repository
}

class ObserveTelemetryLogDetailUseCaseTest {
    @Test
    fun `指定したidのログとその一つ前のログを返す`() = runBlocking {
        val latest = telemetryLog(id = 3L, createdAt = 3000L)
        val current = telemetryLog(id = 2L, createdAt = 2000L)
        val previous = telemetryLog(id = 1L, createdAt = 1000L)
        val repository = createTelemetryLogRepository(
            initialLogs = listOf(previous, current, latest),
        )
        val useCase = ObserveTelemetryLogDetailUseCase(repository)

        assertEquals(
            TelemetryLogDetail(
                current = current,
                previous = previous,
            ),
            useCase(2L).first(),
        )
    }

    @Test
    fun `指定したidのログがない場合はnullを返す`() = runBlocking {
        val repository = createTelemetryLogRepository(
            initialLogs = listOf(telemetryLog(id = 1L, createdAt = 1000L)),
        )
        val useCase = ObserveTelemetryLogDetailUseCase(repository)

        assertNull(useCase(999L).first())
    }

    @Test
    fun `指定したidのログが最も古い場合はpreviousにnullを返す`() = runBlocking {
        val current = telemetryLog(id = 1L, createdAt = 1000L)
        val repository = createTelemetryLogRepository(
            initialLogs = listOf(
                current,
                telemetryLog(id = 2L, createdAt = 2000L),
            ),
        )
        val useCase = ObserveTelemetryLogDetailUseCase(repository)

        assertEquals(
            TelemetryLogDetail(
                current = current,
                previous = null,
            ),
            useCase(1L).first(),
        )
    }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulatorId = "lmu_windows",
    readoutItemKey = "flag",
    telemetryJson = """{"id":$id}""",
)
