package kurou.kodriver.feature.othervoicespeeddetail

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
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.SaveVoiceSpeedUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceSpeedDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: VoiceSpeedPreferencesRepository = mockk()

    private val speedFlow = MutableStateFlow(1.5f)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVoiceSpeedDetailViewModel(
            observeVoiceSpeed = ObserveVoiceSpeedUseCase(repository),
            saveVoiceSpeed = SaveVoiceSpeedUseCase(repository),
        )

    @Test
    fun `購読前の初期状態はデフォルト速度を返す`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.0f), viewModel.uiState.value)
            verify(exactly = 1) { repository.voiceSpeed() }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みの速度をUiStateで返す`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.5f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            confirmVerified(repository)
        }

    @Test
    fun `速度を変更するとUiStateが更新される`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            coEvery { repository.saveVoiceSpeed(0.7f) } answers { speedFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onSpeedChanged(0.7f)

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(0.7f) }
            confirmVerified(repository)
        }

    @Test
    fun `スライダーの浮動小数点誤差を0点1刻みに丸めて保存する`() =
        runTest {
            every { repository.voiceSpeed() } returns speedFlow
            coEvery { repository.saveVoiceSpeed(0.7f) } answers { speedFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onSpeedChanged(0.70000005f)

            assertEquals(OtherVoiceSpeedDetailUiState(speed = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voiceSpeed() }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(0.7f) }
            confirmVerified(repository)
        }
}
