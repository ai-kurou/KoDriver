package kurou.kodriver.feature.telemetryloglist

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSortedTelemetryLogsUseCaseTest {
    private val repository = mockk<TelemetryLogRepository>()
    private val logs = MutableStateFlow(emptyList<TelemetryLog>())
    private val useCase = ObserveSortedTelemetryLogsUseCase(
        ObserveTelemetryLogsUseCase(repository),
    )

    @Test
    fun `createdAtの降順かつ同時刻ではidの降順で返す`() = runTest {
        every { repository.observeTelemetryLogs() } returns logs
        logs.value = listOf(
            telemetryLog(id = 1, createdAt = 100),
            telemetryLog(id = 2, createdAt = 200),
            telemetryLog(id = 3, createdAt = 200),
        )

        assertEquals(listOf(3L, 2L, 1L), useCase().first().map { it.id })
    }

    @Test
    fun `空の一覧を返す`() = runTest {
        every { repository.observeTelemetryLogs() } returns logs

        assertEquals(emptyList(), useCase().first())
    }

    @Test
    fun `ログの更新を並び替えて観測する`() = runTest {
        every { repository.observeTelemetryLogs() } returns logs
        val result = useCase()

        logs.value = listOf(telemetryLog(id = 1, createdAt = 100))
        assertEquals(listOf(1L), result.first().map { it.id })

        logs.value = listOf(
            telemetryLog(id = 1, createdAt = 100),
            telemetryLog(id = 2, createdAt = 300),
        )
        assertEquals(listOf(2L, 1L), result.first().map { it.id })
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
    telemetryJson = "{}",
)
