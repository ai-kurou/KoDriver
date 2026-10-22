package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTelemetryReceivingUseCaseTest {
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()

    @Test
    fun `受信がない場合はfalseを返す`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val useCase =
                ObserveTelemetryReceivingUseCase(
                    ObserveSelectedSimulatorUseCase(simulatorRepository),
                ) { emptyFlow<Int>() }
            assertFalse(useCase().first())
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(simulatorRepository)
        }

    @Test
    fun `受信開始とタイムアウトを通知する`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val telemetry = MutableSharedFlow<Int>(replay = 1)
            telemetry.tryEmit(1)
            val useCase =
                ObserveTelemetryReceivingUseCase(ObserveSelectedSimulatorUseCase(simulatorRepository)) { telemetry }
            assertEquals(listOf(false, true, false), useCase().take(3).toList())
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(simulatorRepository)
        }

    @Test
    fun `シミュレーター切替時に対応するFlowへ切り替える`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            every { simulatorRepository.selectedSimulator() } returns selectedSimulator
            val subscribedSimulators = mutableListOf<Simulator>()
            val useCase =
                ObserveTelemetryReceivingUseCase(ObserveSelectedSimulatorUseCase(simulatorRepository)) { simulator ->
                    subscribedSimulators.add(simulator)
                    emptyFlow<Int>()
                }
            val job = launch { useCase().collect {} }
            runCurrent()
            selectedSimulator.value = Simulator.Gt7Ps5
            runCurrent()
            job.cancel()

            assertEquals(listOf(Simulator.LmuWindows, Simulator.Gt7Ps5), subscribedSimulators)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(simulatorRepository)
        }

    @Test
    fun `新しい受信からタイムアウトをやり直す`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            val telemetry = MutableSharedFlow<Int>(replay = 1)
            telemetry.tryEmit(1)
            val useCase =
                ObserveTelemetryReceivingUseCase(ObserveSelectedSimulatorUseCase(simulatorRepository)) { telemetry }
            val values = mutableListOf<Boolean>()
            val job = launch { useCase().collect(values::add) }
            advanceTimeBy(2_000)
            telemetry.emit(2)
            advanceTimeBy(2_000)
            assertTrue(values.last())
            job.cancel()
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(simulatorRepository)
        }
}
