@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeGt7Ps5RemainingFuelLapsPreferencesRepository
    private lateinit var viewModel: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeGt7Ps5RemainingFuelLapsPreferencesRepository()
        viewModel = Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
            observeGt7Ps5RemainingFuelLaps = ObserveGt7Ps5RemainingFuelLapsUseCase(repository),
            saveGt7Ps5RemainingFuelLaps = SaveGt7Ps5RemainingFuelLapsUseCase(repository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態は燃料残り周回数3のUiStateを返す`() = runTest {
        assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
    }

    @Test
    fun `onRemainingFuelLapsChangedに1を渡すと燃料残り周回数が1になる`() = runTest {
        viewModel.onRemainingFuelLapsChanged(1)

        assertEquals(1, viewModel.uiState.first().remainingFuelLaps)
    }

    @Test
    fun `onResetRemainingFuelLapsを呼ぶと燃料残り周回数が3になる`() = runTest {
        repository.saveRemainingFuelLaps(5)

        viewModel.onResetRemainingFuelLaps()

        assertEquals(3, viewModel.uiState.first().remainingFuelLaps)
    }
}
