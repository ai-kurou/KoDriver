package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTelemetryLogUseCaseTest {
    @Test
    fun `ログを保存する`() = runBlocking {
        val repository = FakeTelemetryLogRepository()
        val saveUseCase = SaveTelemetryLogUseCase(repository)
        val observeUseCase = ObserveTelemetryLogsUseCase(repository)

        saveUseCase(
            createdAt = 1000L,
            simulatorId = "gt7_ps5",
            readoutItemKey = "remaining_fuel_laps",
            telemetryJson = """{"lapCount":1}""",
        )

        assertEquals(
            listOf(
                TelemetryLog(
                    createdAt = 1000L,
                    simulatorId = "gt7_ps5",
                    readoutItemKey = "remaining_fuel_laps",
                    telemetryJson = """{"lapCount":1}""",
                ),
            ),
            observeUseCase().first(),
        )
    }
}
