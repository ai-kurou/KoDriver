package kurou.kodriver.feature.telemetryloglist

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryLogsUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSortedTelemetryLogsUseCaseTest {
    private val repository: TelemetryLogRepository = mockk()
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)

    private val logs = MutableStateFlow(emptyList<TelemetryLog>())
    private lateinit var useCase: ObserveSortedTelemetryLogsUseCase

    @BeforeTest
    fun setUp() {
        logs.update { emptyList() }
        useCase =
            ObserveSortedTelemetryLogsUseCase(
                ObserveTelemetryLogsUseCase(repository),
                ObserveSelectedSimulatorUseCase(simulatorRepository),
            )
    }

    @Test
    fun `createdAtの降順かつ同時刻ではidの降順で返す`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            logs.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 200),
                    telemetryLog(id = 3, createdAt = 200),
                )
            }

            assertEquals(listOf(3L, 2L, 1L), useCase().first().map { it.id })
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `選択中のシミュレーターのみ返し切り替え先にログがなければ空になる`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            logs.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 300, simulator = Simulator.AceWindows),
                )
            }
            val result = useCase()

            assertEquals(listOf(1L), result.first().map { it.id })
            selectedSimulator.update { Simulator.AceWindows }
            assertEquals(listOf(2L), result.first().map { it.id })
            selectedSimulator.update { Simulator.Gt7Ps5 }
            assertEquals(emptyList(), result.first())
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(repository, simulatorRepository)
        }

    @Test
    fun `空の一覧を返す`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator

            assertEquals(emptyList(), useCase().first())
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `ログの更新を並び替えて観測する`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val result = useCase()

            logs.update { listOf(telemetryLog(id = 1, createdAt = 100)) }
            assertEquals(listOf(1L), result.first().map { it.id })

            logs.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100),
                    telemetryLog(id = 2, createdAt = 300),
                )
            }
            assertEquals(listOf(2L, 1L), result.first().map { it.id })
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }

    @Test
    fun `ace_windowsのログを含む場合も並び替えて観測する`() =
        runTest {
            every { repository.observeTelemetryLogs() } returns logs
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            selectedSimulator.update { Simulator.AceWindows }
            logs.update {
                listOf(
                    telemetryLog(id = 1, createdAt = 100, simulator = Simulator.AceWindows),
                    telemetryLog(id = 2, createdAt = 300, simulator = Simulator.AceWindows),
                )
            }

            assertEquals(listOf(2L, 1L), useCase().first().map { it.id })
            verify(exactly = 1) { repository.observeTelemetryLogs() }
            confirmVerified(repository)
        }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    simulator: Simulator = Simulator.LmuWindows,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = simulator,
    readoutItemKey =
        if (simulator == Simulator.AceWindows) {
            AceWindowsReadoutItemKey.Flag.Root
        } else {
            LmuWindowsReadoutItemKey.Flag.Root
        },
    narratedText = "イエローフラッグ",
    narrationOutcome = NarrationOutcome.INTERRUPTED,
    telemetryJson = "{}",
)
