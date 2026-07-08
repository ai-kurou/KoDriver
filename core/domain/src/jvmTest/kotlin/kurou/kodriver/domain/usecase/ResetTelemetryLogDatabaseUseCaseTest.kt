package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class ResetTelemetryLogDatabaseUseCaseTest {
    @Test
    fun `全てのログを削除する`() = runBlocking {
        val repository = FakeTelemetryLogRepository(
            initialLogs = listOf(
                TelemetryLog(
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
