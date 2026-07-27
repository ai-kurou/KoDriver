@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

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
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsReadoutRemainingFuelDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: AceWindowsRemainingFuelPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AceWindowsReadoutRemainingFuelDetailViewModel(
        observeThresholdPercentage = ObserveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
        saveThresholdPercentage = SaveAceWindowsRemainingFuelThresholdPercentageUseCase(repository),
    )

    @Test
    fun `初期状態はリポジトリのデフォルト値を反映したUiStateを返す`() = runTest {
        every { repository.observeThresholdPercentage() } returns MutableStateFlow(30)
        val viewModel = createViewModel()

        assertEquals(
            AceWindowsReadoutRemainingFuelDetailUiState(thresholdPercentage = 30),
            viewModel.uiState.first(),
        )
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdChangedを呼ぶとuiStateのthresholdPercentageが更新される`() = runTest {
        val thresholdFlow = MutableStateFlow(30)
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
        val viewModel = createViewModel()

        viewModel.onThresholdChanged(50)

        assertEquals(50, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
        confirmVerified(repository)
    }

    @Test
    fun `onThresholdResetを呼ぶとthresholdPercentageがデフォルト値30に戻る`() = runTest {
        val thresholdFlow = MutableStateFlow(30)
        every { repository.observeThresholdPercentage() } returns thresholdFlow
        coEvery { repository.saveThresholdPercentage(50) } answers { thresholdFlow.update { 50 } }
        coEvery { repository.saveThresholdPercentage(30) } answers { thresholdFlow.update { 30 } }
        val viewModel = createViewModel()

        viewModel.onThresholdChanged(50)
        viewModel.onThresholdReset()

        assertEquals(30, viewModel.uiState.first().thresholdPercentage)
        verify(exactly = 1) { repository.observeThresholdPercentage() }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(50) }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
        confirmVerified(repository)
    }
}
