package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveTelemetryLogDetailUseCaseTest {
    @Test
    fun `指定したidのログとその一つ前のログを返す`() = runBlocking {
        val latest = telemetryLog(id = 3L, createdAt = 3000L)
        val current = telemetryLog(id = 2L, createdAt = 2000L)
        val previous = telemetryLog(id = 1L, createdAt = 1000L)
        val repository = FakeTelemetryLogRepository(
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
        val repository = FakeTelemetryLogRepository(
            initialLogs = listOf(telemetryLog(id = 1L, createdAt = 1000L)),
        )
        val useCase = ObserveTelemetryLogDetailUseCase(repository)

        assertNull(useCase(999L).first())
    }

    @Test
    fun `指定したidのログが最も古い場合はpreviousにnullを返す`() = runBlocking {
        val current = telemetryLog(id = 1L, createdAt = 1000L)
        val repository = FakeTelemetryLogRepository(
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
