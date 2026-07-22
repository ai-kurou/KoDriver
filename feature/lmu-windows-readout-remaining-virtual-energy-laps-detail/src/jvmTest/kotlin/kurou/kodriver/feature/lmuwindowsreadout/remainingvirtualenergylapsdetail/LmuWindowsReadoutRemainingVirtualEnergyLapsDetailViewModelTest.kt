@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

    private val remainingVirtualEnergyLapsFlow = MutableStateFlow(3)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModel(
        observeLmuWindowsRemainingVirtualEnergyLaps = ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository),
        saveLmuWindowsRemainingVirtualEnergyLaps = SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository),
    )

    @Test
    fun `初期状態はバーチャルエナジー残り周回数3のUiStateを返す`() = runTest {
        every { repository.observeRemainingVirtualEnergyLaps() } returns remainingVirtualEnergyLapsFlow
        val viewModel = createViewModel()

        assertEquals(3, viewModel.uiState.first().remainingVirtualEnergyLaps)
        verify(exactly = 1) { repository.observeRemainingVirtualEnergyLaps() }
        confirmVerified(repository)
    }

    @Test
    fun `onRemainingVirtualEnergyLapsChangedに1を渡すとバーチャルエナジー残り周回数が1になる`() = runTest {
        every { repository.observeRemainingVirtualEnergyLaps() } returns remainingVirtualEnergyLapsFlow
        coEvery { repository.saveRemainingVirtualEnergyLaps(1) } answers {
            remainingVirtualEnergyLapsFlow.update { 1 }
        }
        val viewModel = createViewModel()

        viewModel.onRemainingVirtualEnergyLapsChanged(1)

        assertEquals(1, viewModel.uiState.first().remainingVirtualEnergyLaps)
        verify(exactly = 1) { repository.observeRemainingVirtualEnergyLaps() }
        coVerify(exactly = 1) { repository.saveRemainingVirtualEnergyLaps(1) }
        confirmVerified(repository)
    }

    @Test
    fun `onResetRemainingVirtualEnergyLapsを呼ぶとバーチャルエナジー残り周回数が3になる`() = runTest {
        remainingVirtualEnergyLapsFlow.update { 5 }
        every { repository.observeRemainingVirtualEnergyLaps() } returns remainingVirtualEnergyLapsFlow
        coEvery { repository.saveRemainingVirtualEnergyLaps(3) } answers {
            remainingVirtualEnergyLapsFlow.update { 3 }
        }
        val viewModel = createViewModel()

        viewModel.onResetRemainingVirtualEnergyLaps()

        assertEquals(3, viewModel.uiState.first().remainingVirtualEnergyLaps)
        verify(exactly = 1) { repository.observeRemainingVirtualEnergyLaps() }
        coVerify(exactly = 1) { repository.saveRemainingVirtualEnergyLaps(3) }
        confirmVerified(repository)
    }
}
