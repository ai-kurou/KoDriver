package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailViewModelTest {
    private val repository: AndroidAudioUsagePreferencesRepository = mockk()
    private val usageFlow = MutableStateFlow(AndroidAudioUsage.ALARM)
    private val speakText: SpeakTextUseCase = mockk()
    private val observeSoundVolume: ObserveSoundVolumeUseCase = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `購読前は既定値で購読後は保存済み用途と変更を表示する`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val viewModel = createViewModel()
            try {
                assertEquals(OtherAndroidAudioUsageDetailUiState(), viewModel.uiState.value)
                viewModel.uiState.launchIn(backgroundScope)
                runCurrent()
                assertEquals(AndroidAudioUsage.ALARM, viewModel.uiState.value.usage)
                usageFlow.update { AndroidAudioUsage.COMMUNICATION }
                runCurrent()
                assertEquals(AndroidAudioUsage.COMMUNICATION, viewModel.uiState.value.usage)
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `全用途の選択を保存して監視結果に反映する`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            AndroidAudioUsage.entries.forEach { usage ->
                coEvery { repository.saveAndroidAudioUsage(usage) } answers { usageFlow.update { usage } }
            }
            val viewModel = createViewModel()
            try {
                viewModel.uiState.launchIn(backgroundScope)
                runCurrent()
                AndroidAudioUsage.entries.forEach { usage ->
                    viewModel.onUsageSelected(usage)
                    runCurrent()
                    assertEquals(usage, viewModel.uiState.value.usage)
                    coVerify(exactly = 1) { repository.saveAndroidAudioUsage(usage) }
                }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    private fun createViewModel() =
        OtherAndroidAudioUsageDetailViewModel(
            ObserveAndroidAudioUsageUseCase(repository),
            SaveAndroidAudioUsageUseCase(repository),
            speakText,
            observeSoundVolume,
        )

    @Test
    fun `試聴は保存済み音量で再生中状態を設定し同じ用途の再押下で停止する`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            every { observeSoundVolume() } returns MutableStateFlow(65)
            coEvery { speakText("試聴文", volume = 65, audioUsage = AndroidAudioUsage.ALARM) } coAnswers
                { awaitCancellation() }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            try {
                viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "試聴文")
                runCurrent()
                assertEquals(AndroidAudioUsage.ALARM, viewModel.uiState.value.previewingUsage)
                viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "試聴文")
                runCurrent()
                assertEquals(null, viewModel.uiState.value.previewingUsage)
                verify(exactly = 1) { observeSoundVolume() }
                coVerify(exactly = 1) { speakText("試聴文", volume = 65, audioUsage = AndroidAudioUsage.ALARM) }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository, speakText, observeSoundVolume)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `ゼロ音量では再生せず失敗しても試聴操作を継続できる`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            every { observeSoundVolume() } returnsMany listOf(MutableStateFlow(0), MutableStateFlow(35))
            coEvery { speakText("試聴文", volume = 35, audioUsage = AndroidAudioUsage.MEDIA) } throws
                IllegalStateException("再生失敗")
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            try {
                viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "試聴文")
                runCurrent()
                assertEquals(null, viewModel.uiState.value.previewingUsage)
                viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "試聴文")
                runCurrent()
                assertEquals(null, viewModel.uiState.value.previewingUsage)
                verify(exactly = 2) { observeSoundVolume() }
                coVerify(exactly = 0) { speakText("試聴文", volume = 0, audioUsage = AndroidAudioUsage.ALARM) }
                coVerify(exactly = 1) { speakText("試聴文", volume = 35, audioUsage = AndroidAudioUsage.MEDIA) }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository, speakText, observeSoundVolume)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `別用途の開始で前の試聴を切り替え停止要求で状態を消す`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            every { observeSoundVolume() } returns MutableStateFlow(80)
            coEvery { speakText("試聴文", volume = 80, audioUsage = AndroidAudioUsage.ALARM) } coAnswers
                { awaitCancellation() }
            coEvery { speakText("試聴文", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) } coAnswers
                { awaitCancellation() }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            try {
                viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "試聴文")
                runCurrent()
                viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "試聴文")
                runCurrent()
                assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.value.previewingUsage)
                viewModel.onPreviewStopped()
                assertEquals(null, viewModel.uiState.value.previewingUsage)
                verify(exactly = 2) { observeSoundVolume() }
                coVerify(exactly = 1) { speakText("試聴文", volume = 80, audioUsage = AndroidAudioUsage.ALARM) }
                coVerify(exactly = 1) { speakText("試聴文", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository, speakText, observeSoundVolume)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `古い再生の遅延終了で新しい用途の状態を消さない`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns MutableStateFlow(80)
            coEvery { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) } coAnswers {
                withContext(NonCancellable) { oldFinish.await() }
            }
            coEvery { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) } coAnswers
                { newFinish.await() }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            try {
                viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "古い")
                runCurrent()
                viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "新しい")
                runCurrent()
                oldFinish.complete(Unit)
                runCurrent()
                assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.value.previewingUsage)
                newFinish.complete(Unit)
                runCurrent()
                assertEquals(null, viewModel.uiState.value.previewingUsage)
                verify(exactly = 2) { observeSoundVolume() }
                coVerify(exactly = 1) { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) }
                coVerify(exactly = 1) { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) }
                verify(exactly = 1) { repository.observeAndroidAudioUsage() }
                confirmVerified(repository, speakText, observeSoundVolume)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
