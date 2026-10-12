package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createTelemetryLogRepository(
    repository: TelemetryLogRepository,
    initialLogs: List<TelemetryLog> = emptyList(),
): TelemetryLogRepository {
    val logs = MutableStateFlow(initialLogs)
    every { repository.observeTelemetryLogs() } returns logs
    listOf(
        TelemetryLog(
            id = 0L,
            createdAt = 1000L,
            simulator = Simulator.Gt7Ps5,
            readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
            narratedText = "燃料は残り約1周",
            narrationOutcome = NarrationOutcome.QUEUED,
            telemetryJson = """{"lapCount":1}""",
        ),
        TelemetryLog(
            id = 0L,
            createdAt = 2000L,
            simulator = Simulator.LmuWindows,
            readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
            narratedText = "イエローフラッグ",
            narrationOutcome = NarrationOutcome.QUEUED,
            telemetryJson = """{"currentLap":2}""",
        ),
    ).forEach { log ->
        coEvery {
            repository.saveTelemetryLog(
                log.createdAt,
                log.simulator,
                log.readoutItemKey,
                log.narratedText,
                log.narrationOutcome,
                log.telemetryJson,
            )
        } answers {
            val nextId = (logs.value.maxOfOrNull { it.id } ?: 0) + 1
            logs.update { it + log.copy(id = nextId) }
        }
    }
    listOf(Simulator.Gt7Ps5, Simulator.LmuWindows).forEach { simulator ->
        coEvery { repository.deleteTelemetryLogsBySimulator(simulator) } answers {
            logs.update { current -> current.filterNot { it.simulator == simulator } }
        }
    }
    return repository
}

class ResetTelemetryLogDatabaseUseCaseTest {
    private val repository: TelemetryLogRepository = mockk()

    @Test
    fun `指定シミュレーターのログのみ削除して他シミュレーターのログを残す`() =
        runTest {
            val otherLog =
                TelemetryLog(
                    id = 2L,
                    createdAt = 2000L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
                    narratedText = "イエローフラッグ",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    telemetryJson = "{}",
                )
            val repository =
                createTelemetryLogRepository(
                    repository,
                    initialLogs =
                        listOf(
                            TelemetryLog(
                                id = 1L,
                                createdAt = 1000L,
                                simulator = Simulator.Gt7Ps5,
                                readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                                narratedText = "燃料は残り約1周",
                                narrationOutcome = NarrationOutcome.QUEUED,
                                telemetryJson = """{"lapCount":1}""",
                            ),
                            otherLog,
                        ),
                )
            val resetUseCase = ResetTelemetryLogDatabaseUseCase(repository)
            val observeUseCase = ObserveTelemetryLogsUseCase(repository)

            resetUseCase(Simulator.Gt7Ps5)

            assertEquals(listOf(otherLog), observeUseCase().first())
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(Simulator.Gt7Ps5) }
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }
}
