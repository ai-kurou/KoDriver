@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsreadout.raindetail

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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRainEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRainEnabledStateUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutRainDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: LmuWindowsRainPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        LmuWindowsReadoutRainDetailViewModel(
            observeRainEnabledStates = ObserveLmuWindowsRainEnabledStatesUseCase(repository),
            saveRainEnabledState = SaveLmuWindowsRainEnabledStateUseCase(repository),
        )

    @Test
    fun `初期状態は降り始めの読み上げのデフォルトtrueを反映したUiStateを返す`() =
        runTest {
            every { repository.observeRainEnabledStates() } returns MutableStateFlow(emptyMap())
            val viewModel = createViewModel()

            assertEquals(
                LmuWindowsReadoutRainDetailUiState(startReadoutEnabled = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeRainEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `onStartReadoutEnabledChangedを呼ぶとuiStateのstartReadoutEnabledが更新される`() =
        runTest {
            val statesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeRainEnabledStates() } returns statesFlow
            coEvery {
                repository.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)
            } answers {
                statesFlow.update { it + (ReadoutItemKey.LmuWindows.Rain.Start to false) }
            }
            val viewModel = createViewModel()

            viewModel.onStartReadoutEnabledChanged(false)

            assertEquals(false, viewModel.uiState.first().startReadoutEnabled)
            verify(exactly = 1) { repository.observeRainEnabledStates() }
            coVerify(exactly = 1) {
                repository.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)
            }
            confirmVerified(repository)
        }
}
