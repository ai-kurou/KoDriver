package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test

class SaveTelemetryLogUseCaseTest {
    @Test
    fun `ログを保存する`() = runBlocking {
        val repository = mockk<TelemetryLogRepository>(relaxUnitFun = true)

        SaveTelemetryLogUseCase(repository)(
            createdAt = 1000L,
            simulatorId = "gt7_ps5",
            readoutItemKey = "remaining_fuel_laps",
            telemetryJson = """{"lapCount":1}""",
        )

        coVerify(exactly = 1) {
            repository.saveTelemetryLog(
                createdAt = 1000L,
                simulatorId = "gt7_ps5",
                readoutItemKey = "remaining_fuel_laps",
                telemetryJson = """{"lapCount":1}""",
            )
        }
        confirmVerified(repository)
    }
}
