package kurou.kodriver.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test

class SaveTelemetryLogUseCaseTest {
    private val repository: TelemetryLogRepository = mockk()

    @Test
    fun `ログを保存する`() =
        runTest {
            NarrationSource.entries.forEach { source ->
                coEvery {
                    repository.saveTelemetryLog(
                        createdAt = 1000L,
                        simulator = Simulator.Gt7Ps5,
                        readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                        narratedText = "燃料は残り約1周",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        narrationSource = source,
                        telemetryJson = """{"lapCount":1}""",
                    )
                } just Runs
                SaveTelemetryLogUseCase(repository)(
                    createdAt = 1000L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                    narratedText = "燃料は残り約1周",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    narrationSource = source,
                    telemetryJson = """{"lapCount":1}""",
                )

                coVerify(exactly = 1) {
                    repository.saveTelemetryLog(
                        createdAt = 1000L,
                        simulator = Simulator.Gt7Ps5,
                        readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                        narratedText = "燃料は残り約1周",
                        narrationOutcome = NarrationOutcome.QUEUED,
                        narrationSource = source,
                        telemetryJson = """{"lapCount":1}""",
                    )
                }
            }
            confirmVerified(repository)
        }
}
