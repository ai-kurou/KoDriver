package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.usecase.SaveMicrophoneCutQuietSoundLevelUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherMicrophoneCutQuietSoundDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: MicrophoneCutQuietSoundPreferencesRepository = mockk()

    private val levelFlow = MutableStateFlow(6)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherMicrophoneCutQuietSoundDetailViewModel(
            observeMicrophoneCutQuietSoundLevel = ObserveMicrophoneCutQuietSoundLevelUseCase(repository),
            saveMicrophoneCutQuietSoundLevel = SaveMicrophoneCutQuietSoundLevelUseCase(repository),
        )

    @Test
    fun `保存済みの段階値をUiStateで返す`() =
        runTest {
            every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
            val viewModel = createViewModel()

            assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(level = 6), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
            confirmVerified(repository)
        }

    @Test
    fun `段階値を変更すると保存されUiStateが更新される`() =
        runTest {
            every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
            coEvery { repository.saveMicrophoneCutQuietSoundLevel(9) } answers { levelFlow.update { 9 } }
            val viewModel = createViewModel()

            viewModel.onLevelChanged(9)

            assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(level = 9), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
            coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundLevel(9) }
            confirmVerified(repository)
        }

    @Test
    fun `保存に失敗したらエラーを記録する`() =
        runTest {
            val error = IllegalStateException("failed")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
                coEvery { repository.saveMicrophoneCutQuietSoundLevel(9) } throws error
                val viewModel = createViewModel()

                viewModel.onLevelChanged(9)

                verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
                coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundLevel(9) }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(repository, errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }
}
