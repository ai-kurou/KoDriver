package kurou.kodriver.feature.otherandroidaudiousagedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
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

@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailPreviewLifecycleTest {
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

    private fun createViewModel() =
        OtherAndroidAudioUsageDetailViewModel(
            ObserveAndroidAudioUsageUseCase(repository),
            SaveAndroidAudioUsageUseCase(repository),
            speakText,
            observeSoundVolume,
        )

    @Test
    fun `古い試聴が正常終了しても新しい試聴状態を消さない`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns MutableStateFlow(80)
            coEvery { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) } coAnswers {
                withContext(NonCancellable) { oldFinish.await() }
            }
            coEvery { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) } coAnswers {
                newFinish.await()
            }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "古い")
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "新しい")
            runCurrent()
            assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.first().previewingUsage)
            oldFinish.complete(Unit)
            runCurrent()
            assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.first().previewingUsage)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingUsage)
            subscription.cancel()
            verify(exactly = 1) { repository.observeAndroidAudioUsage() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) }
            coVerify(exactly = 1) { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `古い試聴が例外終了しても新しい試聴状態を消さない`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns MutableStateFlow(80)
            coEvery { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) } coAnswers {
                withContext(NonCancellable) { oldFinish.await() }
                throw IllegalStateException("古い試聴の失敗")
            }
            coEvery { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) } coAnswers {
                newFinish.await()
            }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "古い")
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "新しい")
            runCurrent()
            assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.first().previewingUsage)
            oldFinish.complete(Unit)
            runCurrent()
            assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.first().previewingUsage)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingUsage)
            subscription.cancel()
            verify(exactly = 1) { repository.observeAndroidAudioUsage() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) }
            coVerify(exactly = 1) { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `古い試聴の遅延音量0で新しい試聴状態を消さない`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val newFinish = CompletableDeferred<Unit>()
            val viewModel = createViewModel()
            every { observeSoundVolume() } returnsMany
                listOf(
                    object : Flow<Int> {
                        override suspend fun collect(collector: FlowCollector<Int>) {
                            viewModel.onPreviewClicked(AndroidAudioUsage.MEDIA, "新しい")
                            collector.emit(0)
                        }
                    },
                    MutableStateFlow(80),
                )
            coEvery { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) } coAnswers {
                newFinish.await()
            }
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "古い")
            runCurrent()
            assertEquals(AndroidAudioUsage.MEDIA, viewModel.uiState.first().previewingUsage)
            newFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingUsage)
            subscription.cancel()
            verify(exactly = 1) { repository.observeAndroidAudioUsage() }
            verify(exactly = 2) { observeSoundVolume() }
            coVerify(exactly = 0) { speakText("古い", volume = 0, audioUsage = AndroidAudioUsage.ALARM) }
            coVerify(exactly = 1) { speakText("新しい", volume = 80, audioUsage = AndroidAudioUsage.MEDIA) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }

    @Test
    fun `画面離脱後に古い試聴が終了しても状態と再生を戻さない`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns usageFlow
            val oldFinish = CompletableDeferred<Unit>()
            every { observeSoundVolume() } returns MutableStateFlow(80)
            coEvery { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) } coAnswers {
                withContext(NonCancellable) { oldFinish.await() }
            }
            val viewModel = createViewModel()
            val subscription = viewModel.uiState.launchIn(backgroundScope)
            runCurrent()
            viewModel.onPreviewClicked(AndroidAudioUsage.ALARM, "古い")
            runCurrent()
            assertEquals(AndroidAudioUsage.ALARM, viewModel.uiState.first().previewingUsage)
            viewModel.onPreviewStopped()
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingUsage)
            oldFinish.complete(Unit)
            runCurrent()
            assertEquals(null, viewModel.uiState.first().previewingUsage)
            subscription.cancel()
            verify(exactly = 1) { repository.observeAndroidAudioUsage() }
            verify(exactly = 1) { observeSoundVolume() }
            coVerify(exactly = 1) { speakText("古い", volume = 80, audioUsage = AndroidAudioUsage.ALARM) }
            confirmVerified(repository, observeSoundVolume, speakText)
        }
}
