package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveTelemetryLogUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `ログを保存する`() = runBlocking {
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
