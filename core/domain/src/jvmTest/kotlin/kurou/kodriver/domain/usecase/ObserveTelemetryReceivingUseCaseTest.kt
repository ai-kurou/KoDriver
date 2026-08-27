package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTelemetryReceivingUseCaseTest {
    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var lmuWindowsRepository: LmuWindowsRepository

    @MockK
    private lateinit var gt7Ps5Repository: Gt7Ps5Repository

    @MockK
    private lateinit var aceWindowsStatusRepository: AceWindowsStatusRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun TestScope.createUseCase() =
        ObserveTelemetryReceivingUseCase(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            observeLmuWindows = ObserveLmuWindowsUseCase(lmuWindowsRepository),
            observeGt7Ps5 = ObserveGt7Ps5UseCase(gt7Ps5Repository),
            observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(aceWindowsStatusRepository),
            currentTimeMillis = { testScheduler.currentTime },
        )

    @Test
    fun `選択中シミュレータのテレメトリを受信するとtrueになる`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val telemetry = MutableSharedFlow<LmuWindowsTelemetryData>(replay = 1)
            telemetry.tryEmit(fakeLmuWindowsTelemetryData())
            every { lmuWindowsRepository.telemetryStream() } returns telemetry
            every { gt7Ps5Repository.telemetryStream() } returns emptyFlow()
            every { aceWindowsStatusRepository.statusStream() } returns emptyFlow()
            val useCase = createUseCase()

            assertTrue(withTimeout(5_000L) { useCase().first { it } })
        }

    @Test
    fun `テレメトリを一度も受信していない場合はfalseのまま`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
            every { gt7Ps5Repository.telemetryStream() } returns emptyFlow()
            every { aceWindowsStatusRepository.statusStream() } returns emptyFlow()
            val useCase = createUseCase()

            assertFalse(useCase().first())
        }

    @Test
    fun `受信済みでも一定時間データが来ないとfalseに戻る`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.AceWindows)
            val status = MutableSharedFlow<AceWindowsStatusData>(replay = 1)
            status.tryEmit(AceWindowsStatusData(status = AceWindowsStatusType.LIVE))
            every { lmuWindowsRepository.telemetryStream() } returns emptyFlow()
            every { gt7Ps5Repository.telemetryStream() } returns emptyFlow()
            every { aceWindowsStatusRepository.statusStream() } returns status
            val useCase = createUseCase()

            val results = withTimeout(10_000L) { useCase().take(5).toList() }

            assertEquals(listOf(false, true, true, true, false), results)
        }
}
