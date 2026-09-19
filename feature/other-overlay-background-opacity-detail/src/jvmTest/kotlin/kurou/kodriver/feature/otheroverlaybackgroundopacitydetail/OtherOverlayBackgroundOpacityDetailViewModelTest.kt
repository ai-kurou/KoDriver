@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
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
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.SaveOverlayBackgroundOpacityUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherOverlayBackgroundOpacityDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: OverlayBackgroundOpacityPreferencesRepository = mockk()

    private val opacityFlow = MutableStateFlow(50)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherOverlayBackgroundOpacityDetailViewModel(
            observeOverlayBackgroundOpacity = ObserveOverlayBackgroundOpacityUseCase(repository),
            saveOverlayBackgroundOpacity = SaveOverlayBackgroundOpacityUseCase(repository),
        )

    @Test
    fun `保存済みの透明度をUiStateで返す`() =
        runTest {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            val viewModel = createViewModel()

            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(opacity = 50), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            confirmVerified(repository)
        }

    @Test
    fun `透明度を変更するとUiStateが更新される`() =
        runTest {
            every { repository.observeOverlayBackgroundOpacity() } returns opacityFlow
            coEvery { repository.saveOverlayBackgroundOpacity(80) } answers { opacityFlow.update { 80 } }
            val viewModel = createViewModel()

            viewModel.onOpacityChanged(80)

            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(opacity = 80), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeOverlayBackgroundOpacity() }
            coVerify(exactly = 1) { repository.saveOverlayBackgroundOpacity(80) }
            confirmVerified(repository)
        }
}
