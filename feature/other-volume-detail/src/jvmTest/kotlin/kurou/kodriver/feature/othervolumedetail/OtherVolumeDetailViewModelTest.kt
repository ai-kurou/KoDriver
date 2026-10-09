package kurou.kodriver.feature.othervolumedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val soundVolumeRepository: SoundVolumePreferencesRepository = mockk()

    private val deviceVolumeRepository: DeviceVolumeRepository = mockk()

    private val speakText: SpeakTextUseCase = mockk()

    private val volumeFlow = MutableStateFlow(80)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVolumeDetailViewModel(
            soundVolumeUseCases =
                SoundVolumeUseCases(
                    observeSoundVolume = ObserveSoundVolumeUseCase(soundVolumeRepository),
                    saveSoundVolume = SaveSoundVolumeUseCase(soundVolumeRepository),
                ),
            deviceVolumeUseCases =
                DeviceVolumeUseCases(
                    getDeviceVolume = GetDeviceVolumeUseCase(deviceVolumeRepository),
                    setDeviceVolume = SetDeviceVolumeUseCase(deviceVolumeRepository),
                ),
            speakText = speakText,
        )

    @Test
    fun `保存済みの音量と端末のマスター音量をUiStateで返す`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val viewModel = createViewModel()

            assertEquals(OtherVolumeDetailUiState(volume = 80, deviceVolume = 60), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.getVolume() }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `音量を変更するとUiStateが更新される`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            coEvery { soundVolumeRepository.saveVolume(40) } answers { volumeFlow.update { 40 } }
            val viewModel = createViewModel()

            viewModel.onVolumeChanged(40)

            assertEquals(OtherVolumeDetailUiState(volume = 40, deviceVolume = 60), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.getVolume() }
            coVerify(exactly = 1) { soundVolumeRepository.saveVolume(40) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `端末のマスター音量を変更すると設定後に再取得してUiStateへ反映する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returnsMany listOf(60, 30)
            coEvery { deviceVolumeRepository.setVolume(30) } returns Unit
            val viewModel = createViewModel()
            assertEquals(60, viewModel.uiState.first().deviceVolume)

            viewModel.onDeviceVolumeChanged(30)

            assertEquals(OtherVolumeDetailUiState(volume = 80, deviceVolume = 30), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 2) { deviceVolumeRepository.getVolume() }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(30) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `OS側で音量が変更された後に同じ値を再要求すると再度書き込まれる`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            var deviceVolume = 60
            coEvery { deviceVolumeRepository.getVolume() } answers { deviceVolume }
            coEvery { deviceVolumeRepository.setVolume(50) } coAnswers { deviceVolume = 50 }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            runCurrent()

            viewModel.onDeviceVolumeChanged(50)
            runCurrent()
            assertEquals(50, viewModel.uiState.first().deviceVolume)

            deviceVolume = 30
            advanceTimeBy(500)
            runCurrent()
            assertEquals(30, viewModel.uiState.first().deviceVolume)

            viewModel.onDeviceVolumeChanged(50)
            runCurrent()
            assertEquals(50, viewModel.uiState.first().deviceVolume)

            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 4) { deviceVolumeRepository.getVolume() }
            coVerify(exactly = 2) { deviceVolumeRepository.setVolume(50) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `端末のマスター音量を連続して変更すると書き込みが直列に実行される`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            val callOrder = mutableListOf<String>()
            coEvery { deviceVolumeRepository.setVolume(20) } coAnswers {
                callOrder.add("start:20")
                delay(100)
                callOrder.add("end:20")
            }
            coEvery { deviceVolumeRepository.setVolume(80) } coAnswers {
                callOrder.add("start:80")
            }
            val viewModel = createViewModel()

            viewModel.onDeviceVolumeChanged(20)
            viewModel.onDeviceVolumeChanged(80)
            advanceUntilIdle()

            assertEquals(listOf("start:20", "end:20", "start:80"), callOrder)
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(20) }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(80) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `試聴は既定文言と保存済み音量を使い完了後に停止状態へ戻る`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val finished = CompletableDeferred<Unit>()
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 80) } coAnswers { finished.await() }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)

            viewModel.onPreviewClicked()
            assertTrue(viewModel.uiState.first().isPreviewing)
            finished.complete(Unit)
            runCurrent()
            assertFalse(viewModel.uiState.first().isPreviewing)

            coVerify(exactly = 1) { speakText("自己ベストラップ更新 1分23秒456", volume = 80) }
            confirmVerified(speakText)
        }

    @Test
    fun `試聴中の再タップとペイン離脱で再生をキャンセルする`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            var cancelled = 0
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 80) } coAnswers {
                try {
                    awaitCancellation()
                } finally {
                    cancelled++
                }
            }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)

            viewModel.onPreviewClicked()
            assertTrue(viewModel.uiState.first().isPreviewing)
            viewModel.onPreviewClicked()
            assertFalse(viewModel.uiState.first().isPreviewing)
            viewModel.onPreviewClicked()
            viewModel.onPreviewStopped()
            assertFalse(viewModel.uiState.first().isPreviewing)
            assertEquals(2, cancelled)

            coVerify(exactly = 2) { speakText("自己ベストラップ更新 1分23秒456", volume = 80) }
            confirmVerified(speakText)
        }

    @Test
    fun `音量ゼロでは試聴せず失敗時も停止状態へ戻る`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 80) } throws IOException("失敗")
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            volumeFlow.update { 0 }
            viewModel.onPreviewClicked()
            assertFalse(viewModel.uiState.first().isPreviewing)
            coVerify(exactly = 0) { speakText("自己ベストラップ更新 1分23秒456", volume = 0) }

            volumeFlow.update { 80 }
            viewModel.onPreviewClicked()
            assertFalse(viewModel.uiState.first().isPreviewing)
            coVerify(exactly = 1) { speakText("自己ベストラップ更新 1分23秒456", volume = 80) }
            confirmVerified(speakText)
        }

    @Test
    fun `直前の音量保存が終わってから変更後の音量で試聴する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            val saved = CompletableDeferred<Unit>()
            coEvery { soundVolumeRepository.saveVolume(40) } coAnswers {
                saved.await()
                volumeFlow.update { 40 }
            }
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 40) } returns Unit
            val viewModel = createViewModel()
            viewModel.onVolumeChanged(40)
            viewModel.onPreviewClicked()
            coVerify(exactly = 0) { speakText("自己ベストラップ更新 1分23秒456", volume = 40) }
            saved.complete(Unit)
            runCurrent()

            coVerify(exactly = 1) { soundVolumeRepository.saveVolume(40) }
            coVerify(exactly = 1) { speakText("自己ベストラップ更新 1分23秒456", volume = 40) }
            verify(exactly = 2) { soundVolumeRepository.volume() }
            confirmVerified(soundVolumeRepository, speakText)
        }

    @Test
    fun `試聴のキャンセル例外を再スローして停止状態へ戻る`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val cancelled = CancellationException("キャンセル")
            var completion: Throwable? = null
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 80) } coAnswers {
                currentCoroutineContext()[Job]!!.invokeOnCompletion { completion = it }
                throw cancelled
            }
            val viewModel = createViewModel()
            viewModel.onPreviewClicked()
            assertEquals(cancelled, completion)
            assertFalse(viewModel.uiState.first().isPreviewing)
            coVerify(exactly = 1) { speakText("自己ベストラップ更新 1分23秒456", volume = 80) }
            confirmVerified(speakText)
        }

    @Test
    fun `古い試聴の終了が新しい試聴の状態を解除しない`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val oldFinished = CompletableDeferred<Unit>()
            var count = 0
            coEvery { speakText("自己ベストラップ更新 1分23秒456", volume = 80) } coAnswers {
                if (++count == 1) {
                    // 外部処理がキャンセルに協調せず、停止後に正常終了する状況を再現する。
                    withContext(NonCancellable) { oldFinished.await() }
                } else {
                    awaitCancellation()
                }
            }
            val viewModel = createViewModel()
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.onPreviewClicked()
            viewModel.onPreviewStopped()
            viewModel.onPreviewClicked()
            oldFinished.complete(Unit)
            runCurrent()
            assertTrue(viewModel.uiState.first().isPreviewing)
            viewModel.onPreviewStopped()
            coVerify(exactly = 2) { speakText("自己ベストラップ更新 1分23秒456", volume = 80) }
            confirmVerified(speakText)
        }

    @Test
    fun `detailPane表示中は一定間隔で端末のマスター音量を再取得する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(this)
            runCurrent()

            advanceTimeBy(500)
            runCurrent()
            advanceTimeBy(500)
            runCurrent()

            coVerify(exactly = 3) { deviceVolumeRepository.getVolume() }
            verify(exactly = 1) { soundVolumeRepository.volume() }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
            job.cancel()
        }
}
