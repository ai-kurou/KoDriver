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
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kurou.kodriver.domain.microphone.usecase.ObserveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.microphone.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.microphone.usecase.SaveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.microphone.usecase.SaveMicrophoneCutQuietSoundLevelUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherMicrophoneCutQuietSoundDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: MicrophoneCutQuietSoundPreferencesRepository = mockk()

    private val levelFlow = MutableStateFlow(6)

    private val cutInCommandFlow = MutableStateFlow(false)

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
            observeMicrophoneCutQuietSoundInCommand = ObserveMicrophoneCutQuietSoundInCommandUseCase(repository),
            saveMicrophoneCutQuietSoundInCommand = SaveMicrophoneCutQuietSoundInCommandUseCase(repository),
        )

    @Test
    fun `保存済みの段階値をUiStateで返す`() =
        runTest {
            every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
            every { repository.observeMicrophoneCutQuietSoundInCommand() } returns cutInCommandFlow
            val viewModel = createViewModel()

            assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(level = 6), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundInCommand() }
            confirmVerified(repository)
        }

    @Test
    fun `段階値を変更すると保存されUiStateが更新される`() =
        runTest {
            every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
            every { repository.observeMicrophoneCutQuietSoundInCommand() } returns cutInCommandFlow
            coEvery { repository.saveMicrophoneCutQuietSoundLevel(9) } answers { levelFlow.update { 9 } }
            val viewModel = createViewModel()

            viewModel.onLevelChanged(9)

            assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(level = 9), viewModel.uiState.first())
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundInCommand() }
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
                every { repository.observeMicrophoneCutQuietSoundInCommand() } returns cutInCommandFlow
                coEvery { repository.saveMicrophoneCutQuietSoundLevel(9) } throws error
                val viewModel = createViewModel()

                viewModel.onLevelChanged(9)

                verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
                verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundInCommand() }
                coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundLevel(9) }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(repository, errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `コマンド受付中もカットする設定を変更すると保存されUiStateが更新される`() =
        runTest {
            every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
            every { repository.observeMicrophoneCutQuietSoundInCommand() } returns cutInCommandFlow
            coEvery { repository.saveMicrophoneCutQuietSoundInCommand(true) } answers {
                cutInCommandFlow.update { true }
            }
            val viewModel = createViewModel()

            viewModel.onCutInCommandChanged(true)

            assertEquals(
                OtherMicrophoneCutQuietSoundDetailUiState(level = 6, cutInCommand = true),
                viewModel.uiState.first(),
            )
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
            verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundInCommand() }
            coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundInCommand(true) }
            confirmVerified(repository)
        }

    @Test
    fun `コマンド受付中もカットする設定の保存に失敗したらエラーを記録する`() =
        runTest {
            val error = IllegalStateException("failed")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                every { repository.observeMicrophoneCutQuietSoundLevel() } returns levelFlow
                every { repository.observeMicrophoneCutQuietSoundInCommand() } returns cutInCommandFlow
                coEvery { repository.saveMicrophoneCutQuietSoundInCommand(true) } throws error
                val viewModel = createViewModel()

                viewModel.onCutInCommandChanged(true)

                verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundLevel() }
                verify(exactly = 1) { repository.observeMicrophoneCutQuietSoundInCommand() }
                coVerify(exactly = 1) { repository.saveMicrophoneCutQuietSoundInCommand(true) }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(repository, errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }
}
