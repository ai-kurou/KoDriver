package kurou.kodriver.feature.gt7ps5debugstatedetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5DebugStateDetailViewModelTest {
    private val observeTelemetry: ObserveGt7Ps5UseCase = mockk()
    private val observeVehicleClass: ObserveGt7Ps5VehicleClassUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `未受信時はシミュレーターだけ有効でGT7の6枚を固定順で保持する`() =
        runTest {
            every { observeTelemetry() } returns emptyFlow()
            every { observeVehicleClass() } returns emptyFlow()
            val viewModel =
                Gt7Ps5DebugStateDetailViewModel(Gt7Ps5DebugStateUseCases(observeTelemetry, observeVehicleClass))
            val state = viewModel.uiState.first()

            assertEquals(Gt7Ps5DebugState(), state.gt7Ps5)
            assertEquals(setOf(Gt7Ps5DebugStateCardKey.SIMULATOR), state.enabledCardKeys)
            assertEquals(
                listOf(
                    Gt7Ps5DebugStateCardKey.SIMULATOR,
                    Gt7Ps5DebugStateCardKey.VEHICLE_CLASS,
                    Gt7Ps5DebugStateCardKey.CURRENT_LAP,
                    Gt7Ps5DebugStateCardKey.BEST_LAP,
                    Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE,
                    Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION,
                ),
                state.cardOrder,
            )
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleClass() }
            confirmVerified(observeTelemetry, observeVehicleClass)
        }

    @Test
    fun `車両クラスとテレメトリを受信すると対応カードを有効にして状態を更新する`() =
        runTest {
            val telemetry = MutableSharedFlow<Gt7Ps5TelemetryData>()
            val vehicleClass = MutableSharedFlow<Gt7Ps5VehicleClassData>()
            every { observeTelemetry() } returns telemetry
            every { observeVehicleClass() } returns vehicleClass
            val viewModel =
                Gt7Ps5DebugStateDetailViewModel(Gt7Ps5DebugStateUseCases(observeTelemetry, observeVehicleClass))
            testScheduler.runCurrent()
            assertEquals(1, telemetry.subscriptionCount.value)
            assertEquals(1, vehicleClass.subscriptionCount.value)

            val category = Gt7Ps5VehicleClassData("GR3")
            vehicleClass.emit(category)
            val classState = viewModel.uiState.first { it.gt7Ps5.vehicleClass == category }
            assertEquals(
                setOf(Gt7Ps5DebugStateCardKey.SIMULATOR, Gt7Ps5DebugStateCardKey.VEHICLE_CLASS),
                classState.enabledCardKeys,
            )
            assertEquals(null, classState.gt7Ps5.telemetry)

            val firstTelemetry = sampleGt7Ps5Telemetry()
            telemetry.emit(firstTelemetry)
            val fullState = viewModel.uiState.first { it.gt7Ps5.telemetry == firstTelemetry }
            assertEquals(gt7Ps5SupportedCardKeys, fullState.enabledCardKeys)
            assertEquals(Gt7Ps5DebugState(firstTelemetry, category), fullState.gt7Ps5)
            assertEquals(defaultDebugStateCardOrder, fullState.cardOrder)

            // 無効値を受信しても受信済みカードは無効に戻さない。
            val nextTelemetry = sampleGt7Ps5Telemetry(lapCount = 0, bestLapTimeMs = -1)
            telemetry.emit(nextTelemetry)
            vehicleClass.emit(Gt7Ps5VehicleClassData(""))
            val updated =
                viewModel.uiState.first {
                    it.gt7Ps5.telemetry == nextTelemetry && it.gt7Ps5.vehicleClass?.name == ""
                }
            assertEquals(gt7Ps5SupportedCardKeys, updated.enabledCardKeys)
            assertEquals(defaultDebugStateCardOrder, updated.cardOrder)
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleClass() }
            confirmVerified(observeTelemetry, observeVehicleClass)
        }

    @Test
    fun `テレメトリだけ受信すると車両クラス以外のカードを有効にする`() =
        runTest {
            val telemetry = MutableSharedFlow<Gt7Ps5TelemetryData>()
            every { observeTelemetry() } returns telemetry
            every { observeVehicleClass() } returns emptyFlow()
            val viewModel =
                Gt7Ps5DebugStateDetailViewModel(Gt7Ps5DebugStateUseCases(observeTelemetry, observeVehicleClass))
            testScheduler.runCurrent()
            telemetry.emit(sampleGt7Ps5Telemetry())
            val state = viewModel.uiState.first { it.gt7Ps5.telemetry != null }

            assertEquals(gt7Ps5SupportedCardKeys - Gt7Ps5DebugStateCardKey.VEHICLE_CLASS, state.enabledCardKeys)
            assertEquals(null, state.gt7Ps5.vehicleClass)
            verify(exactly = 1) { observeTelemetry() }
            verify(exactly = 1) { observeVehicleClass() }
            confirmVerified(observeTelemetry, observeVehicleClass)
        }
}
