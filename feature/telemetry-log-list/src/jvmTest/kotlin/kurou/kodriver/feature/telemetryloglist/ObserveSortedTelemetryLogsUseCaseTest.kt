package kurou.kodriver.feature.telemetryloglist

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSortedTelemetryLogsUseCaseTest {
    @MockK
    private lateinit var repository: TelemetryLogRepository

    private val logs = MutableStateFlow(emptyList<TelemetryLog>())
    private lateinit var useCase: ObserveSortedTelemetryLogsUseCase

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        logs.value = emptyList()
        useCase = ObserveSortedTelemetryLogsUseCase(ObserveTelemetryLogsUseCase(repository))
    }

    @Test
    fun `createdAtの降順かつ同時刻ではidの降順で返す`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            logs.value =
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 200),
                    telemetryLog(id = 3, createdAt = 200),
                )

            assertEquals(listOf(3L, 2L, 1L), useCase().first().map { it.id })
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `空の一覧を返す`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs

            assertEquals(emptyList(), useCase().first())
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `ログの更新を並び替えて観測する`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            val result = useCase()

            logs.value = listOf(telemetryLog(id = 1, createdAt = 100))
            assertEquals(listOf(1L), result.first().map { it.id })

            logs.value =
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 300),
                )
            assertEquals(listOf(2L, 1L), result.first().map { it.id })
            verify(exactly = 1) { repository.observeTelemetryLogs() }
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
    telemetryJson = "{}",
)
