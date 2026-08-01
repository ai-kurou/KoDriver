package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.BeforeTest
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
            readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetryJson = """{"lapCount":1}""",
        ),
        TelemetryLog(
            id = 0L,
            createdAt = 2000L,
            simulator = Simulator.LmuWindows,
            readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
            telemetryJson = """{"currentLap":2}""",
        ),
    ).forEach { log ->
        coEvery {
            repository.saveTelemetryLog(log.createdAt, log.simulator, log.readoutItemKey, log.telemetryJson)
        } answers {
            val nextId = (logs.value.maxOfOrNull { it.id } ?: 0) + 1
            logs.update { it + log.copy(id = nextId) }
        }
    }
    coEvery { repository.deleteAllTelemetryLogs() } answers {
        logs.update { emptyList() }
    }
    return repository
}

class ObserveTelemetryLogsUseCaseTest {
    @MockK
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値が空のとき空リストを返し・保存済みのログをそのまま返す`() =
        runBlocking {
            val repository = createTelemetryLogRepository(repository)
            val useCase = ObserveTelemetryLogsUseCase(repository)

            assertEquals(emptyList(), useCase().first())

            repository.saveTelemetryLog(
                createdAt = 2000L,
                simulator = Simulator.LmuWindows,
                readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                telemetryJson = """{"currentLap":2}""",
            )
            assertEquals(
                listOf(
                    TelemetryLog(
                        id = 1L,
                        createdAt = 2000L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                        telemetryJson = """{"currentLap":2}""",
                    ),
                ),
                useCase().first(),
            )
            verify(exactly = 2) { repository.observeTelemetryLogs() }
            coVerify(exactly = 1) {
                repository.saveTelemetryLog(
                    createdAt = 2000L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    telemetryJson = """{"currentLap":2}""",
                )
            }
            confirmVerified(repository)
        }
}
