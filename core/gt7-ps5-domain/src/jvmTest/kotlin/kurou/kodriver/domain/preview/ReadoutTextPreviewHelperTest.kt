package kurou.kodriver.domain.preview

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutTextPreviewHelperTest {
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root

    private fun TestScope.helper() =
        ReadoutTextPreviewHelper(backgroundScope, checkAvailable, observeVolume, playStartSound, speakText)

    @Test
    fun `利用可否は初期値falseから購読なしで一度取得する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            val helper = helper()
            assertFalse(helper.textToSpeechAvailable.value)
            helper.preview("試聴", key)
            runCurrent()
            assertTrue(helper.textToSpeechAvailable.value)
            runCurrent()
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 0) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `空白文言と利用不可では音量も再生も取得しない`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val unavailable = helper()
            runCurrent()
            unavailable.preview("試聴", key)
            coEvery { checkAvailable() } returns true
            val available = helper()
            runCurrent()
            available.preview("", key)
            available.preview("  ", key)
            coVerify(exactly = 2) { checkAvailable() }
            verify(exactly = 0) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `音量ゼロと負数では開始音も本文も再生しない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(0), flowOf(-1))
            val helper = helper()
            runCurrent()
            repeat(2) { helper.preview("試聴", key) }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `項目の開始音完了後に本文を現在の音量で再生する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(1), flowOf(42))
            val completed = CompletableDeferred<Unit>()
            coEvery { playStartSound(key) } coAnswers { completed.await() }
            coEvery { speakText("試聴", volume = 1) } returns Unit
            coEvery { speakText("次の試聴", volume = 42) } returns Unit
            val helper = helper()
            runCurrent()
            val job = launch { helper.preview("試聴", key) }
            runCurrent()
            coVerify(exactly = 0) { speakText("試聴", volume = 1) }
            completed.complete(Unit)
            job.join()
            helper.preview("次の試聴", key)
            coVerifyOrder {
                playStartSound(key)
                speakText("試聴", volume = 1)
                playStartSound(key)
                speakText("次の試聴", volume = 42)
            }
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("試聴", volume = 1) }
            coVerify(exactly = 1) { speakText("次の試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `開始音のキャンセルと失敗は呼び出し元に伝播し本文を再生しない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            val helper = helper()
            runCurrent()
            val cancellation = CancellationException("cancelled")
            coEvery { playStartSound(key) } throws cancellation
            assertEquals(cancellation, assertFailsWith<CancellationException> { helper.preview("試聴", key) })
            val failure = IllegalStateException("failed")
            coEvery { playStartSound(key) } throws failure
            assertEquals(failure, assertFailsWith<IllegalStateException> { helper.preview("試聴", key) })
            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `試聴中の再押しは本文をキャンセルして停止する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            coEvery { playStartSound(key) } returns Unit
            var speakJob: Job? = null
            coEvery { speakText("試聴", volume = 42) } coAnswers {
                speakJob = currentCoroutineContext()[Job]
                CompletableDeferred<Unit>().await()
            }
            val helper = helper()
            assertFalse(helper.isPreviewing.value)
            helper.stop()
            runCurrent()

            helper.onPreviewClicked("試聴", key)
            runCurrent()
            assertTrue(helper.isPreviewing.value)
            helper.onPreviewClicked("次の試聴", key)
            assertFalse(helper.isPreviewing.value)
            runCurrent()
            assertTrue(requireNotNull(speakJob).isCancelled)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            coVerify(exactly = 0) { speakText("次の試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `開始待ちの連打は前ジョブをキャンセルして最新の本文だけ再生する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            var waitingJob: Job? = null
            val waitingVolume =
                flow {
                    waitingJob = currentCoroutineContext()[Job]
                    CompletableDeferred<Unit>().await()
                    emit(42)
                }
            every { observeVolume() } returnsMany listOf(waitingVolume, flowOf(42))
            coEvery { playStartSound(key) } returns Unit
            coEvery { speakText("最新の試聴", volume = 42) } returns Unit
            val helper = helper()
            runCurrent()

            helper.onPreviewClicked("最初の試聴", key)
            runCurrent()
            assertFalse(helper.isPreviewing.value)
            helper.onPreviewClicked("中間の試聴", key)
            helper.onPreviewClicked("最新の試聴", key)
            runCurrent()
            assertTrue(requireNotNull(waitingJob).isCancelled)
            assertFalse(helper.isPreviewing.value)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(key) }
            coVerify(exactly = 0) { speakText("最初の試聴", volume = 42) }
            coVerify(exactly = 0) { speakText("中間の試聴", volume = 42) }
            coVerify(exactly = 1) { speakText("最新の試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `開始音の再生中にstopするとキャンセルされ本文は再生されない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            var startJob: Job? = null
            coEvery { playStartSound(key) } coAnswers {
                startJob = currentCoroutineContext()[Job]
                CompletableDeferred<Unit>().await()
            }
            val helper = helper()
            runCurrent()

            helper.onPreviewClicked("試聴", key)
            runCurrent()
            assertTrue(helper.isPreviewing.value)
            helper.stop()
            assertFalse(helper.isPreviewing.value)
            runCurrent()
            assertTrue(requireNotNull(startJob).isCancelled)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(key) }
            coVerify(exactly = 0) { speakText("試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `クリックしても利用不可や空白や音量0以下では試聴中にならない`() =
        runTest {
            coEvery { checkAvailable() } returns false
            val unavailable = helper()
            val unavailableStates = mutableListOf<Boolean>()
            val unavailableSubscription =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    unavailable.isPreviewing.collect { unavailableStates.add(it) }
                }
            runCurrent()
            unavailable.onPreviewClicked("試聴", key)
            runCurrent()
            assertFalse(unavailable.isPreviewing.value)

            coEvery { checkAvailable() } returns true
            every { observeVolume() } returnsMany listOf(flowOf(0), flowOf(-1))
            val available = helper()
            val availableStates = mutableListOf<Boolean>()
            val availableSubscription =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    available.isPreviewing.collect { availableStates.add(it) }
                }
            runCurrent()
            listOf("", "  ", "試聴", "試聴").forEach { text ->
                available.onPreviewClicked(text, key)
                runCurrent()
                assertFalse(available.isPreviewing.value)
            }

            assertEquals(listOf(false), unavailableStates)
            assertEquals(listOf(false), availableStates)
            unavailableSubscription.cancel()
            availableSubscription.cancel()
            coVerify(exactly = 2) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `開始音と本文の失敗を握りつぶして試聴中を解除する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            coEvery { playStartSound(key) } throws IllegalStateException("開始音失敗")
            val helper = helper()
            runCurrent()
            helper.onPreviewClicked("試聴", key)
            runCurrent()
            assertFalse(helper.isPreviewing.value)

            coEvery { playStartSound(key) } returns Unit
            coEvery { speakText("試聴", volume = 42) } throws IllegalStateException("本文失敗")
            helper.onPreviewClicked("試聴", key)
            runCurrent()
            assertFalse(helper.isPreviewing.value)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `試聴のCancellationExceptionは再送出され試聴中を解除する`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            coEvery { playStartSound(key) } returns Unit
            val cancellation = CancellationException("キャンセル")
            var completionCause: Throwable? = null
            coEvery { speakText("試聴", volume = 42) } coAnswers {
                currentCoroutineContext()[Job]!!.invokeOnCompletion { completionCause = it }
                throw cancellation
            }
            val helper = helper()
            runCurrent()
            helper.onPreviewClicked("試聴", key)
            runCurrent()
            assertEquals(cancellation, completionCause)
            assertFalse(helper.isPreviewing.value)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 1) { observeVolume() }
            coVerify(exactly = 1) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }

    @Test
    fun `停止した古いジョブの終了は新しい試聴状態を消さない`() =
        runTest {
            coEvery { checkAvailable() } returns true
            every { observeVolume() } returns flowOf(42)
            coEvery { playStartSound(key) } returns Unit
            val oldFinish = CompletableDeferred<Unit>()
            val newFinish = CompletableDeferred<Unit>()
            coEvery { speakText("古い試聴", volume = 42) } coAnswers {
                // キャンセルに協調しない外部処理が、停止と再開の後に終了する状況を再現する。
                withContext(NonCancellable) { oldFinish.await() }
            }
            coEvery { speakText("新しい試聴", volume = 42) } coAnswers { newFinish.await() }
            val helper = helper()
            runCurrent()

            helper.onPreviewClicked("古い試聴", key)
            runCurrent()
            helper.stop()
            assertFalse(helper.isPreviewing.value)
            helper.onPreviewClicked("新しい試聴", key)
            runCurrent()
            assertTrue(helper.isPreviewing.value)
            oldFinish.complete(Unit)
            runCurrent()
            assertTrue(helper.isPreviewing.value)
            newFinish.complete(Unit)
            runCurrent()
            assertFalse(helper.isPreviewing.value)

            coVerify(exactly = 1) { checkAvailable() }
            verify(exactly = 2) { observeVolume() }
            coVerify(exactly = 2) { playStartSound(key) }
            coVerify(exactly = 1) { speakText("古い試聴", volume = 42) }
            coVerify(exactly = 1) { speakText("新しい試聴", volume = 42) }
            confirmVerified(checkAvailable, observeVolume, playStartSound, speakText)
        }
}
