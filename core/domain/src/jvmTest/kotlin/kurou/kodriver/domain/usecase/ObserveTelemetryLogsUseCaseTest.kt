package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveTelemetryLogsUseCaseTest {
    @Test
    fun `初期値が空のとき空リストを返し・保存済みのログをそのまま返す`() = runBlocking {
        val expected = TelemetryLog(
            createdAt = 2000L,
            simulatorId = "lmu_windows",
            readoutItemKey = "flag",
            telemetryJson = """{"currentLap":2}""",
        )
        val repository = FakeTelemetryLogRepository()
        val useCase = ObserveTelemetryLogsUseCase(repository)

        assertEquals(emptyList(), useCase().first())

        repository.saveTelemetryLog(expected)
        assertEquals(listOf(expected), useCase().first())
    }
}
