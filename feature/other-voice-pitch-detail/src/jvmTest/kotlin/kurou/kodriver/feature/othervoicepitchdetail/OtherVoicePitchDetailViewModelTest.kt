package kurou.kodriver.feature.othervoicepitchdetail

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
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.SaveVoicePitchUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoicePitchDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: VoicePitchPreferencesRepository = mockk()

    private val pitchFlow = MutableStateFlow(1.5f)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVoicePitchDetailViewModel(
            observeVoicePitch = ObserveVoicePitchUseCase(repository),
            saveVoicePitch = SaveVoicePitchUseCase(repository),
        )

    @Test
    fun `購読前の初期状態はデフォルトの声の高さを返す`() =
        runTest {
            every { repository.voicePitch() } returns pitchFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoicePitchDetailUiState(pitch = 1.0f), viewModel.uiState.value)
            verify(exactly = 1) { repository.voicePitch() }
            confirmVerified(repository)
        }

    @Test
    fun `保存済みの声の高さをUiStateで返す`() =
        runTest {
            every { repository.voicePitch() } returns pitchFlow
            val viewModel = createViewModel()

            assertEquals(OtherVoicePitchDetailUiState(pitch = 1.5f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voicePitch() }
            confirmVerified(repository)
        }

    @Test
    fun `声の高さを変更するとUiStateが更新される`() =
        runTest {
            every { repository.voicePitch() } returns pitchFlow
            coEvery { repository.saveVoicePitch(0.7f) } answers { pitchFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onPitchChanged(0.7f)

            assertEquals(OtherVoicePitchDetailUiState(pitch = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voicePitch() }
            coVerify(exactly = 1) { repository.saveVoicePitch(0.7f) }
            confirmVerified(repository)
        }

    @Test
    fun `スライダーの浮動小数点誤差を0点1刻みに丸めて保存する`() =
        runTest {
            every { repository.voicePitch() } returns pitchFlow
            coEvery { repository.saveVoicePitch(0.7f) } answers { pitchFlow.update { 0.7f } }
            val viewModel = createViewModel()

            viewModel.onPitchChanged(0.70000005f)

            assertEquals(OtherVoicePitchDetailUiState(pitch = 0.7f), viewModel.uiState.first())
            verify(exactly = 1) { repository.voicePitch() }
            coVerify(exactly = 1) { repository.saveVoicePitch(0.7f) }
            confirmVerified(repository)
        }

    @Test
    fun `声の高さの下限と上限とデフォルト値を保存する`() =
        runTest {
            every { repository.voicePitch() } returns pitchFlow
            coEvery { repository.saveVoicePitch(0.5f) } answers { pitchFlow.update { 0.5f } }
            coEvery { repository.saveVoicePitch(2.0f) } answers { pitchFlow.update { 2.0f } }
            coEvery { repository.saveVoicePitch(1.0f) } answers { pitchFlow.update { 1.0f } }
            val viewModel = createViewModel()

            viewModel.onPitchChanged(0.5f)
            assertEquals(OtherVoicePitchDetailUiState(pitch = 0.5f), viewModel.uiState.first())
            viewModel.onPitchChanged(2.0f)
            assertEquals(OtherVoicePitchDetailUiState(pitch = 2.0f), viewModel.uiState.first())
            viewModel.onPitchChanged(1.0f)
            assertEquals(OtherVoicePitchDetailUiState(pitch = 1.0f), viewModel.uiState.first())

            verify(exactly = 1) { repository.voicePitch() }
            coVerify(exactly = 1) { repository.saveVoicePitch(0.5f) }
            coVerify(exactly = 1) { repository.saveVoicePitch(2.0f) }
            coVerify(exactly = 1) { repository.saveVoicePitch(1.0f) }
            confirmVerified(repository)
        }
}
