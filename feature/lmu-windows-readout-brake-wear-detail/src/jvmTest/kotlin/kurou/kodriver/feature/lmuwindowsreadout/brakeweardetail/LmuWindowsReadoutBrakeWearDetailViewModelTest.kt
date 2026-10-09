package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeWearDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val wearRepository: LmuWindowsBrakeWearRepository = mockk()
    private val classRepository: LmuWindowsVehicleClassRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutBrakeWearDetailViewModel(
            ObserveLmuWindowsBrakeWearRemainingUseCase(wearRepository, classRepository),
        )

    private fun wear(thickness: Float) =
        LmuWindowsBrakeWearData(wheels = WheelIndex.entries.associateWith { BrakeThicknessMeters(thickness) })

    @Test
    fun `値を取得するまではremainingがnull`() =
        runTest {
            every { wearRepository.brakeWearStream() } returns MutableSharedFlow()
            every { classRepository.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertNull(viewModel.uiState.value.remaining)
            verify(exactly = 1) { wearRepository.brakeWearStream() }
            verify(exactly = 1) { classRepository.vehicleClassStream() }
            confirmVerified(wearRepository, classRepository)
        }

    @Test
    fun `取得した厚さから計算した残量がuiStateに反映される`() =
        runTest {
            val wears = MutableSharedFlow<LmuWindowsBrakeWearData>()
            every { wearRepository.brakeWearStream() } returns wears
            every { classRepository.vehicleClassStream() } returns flowOf(LmuWindowsVehicleClassData.Hypercar)
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            wears.emit(wear(0.036f))
            wears.emit(wear(0.0305f))

            val state = viewModel.uiState.first { it.remaining != null }
            val frontLeft = state.remaining?.wheels?.get(WheelIndex.FRONT_LEFT)
            assertEquals(50, frontLeft?.remainingPercent)
            assertEquals(BrakeThicknessMeters(0.0305f), frontLeft?.thickness)
            verify(exactly = 1) { wearRepository.brakeWearStream() }
            verify(exactly = 1) { classRepository.vehicleClassStream() }
            confirmVerified(wearRepository, classRepository)
        }
}
