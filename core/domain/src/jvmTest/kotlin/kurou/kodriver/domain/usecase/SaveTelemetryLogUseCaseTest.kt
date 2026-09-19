package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test

class SaveTelemetryLogUseCaseTest {
    private val repository: TelemetryLogRepository = mockk(relaxUnitFun = true)

    @Test
    fun `ログを保存する`() =
        runTest {
            SaveTelemetryLogUseCase(repository)(
                createdAt = 1000L,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                narratedText = "燃料は残り約1周",
                telemetryJson = """{"lapCount":1}""",
            )

            coVerify(exactly = 1) {
                repository.saveTelemetryLog(
                    createdAt = 1000L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                    narratedText = "燃料は残り約1周",
                    telemetryJson = """{"lapCount":1}""",
                )
            }
            confirmVerified(repository)
        }
}
