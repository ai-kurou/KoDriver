package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private fun createTelemetryLogRepository(
    repository: TelemetryLogRepository,
    initialLogs: List<TelemetryLog> = emptyList(),
): TelemetryLogRepository {
    val logs = MutableStateFlow(initialLogs)
    every { repository.observeTelemetryLogs() } returns logs
    listOf(1L, 2L, 999L).forEach { id ->
        every { repository.observeTelemetryLogDetail(id) } answers {
            logs.map { currentLogs ->
                val sortedLogs =
                    currentLogs.sortedWith(
                        compareByDescending<TelemetryLog> { it.createdAt }.thenByDescending { it.id },
                    )
                val index = sortedLogs.indexOfFirst { it.id == id }
                if (index == -1) {
                    null
                } else {
                    TelemetryLogDetail(
                        current = sortedLogs[index],
                        previous = sortedLogs.getOrNull(index + 1),
                    )
                }
            }
        }
    }
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

class ObserveTelemetryLogDetailUseCaseTest {
    @MockK
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `指定したidのログとその一つ前のログを返す`() =
        runTest {
            val latest = telemetryLog(id = 3L, createdAt = 3000L)
            val current = telemetryLog(id = 2L, createdAt = 2000L)
            val previous = telemetryLog(id = 1L, createdAt = 1000L)
            val repository =
                createTelemetryLogRepository(
                    repository,
                    initialLogs = listOf(previous, current, latest),
                )
            val useCase = ObserveTelemetryLogDetailUseCase(repository)

            assertEquals(
                TelemetryLogDetail(
                    current = current,
                    previous = previous,
                ),
                useCase(2L).first(),
            )
            verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
            confirmVerified(repository)
        }

    @Test
    fun `指定したidのログがない場合はnullを返す`() =
        runTest {
            val repository =
                createTelemetryLogRepository(
                    repository,
                    initialLogs = listOf(telemetryLog(id = 1L, createdAt = 1000L)),
                )
            val useCase = ObserveTelemetryLogDetailUseCase(repository)

            assertNull(useCase(999L).first())
            verify(exactly = 1) { repository.observeTelemetryLogDetail(999L) }
            confirmVerified(repository)
        }

    @Test
    fun `指定したidのログが最も古い場合はpreviousにnullを返す`() =
        runTest {
            val current = telemetryLog(id = 1L, createdAt = 1000L)
            val repository =
                createTelemetryLogRepository(
                    repository,
                    initialLogs =
                        listOf(
                            current,
                            telemetryLog(id = 2L, createdAt = 2000L),
                        ),
                )
            val useCase = ObserveTelemetryLogDetailUseCase(repository)

            assertEquals(
                TelemetryLogDetail(
                    current = current,
                    previous = null,
                ),
                useCase(1L).first(),
            )
            verify(exactly = 1) { repository.observeTelemetryLogDetail(1L) }
            confirmVerified(repository)
        }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = Simulator.LmuWindows,
    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
    telemetryJson = """{"id":$id}""",
)
