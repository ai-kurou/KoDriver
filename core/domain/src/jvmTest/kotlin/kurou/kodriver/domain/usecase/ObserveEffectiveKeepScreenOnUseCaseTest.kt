package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveEffectiveKeepScreenOnUseCaseTest {
    private val keepScreenOnRepository: KeepScreenOnEnabledRepository = mockk()

    private val simulatorRepository: SimulatorPreferencesRepository = mockk()

    private fun createUseCase(telemetry: Flow<*>) =
        ObserveEffectiveKeepScreenOnUseCase(
            observeKeepScreenOnEnabled = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeTelemetryReceiving =
                ObserveTelemetryReceivingUseCase(ObserveSelectedSimulatorUseCase(simulatorRepository)) { telemetry },
        )

    @Test
    fun `設定が有効かつテレメトリ受信中の場合trueになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(true)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            val useCase = createUseCase(flowOf(Unit))

            assertTrue(useCase().first { it })

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(keepScreenOnRepository, simulatorRepository)
        }

    @Test
    fun `設定が有効でもテレメトリ未受信の場合falseになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(true)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            val useCase = createUseCase(emptyFlow<Unit>())

            assertFalse(useCase().first())

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(keepScreenOnRepository, simulatorRepository)
        }

    @Test
    fun `テレメトリ受信中でも設定が無効な場合falseになる`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns flowOf(false)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            val useCase = createUseCase(flowOf(Unit))

            assertFalse(useCase().first())

            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(keepScreenOnRepository, simulatorRepository)
        }
}
