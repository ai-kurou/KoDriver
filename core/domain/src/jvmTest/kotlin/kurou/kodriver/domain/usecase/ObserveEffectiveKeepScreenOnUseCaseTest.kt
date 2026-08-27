package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveEffectiveKeepScreenOnUseCaseTest {
    @MockK
    private lateinit var keepScreenOnRepository: KeepScreenOnEnabledRepository

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

    private fun createUseCase() =
        ObserveEffectiveKeepScreenOnUseCase(
            observeKeepScreenOnEnabled = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeTelemetryReceiving =
                ObserveTelemetryReceivingUseCase(
                    observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
                    observeLmuWindows = ObserveLmuWindowsUseCase(lmuWindowsRepository),
                    observeGt7Ps5 = ObserveGt7Ps5UseCase(gt7Ps5Repository),
                    observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(aceWindowsStatusRepository),
                ),
        )

    @Test
    fun `設定が有効かつテレメトリ受信中の場合trueになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(true)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            every { gt7Ps5Repository.telemetryStream() } returns flowOf(fakeGt7Ps5TelemetryData())
            val useCase = createUseCase()

            assertTrue(useCase().first { it })

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            confirmVerified(keepScreenOnRepository)
        }

    @Test
    fun `設定が有効でもテレメトリ未受信の場合falseになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(true)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            every { gt7Ps5Repository.telemetryStream() } returns emptyFlow()
            val useCase = createUseCase()

            assertFalse(useCase().first())

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            confirmVerified(keepScreenOnRepository)
        }

    @Test
    fun `テレメトリ受信中でも設定が無効な場合falseになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(false)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            every { gt7Ps5Repository.telemetryStream() } returns flowOf(fakeGt7Ps5TelemetryData())
            val useCase = createUseCase()

            assertFalse(useCase().first())

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            confirmVerified(keepScreenOnRepository)
        }
}
